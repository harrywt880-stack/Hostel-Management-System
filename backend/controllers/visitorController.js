const User = require("../models/User");
const Visitor = require("../models/Visitor");

const serializeUser = (user) => ({
  id: user._id,
  name: user.name,
  email: user.email,
  role: user.role
});

const serializeVisitor = (visitor) => ({
  id: visitor._id,
  visitorName: visitor.visitorName,
  visitorPhone: visitor.visitorPhone,
  relationToUser: visitor.relationToUser,
  purpose: visitor.purpose,
  visitDate: visitor.visitDate,
  createdAt: visitor.createdAt,
  user: visitor.user ? serializeUser(visitor.user) : null,
  createdBy: visitor.createdBy ? serializeUser(visitor.createdBy) : null
});

const emitVisitorEvent = (req, payload = {}) => {
  const io = req.app.get("io");

  if (io) {
    io.emit("visitorsUpdated", payload);
  }
};

const createVisitor = async (req, res) => {
  try {
    const { userId, visitorName, visitorPhone, relationToUser, purpose, visitDate } = req.body;

    if (!userId || !visitorName || !visitorPhone || !relationToUser || !purpose || !visitDate) {
      return res.status(400).json({ message: "All visitor fields are required" });
    }

    const [admin, user] = await Promise.all([
      User.findById(req.user?._id).select("-password"),
      User.findById(userId).select("-password")
    ]);

    if (!admin || admin.role !== "admin") {
      return res.status(403).json({ message: "Only admin can add visitors" });
    }

    if (!user) {
      return res.status(404).json({ message: "Selected user not found" });
    }

    const parsedVisitDate = new Date(visitDate);

    if (Number.isNaN(parsedVisitDate.getTime())) {
      return res.status(400).json({ message: "Visit date is invalid" });
    }

    const visitor = await Visitor.create({
      user: user._id,
      visitorName: visitorName.trim(),
      visitorPhone: visitorPhone.trim(),
      relationToUser: relationToUser.trim(),
      purpose: purpose.trim(),
      visitDate: parsedVisitDate,
      createdBy: admin._id
    });

    const populatedVisitor = await Visitor.findById(visitor._id)
      .populate("user", "-password")
      .populate("createdBy", "-password");

    emitVisitorEvent(req, { visitorId: String(visitor._id) });

    res.status(201).json({
      message: "Visitor added successfully",
      visitor: serializeVisitor(populatedVisitor)
    });
  } catch (error) {
    res.status(500).json({ message: "Server error", error: error.message });
  }
};

const getVisitors = async (req, res) => {
  try {
    const visitors = await Visitor.find()
      .populate("user", "-password")
      .populate("createdBy", "-password")
      .sort({ visitDate: -1, createdAt: -1 })
      .limit(100);

    res.status(200).json(visitors.map(serializeVisitor));
  } catch (error) {
    res.status(500).json({ message: "Server error", error: error.message });
  }
};

module.exports = {
  createVisitor,
  getVisitors
};
