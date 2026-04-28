const Complaint = require("../models/Complaint");
const User = require("../models/User");
const UserNotification = require("../models/UserNotification");

const serializeUser = (user) => ({
  id: user._id,
  name: user.name,
  email: user.email,
  role: user.role
});

const serializeComplaint = (complaint) => ({
  id: complaint._id,
  complaintId: complaint.complaintId,
  subject: complaint.subject,
  description: complaint.description,
  status: complaint.status,
  adminNotes: complaint.adminNotes,
  resolvedAt: complaint.resolvedAt,
  createdAt: complaint.createdAt,
  updatedAt: complaint.updatedAt,
  user: complaint.user ? serializeUser(complaint.user) : null
});

const emitComplaintEvent = (req, eventName, payload = {}) => {
  const io = req.app.get("io");

  if (io) {
    io.emit(eventName, payload);
  }
};

const generateComplaintId = async () => {
  let complaintId = "";
  let exists = true;

  while (exists) {
    const now = new Date();
    const datePart = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, "0")}${String(now.getDate()).padStart(2, "0")}`;
    const randomPart = Math.random().toString(36).slice(2, 6).toUpperCase();
    complaintId = `CMP-${datePart}-${randomPart}`;
    exists = await Complaint.exists({ complaintId });
  }

  return complaintId;
};

const createComplaint = async (req, res) => {
  try {
    const { userId, subject, description } = req.body;
    const effectiveUserId = req.user?.role === "student" ? String(req.user._id) : userId;

    if (!effectiveUserId || !subject || !description) {
      return res.status(400).json({ message: "User, subject and description are required" });
    }

    const user = await User.findById(effectiveUserId).select("-password");

    if (!user) {
      return res.status(404).json({ message: "User not found" });
    }

    const complaintId = await generateComplaintId();

    const complaint = await Complaint.create({
      complaintId,
      user: user._id,
      subject: subject.trim(),
      description: description.trim()
    });

    const populatedComplaint = await Complaint.findById(complaint._id).populate("user");

    emitComplaintEvent(req, "adminComplaintsUpdated");
    emitComplaintEvent(req, "userComplaintsUpdated", { userId: String(user._id) });

    res.status(201).json({
      message: "Complaint submitted successfully",
      complaint: serializeComplaint(populatedComplaint)
    });
  } catch (error) {
    res.status(500).json({ message: "Server error", error: error.message });
  }
};

const getUserComplaints = async (req, res) => {
  try {
    const { userId } = req.params;
    const canAccess = req.user && (
      String(req.user._id) === String(userId) ||
      req.user.role === "admin"
    );

    if (!canAccess) {
      return res.status(403).json({ message: "Forbidden" });
    }

    const complaints = await Complaint.find({ user: userId })
      .populate("user")
      .sort({ createdAt: -1 });

    res.status(200).json(complaints.map(serializeComplaint));
  } catch (error) {
    res.status(500).json({ message: "Server error", error: error.message });
  }
};

const getAdminComplaints = async (req, res) => {
  try {
    const complaints = await Complaint.find()
      .populate("user")
      .sort({ createdAt: -1 });

    res.status(200).json(complaints.map(serializeComplaint));
  } catch (error) {
    res.status(500).json({ message: "Server error", error: error.message });
  }
};

const updateComplaintStatus = async (req, res) => {
  try {
    const { complaintId } = req.params;
    const { status, adminNotes } = req.body;

    if (!status) {
      return res.status(400).json({ message: "Admin and status are required" });
    }

    const [admin, complaint] = await Promise.all([
      User.findById(req.user?._id).select("-password"),
      Complaint.findById(complaintId).populate("user")
    ]);

    if (!admin || admin.role !== "admin") {
      return res.status(403).json({ message: "Only admin can manage complaints" });
    }

    if (!complaint) {
      return res.status(404).json({ message: "Complaint not found" });
    }

    if (!["open", "accepted", "in_progress", "resolved", "closed"].includes(status)) {
      return res.status(400).json({ message: "Invalid complaint status" });
    }

    complaint.status = status;
    complaint.adminNotes = (adminNotes || "").trim();
    complaint.resolvedAt = ["resolved", "closed"].includes(status) ? new Date() : null;
    await complaint.save();

    const updatedComplaint = await Complaint.findById(complaint._id).populate("user");

    if (status === "accepted" || status === "resolved") {
      await UserNotification.create({
        user: updatedComplaint.user._id,
        title: status === "accepted" ? "Complaint accepted" : "Complaint resolved",
        message: status === "accepted"
          ? `Complaint ${updatedComplaint.complaintId} has been accepted by admin.`
          : `Complaint ${updatedComplaint.complaintId} has been resolved by admin.`,
        type: status === "accepted" ? "complaint_accepted" : "complaint_resolved"
      });
    } else {
      await UserNotification.create({
        user: updatedComplaint.user._id,
        title: "Complaint updated",
        message: `Complaint ${updatedComplaint.complaintId} status changed to ${status.replace("_", " ")}.`,
        type: "complaint_updated"
      });
    }

    emitComplaintEvent(req, "adminComplaintsUpdated");
    emitComplaintEvent(req, "userComplaintsUpdated", { userId: String(updatedComplaint.user._id) });
    emitComplaintEvent(req, "userNotificationsUpdated", { userId: String(updatedComplaint.user._id) });

    res.status(200).json({
      message: "Complaint updated successfully",
      complaint: serializeComplaint(updatedComplaint)
    });
  } catch (error) {
    res.status(500).json({ message: "Server error", error: error.message });
  }
};

module.exports = {
  createComplaint,
  getUserComplaints,
  getAdminComplaints,
  updateComplaintStatus,
  serializeComplaint
};
