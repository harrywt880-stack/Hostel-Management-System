const express = require("express");
const http = require("http");
const cors = require("cors");
const dotenv = require("dotenv");
const path = require("path");
const { Server } = require("socket.io");
const connectDB = require("./config/db");
const seedAdmin = require("./utils/seedAdmin");

dotenv.config({ path: path.join(__dirname, ".env") });

const allowedOrigins = (process.env.ALLOWED_ORIGINS || "")
  .split(",")
  .map((origin) => origin.trim())
  .filter(Boolean);

const corsOptions = {
  origin(origin, callback) {
    if (!origin || allowedOrigins.length === 0 || allowedOrigins.includes(origin)) {
      callback(null, true);
      return;
    }

    callback(new Error("CORS origin not allowed"));
  }
};

const app = express();
const server = http.createServer(app);
const io = new Server(server, {
  cors: corsOptions
});

app.use(cors(corsOptions));
app.use(express.json());
app.set("io", io);

app.use("/api/auth", require("./routes/authRoutes"));
app.use("/api/rooms", require("./routes/roomRoutes"));
app.use("/api/dashboard", require("./routes/dashboardRoutes"));
app.use("/api/user", require("./routes/userRoutes"));
app.use("/api/booking-requests", require("./routes/bookingRequestRoutes"));
app.use("/api/complaints", require("./routes/complaintRoutes"));
app.use("/api/notices", require("./routes/noticeRoutes"));
app.use("/api/visitors", require("./routes/visitorRoutes"));
app.use("/api/mess-plans", require("./routes/messPlanRoutes"));
app.use("/api/payments", require("./routes/paymentRoutes"));

app.get("/", (req, res) => {
  res.send("Hostel Management Backend Running");
});

const PORT = process.env.PORT || 5000;

const startServer = async () => {
  await connectDB();
  await seedAdmin();

  io.on("connection", (socket) => {
    console.log(`Socket connected: ${socket.id}`);

    socket.on("disconnect", () => {
      console.log(`Socket disconnected: ${socket.id}`);
    });
  });

  server.listen(PORT, "0.0.0.0", () => {
    console.log(`Server running on port ${PORT}`);
  });
};

startServer();
