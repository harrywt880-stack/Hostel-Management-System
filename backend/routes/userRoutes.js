const express = require("express");
const router = express.Router();
const { protect, allowSelfOrRoles } = require("../middleware/authMiddleware");

const {
  getUserDashboard,
  releaseRoom
} = require("../controllers/userController");

router.get("/dashboard/:userId", protect, allowSelfOrRoles("userId", "admin"), getUserDashboard);
router.patch("/release-room/:userId", protect, allowSelfOrRoles("userId", "admin"), releaseRoom);

module.exports = router;
