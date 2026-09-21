package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CourierBid
import com.example.data.model.CourierProfile
import com.example.data.model.DeliveryOrder
import com.example.data.model.OrderStatus
import com.example.ui.components.BargainingSheet
import com.example.ui.components.OrderCard
import com.example.ui.theme.DriveeGreen
import com.example.ui.theme.DriveeGreenDark
import com.example.ui.theme.DriveeOrange
import com.example.ui.theme.DriveeRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourierFeedScreen(
  orders: List<DeliveryOrder>,
  courierProfile: CourierProfile,
  compactView: Boolean,
  onOrderClick: (DeliveryOrder) -> Unit,
  onAcceptDirect: (DeliveryOrder) -> Unit,
  onSubmitCounterBid: (String, CourierBid) -> Unit,
  onOpenProfile: () -> Unit,
  onSwitchRole: () -> Unit,
  onClearOrderHistory: () -> Unit,
  liveActivityText: String = "🟢 18 курьеров на линии в Якутске • Высокий спрос",
  snackbarHostState: SnackbarHostState,
  modifier: Modifier = Modifier
) {
  var selectedFilter by remember { mutableStateOf("Все") }
  var orderForBargaining by remember { mutableStateOf<DeliveryOrder?>(null) }
  var showClearHistoryDialog by remember { mutableStateOf(false) }
  var showMenuDropdown by remember { mutableStateOf(false) }

  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.4f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulseAlpha"
  )

  val filters = listOf("Все", "Рядом (< 3 км)", "Высокий чек (> 300 ₽)", "Срочные", "Завершенные")

  val filteredOrders = remember(orders, selectedFilter) {
    if (selectedFilter == "Завершенные") {
      orders.filter { it.status == OrderStatus.DELIVERED }
    } else {
      val active = orders.filter { it.status != OrderStatus.DELIVERED && it.status != OrderStatus.CANCELLED }
      when (selectedFilter) {
        "Рядом (< 3 км)" -> active.filter { it.distanceKm <= 3.0 }
        "Высокий чек (> 300 ₽)" -> active.filter { it.priceRub >= 300 }
        "Срочные" -> active.filter { it.tags.contains("Срочно") }
        else -> active
      }
    }
  }

  if (showClearHistoryDialog) {
    AlertDialog(
      onDismissRequest = { showClearHistoryDialog = false },
      icon = {
        Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = DriveeRed, modifier = Modifier.size(36.dp))
      },
      title = {
        Text("Очистить историю заказов?", fontWeight = FontWeight.Bold)
      },
      text = {
        Text("История выполненных заказов и текущие неактивные заказы будут полностью удалены.")
      },
      confirmButton = {
        Button(
          onClick = {
            showClearHistoryDialog = false
            onClearOrderHistory()
          },
          colors = ButtonDefaults.buttonColors(containerColor = DriveeRed, contentColor = Color.White)
        ) {
          Text("Очистить", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showClearHistoryDialog = false }) {
          Text("Отмена")
        }
      }
    )
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "Лента заказов",
              fontWeight = FontWeight.Black,
              fontSize = 20.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "г. Якутск • Заказы в реальном времени",
              fontWeight = FontWeight.Medium,
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        actions = {
          // Balance Chip
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
              .clip(RoundedCornerShape(16.dp))
              .clickable { onOpenProfile() }
              .padding(end = 4.dp)
              .testTag("courier_feed_balance_chip")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.AccountBalanceWallet,
                contentDescription = null,
                tint = DriveeGreenDark,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "${courierProfile.balanceRub} ₽",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }

          // Fast Role Switch Button
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = DriveeOrange.copy(alpha = 0.15f),
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .clickable { onSwitchRole() }
              .padding(end = 4.dp)
              .testTag("feed_role_switch_btn")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = "Сменить роль",
                tint = DriveeOrange,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Заказчик",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = DriveeOrange
              )
            }
          }

          // More actions dropdown
          Box {
            IconButton(
              onClick = { showMenuDropdown = true },
              modifier = Modifier.testTag("feed_overflow_menu_btn")
            ) {
              Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Меню",
                tint = MaterialTheme.colorScheme.onSurface
              )
            }
            DropdownMenu(
              expanded = showMenuDropdown,
              onDismissRequest = { showMenuDropdown = false }
            ) {
              DropdownMenuItem(
                text = { Text("Очистить историю заказов") },
                leadingIcon = {
                  Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = DriveeRed)
                },
                onClick = {
                  showMenuDropdown = false
                  showClearHistoryDialog = true
                },
                modifier = Modifier.testTag("menu_clear_order_history_btn")
              )
              DropdownMenuItem(
                text = { Text("Личный кабинет") },
                leadingIcon = {
                  Icon(Icons.Default.Person, contentDescription = null)
                },
                onClick = {
                  showMenuDropdown = false
                  onOpenProfile()
                }
              )
            }
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    snackbarHost = { SnackbarHost(snackbarHostState) },
    modifier = modifier
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      // Live Activity Status Banner
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = DriveeGreenDark.copy(alpha = 0.12f),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp)
          .testTag("live_activity_banner")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(10.dp)
              .clip(CircleShape)
              .background(DriveeGreen.copy(alpha = pulseAlpha))
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = liveActivityText,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
          )
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = DriveeGreen,
            modifier = Modifier.padding(start = 4.dp)
          ) {
            Text(
              text = "LIVE",
              fontSize = 10.sp,
              fontWeight = FontWeight.ExtraBold,
              color = Color.Black,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }
      }

      // Filter Chips Row
      LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(filters) { filterName ->
          val isSelected = selectedFilter == filterName
          FilterChip(
            selected = isSelected,
            onClick = { selectedFilter = filterName },
            label = {
              Text(
                text = if (filterName == "Все") "Все (${filteredOrders.size})" else filterName,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 13.sp
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = DriveeGreen,
              selectedLabelColor = Color.Black
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("filter_chip_$filterName")
          )
        }
      }

      // Orders List
      if (filteredOrders.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.Inbox,
              contentDescription = null,
              modifier = Modifier.size(64.dp),
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "Нет доступных заказов",
              fontWeight = FontWeight.Bold,
              fontSize = 17.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Заказы исчезают через 15 секунд, если на них не откликнулись.",
              fontSize = 13.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
              onClick = onSwitchRole,
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = DriveeGreen, contentColor = Color.Black)
            ) {
              Text("Создать тестовый заказ как заказчик", fontWeight = FontWeight.Bold)
            }
          }
        }
      } else {
        LazyColumn(
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier
            .fillMaxSize()
            .testTag("orders_lazy_column")
        ) {
          items(filteredOrders, key = { it.id }) { order ->
            OrderCard(
              order = order,
              onAcceptDirect = onAcceptDirect,
              onBargainClick = { orderForBargaining = it },
              onCardClick = onOrderClick,
              compactView = compactView
            )
          }
          item {
            Spacer(modifier = Modifier.height(24.dp))
          }
        }
      }
    }

    // Modal Bargaining Sheet
    orderForBargaining?.let { order ->
      BargainingSheet(
        order = order,
        sheetState = sheetState,
        onDismiss = { orderForBargaining = null },
        onAcceptDirect = {
          onAcceptDirect(it)
          orderForBargaining = null
        },
        onSubmitCounterBid = { orderId, bid ->
          onSubmitCounterBid(orderId, bid)
          orderForBargaining = null
        }
      )
    }
  }
}
