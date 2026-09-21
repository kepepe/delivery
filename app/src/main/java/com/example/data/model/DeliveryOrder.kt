package com.example.data.model

enum class OrderStatus(val titleRu: String) {
  NEW("Новый"),
  BARGAINING("Идет торг"),
  ACCEPTED_BY_COURIER("Взят курьером"),
  IN_TRANSIT("В пути"),
  DELIVERED("Доставлен"),
  TAKEN_BY_OTHER("Заказ уже взят"),
  CANCELLED("Отменен")
}

data class DeliveryOrder(
  val id: String = "",
  val pickupAddress: String = "",
  val dropoffAddress: String = "",
  val city: String = "Якутск",
  val distanceKm: Double = 0.0,
  val etaMinutes: Int = 0,
  val priceRub: Int = 0,
  val tags: List<String> = listOf("Курьер", "Перевод"),
  val comment: String = "",
  val senderPhone: String = "",
  val clientName: String = "Клиент",
  val clientRating: Double = 5.0,
  val status: OrderStatus = OrderStatus.NEW,
  val assignedCourierId: String? = null,
  val assignedCourierName: String? = null,
  val bids: List<CourierBid> = emptyList(),
  val createdAt: Long = System.currentTimeMillis(),
  val expiresAt: Long = createdAt + 15_000L,
  // Realtime GPS tracking coordinates of the assigned courier
  val courierLat: Double? = null,
  val courierLng: Double? = null,
  val courierHeading: Float = 0f,
  // Coordinates around Yakutsk center for realistic routing
  val startLat: Float = 62.0281f,
  val startLng: Float = 129.7325f,
  val endLat: Float = 62.0395f,
  val endLng: Float = 129.7540f
) {
  // Checks if order acceptance time (15s) expired without courier assignment
  fun isExpired(now: Long = System.currentTimeMillis()): Boolean {
    if (status == OrderStatus.DELIVERED || status == OrderStatus.CANCELLED) return true
    if (assignedCourierId == null && (status == OrderStatus.NEW || status == OrderStatus.BARGAINING)) {
      return now > expiresAt
    }
    return false
  }

  // Returns remaining seconds (0 to 15) for couriers to accept before order is cancelled/disappears
  fun remainingAcceptanceSeconds(now: Long = System.currentTimeMillis()): Int {
    if (assignedCourierId != null || status == OrderStatus.ACCEPTED_BY_COURIER || status == OrderStatus.IN_TRANSIT || status == OrderStatus.DELIVERED || status == OrderStatus.CANCELLED) {
      return 0
    }
    return ((expiresAt - now) / 1000).coerceAtLeast(0).toInt()
  }
}
