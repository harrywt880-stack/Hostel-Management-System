const Booking = require("../models/Booking");
const Room = require("../models/Room");
const RoomRequest = require("../models/RoomRequest");
const User = require("../models/User");
const UserNotification = require("../models/UserNotification");

const serializeRoom = (room) => ({
  _id: room._id,
  roomNumber: room.roomNumber,
  floor: room.floor,
  capacity: room.capacity,
  occupiedBeds: room.occupiedBeds,
  status: room.status
});

const serializeUser = (user) => ({
  id: user._id,
  name: user.name,
  email: user.email,
  role: user.role
});

const serializeRoomRequest = (request) => ({
  id: request._id,
  status: request.status,
  createdAt: request.createdAt,
  assignedAt: request.assignedAt,
  cancelledAt: request.cancelledAt,
  user: request.user ? serializeUser(request.user) : null,
  preferredRoom: request.preferredRoom ? serializeRoom(request.preferredRoom) : null,
  assignedRoom: request.assignedRoom ? serializeRoom(request.assignedRoom) : null
});

const serializeNotification = (notification) => ({
  id: notification._id,
  title: notification.title,
  message: notification.message,
  type: notification.type,
  isRead: notification.isRead,
  createdAt: notification.createdAt,
  room: notification.room ? serializeRoom(notification.room) : null,
  noticeId: notification.notice ? String(notification.notice._id || notification.notice) : null
});

const emitAppEvent = (req, eventName, payload = {}) => {
  const io = req.app.get("io");

  if (io) {
    io.emit(eventName, payload);
  }
};

const createRequestSubmittedNotification = async (userId, roomRequest, room) => {
  await UserNotification.create({
    user: userId,
    title: "Room request submitted",
    message: `Your request for room ${room.roomNumber} is pending admin approval.`,
    type: "request_submitted",
    room: room._id,
    roomRequest: roomRequest._id
  });
};

const requestRoomBooking = async (req, res) => {
  try {
    const { userId, preferredRoomId } = req.body;
    const effectiveUserId = req.user?.role === "student" ? String(req.user._id) : userId;

    if (!effectiveUserId || !preferredRoomId) {
      return res.status(400).json({ message: "User and preferred room are required" });
    }

    const [user, room, activeBooking, pendingRequest] = await Promise.all([
      User.findById(effectiveUserId).select("-password"),
      Room.findById(preferredRoomId),
      Booking.findOne({ user: effectiveUserId, status: "active" }),
      RoomRequest.findOne({ user: effectiveUserId, status: "pending" })
    ]);

    if (!user) {
      return res.status(404).json({ message: "User not found" });
    }

    if (!room) {
      return res.status(404).json({ message: "Preferred room not found" });
    }

    if (activeBooking) {
      return res.status(400).json({ message: "User already has an assigned room" });
    }

    if (pendingRequest) {
      return res.status(400).json({ message: "User already has a pending request" });
    }

    if (room.status === "maintenance" || room.occupiedBeds >= room.capacity) {
      return res.status(400).json({ message: "Preferred room is not available" });
    }

    const roomRequest = await RoomRequest.create({
      user: user._id,
      preferredRoom: room._id
    });

    await createRequestSubmittedNotification(user._id, roomRequest, room);

    const populatedRequest = await RoomRequest.findById(roomRequest._id)
      .populate("user")
      .populate("preferredRoom")
      .populate("assignedRoom");

    emitAppEvent(req, "adminRequestsUpdated");
    emitAppEvent(req, "userNotificationsUpdated", { userId: String(user._id) });

    res.status(201).json({
      message: "Room request submitted successfully",
      request: serializeRoomRequest(populatedRequest)
    });
  } catch (error) {
    res.status(500).json({ message: "Server error", error: error.message });
  }
};

const cancelRoomRequest = async (req, res) => {
  try {
    const { requestId } = req.params;

    const roomRequest = await RoomRequest.findById(requestId).populate("user").populate("preferredRoom");

    if (!roomRequest) {
      return res.status(404).json({ message: "Request not found" });
    }

    if (roomRequest.status !== "pending") {
      return res.status(400).json({ message: "Only pending requests can be cancelled" });
    }

    const isSelf = req.user && String(req.user._id) === String(roomRequest.user._id);
    const isAdmin = req.user && req.user.role === "admin";

    if (!isSelf && !isAdmin) {
      return res.status(403).json({ message: "Forbidden" });
    }

    roomRequest.status = "cancelled";
    roomRequest.cancelledAt = new Date();
    await roomRequest.save();

    await UserNotification.create({
      user: roomRequest.user._id,
      title: "Room request cancelled",
      message: `Your request for room ${roomRequest.preferredRoom.roomNumber} was cancelled.`,
      type: "request_cancelled",
      room: roomRequest.preferredRoom._id,
      roomRequest: roomRequest._id
    });

    emitAppEvent(req, "adminRequestsUpdated");
    emitAppEvent(req, "userNotificationsUpdated", { userId: String(roomRequest.user._id) });

    res.status(200).json({ message: "Room request cancelled successfully" });
  } catch (error) {
    res.status(500).json({ message: "Server error", error: error.message });
  }
};

const getPendingRequestsForAdmin = async (req, res) => {
  try {
    const [requests, rooms] = await Promise.all([
      RoomRequest.find({ status: "pending" })
        .populate("user")
        .populate("preferredRoom")
        .sort({ createdAt: 1 }),
      Room.find().sort({ roomNumber: 1 })
    ]);

    const availableRooms = rooms.filter(
      (room) => room.status !== "maintenance" && room.occupiedBeds < room.capacity
    );

    res.status(200).json({
      requests: requests.map(serializeRoomRequest),
      availableRooms: availableRooms.map(serializeRoom)
    });
  } catch (error) {
    res.status(500).json({ message: "Server error", error: error.message });
  }
};

const assignRoomToRequest = async (req, res) => {
  try {
    const { requestId } = req.params;
    const { roomId } = req.body;

    const [roomRequest, admin, room] = await Promise.all([
      RoomRequest.findById(requestId).populate("user").populate("preferredRoom"),
      User.findById(req.user?._id).select("-password"),
      Room.findById(roomId)
    ]);

    if (!roomRequest) {
      return res.status(404).json({ message: "Request not found" });
    }

    if (!admin || admin.role !== "admin") {
      return res.status(403).json({ message: "Only admin can assign rooms" });
    }

    if (!room) {
      return res.status(404).json({ message: "Room not found" });
    }

    if (roomRequest.status !== "pending") {
      return res.status(400).json({ message: "Only pending requests can be assigned" });
    }

    const existingBooking = await Booking.findOne({ user: roomRequest.user._id, status: "active" });

    if (existingBooking) {
      return res.status(400).json({ message: "User already has an active room assignment" });
    }

    if (room.status === "maintenance" || room.occupiedBeds >= room.capacity) {
      return res.status(400).json({ message: "Selected room is not available" });
    }

    room.occupiedBeds += 1;
    room.status = room.occupiedBeds >= room.capacity ? "full" : "available";
    await room.save();

    await Booking.create({
      user: roomRequest.user._id,
      room: room._id,
      status: "active"
    });

    roomRequest.status = "assigned";
    roomRequest.assignedRoom = room._id;
    roomRequest.assignedBy = admin._id;
    roomRequest.assignedAt = new Date();
    await roomRequest.save();

    await UserNotification.create({
      user: roomRequest.user._id,
      title: "Room assigned successfully",
      message: `Room ${room.roomNumber} on floor ${room.floor} has been assigned to you. Capacity ${room.capacity}, occupied ${room.occupiedBeds}.`,
      type: "room_assigned",
      room: room._id,
      roomRequest: roomRequest._id
    });

    const populatedRequest = await RoomRequest.findById(roomRequest._id)
      .populate("user")
      .populate("preferredRoom")
      .populate("assignedRoom");

    emitAppEvent(req, "adminRequestsUpdated");
    emitAppEvent(req, "roomsUpdated");
    emitAppEvent(req, "bookingsUpdated", { userId: String(roomRequest.user._id) });
    emitAppEvent(req, "userNotificationsUpdated", { userId: String(roomRequest.user._id) });

    res.status(200).json({
      message: "Room assigned successfully",
      request: serializeRoomRequest(populatedRequest)
    });
  } catch (error) {
    res.status(500).json({ message: "Server error", error: error.message });
  }
};

const getUserNotifications = async (req, res) => {
  try {
    const { userId } = req.params;
    const canAccess = req.user && (
      String(req.user._id) === String(userId) ||
      req.user.role === "admin"
    );

    if (!canAccess) {
      return res.status(403).json({ message: "Forbidden" });
    }

    const notifications = await UserNotification.find({ user: userId })
      .populate("room")
      .sort({ createdAt: -1 })
      .limit(20);

    res.status(200).json(notifications.map(serializeNotification));
  } catch (error) {
    res.status(500).json({ message: "Server error", error: error.message });
  }
};

const markNotificationsRead = async (req, res) => {
  try {
    const { userId } = req.params;
    const canAccess = req.user && (
      String(req.user._id) === String(userId) ||
      req.user.role === "admin"
    );

    if (!canAccess) {
      return res.status(403).json({ message: "Forbidden" });
    }

    await UserNotification.updateMany({ user: userId, isRead: false }, { isRead: true });

    res.status(200).json({ message: "Notifications marked as read" });
  } catch (error) {
    res.status(500).json({ message: "Server error", error: error.message });
  }
};

module.exports = {
  requestRoomBooking,
  cancelRoomRequest,
  getPendingRequestsForAdmin,
  assignRoomToRequest,
  getUserNotifications,
  markNotificationsRead,
  serializeRoomRequest,
  serializeNotification
};
