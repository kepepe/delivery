package com.example.data.repository

import com.example.data.model.CourierBid
import com.example.data.model.CourierProfile
import com.example.data.model.DeliveryOrder
import com.example.data.model.OrderStatus
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface DeliveryRepository {
  val orders: StateFlow<List<DeliveryOrder>>
  val activeOrder: StateFlow<DeliveryOrder?>
  val courierProfile: StateFlow<CourierProfile>
  val userProfile: StateFlow<UserProfile?>
  val toastEvents: SharedFlow<String>

  suspend fun saveUserProfile(profile: UserProfile): Boolean

  suspend fun createOrder(
    pickupAddress: String,
    dropoffAddress: String,
    priceRub: Int,
    comment: String,
    tags: List<String>,
    senderPhone: String
  ): DeliveryOrder

  suspend fun acceptOrderDirectly(orderId: String, courierId: String, courierName: String): Boolean

  suspend fun submitCounterBid(orderId: String, courierBid: CourierBid): Boolean

  suspend fun clientAcceptBid(orderId: String, bidId: String): Boolean

  suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus)
  suspend fun updateCourierLocation(orderId: String, lat: Double, lng: Double, heading: Float = 0f)
  suspend fun removeExpiredOrders()
  suspend fun deleteOrderPermanently(orderId: String)
  suspend fun clearOrderHistory()
}
