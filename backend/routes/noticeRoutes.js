const express = require("express");
const { createNotice, getNotices } = require("../controllers/noticeController");
const { protect, authorize } = require("../middleware/authMiddleware");

const router = express.Router();

router.get("/", protect, getNotices);
router.post("/", protect, authorize("admin"), createNotice);

module.exports = router;
