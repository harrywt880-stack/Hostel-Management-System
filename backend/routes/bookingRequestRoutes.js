const express = require("express");
const router = express.Router();
const { protect, authorize, allowSelfOrRoles } = require("../middleware/authMiddleware");

const {
  requestRoomBooking,
  cancelRoomRequest,
  getPendingRequestsForAdmin,
  assignRoomToRequest,
  getUserNotifications,
  markNotificationsRead
} = require("../controllers/bookingRequestController");

router.post("/", protect, authorize("student", "admin"), requestRoomBooking);
router.get("/admin/pending", protect, authorize("admin"), getPendingRequestsForAdmin);
router.patch("/:requestId/cancel", protect, cancelRoomRequest);
router.patch("/:requestId/assign", protect, authorize("admin"), assignRoomToRequest);
router.get("/notifications/:userId", protect, allowSelfOrRoles("userId", "admin"), getUserNotifications);
router.patch("/notifications/:userId/read", protect, allowSelfOrRoles("userId", "admin"), markNotificationsRead);

module.exports = router;
