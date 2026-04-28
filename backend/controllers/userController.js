const Booking = require("../models/Booking");
const RoomRequest = require("../models/RoomRequest");
const Room = require("../models/Room");
const User = require("../models/User");
const UserNotification = require("../models/UserNotification");
const Complaint = require("../models/Complaint");
const MessPlan = require("../models/MessPlan");
const Fee = require("../models/Fee");
const {
  serializeNotification,
  serializeRoomRequest
} = require("./bookingRequestController");
const { serializeComplaint } = require("./complaintController");
const { serializeMessPlan } = require("./messPlanController");
const { serializeFee } = require("./paymentController");

const serializeUser = (user) => ({
  id: user._id,
  name: user.name,
  email: user.email,
  role: user.role
});

const serializeRoom = (room) => ({
  _id: room._id,
  roomNumber: room.roomNumber,
  floor: room.floor,
  capacity: room.capacity,
  occupiedBeds: room.occupiedBeds,
  status: room.status
});

const serializeBooking = (booking) => {
  if (!booking) {
    return null;
  }

  return {
    id: booking._id,
    status: booking.status,
    bookedAt: booking.bookedAt,
    releasedAt: booking.releasedAt,
    room: booking.room ? serializeRoom(booking.room) : null
  };
};

const getTodayDateKey = () => {
  const now = new Date();
  return new Date(Date.UTC(now.getUTCFullYear(), now.getUTCMonth(), now.getUTCDate())).toISOString();
};

const emitRealtimeEvent = (req, eventName, payload = {}) => {
  const io = req.app.get("io");

  if (io) {
    io.emit(eventName, payload);
  }
};

const filterDashboardNotifications = (notifications, activeBooking, pendingRequest) => {
  return notifications.filter((notification) => {
    if (
      notification.type === "room_assigned" &&
      (!activeBooking || String(notification.room?._id || notification.room) !== String(activeBooking.room?._id))
    ) {
      return false;
    }

    if (notification.type === "request_submitted" && !pendingRequest) {
      return false;
    }

    if (
      notification.type === "request_cancelled" &&
      pendingRequest &&
      pendingRequest.status === "pending"
    ) {
      return false;
    }

    return true;
  });
};

const getUserDashboard = async (req, res) => {
  try {
    const { userId } = req.params;
    const canAccess = req.user && (
      String(req.user._id) === String(userId) ||
      req.user.role === "admin"
    );

    if (!canAccess) {
      return res.status(403).json({ message: "Forbidden" });
    }

    const [user, rooms, activeBooking, pendingRequest, notifications, complaints, messPlans, fees] = await Promise.all([
      User.findById(userId).select("-password"),
      Room.find().sort({ roomNumber: 1 }),
      Booking.findOne({ user: userId, status: "active" }).populate("room"),
      RoomRequest.findOne({ user: userId, status: "pending" }).populate("user").populate("preferredRoom").populate("assignedRoom"),
      UserNotification.find({ user: userId }).populate("room").sort({ createdAt: -1 }).limit(10),
      Complaint.find({ user: userId }).populate("user").sort({ createdAt: -1 }).limit(10),
      MessPlan.find().populate("createdBy", "-password").sort({ planDate: -1 }).limit(7),
      Fee.find({ user: userId }).sort({ createdAt: -1 }).limit(10)
    ]);

    if (!user) {
      return res.status(404).json({ message: "User not found" });
    }

    const availableRoomList = rooms.filter(
      (room) => room.status !== "maintenance" && room.occupiedBeds < room.capacity
    );
    const availableRooms = availableRoomList.length;
    const occupiedBeds = rooms.reduce((sum, room) => sum + room.occupiedBeds, 0);
    const totalCapacity = rooms.reduce((sum, room) => sum + room.capacity, 0);

    const dashboardNotifications = filterDashboardNotifications(
      notifications,
      activeBooking,
      pendingRequest
    );

    const todayDateKey = getTodayDateKey();
    const todaysPlan =
      messPlans.find((plan) => new Date(plan.planDate).toISOString() === todayDateKey) || null;

    const currentFee =
      fees.find((fee) => fee.status === "pending" || fee.status === "failed") ||
      fees[0] ||
      null;

    res.status(200).json({
      user: serializeUser(user),
      currentBooking: serializeBooking(activeBooking),
      pendingRequest: pendingRequest ? serializeRoomRequest(pendingRequest) : null,
      currentFee: serializeFee(currentFee),
      feeHistory: fees.map(serializeFee),
      stats: {
        totalRooms: rooms.length,
        availableRooms,
        occupiedBeds,
        totalCapacity
      },
      availableRooms: availableRoomList.map(serializeRoom),
      rooms: rooms.map(serializeRoom),
      notifications: dashboardNotifications.map(serializeNotification),
      complaints: complaints.map(serializeComplaint),
      todaysMessPlan: todaysPlan ? serializeMessPlan(todaysPlan) : null,
      messPlanHistory: messPlans.map(serializeMessPlan)
    });
  } catch (error) {
    res.status(500).json({ message: "Server error", error: error.message });
  }
};

const bookRoom = async (req, res) => {
  try {
    const { userId, roomId } = req.body;

    if (!userId || !roomId) {
      return res.status(400).json({ message: "User and room are required" });
    }

    const [user, room, existingBooking] = await Promise.all([
      User.findById(userId).select("-password"),
      Room.findById(roomId),
      Booking.findOne({ user: userId, status: "active" }).populate("room")
    ]);

    if (!user) {
      return res.status(404).json({ message: "User not found" });
    }

    if (!room) {
      return res.status(404).json({ message: "Room not found" });
    }

    if (existingBooking) {
      return res.status(400).json({
        message: "User already has an active room booking",
        booking: serializeBooking(existingBooking)
      });
    }

    if (room.status === "maintenance" || room.occupiedBeds >= room.capacity) {
      return res.status(400).json({ message: "Room is not available for booking" });
    }

    room.occupiedBeds += 1;
    room.status = room.occupiedBeds >= room.capacity ? "full" : "available";
    await room.save();

    const booking = await Booking.create({
      user: user._id,
      room: room._id
    });

    const populatedBooking = await Booking.findById(booking._id).populate("room");

    emitRealtimeEvent(req, "roomsUpdated");
    emitRealtimeEvent(req, "bookingsUpdated", { userId: String(user._id) });

    res.status(201).json({
      message: "Room booked successfully",
      booking: serializeBooking(populatedBooking)
    });
  } catch (error) {
    res.status(500).json({ message: "Server error", error: error.message });
  }
};

const releaseRoom = async (req, res) => {
  try {
    const { userId } = req.params;
    const canAccess = req.user && (
      String(req.user._id) === String(userId) ||
      req.user.role === "admin"
    );

    if (!canAccess) {
      return res.status(403).json({ message: "Forbidden" });
    }

    const booking = await Booking.findOne({ user: userId, status: "active" }).populate("room");

    if (!booking) {
      return res.status(404).json({ message: "No active booking found" });
    }

    const room = await Room.findById(booking.room._id);

    if (room) {
      room.occupiedBeds = Math.max(room.occupiedBeds - 1, 0);
      room.status = room.occupiedBeds >= room.capacity ? "full" : "available";
      await room.save();
    }

    booking.status = "released";
    booking.releasedAt = new Date();
    await booking.save();

    emitRealtimeEvent(req, "roomsUpdated");
    emitRealtimeEvent(req, "bookingsUpdated", { userId: String(userId) });

    res.status(200).json({
      message: "Room released successfully"
    });
  } catch (error) {
    res.status(500).json({ message: "Server error", error: error.message });
  }
};

module.exports = {
  getUserDashboard,
  bookRoom,
  releaseRoom
};
