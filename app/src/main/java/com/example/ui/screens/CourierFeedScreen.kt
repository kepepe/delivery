package com.example.ui.screens

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
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
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
import com.example.data.model.UserRole
import com.example.ui.components.BargainingSheet
import com.example.ui.components.OrderCard
import com.example.ui.theme.DriveeGreen
import com.example.ui.theme.DriveeGreenDark
import com.example.ui.theme.DriveeOrange
import kotlinx.coroutines.launch

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
  snackbarHostState: SnackbarHostState,
  modifier: Modifier = Modifier
) {
  var selectedFilter by remember { mutableStateOf("Все") }
  var orderForBargaining by remember { mutableStateOf<DeliveryOrder?>(null) }

  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val scope = rememberCoroutineScope()

  val filters = listOf("Все", "Рядом (< 3 км)", "Высокий чек (> 300 ₽)", "Срочные")

  val filteredOrders = remember(orders, selectedFilter) {
    when (selectedFilter) {
      "Рядом (< 3 км)" -> orders.filter { it.distanceKm <= 3.0 }
      "Высокий чек (> 300 ₽)" -> orders.filter { it.priceRub >= 300 }
      "Срочные" -> orders.filter { it.tags.contains("Срочно") }
      else -> orders
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .background(DriveeGreen, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.DirectionsBike,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "Лента заказов",
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "г. Якутск",
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
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
              .padding(end = 6.dp)
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
              .padding(end = 8.dp)
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

          // Profile button
          IconButton(
            onClick = onOpenProfile,
            modifier = Modifier.testTag("feed_profile_btn")
          ) {
            Icon(
              imageVector = Icons.Default.Person,
              contentDescription = "Профиль",
              tint = MaterialTheme.colorScheme.onSurface
            )
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
      // Filter Chips Row
      LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(filters) { filterName ->
          val isSelected = selectedFilter == filterName
          FilterChip(
            selected = isSelected,
            onClick = { selectedFilter = filterName },
            label = {
              Text(
                text = if (filterName == "Все") "Все (${orders.size})" else filterName,
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
              imageVector = Icons.Default.DirectionsBike,
              contentDescription = null,
              modifier = Modifier.size(64.dp),
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "Нет активных заказов в Якутске",
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Новые заказы от заказчиков появятся здесь в реальном времени",
              fontSize = 13.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      } else {
        LazyColumn(
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
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
