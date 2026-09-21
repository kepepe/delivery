package com.example.data.repository

import com.example.data.model.CourierBid
import com.example.data.model.CourierProfile
import com.example.data.model.DeliveryOrder
import com.example.data.model.OrderStatus
import com.example.data.model.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class LocalPropDeliveryRepository(
  private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) : DeliveryRepository {

  private val _orders = MutableStateFlow<List<DeliveryOrder>>(generateInitialYakutskOrders())
  override val orders: StateFlow<List<DeliveryOrder>> = _orders.asStateFlow()

  private val _activeOrder = MutableStateFlow<DeliveryOrder?>(null)
  override val activeOrder: StateFlow<DeliveryOrder?> = _activeOrder.asStateFlow()

  private val _courierProfile = MutableStateFlow(CourierProfile())
  override val courierProfile: StateFlow<CourierProfile> = _courierProfile.asStateFlow()

  private val _userProfile = MutableStateFlow<UserProfile?>(null)
  override val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

  private val _toastEvents = MutableSharedFlow<String>(extraBufferCapacity = 10)
  override val toastEvents: SharedFlow<String> = _toastEvents.asSharedFlow()

  override suspend fun saveUserProfile(profile: UserProfile): Boolean {
    _userProfile.value = profile
    return true
  }

  override suspend fun createOrder(
    pickupAddress: String,
    dropoffAddress: String,
    priceRub: Int,
    comment: String,
    tags: List<String>,
    senderPhone: String
  ): DeliveryOrder {
    val now = System.currentTimeMillis()
    val newOrder = DeliveryOrder(
      id = "ord_${now % 100000}",
      pickupAddress = pickupAddress,
      dropoffAddress = dropoffAddress,
      city = "Якутск",
      distanceKm = 3.2,
      etaMinutes = 12,
      priceRub = priceRub,
      tags = tags.ifEmpty { listOf("Курьер", "Перевод") },
      comment = comment,
      senderPhone = senderPhone.ifEmpty { "+7 (914) 271-88-00" },
      clientName = "Вы (Заказчик)",
      clientRating = 5.0,
      status = OrderStatus.NEW,
      createdAt = now,
      expiresAt = now + 15_000L
    )

    _orders.value = listOf(newOrder) + _orders.value
    _activeOrder.value = newOrder

    // Automatically simulate incoming courier responses after short delays for on-set realism!
    simulateIncomingCourierBids(newOrder.id, priceRub)

    return newOrder
  }

  private fun simulateIncomingCourierBids(orderId: String, initialPrice: Int) {
    scope.launch {
      delay(2500)
      addBid(
        orderId,
        CourierBid(
          id = "bid_1",
          courierId = "c_1",
          courierName = "Айсен В.",
          courierRating = 4.98,
          vehicle = "Toyota Corolla • белый",
          bidPrice = initialPrice + 30,
          etaMinutes = 7
        )
      )

      delay(3500)
      addBid(
        orderId,
        CourierBid(
          id = "bid_2",
          courierId = "c_2",
          courierName = "Дьулустан П.",
          courierRating = 4.89,
          vehicle = "ВАЗ-2114 • серебристый",
          bidPrice = initialPrice,
          etaMinutes = 11
        )
      )

      delay(3000)
      addBid(
        orderId,
        CourierBid(
          id = "bid_3",
          courierId = "c_3",
          courierName = "Максим С.",
          courierRating = 4.92,
          vehicle = "Электросамокат (пеший)",
          bidPrice = initialPrice + 50,
          etaMinutes = 5
        )
      )
    }
  }

  private fun addBid(orderId: String, bid: CourierBid) {
    val current = _orders.value.toMutableList()
    val index = current.indexOfFirst { it.id == orderId }
    if (index != -1) {
      val order = current[index]
      if (order.status == OrderStatus.BARGAINING || order.status == OrderStatus.NEW) {
        val updatedBids = order.bids + bid
        val updatedOrder = order.copy(
          bids = updatedBids,
          status = OrderStatus.BARGAINING
        )
        current[index] = updatedOrder
        _orders.value = current
        if (_activeOrder.value?.id == orderId) {
          _activeOrder.value = updatedOrder
        }
      }
    }
  }

  override suspend fun acceptOrderDirectly(
    orderId: String,
    courierId: String,
    courierName: String
  ): Boolean {
    val current = _orders.value.toMutableList()
    val index = current.indexOfFirst { it.id == orderId }
    if (index == -1) return false

    val order = current[index]
    if (order.status == OrderStatus.TAKEN_BY_OTHER || order.status == OrderStatus.ACCEPTED_BY_COURIER) {
      _toastEvents.tryEmit("Заказ уже взят другим курьером")
      return false
    }

    val updated = order.copy(
      status = OrderStatus.ACCEPTED_BY_COURIER,
      assignedCourierId = courierId,
      assignedCourierName = courierName
    )
    current[index] = updated
    _orders.value = current
    _activeOrder.value = updated
    _toastEvents.tryEmit("Вы успешно приняли заказ на ${order.priceRub} ₽!")
    return true
  }

  override suspend fun submitCounterBid(orderId: String, courierBid: CourierBid): Boolean {
    val current = _orders.value.toMutableList()
    val index = current.indexOfFirst { it.id == orderId }
    if (index == -1) return false

    val order = current[index]
    if (order.status == OrderStatus.TAKEN_BY_OTHER) {
      _toastEvents.tryEmit("Заказ уже взят")
      return false
    }

    val updatedBids = order.bids.filterNot { it.courierId == courierBid.courierId } + courierBid
    val updated = order.copy(
      bids = updatedBids,
      status = OrderStatus.BARGAINING
    )
    current[index] = updated
    _orders.value = current
    _activeOrder.value = updated
    _toastEvents.tryEmit("Ставка ${courierBid.bidPrice} ₽ предложена заказчику")

    // Realistic film prop response: Client auto-accepts your bid after 4 seconds
    scope.launch {
      delay(4000)
      val latest = _orders.value.find { it.id == orderId }
      if (latest != null && latest.status == OrderStatus.BARGAINING) {
        clientAcceptBid(orderId, courierBid.id)
      }
    }
    return true
  }

  override suspend fun clientAcceptBid(orderId: String, bidId: String): Boolean {
    val current = _orders.value.toMutableList()
    val index = current.indexOfFirst { it.id == orderId }
    if (index == -1) return false

    val order = current[index]
    val bid = order.bids.find { it.id == bidId } ?: return false

    val updatedBids = order.bids.map { if (it.id == bidId) it.copy(isAccepted = true) else it }
    val updated = order.copy(
      status = OrderStatus.ACCEPTED_BY_COURIER,
      priceRub = bid.bidPrice,
      assignedCourierId = bid.courierId,
      assignedCourierName = bid.courierName,
      bids = updatedBids
    )
    current[index] = updated
    _orders.value = current
    _activeOrder.value = updated
    _toastEvents.tryEmit("Курьер ${bid.courierName} назначен! Цена: ${bid.bidPrice} ₽")
    return true
  }

  override suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
    val current = _orders.value.toMutableList()
    val index = current.indexOfFirst { it.id == orderId }
    if (index != -1) {
      val original = current[index]
      val updated = original.copy(status = newStatus)

      if (newStatus == OrderStatus.DELIVERED || newStatus == OrderStatus.CANCELLED) {
        // Permanently remove finished/cancelled order from active list
        current.removeAt(index)
        _orders.value = current
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
        current[index] = updated
        _orders.value = current
        if (_activeOrder.value?.id == orderId) {
          _activeOrder.value = updated
        }
      }
      _toastEvents.tryEmit("Статус заказа: ${newStatus.titleRu}")
    }
  }

  override suspend fun updateCourierLocation(orderId: String, lat: Double, lng: Double, heading: Float) {
    val current = _orders.value.toMutableList()
    val index = current.indexOfFirst { it.id == orderId }
    if (index != -1) {
      val updated = current[index].copy(
        courierLat = lat,
        courierLng = lng,
        courierHeading = heading
      )
      current[index] = updated
      _orders.value = current
      if (_activeOrder.value?.id == orderId) {
        _activeOrder.value = updated
      }
    } else if (_activeOrder.value?.id == orderId) {
      _activeOrder.value = _activeOrder.value?.copy(
        courierLat = lat,
        courierLng = lng,
        courierHeading = heading
      )
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

  private fun generateInitialYakutskOrders(): List<DeliveryOrder> {
    val now = System.currentTimeMillis()
    return listOf(
      DeliveryOrder(
        id = "ord_ykt_1",
        pickupAddress = "ул. Ленина, 14 (Гостиница Лена)",
        dropoffAddress = "203 мкрн, корп. 6",
        city = "г. Якутск",
        distanceKm = 2.4,
        etaMinutes = 11,
        priceRub = 250,
        tags = listOf("Курьер", "Перевод", "Срочно"),
        comment = "Доставить документы в приемную",
        senderPhone = "+7 (914) 271-88-00",
        clientName = "Саргылана М.",
        clientRating = 4.9,
        status = OrderStatus.NEW,
        createdAt = now,
        expiresAt = now + 15_000L,
        startLat = 62.0285f,
        startLng = 129.7330f,
        endLat = 62.0380f,
        endLng = 129.7540f
      ),
      DeliveryOrder(
        id = "ord_ykt_2",
        pickupAddress = "ТЦ Туймаада (Орджоникидзе, 38)",
        dropoffAddress = "ул. Ойунского, 24 (СВФУ)",
        city = "г. Якутск",
        distanceKm = 3.1,
        etaMinutes = 14,
        priceRub = 320,
        tags = listOf("Курьер", "От двери до двери"),
        comment = "Пакет с одеждой, забрать с 2 этажа",
        senderPhone = "+7 (914) 275-11-22",
        clientName = "Петр С.",
        clientRating = 5.0,
        status = OrderStatus.NEW,
        createdAt = now,
        expiresAt = now + 15_000L,
        startLat = 62.0310f,
        startLng = 129.7280f,
        endLat = 62.0190f,
        endLng = 129.7090f
      ),
      DeliveryOrder(
        id = "ord_ykt_3",
        pickupAddress = "ул. Дзержинского, 18 (Главпочтамт)",
        dropoffAddress = "ул. Ярославского, 20",
        city = "г. Якутск",
        distanceKm = 1.8,
        etaMinutes = 8,
        priceRub = 200,
        tags = listOf("Курьер", "Перевод"),
        comment = "Забрать конверт",
        senderPhone = "+7 (914) 299-44-55",
        clientName = "Елена В.",
        clientRating = 4.8,
        status = OrderStatus.NEW,
        createdAt = now,
        expiresAt = now + 15_000L,
        startLat = 62.0350f,
        startLng = 129.7390f,
        endLat = 62.0260f,
        endLng = 129.7380f
      )
    )
  }
}
