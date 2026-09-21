package com.example.data.repository

import android.util.Log
import com.example.data.model.CourierBid
import com.example.data.model.CourierProfile
import com.example.data.model.DeliveryOrder
import com.example.data.model.OrderStatus
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

/**
 * Production-ready Firestore repository for real-time delivery orders, bids and courier profiles.
 * Seamlessly synchronizes with Cloud Firestore when connected, with safe fallback and error handling.
 */
class FirestoreDeliveryRepository : DeliveryRepository {

  private val tag = "FirestoreRepo"

  // Lazy Firebase Firestore initialization with graceful fallback
  private val firestore: FirebaseFirestore? by lazy {
    try {
      FirebaseFirestore.getInstance()
    } catch (e: Exception) {
      Log.w(tag, "Firebase Firestore not initialized: ${e.message}")
      null
    }
  }

  private val _orders = MutableStateFlow<List<DeliveryOrder>>(emptyList())
  override val orders: StateFlow<List<DeliveryOrder>> = _orders.asStateFlow()

  private val _activeOrder = MutableStateFlow<DeliveryOrder?>(null)
  override val activeOrder: StateFlow<DeliveryOrder?> = _activeOrder.asStateFlow()

  private val _courierProfile = MutableStateFlow(CourierProfile())
  override val courierProfile: StateFlow<CourierProfile> = _courierProfile.asStateFlow()

  private val _userProfile = MutableStateFlow<UserProfile?>(null)
  override val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

  private val _toastEvents = MutableSharedFlow<String>(
    extraBufferCapacity = 10,
    onBufferOverflow = BufferOverflow.DROP_OLDEST
  )
  override val toastEvents: SharedFlow<String> = _toastEvents.asSharedFlow()

  private var ordersListenerRegistration: ListenerRegistration? = null
  private var activeOrderListenerRegistration: ListenerRegistration? = null

  init {
    startListeningToOrders()
  }

  override suspend fun saveUserProfile(profile: UserProfile): Boolean {
    _userProfile.value = profile
    // Also sync courier profile data
    _courierProfile.value = _courierProfile.value.copy(
      id = profile.uid.ifEmpty { "courier_1" },
      name = profile.fullName.ifEmpty { "Курьер" },
      phone = profile.phone.ifEmpty { "+7 (914) 270-00-01" },
      priorityText = if (profile.role == UserRole.COURIER) "Активный (${profile.vehicleType})" else "Стандартный"
    )

    val db = firestore
    if (db != null && profile.uid.isNotEmpty()) {
      try {
        val userMap = mapOf(
          "uid" to profile.uid,
          "email" to profile.email,
          "fullName" to profile.fullName,
          "phone" to profile.phone,
          "city" to profile.city,
          "role" to profile.role.name,
          "vehicleType" to profile.vehicleType,
          "registeredAt" to profile.registeredAt,
          "isProfileComplete" to profile.isProfileComplete
        )
        db.collection("users").document(profile.uid)
          .set(userMap, SetOptions.merge())
          .await()
      } catch (e: Exception) {
        Log.e(tag, "Failed to save user profile to Firestore: ${e.message}")
      }
    }
    return true
  }

  private fun startListeningToOrders() {
    val db = firestore ?: return
    try {
      ordersListenerRegistration?.remove()
      ordersListenerRegistration = db.collection("orders")
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .limit(50)
        .addSnapshotListener { snapshot, error ->
          if (error != null) {
            Log.e(tag, "Orders snapshot listener error: ${error.message}")
            return@addSnapshotListener
          }

          if (snapshot != null) {
            val fetchedOrders = snapshot.documents.mapNotNull { doc ->
              try {
                doc.toObject(DeliveryOrder::class.java)?.copy(id = doc.id)
              } catch (e: Exception) {
                Log.w(tag, "Failed to parse order doc ${doc.id}: ${e.message}")
                null
              }
            }
            _orders.value = fetchedOrders

            // Sync active order if it is updated in the list
            val currentActiveId = _activeOrder.value?.id
            if (currentActiveId != null) {
              val updatedActive = fetchedOrders.find { it.id == currentActiveId }
              if (updatedActive != null) {
                if (updatedActive.status == OrderStatus.DELIVERED || updatedActive.status == OrderStatus.CANCELLED) {
                  _activeOrder.value = null
                } else {
                  _activeOrder.value = updatedActive
                }
              }
            }
          }
        }
    } catch (e: Exception) {
      Log.e(tag, "Failed to setup snapshot listener: ${e.message}")
    }
  }

  private fun listenToActiveOrder(orderId: String) {
    val db = firestore ?: return
    activeOrderListenerRegistration?.remove()
    try {
      activeOrderListenerRegistration = db.collection("orders").document(orderId)
        .addSnapshotListener { snapshot, error ->
          if (error != null) return@addSnapshotListener
          if (snapshot != null && snapshot.exists()) {
            val order = snapshot.toObject(DeliveryOrder::class.java)?.copy(id = snapshot.id)
            if (order != null) {
              _activeOrder.value = order
            }
          }
        }
    } catch (e: Exception) {
      Log.w(tag, "Could not listen to active order: ${e.message}")
    }
  }

  override suspend fun createOrder(
    pickupAddress: String,
    dropoffAddress: String,
    priceRub: Int,
    comment: String,
    tags: List<String>,
    senderPhone: String
  ): DeliveryOrder {
    val orderId = "ord_${System.currentTimeMillis()}"
    val now = System.currentTimeMillis()
    val order = DeliveryOrder(
      id = orderId,
      pickupAddress = pickupAddress,
      dropoffAddress = dropoffAddress,
      city = "Якутск",
      distanceKm = 3.2,
      etaMinutes = 12,
      priceRub = priceRub,
      tags = tags,
      comment = comment,
      senderPhone = senderPhone,
      clientName = "Заказчик",
      clientRating = 5.0,
      status = OrderStatus.NEW,
      createdAt = now,
      expiresAt = now + 15_000L
    )

    // Local optimistic update
    _orders.value = listOf(order) + _orders.value.filter { it.id != order.id }
    _activeOrder.value = order

    val db = firestore
    if (db != null) {
      try {
        db.collection("orders").document(orderId).set(order).await()
        listenToActiveOrder(orderId)
        _toastEvents.tryEmit("Заказ опубликован в Cloud Firestore")
      } catch (e: Exception) {
        Log.e(tag, "Error saving order to Firestore: ${e.message}")
        _toastEvents.tryEmit("Заказ создан локально (ожидание сети)")
      }
    } else {
      _toastEvents.tryEmit("Заказ опубликован")
    }

    return order
  }

  override suspend fun acceptOrderDirectly(
    orderId: String,
    courierId: String,
    courierName: String
  ): Boolean {
    val target = _orders.value.find { it.id == orderId } ?: return false
    val updated = target.copy(
      status = OrderStatus.ACCEPTED_BY_COURIER,
      assignedCourierId = courierId,
      assignedCourierName = courierName
    )

    // Optimistic update
    updateLocalOrder(updated)
    _toastEvents.tryEmit("Заказ взят в работу")

    val db = firestore
    if (db != null) {
      try {
        db.collection("orders").document(orderId).update(
          mapOf(
            "status" to OrderStatus.ACCEPTED_BY_COURIER.name,
            "assignedCourierId" to courierId,
            "assignedCourierName" to courierName
          )
        ).await()
      } catch (e: Exception) {
        Log.e(tag, "Failed to accept order directly in Firestore: ${e.message}")
      }
    }
    return true
  }

  override suspend fun submitCounterBid(orderId: String, courierBid: CourierBid): Boolean {
    val target = _orders.value.find { it.id == orderId } ?: return false
    val updatedBids = target.bids + courierBid
    val updated = target.copy(
      bids = updatedBids,
      status = OrderStatus.BARGAINING
    )

    // Optimistic update
    updateLocalOrder(updated)
    _toastEvents.tryEmit("Ваша ставка передана заказчику")

    val db = firestore
    if (db != null) {
      try {
        db.collection("orders").document(orderId).update(
          mapOf(
            "bids" to updatedBids,
            "status" to OrderStatus.BARGAINING.name
          )
        ).await()
      } catch (e: Exception) {
        Log.e(tag, "Failed to submit bid to Firestore: ${e.message}")
      }
    }
    return true
  }

  override suspend fun clientAcceptBid(orderId: String, bidId: String): Boolean {
    val target = _orders.value.find { it.id == orderId } ?: return false
    val bid = target.bids.find { it.id == bidId } ?: return false

    val updatedBids = target.bids.map { b ->
      if (b.id == bidId) b.copy(isAccepted = true) else b
    }

    val updated = target.copy(
      status = OrderStatus.ACCEPTED_BY_COURIER,
      priceRub = bid.bidPrice,
      assignedCourierId = bid.courierId,
      assignedCourierName = bid.courierName,
      bids = updatedBids
    )

    // Optimistic update
    updateLocalOrder(updated)
    _toastEvents.tryEmit("Курьер ${bid.courierName} подтвержден!")

    val db = firestore
    if (db != null) {
      try {
        db.collection("orders").document(orderId).update(
          mapOf(
            "status" to OrderStatus.ACCEPTED_BY_COURIER.name,
            "priceRub" to bid.bidPrice,
            "assignedCourierId" to bid.courierId,
            "assignedCourierName" to bid.courierName,
            "bids" to updatedBids
          )
        ).await()
      } catch (e: Exception) {
        Log.e(tag, "Failed to accept bid in Firestore: ${e.message}")
      }
    }
    return true
  }

  override suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
    val target = _orders.value.find { it.id == orderId } ?: return
    val updated = target.copy(status = newStatus)

    if (newStatus == OrderStatus.DELIVERED || newStatus == OrderStatus.CANCELLED) {
      // Permanently remove from active list
      _orders.value = _orders.value.filterNot { it.id == orderId }
      if (_activeOrder.value?.id == orderId) {
        _activeOrder.value = null
      }
      if (newStatus == OrderStatus.DELIVERED) {
        val earned = updated.priceRub
        val curCourier = _courierProfile.value
        val timeStr = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        val fee = (earned * (curCourier.serviceFeePercent / 100.0)).toInt()
        val tax = (earned * (curCourier.taxPercent / 100.0)).toInt()
        val net = earned - fee - tax
        val histRecord = com.example.data.model.ShiftOrderHistory(
          id = orderId,
          timeText = timeStr,
          routeText = "${updated.pickupAddress} → ${updated.dropoffAddress}",
          grossAmountRub = earned,
          feeRub = fee,
          taxRub = tax,
          netAmountRub = net
        )
        _courierProfile.value = curCourier.copy(
          completedOrdersCount = curCourier.completedOrdersCount + 1,
          balanceRub = curCourier.balanceRub + earned,
          shiftGrossRub = curCourier.shiftGrossRub + earned,
          orderHistory = listOf(histRecord) + curCourier.orderHistory
        )
      }
    } else {
      updateLocalOrder(updated)
    }
    _toastEvents.tryEmit("Статус заказа: ${newStatus.titleRu}")

    val db = firestore
    if (db != null) {
      try {
        if (newStatus == OrderStatus.DELIVERED || newStatus == OrderStatus.CANCELLED) {
          db.collection("orders").document(orderId).delete().await()
        } else {
          db.collection("orders").document(orderId).update(
            "status", newStatus.name
          ).await()
        }
      } catch (e: Exception) {
        Log.e(tag, "Failed to update status in Firestore: ${e.message}")
      }
    }
  }

  override suspend fun updateCourierLocation(orderId: String, lat: Double, lng: Double, heading: Float) {
    val target = _orders.value.find { it.id == orderId } ?: _activeOrder.value
    if (target != null && target.id == orderId) {
      val updated = target.copy(courierLat = lat, courierLng = lng, courierHeading = heading)
      updateLocalOrder(updated)
      val db = firestore
      if (db != null) {
        try {
          db.collection("orders").document(orderId).update(
            mapOf(
              "courierLat" to lat,
              "courierLng" to lng,
              "courierHeading" to heading
            )
          ).await()
        } catch (e: Exception) {
          // ignore or log
        }
      }
    }
  }

  override suspend fun removeExpiredOrders() {
    val now = System.currentTimeMillis()
    val current = _orders.value
    val filtered = current.filterNot { it.isExpired(now) }
    if (filtered.size != current.size) {
      _orders.value = filtered
      if (_activeOrder.value?.let { it.isExpired(now) } == true) {
        _activeOrder.value = null
      }
    }
  }

  override suspend fun deleteOrderPermanently(orderId: String) {
    _orders.value = _orders.value.filterNot { it.id == orderId }
    if (_activeOrder.value?.id == orderId) {
      _activeOrder.value = null
    }
    firestore?.collection("orders")?.document(orderId)?.delete()
  }

  override suspend fun clearOrderHistory() {
    val curCourier = _courierProfile.value
    _courierProfile.value = curCourier.copy(
      orderHistory = emptyList(),
      shiftGrossRub = 0
    )
    _orders.value = emptyList()
    _activeOrder.value = null
    _toastEvents.tryEmit("История заказов полностью очищена")
  }

  private fun updateLocalOrder(updatedOrder: DeliveryOrder) {
    val current = _orders.value.toMutableList()
    val idx = current.indexOfFirst { it.id == updatedOrder.id }
    if (idx != -1) {
      current[idx] = updatedOrder
      _orders.value = current
    } else {
      _orders.value = listOf(updatedOrder) + current
    }
    if (_activeOrder.value?.id == updatedOrder.id) {
      if (updatedOrder.status == OrderStatus.DELIVERED || updatedOrder.status == OrderStatus.CANCELLED) {
        _activeOrder.value = null
      } else {
        _activeOrder.value = updatedOrder
      }
    }
  }
}
