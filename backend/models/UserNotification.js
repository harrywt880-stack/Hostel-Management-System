const mongoose = require("mongoose");

const userNotificationSchema = new mongoose.Schema(
  {
    user: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "User",
      required: true
    },
    title: {
      type: String,
      required: true
    },
    message: {
      type: String,
      required: true
    },
    type: {
      type: String,
      enum: [
        "room_assigned",
        "request_submitted",
        "request_cancelled",
        "admin_notice",
        "complaint_accepted",
        "complaint_resolved",
        "complaint_updated"
      ],
      default: "room_assigned"
    },
    room: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "Room"
    },
    roomRequest: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "RoomRequest"
    },
    notice: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "Notice"
    },
    isRead: {
      type: Boolean,
      default: false
    }
  },
  { timestamps: true }
);

module.exports = mongoose.model("UserNotification", userNotificationSchema);
