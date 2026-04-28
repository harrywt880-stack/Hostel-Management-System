const Room = require("../models/Room");

const emitRoomsUpdated = (req) => {
  const io = req.app.get("io");

  if (io) {
    io.emit("roomsUpdated");
  }
};

const getRooms = async (req, res) => {
  try {
    const rooms = await Room.find().sort({ createdAt: -1 });
    res.status(200).json(rooms);
  } catch (error) {
    res.status(500).json({ message: "Server error", error: error.message });
  }
};

const addRoom = async (req, res) => {
  try {
    const { roomNumber, floor, capacity, occupiedBeds, status } = req.body;

    if (!roomNumber || !floor || !capacity) {
      return res.status(400).json({ message: "Room number, floor and capacity are required" });
    }

    const existingRoom = await Room.findOne({ roomNumber });

    if (existingRoom) {
      return res.status(400).json({ message: "Room already exists" });
    }

    const finalStatus =
      Number(occupiedBeds || 0) >= Number(capacity) ? "full" : status || "available";

    const room = await Room.create({
      roomNumber,
      floor,
      capacity,
      occupiedBeds: occupiedBeds || 0,
      status: finalStatus
    });

    res.status(201).json({
      message: "Room added successfully",
      room
    });

    emitRoomsUpdated(req);
  } catch (error) {
    res.status(500).json({ message: "Server error", error: error.message });
  }
};

module.exports = {
  getRooms,
  addRoom
};
