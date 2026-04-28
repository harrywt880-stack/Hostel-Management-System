const mongoose = require("mongoose");

const visitorSchema = new mongoose.Schema(
  {
    user: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "User",
      required: true
    },
    visitorName: {
      type: String,
      required: true,
      trim: true
    },
    visitorPhone: {
      type: String,
      required: true,
      trim: true
    },
    relationToUser: {
      type: String,
      required: true,
      trim: true
    },
    purpose: {
      type: String,
      required: true,
      trim: true
    },
    visitDate: {
      type: Date,
      required: true
    },
    createdBy: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "User",
      required: true
    }
  },
  { timestamps: true }
);

module.exports = mongoose.model("Visitor", visitorSchema);
