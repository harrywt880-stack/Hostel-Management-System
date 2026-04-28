const Notice = require("../models/Notice");
const User = require("../models/User");
const UserNotification = require("../models/UserNotification");

const serializeNotice = (notice) => ({
  id: notice._id,
  title: notice.title,
  message: notice.message,
  audienceCount: notice.audienceCount,
  createdAt: notice.createdAt,
  createdBy: notice.createdBy
    ? {
        id: notice.createdBy._id,
        name: notice.createdBy.name,
        email: notice.createdBy.email,
        role: notice.createdBy.role
      }
    : null
});

const emitNoticeEvents = (req, payload = {}) => {
  const io = req.app.get("io");

  if (!io) {
    return;
  }

  io.emit("noticesUpdated", payload);
};

const createNotice = async (req, res) => {
  try {
    const { title, message } = req.body;

    if (!title || !message) {
      return res.status(400).json({ message: "Admin, title and message are required" });
    }

    const [admin, recipients] = await Promise.all([
      User.findById(req.user?._id).select("-password"),
      User.find({ role: { $ne: "admin" } }).select("_id")
    ]);

    if (!admin || admin.role !== "admin") {
      return res.status(403).json({ message: "Only admin can publish notices" });
    }

    const trimmedTitle = title.trim();
    const trimmedMessage = message.trim();

    if (!trimmedTitle || !trimmedMessage) {
      return res.status(400).json({ message: "Title and message cannot be empty" });
    }

    const notice = await Notice.create({
      title: trimmedTitle,
      message: trimmedMessage,
      createdBy: admin._id,
      audienceCount: recipients.length
    });

    if (recipients.length > 0) {
      await UserNotification.insertMany(
        recipients.map((user) => ({
          user: user._id,
          title: trimmedTitle,
          message: trimmedMessage,
          type: "admin_notice",
          notice: notice._id
        }))
      );
    }

    const populatedNotice = await Notice.findById(notice._id).populate("createdBy", "-password");

    emitNoticeEvents(req, { noticeId: String(notice._id) });

    res.status(201).json({
      message: "Notice announced successfully",
      notice: serializeNotice(populatedNotice)
    });
  } catch (error) {
    res.status(500).json({ message: "Server error", error: error.message });
  }
};

const getNotices = async (req, res) => {
  try {
    const notices = await Notice.find()
      .populate("createdBy", "-password")
      .sort({ createdAt: -1 })
      .limit(50);

    res.status(200).json(notices.map(serializeNotice));
  } catch (error) {
    res.status(500).json({ message: "Server error", error: error.message });
  }
};

module.exports = {
  createNotice,
  getNotices,
  serializeNotice
};
