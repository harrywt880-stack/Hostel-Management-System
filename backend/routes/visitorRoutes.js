const express = require("express");
const { createVisitor, getVisitors } = require("../controllers/visitorController");
const { protect, authorize } = require("../middleware/authMiddleware");

const router = express.Router();

router.get("/", protect, authorize("admin"), getVisitors);
router.post("/", protect, authorize("admin"), createVisitor);

module.exports = router;
