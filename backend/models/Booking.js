const mongoose = require("mongoose");

const bookingSchema = new mongoose.Schema(
  {
    user: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "User",
      required: true
    },
    room: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "Room",
      required: true
    },
    status: {
      type: String,
      enum: ["active", "released"],
      default: "active"
    },
    bookedAt: {
      type: Date,
      default: Date.now
    },
    releasedAt: {
      type: Date
    }
  },
  { timestamps: true }
);

module.exports = mongoose.model("Booking", bookingSchema);
