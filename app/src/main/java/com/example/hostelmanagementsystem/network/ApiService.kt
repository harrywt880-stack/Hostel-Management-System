package com.example.hostelmanagementsystem.network

import com.example.hostelmanagementsystem.models.AdminPendingRequestsResponse
import com.example.hostelmanagementsystem.models.AnalyticsResponse
import com.example.hostelmanagementsystem.models.AssignRoomRequest
import com.example.hostelmanagementsystem.models.BookingResponse
import com.example.hostelmanagementsystem.models.Complaint
import com.example.hostelmanagementsystem.models.ComplaintRequest
import com.example.hostelmanagementsystem.models.ComplaintResponse
import com.example.hostelmanagementsystem.models.ComplaintStatusUpdateRequest
import com.example.hostelmanagementsystem.models.CreateFeeRequest
import com.example.hostelmanagementsystem.models.CreatePaymentOrderRequest
import com.example.hostelmanagementsystem.models.CreatePaymentOrderResponse
import com.example.hostelmanagementsystem.models.DashboardSummary
import com.example.hostelmanagementsystem.models.Fee
import com.example.hostelmanagementsystem.models.FeeResponse
import com.example.hostelmanagementsystem.models.LoginRequest
import com.example.hostelmanagementsystem.models.LoginResponse
import com.example.hostelmanagementsystem.models.MessPlan
import com.example.hostelmanagementsystem.models.MessPlanRequest
import com.example.hostelmanagementsystem.models.MessPlanResponse
import com.example.hostelmanagementsystem.models.Notice
import com.example.hostelmanagementsystem.models.NoticeRequest
import com.example.hostelmanagementsystem.models.NoticeResponse
import com.example.hostelmanagementsystem.models.PaymentVerificationResponse
import com.example.hostelmanagementsystem.models.RegisterRequest
import com.example.hostelmanagementsystem.models.RequestRoomBookingRequest
import com.example.hostelmanagementsystem.models.Room
import com.example.hostelmanagementsystem.models.RoomNotification
import com.example.hostelmanagementsystem.models.RoomRequestResponse
import com.example.hostelmanagementsystem.models.RoomResponse
import com.example.hostelmanagementsystem.models.UpdateFeeRequest
import com.example.hostelmanagementsystem.models.User
import com.example.hostelmanagementsystem.models.UserDashboardResponse
import com.example.hostelmanagementsystem.models.VerifyPaymentRequest
import com.example.hostelmanagementsystem.models.Visitor
import com.example.hostelmanagementsystem.models.VisitorRequest
import com.example.hostelmanagementsystem.models.VisitorResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @POST("auth/login")
    fun loginUser(
        @Body loginRequest: LoginRequest
    ): Call<LoginResponse>

    @POST("auth/register")
    fun registerUser(
        @Body registerRequest: RegisterRequest
    ): Call<LoginResponse>

    @GET("auth/users")
    fun getUsers(): Call<List<User>>

    @GET("rooms")
    fun getRooms(): Call<List<Room>>

    @POST("rooms")
    fun addRoom(
        @Body room: Room
    ): Call<RoomResponse>

    @GET("dashboard/summary")
    fun getDashboardSummary(): Call<DashboardSummary>

    @GET("dashboard/analytics")
    fun getAnalyticsReport(): Call<AnalyticsResponse>

    @GET("user/dashboard/{userId}")
    fun getUserDashboard(
        @Path("userId") userId: String
    ): Call<UserDashboardResponse>

    @POST("payments/create-order")
    fun createPaymentOrder(
        @Body createPaymentOrderRequest: CreatePaymentOrderRequest
    ): Call<CreatePaymentOrderResponse>

    @GET("payments/fees")
    fun getAllFees(): Call<List<Fee>>

    @POST("payments/fees")
    fun createFee(
        @Body createFeeRequest: CreateFeeRequest
    ): Call<FeeResponse>

    @PATCH("payments/fees/{feeId}")
    fun updateFee(
        @Path("feeId") feeId: String,
        @Body updateFeeRequest: UpdateFeeRequest
    ): Call<FeeResponse>

    @POST("payments/verify")
    fun verifyPayment(
        @Body verifyPaymentRequest: VerifyPaymentRequest
    ): Call<PaymentVerificationResponse>

    @POST("booking-requests")
    fun requestRoomBooking(
        @Body requestRoomBookingRequest: RequestRoomBookingRequest
    ): Call<RoomRequestResponse>

    @GET("booking-requests/admin/pending")
    fun getAdminPendingRequests(): Call<AdminPendingRequestsResponse>

    @PATCH("booking-requests/{requestId}/assign")
    fun assignRoomToRequest(
        @Path("requestId") requestId: String,
        @Body assignRoomRequest: AssignRoomRequest
    ): Call<RoomRequestResponse>

    @PATCH("booking-requests/{requestId}/cancel")
    fun cancelRoomRequest(
        @Path("requestId") requestId: String
    ): Call<RoomRequestResponse>

    @GET("booking-requests/notifications/{userId}")
    fun getUserNotifications(
        @Path("userId") userId: String
    ): Call<List<RoomNotification>>

    @PATCH("booking-requests/notifications/{userId}/read")
    fun markNotificationsRead(
        @Path("userId") userId: String
    ): Call<BookingResponse>

    @PATCH("user/release-room/{userId}")
    fun releaseRoom(
        @Path("userId") userId: String
    ): Call<BookingResponse>

    @POST("complaints")
    fun createComplaint(
        @Body complaintRequest: ComplaintRequest
    ): Call<ComplaintResponse>

    @GET("complaints/user/{userId}")
    fun getUserComplaints(
        @Path("userId") userId: String
    ): Call<List<Complaint>>

    @GET("complaints/admin")
    fun getAdminComplaints(): Call<List<Complaint>>

    @PATCH("complaints/{complaintId}/status")
    fun updateComplaintStatus(
        @Path("complaintId") complaintId: String,
        @Body complaintStatusUpdateRequest: ComplaintStatusUpdateRequest
    ): Call<ComplaintResponse>

    @GET("notices")
    fun getNotices(): Call<List<Notice>>

    @POST("notices")
    fun createNotice(
        @Body noticeRequest: NoticeRequest
    ): Call<NoticeResponse>

    @GET("visitors")
    fun getVisitors(): Call<List<Visitor>>

    @POST("visitors")
    fun createVisitor(
        @Body visitorRequest: VisitorRequest
    ): Call<VisitorResponse>

    @GET("mess-plans")
    fun getMessPlans(): Call<List<MessPlan>>

    @POST("mess-plans")
    fun createOrUpdateMessPlan(
        @Body messPlanRequest: MessPlanRequest
    ): Call<MessPlanResponse>
}
