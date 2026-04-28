const express = require("express");
const { createOrUpdateMessPlan, getMessPlans } = require("../controllers/messPlanController");
const { protect, authorize } = require("../middleware/authMiddleware");

const router = express.Router();

router.get("/", protect, getMessPlans);
router.post("/", protect, authorize("admin"), createOrUpdateMessPlan);

module.exports = router;
