const mongoose = require("mongoose");

const roomSchema = new mongoose.Schema(
  {
    roomNumber: {
      type: String,
      required: true,
      unique: true
    },
    floor: {
      type: String,
      required: true
    },
    capacity: {
      type: Number,
      required: true
    },
    occupiedBeds: {
      type: Number,
      default: 0
    },
    status: {
      type: String,
      enum: ["available", "full", "maintenance"],
      default: "available"
    }
  },
  { timestamps: true }
);

module.exports = mongoose.model("Room", roomSchema);