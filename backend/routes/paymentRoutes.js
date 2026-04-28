const express = require("express");
const router = express.Router();
const { protect, authorize, allowSelfOrRoles } = require("../middleware/authMiddleware");

const {
  createFee,
  getAllFees,
  getUserFees,
  createPaymentOrder,
  verifyPayment,
  updateFeeByAdmin
} = require("../controllers/paymentController");

router.get("/fees", protect, authorize("admin"), getAllFees);
router.post("/fees", protect, authorize("admin"), createFee);
router.get("/fees/user/:userId", protect, allowSelfOrRoles("userId", "admin"), getUserFees);
router.patch("/fees/:feeId", protect, authorize("admin"), updateFeeByAdmin);
router.post("/create-order", protect, authorize("student", "admin"), createPaymentOrder);
router.post("/verify", protect, authorize("student", "admin"), verifyPayment);

module.exports = router;
