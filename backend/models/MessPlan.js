const mongoose = require("mongoose");

const messPlanSchema = new mongoose.Schema(
  {
    planDate: {
      type: Date,
      required: true,
      unique: true
    },
    breakfast: {
      type: String,
      required: true,
      trim: true
    },
    lunch: {
      type: String,
      required: true,
      trim: true
    },
    eveningSnacks: {
      type: String,
      required: true,
      trim: true
    },
    dinner: {
      type: String,
      required: true,
      trim: true
    },
    notes: {
      type: String,
      trim: true,
      default: ""
    },
    createdBy: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "User",
      required: true
    }
  },
  { timestamps: true }
);

module.exports = mongoose.model("MessPlan", messPlanSchema);
