const mongoose = require("mongoose");
const RoomRequest = require("../models/RoomRequest");
const Room = require("../models/Room");
const User = require("../models/User");
const Complaint = require("../models/Complaint");
const Booking = require("../models/Booking");
const Visitor = require("../models/Visitor");
const Notice = require("../models/Notice");
const MessPlan = require("../models/MessPlan");

const getUtcWindow = (days) => {
  const now = new Date();
  const end = new Date(Date.UTC(
    now.getUTCFullYear(),
    now.getUTCMonth(),
    now.getUTCDate(),
    23,
    59,
    59,
    999
  ));
  const start = new Date(end);
  start.setUTCDate(start.getUTCDate() - (days - 1));
  start.setUTCHours(0, 0, 0, 0);
  return { start, end };
};

const serializePeriod = (label, start, end, data) => ({
  label,
  startDate: start,
  endDate: end,
  ...data
});

const countDocumentsInWindow = async (Model, fieldName, start, end, extraQuery = {}) =>
  Model.countDocuments({
    ...extraQuery,
    [fieldName]: {
      $gte: start,
      $lte: end
    }
  });

const buildAnalyticsPeriod = async (label, start, end) => {
  const [
    newUsers,
    bookingCreated,
    bookingReleased,
    requestsSubmitted,
    requestsAssigned,
    complaintsRaised,
    complaintsResolved,
    visitorsLogged,
    noticesPublished,
    messPlansPublished
  ] = await Promise.all([
    countDocumentsInWindow(User, "createdAt", start, end),
    countDocumentsInWindow(Booking, "bookedAt", start, end),
    countDocumentsInWindow(Booking, "releasedAt", start, end, { status: "released" }),
    countDocumentsInWindow(RoomRequest, "createdAt", start, end),
    countDocumentsInWindow(RoomRequest, "assignedAt", start, end, { status: "assigned" }),
    countDocumentsInWindow(Complaint, "createdAt", start, end),
    countDocumentsInWindow(Complaint, "resolvedAt", start, end, {
      status: { $in: ["resolved", "closed"] }
    }),
    countDocumentsInWindow(Visitor, "visitDate", start, end),
    countDocumentsInWindow(Notice, "createdAt", start, end),
    countDocumentsInWindow(MessPlan, "planDate", start, end)
  ]);

  return serializePeriod(label, start, end, {
    newUsers,
    bookingCreated,
    bookingReleased,
    requestsSubmitted,
    requestsAssigned,
    complaintsRaised,
    complaintsResolved,
    visitorsLogged,
    noticesPublished,
    messPlansPublished
  });
};

const getDashboardSummary = async (req, res) => {
  try {
    const [users, rooms, pendingRequests, complaints] = await Promise.all([
      User.find().select("-password").sort({ createdAt: -1 }),
      Room.find().sort({ createdAt: -1 }),
      RoomRequest.find({ status: "pending" }),
      Complaint.find({ status: { $in: ["open", "accepted", "in_progress"] } })
    ]);

    const totalUsers = users.length;
    const totalAdmins = users.filter((user) => user.role === "admin").length;
    const totalStudents = users.filter((user) => user.role === "student").length;
    const totalWardens = users.filter((user) => user.role === "warden").length;
    const totalRooms = rooms.length;
    const availableRooms = rooms.filter((room) => room.status === "available").length;
    const fullRooms = rooms.filter((room) => room.status === "full").length;
    const maintenanceRooms = rooms.filter((room) => room.status === "maintenance").length;
    const totalCapacity = rooms.reduce((sum, room) => sum + room.capacity, 0);
    const occupiedBeds = rooms.reduce((sum, room) => sum + room.occupiedBeds, 0);
    const occupancyRate = totalCapacity === 0 ? 0 : Math.round((occupiedBeds / totalCapacity) * 100);

    res.status(200).json({
      totalUsers,
      totalAdmins,
      totalStudents,
      totalWardens,
      totalRooms,
      availableRooms,
      fullRooms,
      maintenanceRooms,
      totalCapacity,
      occupiedBeds,
      vacantBeds: Math.max(totalCapacity - occupiedBeds, 0),
      occupancyRate,
      pendingRequests: pendingRequests.length,
      openComplaints: complaints.length,
      recentUsers: users.slice(0, 5).map((user) => ({
        id: user._id,
        name: user.name,
        email: user.email,
        role: user.role
      })),
      recentRooms: rooms.slice(0, 5)
    });
  } catch (error) {
    res.status(500).json({
      message: "Server error",
      error: error.message
    });
  }
};

const getHealthStatus = (req, res) => {
  const stateMap = {
    0: "disconnected",
    1: "connected",
    2: "connecting",
    3: "disconnecting"
  };

  res.status(200).json({
    status: "ok",
    database: stateMap[mongoose.connection.readyState] || "unknown"
  });
};

const getAnalyticsReport = async (req, res) => {
  try {
    const dailyWindow = getUtcWindow(1);
    const weeklyWindow = getUtcWindow(7);
    const monthlyWindow = getUtcWindow(30);

    const [rooms, activeBookings, pendingRequests, openComplaints, daily, weekly, monthly] = await Promise.all([
      Room.find(),
      Booking.countDocuments({ status: "active" }),
      RoomRequest.countDocuments({ status: "pending" }),
      Complaint.countDocuments({ status: { $in: ["open", "accepted", "in_progress"] } }),
      buildAnalyticsPeriod("Daily", dailyWindow.start, dailyWindow.end),
      buildAnalyticsPeriod("Weekly", weeklyWindow.start, weeklyWindow.end),
      buildAnalyticsPeriod("Monthly", monthlyWindow.start, monthlyWindow.end)
    ]);

    const totalCapacity = rooms.reduce((sum, room) => sum + room.capacity, 0);
    const occupiedBeds = rooms.reduce((sum, room) => sum + room.occupiedBeds, 0);
    const occupancyRate = totalCapacity === 0 ? 0 : Math.round((occupiedBeds / totalCapacity) * 100);

    res.status(200).json({
      generatedAt: new Date(),
      snapshot: {
        totalRooms: rooms.length,
        totalCapacity,
        occupiedBeds,
        activeBookings,
        pendingRequests,
        openComplaints,
        occupancyRate
      },
      daily,
      weekly,
      monthly
    });
  } catch (error) {
    res.status(500).json({
      message: "Server error",
      error: error.message
    });
  }
};

module.exports = {
  getDashboardSummary,
  getHealthStatus,
  getAnalyticsReport
};
