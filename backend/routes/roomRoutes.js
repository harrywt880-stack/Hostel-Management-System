const express = require("express");
const router = express.Router();
const { protect, authorize } = require("../middleware/authMiddleware");

const {
  getRooms,
  addRoom
} = require("../controllers/roomController");

router.get("/", protect, getRooms);
router.post("/", protect, authorize("admin"), addRoom);

module.exports = router;
