package com.example.data.model

data class CourierReview(
  val id: String,
  val authorName: String,
  val rating: Int,
  val dateText: String,
  val commentText: String
)

data class ShiftOrderHistory(
  val id: String,
  val timeText: String,
  val routeText: String,
  val grossAmountRub: Int,
  val feeRub: Int,
  val taxRub: Int,
  val netAmountRub: Int
)

data class CourierProfile(
  val id: String = "courier_1",
  val name: String = "Курьер",
  val phone: String = "+7 (914) 270-00-01",
  val rating: Double = 5.0,
  val completedOrdersCount: Int = 0,
  val priorityText: String = "Стандартный",
  val accountNumber: String = "4081781009923481",
  val balanceRub: Int = 0,
  val shiftGrossRub: Int = 0,
  val serviceFeePercent: Double = 9.5,
  val taxPercent: Double = 4.0,
  val reviews: List<CourierReview> = emptyList(),
  val orderHistory: List<ShiftOrderHistory> = emptyList()
)
