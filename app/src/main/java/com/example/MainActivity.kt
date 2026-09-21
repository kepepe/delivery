package com.example

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.location.LocationTrackingManager
import com.example.data.model.OrderStatus
import com.example.data.model.UserRole
import com.example.navigation.NavRoutes
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ClientCreateOrderScreen
import com.example.ui.screens.ClientWaitingBidsScreen
import com.example.ui.screens.CourierFeedScreen
import com.example.ui.screens.CourierProfileScreen
import com.example.ui.screens.OrderDetailScreen
import com.example.ui.screens.ProfileSetupScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val mainViewModel: MainViewModel = viewModel()
      val isDarkTheme by mainViewModel.isDarkTheme.collectAsState()

      MyApplicationTheme(darkTheme = isDarkTheme) {
        DriveeApp(viewModel = mainViewModel)
      }
    }
  }
}

@Composable
fun DriveeApp(
  viewModel: MainViewModel,
  navController: NavHostController = rememberNavController()
) {
  val orders by viewModel.orders.collectAsState()
  val activeOrder by viewModel.activeOrder.collectAsState()
  val selectedOrder by viewModel.selectedOrder.collectAsState()
  val courierProfile by viewModel.courierProfile.collectAsState()
  val userProfile by viewModel.userProfile.collectAsState()
  val isDarkTheme by viewModel.isDarkTheme.collectAsState()
  val compactView by viewModel.compactView.collectAsState()
  val soundAlerts by viewModel.soundAlerts.collectAsState()
  val chainOrders by viewModel.chainOrders.collectAsState()
  val liveActivityText by viewModel.liveActivityEvent.collectAsState()

  val snackbarHostState = remember { SnackbarHostState() }
  val context = LocalContext.current
  val locationTracker = remember { LocationTrackingManager(context) }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { /* Permissions result handled */ }

  LaunchedEffect(Unit) {
    permissionLauncher.launch(
      arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
      )
    )
  }

  // Real-time GPS location tracking loop when an active order is in progress
  val trackingOrder = activeOrder ?: selectedOrder
  LaunchedEffect(trackingOrder?.id, trackingOrder?.status) {
    val order = trackingOrder
    if (order != null && (order.status == OrderStatus.ACCEPTED_BY_COURIER || order.status == OrderStatus.IN_TRANSIT)) {
      locationTracker.startTracking(order) { lat, lng, heading ->
        viewModel.updateCourierLocation(order.id, lat, lng, heading)
      }
    } else {
      locationTracker.stopTracking()
    }
  }

  DisposableEffect(Unit) {
    onDispose {
      locationTracker.stopTracking()
    }
  }

  LaunchedEffect(Unit) {
    viewModel.toastMessage.collect { message ->
      snackbarHostState.showSnackbar(message)
    }
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    snackbarHost = { SnackbarHost(snackbarHostState) }
  ) { _ ->
    NavHost(
      navController = navController,
      startDestination = NavRoutes.COURIER_FEED,
      enterTransition = {
        fadeIn(animationSpec = tween(280)) + slideIntoContainer(
          AnimatedContentTransitionScope.SlideDirection.Start,
          animationSpec = tween(280)
        )
      },
      exitTransition = {
        fadeOut(animationSpec = tween(240)) + slideOutOfContainer(
          AnimatedContentTransitionScope.SlideDirection.Start,
          animationSpec = tween(240)
        )
      },
      popEnterTransition = {
        fadeIn(animationSpec = tween(280)) + slideIntoContainer(
          AnimatedContentTransitionScope.SlideDirection.End,
          animationSpec = tween(280)
        )
      },
      popExitTransition = {
        fadeOut(animationSpec = tween(240)) + slideOutOfContainer(
          AnimatedContentTransitionScope.SlideDirection.End,
          animationSpec = tween(240)
        )
      },
      modifier = Modifier.fillMaxSize()
    ) {
      // 1. Auth Screen
      composable(NavRoutes.AUTH) {
        AuthScreen(
          onGoogleSignInSuccess = { uid, email, displayName, role ->
            viewModel.onGoogleLogin(uid, email, displayName, role)
            navController.navigate(NavRoutes.PROFILE_SETUP) {
              popUpTo(NavRoutes.AUTH) { inclusive = true }
            }
          },
          onLoginSuccess = { role ->
            viewModel.onLogin(role)
            val destination = if (role == UserRole.COURIER) NavRoutes.COURIER_FEED else NavRoutes.CLIENT_CREATE
            navController.navigate(destination) {
              popUpTo(NavRoutes.AUTH) { inclusive = true }
            }
          },
          modifier = Modifier.fillMaxSize()
        )
      }

      // 1.1 Personal Data Entry on First Sign-in
      composable(NavRoutes.PROFILE_SETUP) {
        ProfileSetupScreen(
          initialProfile = userProfile,
          onProfileCompleted = { profile ->
            viewModel.saveProfile(profile)
            val destination = if (profile.role == UserRole.COURIER) NavRoutes.COURIER_FEED else NavRoutes.CLIENT_CREATE
            navController.navigate(destination) {
              popUpTo(NavRoutes.PROFILE_SETUP) { inclusive = true }
            }
          },
          modifier = Modifier.fillMaxSize()
        )
      }

      // 2. Courier Feed Screen
      composable(NavRoutes.COURIER_FEED) {
        CourierFeedScreen(
          orders = orders,
          courierProfile = courierProfile,
          compactView = compactView,
          onOrderClick = { order ->
            viewModel.selectOrder(order)
            navController.navigate(NavRoutes.ORDER_DETAIL)
          },
          onAcceptDirect = { order ->
            viewModel.acceptOrderDirectly(order)
            navController.navigate(NavRoutes.ORDER_DETAIL)
          },
          onSubmitCounterBid = { orderId, bid -> viewModel.submitCounterBid(orderId, bid) },
          onOpenProfile = { navController.navigate(NavRoutes.COURIER_PROFILE) },
          onSwitchRole = {
            viewModel.switchRole()
            navController.navigate(NavRoutes.CLIENT_CREATE)
          },
          onClearOrderHistory = { viewModel.clearOrderHistory() },
          liveActivityText = liveActivityText,
          snackbarHostState = snackbarHostState,
          modifier = Modifier.fillMaxSize()
        )
      }

      // 3. Active Order Details Screen
      composable(NavRoutes.ORDER_DETAIL) {
        val order = selectedOrder ?: orders.firstOrNull { it.status != OrderStatus.DELIVERED }
        if (order != null) {
          OrderDetailScreen(
            order = order,
            onBack = {
              viewModel.clearSelectedOrder()
              navController.popBackStack()
            },
            onAcceptDirect = { viewModel.acceptOrderDirectly(it) },
            onSubmitCounterBid = { orderId, bid -> viewModel.submitCounterBid(orderId, bid) },
            onMarkDelivered = { orderId -> viewModel.markDelivered(orderId) },
            isDarkTheme = isDarkTheme,
            modifier = Modifier.fillMaxSize()
          )
        } else {
          LaunchedEffect(Unit) {
            navController.navigate(NavRoutes.COURIER_FEED) {
              popUpTo(NavRoutes.COURIER_FEED) { inclusive = true }
            }
          }
        }
      }

      // 4. User Profile Screen
      composable(NavRoutes.COURIER_PROFILE) {
        CourierProfileScreen(
          profile = courierProfile,
          isDarkTheme = isDarkTheme,
          onToggleDarkTheme = { viewModel.setDarkTheme(it) },
          compactView = compactView,
          onToggleCompactView = { viewModel.setCompactView(it) },
          soundAlerts = soundAlerts,
          onToggleSoundAlerts = { viewModel.setSoundAlerts(it) },
          chainOrders = chainOrders,
          onToggleChainOrders = { viewModel.setChainOrders(it) },
          onSwitchRole = {
            viewModel.switchRole()
            navController.navigate(NavRoutes.CLIENT_CREATE)
          },
          onClearOrderHistory = { viewModel.clearOrderHistory() },
          onBack = { navController.popBackStack() },
          onLogout = {
            navController.navigate(NavRoutes.AUTH) {
              popUpTo(0) { inclusive = true }
            }
          },
          modifier = Modifier.fillMaxSize()
        )
      }

      // 5. Client Create Order Screen
      composable(NavRoutes.CLIENT_CREATE) {
        ClientCreateOrderScreen(
          onCreateOrder = { pickup, dropoff, price, comment, tags, phone ->
            viewModel.createOrder(pickup, dropoff, price, comment, tags, phone)
            navController.navigate(NavRoutes.CLIENT_WAITING)
          },
          onSwitchRole = {
            viewModel.switchRole()
            navController.navigate(NavRoutes.COURIER_FEED)
          },
          modifier = Modifier.fillMaxSize()
        )
      }

      // 6. Client Waiting Bids Screen
      composable(NavRoutes.CLIENT_WAITING) {
        val order = activeOrder ?: selectedOrder
        if (order != null) {
          ClientWaitingBidsScreen(
            order = order,
            onAcceptBid = { bidId -> viewModel.clientAcceptBid(bidId) },
            onCancelOrder = {
              viewModel.cancelOrder()
              navController.navigate(NavRoutes.CLIENT_CREATE) {
                popUpTo(NavRoutes.CLIENT_CREATE) { inclusive = true }
              }
            },
            isDarkTheme = isDarkTheme,
            modifier = Modifier.fillMaxSize()
          )
        } else {
          LaunchedEffect(Unit) {
            navController.navigate(NavRoutes.CLIENT_CREATE) {
              popUpTo(NavRoutes.CLIENT_CREATE) { inclusive = true }
            }
          }
        }
      }
    }
  }
}
