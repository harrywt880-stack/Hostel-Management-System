const User = require("../models/User");
const bcrypt = require("bcryptjs");
const generateToken = require("../utils/generateToken");

const serializeUser = (user) => ({
  id: user._id,
  name: user.name,
  email: user.email,
  role: user.role,
  createdAt: user.createdAt
});

const VALID_ROLES = ["admin", "warden", "student"];

// REGISTER USER
const registerUser = async (req, res) => {
  try {
    const { name, email, password, role } = req.body;
    const normalizedEmail = (email || "").trim().toLowerCase();

    if (!name || !normalizedEmail || typeof password !== "string" || !password) {
      return res.status(400).json({
        message: "Please fill all required fields"
      });
    }

    const existingUser = await User.findOne({ email: normalizedEmail });

    if (existingUser) {
      return res.status(400).json({
        message: "User already exists"
      });
    }

    const normalizedRole = (role || "student").trim().toLowerCase();

    if (!VALID_ROLES.includes(normalizedRole)) {
      return res.status(400).json({
        message: "Invalid role"
      });
    }

    const isPrivilegedRole = normalizedRole !== "student";
    const isAdminRequest = req.user?.role === "admin";

    if (isPrivilegedRole && !isAdminRequest) {
      return res.status(403).json({
        message: "Only admin can create admin or warden accounts"
      });
    }

    const hashedPassword = await bcrypt.hash(password, 10);

    const user = await User.create({
      name,
      email: normalizedEmail,
      password: hashedPassword,
      role: isAdminRequest ? normalizedRole : "student"
    });

    res.status(201).json({
      message: "User registered successfully",
      user: serializeUser(user),
      token: generateToken(user._id)
    });

  } catch (error) {
    res.status(500).json({
      message: "Server error",
      error: error.message
    });
  }
};

// LOGIN USER
const loginUser = async (req, res) => {
  try {
    const { email, password } = req.body;
    const normalizedEmail = (email || "").trim().toLowerCase();

    if (!normalizedEmail || typeof password !== "string" || !password) {
      return res.status(400).json({
        message: "Email and password are required"
      });
    }

    const user = await User.findOne({ email: normalizedEmail });

    if (!user || !user.password) {
      return res.status(401).json({
        message: "User not found"
      });
    }

    const isPasswordMatch = await bcrypt.compare(password, user.password);

    if (!isPasswordMatch) {
      return res.status(401).json({
        message: "Invalid password"
      });
    }

    res.status(200).json({
      message: "Login successful",
      user: serializeUser(user),
      token: generateToken(user._id)
    });

  } catch (error) {
    res.status(500).json({
      message: "Server error",
      error: error.message
    });
  }
};

const getAllUsers = async (req, res) => {
  try {
    const users = await User.find().select("-password").sort({ createdAt: -1 });

    res.status(200).json(users.map(serializeUser));
  } catch (error) {
    res.status(500).json({
      message: "Server error",
      error: error.message
    });
  }
};

const getCurrentUser = async (req, res) => {
  res.status(200).json({
    user: serializeUser(req.user)
  });
};

const getDatabaseInfo = async (req, res) => {
  try {
    const db = User.db;
    const userCount = await User.countDocuments();

    res.status(200).json({
      databaseName: db.name,
      host: db.host,
      readyState: db.readyState,
      usersCollection: User.collection.name,
      userCount
    });
  } catch (error) {
    res.status(500).json({
      message: "Server error",
      error: error.message
    });
  }
};

const deleteUser = async (req, res) => {
  try {
    const { userId } = req.params;

    if (String(req.user._id) === String(userId)) {
      return res.status(400).json({
        message: "Admin cannot delete their own account"
      });
    }

    const deletedUser = await User.findByIdAndDelete(userId);

    if (!deletedUser) {
      return res.status(404).json({
        message: "User not found"
      });
    }

    res.status(200).json({
      message: "User deleted successfully",
      user: serializeUser(deletedUser)
    });
  } catch (error) {
    res.status(500).json({
      message: "Server error",
      error: error.message
    });
  }
};

module.exports = {
  registerUser,
  loginUser,
  getAllUsers,
  getCurrentUser,
  getDatabaseInfo,
  deleteUser
};
