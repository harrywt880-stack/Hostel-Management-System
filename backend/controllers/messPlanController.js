const MessPlan = require("../models/MessPlan");
const User = require("../models/User");

const startOfDayUtc = (value) => {
  const date = new Date(value);
  return new Date(Date.UTC(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate()));
};

const serializeUser = (user) => ({
  id: user._id,
  name: user.name,
  email: user.email,
  role: user.role
});

const serializeMessPlan = (plan) => ({
  id: plan._id,
  planDate: plan.planDate,
  breakfast: plan.breakfast,
  lunch: plan.lunch,
  eveningSnacks: plan.eveningSnacks,
  dinner: plan.dinner,
  notes: plan.notes,
  createdAt: plan.createdAt,
  updatedAt: plan.updatedAt,
  createdBy: plan.createdBy ? serializeUser(plan.createdBy) : null
});

const emitMessPlanEvent = (req, payload = {}) => {
  const io = req.app.get("io");

  if (io) {
    io.emit("messPlansUpdated", payload);
  }
};

const createOrUpdateMessPlan = async (req, res) => {
  try {
    const { planDate, breakfast, lunch, eveningSnacks, dinner, notes } = req.body;

    if (!planDate || !breakfast || !lunch || !eveningSnacks || !dinner) {
      return res.status(400).json({ message: "Date and all meal fields are required" });
    }

    const admin = await User.findById(req.user?._id).select("-password");

    if (!admin || admin.role !== "admin") {
      return res.status(403).json({ message: "Only admin can manage mess plans" });
    }

    const normalizedDate = startOfDayUtc(planDate);

    if (Number.isNaN(normalizedDate.getTime())) {
      return res.status(400).json({ message: "Plan date is invalid" });
    }

    const updatedPlan = await MessPlan.findOneAndUpdate(
      { planDate: normalizedDate },
      {
        planDate: normalizedDate,
        breakfast: breakfast.trim(),
        lunch: lunch.trim(),
        eveningSnacks: eveningSnacks.trim(),
        dinner: dinner.trim(),
        notes: (notes || "").trim(),
        createdBy: admin._id
      },
      {
        upsert: true,
        new: true,
        runValidators: true,
        setDefaultsOnInsert: true
      }
    ).populate("createdBy", "-password");

    emitMessPlanEvent(req, { planDate: normalizedDate.toISOString() });

    res.status(200).json({
      message: "Mess plan saved successfully",
      plan: serializeMessPlan(updatedPlan)
    });
  } catch (error) {
    res.status(500).json({ message: "Server error", error: error.message });
  }
};

const getMessPlans = async (req, res) => {
  try {
    const plans = await MessPlan.find()
      .populate("createdBy", "-password")
      .sort({ planDate: -1 })
      .limit(30);

    res.status(200).json(plans.map(serializeMessPlan));
  } catch (error) {
    res.status(500).json({ message: "Server error", error: error.message });
  }
};

module.exports = {
  createOrUpdateMessPlan,
  getMessPlans,
  serializeMessPlan
};
