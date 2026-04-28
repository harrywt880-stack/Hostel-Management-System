const express = require("express");
const router = express.Router();
const { protect, authorize, allowSelfOrRoles } = require("../middleware/authMiddleware");

const {
  createComplaint,
  getUserComplaints,
  getAdminComplaints,
  updateComplaintStatus
} = require("../controllers/complaintController");

router.post("/", protect, authorize("student", "admin"), createComplaint);
router.get("/user/:userId", protect, allowSelfOrRoles("userId", "admin"), getUserComplaints);
router.get("/admin", protect, authorize("admin"), getAdminComplaints);
router.patch("/:complaintId/status", protect, authorize("admin"), updateComplaintStatus);

module.exports = router;
