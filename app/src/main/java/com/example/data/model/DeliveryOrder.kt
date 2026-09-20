package com.example.data.model

enum class OrderStatus(val titleRu: String) {
  NEW("Новый"),
  BARGAINING("Идет торг"),
  ACCEPTED_BY_COURIER("Взят курьером"),
  IN_TRANSIT("В пути"),
  DELIVERED("Доставлен"),
  TAKEN_BY_OTHER("Заказ уже взят")
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
  // Coordinates around Yakutsk center for realistic vector routing
  val startLat: Float = 62.0281f,
  val startLng: Float = 129.7325f,
  val endLat: Float = 62.0395f,
  val endLng: Float = 129.7540f
)
