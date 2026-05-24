const express = require("express");
const router = express.Router();
const { optionalProtect, protect, authorize } = require("../middleware/authMiddleware");

const {
  registerUser,
  loginUser,
  getAllUsers,
  getCurrentUser,
  getDatabaseInfo,
  deleteUser
} = require("../controllers/authController");

router.post("/register", optionalProtect, registerUser);
router.post("/login", loginUser);
router.get("/me", protect, getCurrentUser);
router.get("/database-info", protect, authorize("admin"), getDatabaseInfo);
router.get("/users", protect, authorize("admin"), getAllUsers);
router.delete("/users/:userId", protect, authorize("admin"), deleteUser);

module.exports = router;
