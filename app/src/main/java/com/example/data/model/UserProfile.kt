package com.example.data.model

data class UserProfile(
  val uid: String = "",
  val email: String = "",
  val fullName: String = "",
  val phone: String = "",
  val city: String = "Якутск",
  val role: UserRole = UserRole.COURIER,
  val vehicleType: String = "Велосипед",
  val registeredAt: Long = System.currentTimeMillis(),
  val isProfileComplete: Boolean = false
)
