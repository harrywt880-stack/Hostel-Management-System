const mongoose = require("mongoose");

const feeSchema = new mongoose.Schema(
  {
    user: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "User",
      required: true
    },
    title: {
      type: String,
      required: true,
      trim: true
    },
    monthLabel: {
      type: String,
      required: true,
      trim: true
    },
    amount: {
      type: Number,
      required: true,
      min: 1
    },
    currency: {
      type: String,
      default: "INR",
      uppercase: true,
      trim: true
    },
    status: {
      type: String,
      enum: ["pending", "paid", "failed"],
      default: "pending"
    },
    dueDate: {
      type: Date,
      default: null
    },
    receipt: {
      type: String,
      default: ""
    },
    gateway: {
      type: String,
      enum: ["razorpay"],
      default: "razorpay"
    },
    gatewayOrderId: {
      type: String,
      default: ""
    },
    gatewayPaymentId: {
      type: String,
      default: ""
    },
    gatewaySignature: {
      type: String,
      default: ""
    },
    paidSource: {
      type: String,
      enum: ["razorpay", "manual"],
      default: "razorpay"
    },
    adminNote: {
      type: String,
      default: "",
      trim: true
    },
    paidAt: {
      type: Date,
      default: null
    }
  },
  { timestamps: true }
);

module.exports = mongoose.model("Fee", feeSchema);
