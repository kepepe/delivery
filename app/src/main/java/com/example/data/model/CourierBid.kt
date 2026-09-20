package com.example.data.model

data class CourierBid(
  val id: String = "",
  val courierId: String = "",
  val courierName: String = "",
  val courierRating: Double = 5.0,
  val vehicle: String = "Пеший",
  val bidPrice: Int = 0,
  val etaMinutes: Int = 0,
  val isAccepted: Boolean = false,
  val timestamp: Long = System.currentTimeMillis()
)
