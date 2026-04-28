const express = require("express");
const router = express.Router();
const { optionalProtect, protect, authorize } = require("../middleware/authMiddleware");

const {
  registerUser,
  loginUser,
  getAllUsers
} = require("../controllers/authController");

router.post("/register", optionalProtect, registerUser);
router.post("/login", loginUser);
router.get("/users", protect, authorize("admin"), getAllUsers);

module.exports = router;
