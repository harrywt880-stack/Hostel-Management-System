const express = require("express");
const router = express.Router();
const { protect, authorize } = require("../middleware/authMiddleware");

const {
  getDashboardSummary,
  getHealthStatus,
  getAnalyticsReport
} = require("../controllers/dashboardController");

router.get("/summary", protect, authorize("admin"), getDashboardSummary);
router.get("/analytics", protect, authorize("admin"), getAnalyticsReport);
router.get("/health", getHealthStatus);

module.exports = router;
