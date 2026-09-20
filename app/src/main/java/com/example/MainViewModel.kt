package com.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CourierBid
import com.example.data.model.CourierProfile
import com.example.data.model.DeliveryOrder
import com.example.data.model.OrderStatus
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.data.repository.DeliveryRepository
import com.example.data.repository.FirestoreDeliveryRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
  AUTH,
  PROFILE_SETUP,
  COURIER_FEED,
  ORDER_DETAIL,
  COURIER_PROFILE,
  CLIENT_CREATE,
  CLIENT_WAITING
}

class MainViewModel(
  private val repository: DeliveryRepository = FirestoreDeliveryRepository()
) : ViewModel() {

  val orders: StateFlow<List<DeliveryOrder>> = repository.orders
  val activeOrder: StateFlow<DeliveryOrder?> = repository.activeOrder
  val courierProfile: StateFlow<CourierProfile> = repository.courierProfile
  val userProfile: StateFlow<UserProfile?> = repository.userProfile

  private val _currentScreen = MutableStateFlow(AppScreen.COURIER_FEED)
  val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

  private val _userRole = MutableStateFlow(UserRole.COURIER)
  val userRole: StateFlow<UserRole> = _userRole.asStateFlow()

  private val _selectedOrder = MutableStateFlow<DeliveryOrder?>(null)
  val selectedOrder: StateFlow<DeliveryOrder?> = _selectedOrder.asStateFlow()

  // App Settings
  private val _isDarkTheme = MutableStateFlow(true)
  val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

  private val _compactView = MutableStateFlow(false)
  val compactView: StateFlow<Boolean> = _compactView.asStateFlow()

  private val _soundAlerts = MutableStateFlow(true)
  val soundAlerts: StateFlow<Boolean> = _soundAlerts.asStateFlow()

  private val _chainOrders = MutableStateFlow(true)
  val chainOrders: StateFlow<Boolean> = _chainOrders.asStateFlow()

  private val _toastMessage = MutableSharedFlow<String>(extraBufferCapacity = 10)
  val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

  init {
    viewModelScope.launch {
      repository.toastEvents.collect { msg ->
        _toastMessage.emit(msg)
      }
    }
  }

  fun onGoogleLogin(uid: String, email: String, displayName: String?, role: UserRole) {
    _userRole.value = role
    val current = repository.userProfile.value
    if (current == null || !current.isProfileComplete) {
      val tempProfile = UserProfile(
        uid = uid,
        email = email,
        fullName = displayName ?: (if (role == UserRole.COURIER) "Курьер" else "Заказчик"),
        phone = "",
        city = "Якутск",
        role = role,
        isProfileComplete = false
      )
      viewModelScope.launch {
        repository.saveUserProfile(tempProfile)
        _currentScreen.value = AppScreen.PROFILE_SETUP
      }
    } else {
      if (role == UserRole.COURIER) {
        _currentScreen.value = AppScreen.COURIER_FEED
      } else {
        _currentScreen.value = AppScreen.CLIENT_CREATE
      }
    }
  }

  fun saveProfile(profile: UserProfile) {
    _userRole.value = profile.role
    viewModelScope.launch {
      repository.saveUserProfile(profile.copy(isProfileComplete = true))
      _toastMessage.emit("Данные профиля успешно сохранены")
      if (profile.role == UserRole.COURIER) {
        _currentScreen.value = AppScreen.COURIER_FEED
      } else {
        _currentScreen.value = AppScreen.CLIENT_CREATE
      }
    }
  }

  fun onLogin(role: UserRole) {
    _userRole.value = role
    if (role == UserRole.COURIER) {
      _currentScreen.value = AppScreen.COURIER_FEED
    } else {
      _currentScreen.value = AppScreen.CLIENT_CREATE
    }
  }

  fun logout() {
    try {
      FirebaseAuth.getInstance().signOut()
    } catch (_: Exception) {}
    _currentScreen.value = AppScreen.AUTH
  }

  fun switchRole() {
    val newRole = if (_userRole.value == UserRole.COURIER) UserRole.CLIENT else UserRole.COURIER
    _userRole.value = newRole
    if (newRole == UserRole.COURIER) {
      _currentScreen.value = AppScreen.COURIER_FEED
    } else {
      val curActive = activeOrder.value
      if (curActive != null && curActive.status == OrderStatus.BARGAINING) {
        _currentScreen.value = AppScreen.CLIENT_WAITING
      } else {
        _currentScreen.value = AppScreen.CLIENT_CREATE
      }
    }
    viewModelScope.launch {
      _toastMessage.emit("Переключено на роль: ${newRole.titleRu}")
    }
  }

  fun navigateTo(screen: AppScreen) {
    _currentScreen.value = screen
  }

  fun selectOrder(order: DeliveryOrder) {
    _selectedOrder.value = order
    _currentScreen.value = AppScreen.ORDER_DETAIL
  }

  fun acceptOrderDirectly(order: DeliveryOrder) {
    viewModelScope.launch {
      val success = repository.acceptOrderDirectly(
        orderId = order.id,
        courierId = "c_user",
        courierName = courierProfile.value.name
      )
      if (success) {
        _selectedOrder.value = orders.value.find { it.id == order.id }
      }
    }
  }

  fun submitCounterBid(orderId: String, bid: CourierBid) {
    viewModelScope.launch {
      repository.submitCounterBid(orderId, bid)
      _selectedOrder.value = orders.value.find { it.id == orderId }
    }
  }

  fun markDelivered(orderId: String) {
    viewModelScope.launch {
      repository.updateOrderStatus(orderId, OrderStatus.DELIVERED)
      _selectedOrder.value = orders.value.find { it.id == orderId }
    }
  }

  // Client actions
  fun createOrder(
    pickup: String,
    dropoff: String,
    price: Int,
    comment: String,
    tags: List<String>,
    phone: String
  ) {
    viewModelScope.launch {
      val newOrder = repository.createOrder(pickup, dropoff, price, comment, tags, phone)
      _selectedOrder.value = newOrder
      _currentScreen.value = AppScreen.CLIENT_WAITING
    }
  }

  fun clientAcceptBid(bidId: String) {
    viewModelScope.launch {
      val order = activeOrder.value ?: selectedOrder.value ?: return@launch
      repository.clientAcceptBid(order.id, bidId)
    }
  }

  fun cancelOrder() {
    _currentScreen.value = AppScreen.CLIENT_CREATE
  }

  // Settings
  fun setDarkTheme(enabled: Boolean) {
    _isDarkTheme.value = enabled
  }

  fun setCompactView(enabled: Boolean) {
    _compactView.value = enabled
  }

  fun setSoundAlerts(enabled: Boolean) {
    _soundAlerts.value = enabled
  }

  fun setChainOrders(enabled: Boolean) {
    _chainOrders.value = enabled
  }
}
