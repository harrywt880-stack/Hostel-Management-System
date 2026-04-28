const mongoose = require("mongoose");

const roomRequestSchema = new mongoose.Schema(
  {
    user: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "User",
      required: true
    },
    preferredRoom: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "Room",
      required: true
    },
    assignedRoom: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "Room"
    },
    assignedBy: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "User"
    },
    status: {
      type: String,
      enum: ["pending", "assigned", "cancelled"],
      default: "pending"
    },
    assignedAt: {
      type: Date
    },
    cancelledAt: {
      type: Date
    }
  },
  { timestamps: true }
);

module.exports = mongoose.model("RoomRequest", roomRequestSchema);
