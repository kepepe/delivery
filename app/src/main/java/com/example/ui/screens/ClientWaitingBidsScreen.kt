package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CourierBid
import com.example.data.model.DeliveryOrder
import com.example.data.model.OrderStatus
import com.example.ui.components.InteractiveRouteMap
import com.example.ui.theme.DriveeGreen
import com.example.ui.theme.DriveeGreenContainer
import com.example.ui.theme.DriveeGreenDark
import com.example.ui.theme.DriveeOrange
import com.example.ui.theme.DriveeOrangeLight
import com.example.ui.theme.DriveeRed
import com.example.ui.theme.DriveeRedLight
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientWaitingBidsScreen(
  order: DeliveryOrder,
  onAcceptBid: (bidId: String) -> Unit,
  onCancelOrder: () -> Unit,
  isDarkTheme: Boolean,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val isDelivered = order.status == OrderStatus.DELIVERED
  val isCourierAssigned = (order.status == OrderStatus.ACCEPTED_BY_COURIER || order.status == OrderStatus.IN_TRANSIT) && !isDelivered
  val acceptedBid = order.bids.find { it.isAccepted } ?: order.bids.firstOrNull { it.courierName == order.assignedCourierName }
  var clientRating by remember { mutableIntStateOf(5) }
  var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }

  LaunchedEffect(order.id) {
    while (true) {
      delay(1000)
      nowMs = System.currentTimeMillis()
    }
  }

  val remainingSeconds = order.remainingAcceptanceSeconds(nowMs)

  val infiniteTransition = rememberInfiniteTransition(label = "radar")
  val rotation by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(2500, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "radarRotation"
  )

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = when {
              isDelivered -> "Заказ доставлен!"
              isCourierAssigned -> "Курьер в пути"
              else -> "Поиск курьеров"
            },
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
          )
        },
        navigationIcon = {
          IconButton(onClick = onCancelOrder, modifier = Modifier.testTag("waiting_back_btn")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
          }
        },
        actions = {
          if (!isCourierAssigned || isDelivered) {
            OutlinedButton(
              onClick = onCancelOrder,
              shape = RoundedCornerShape(10.dp),
              border = BorderStroke(1.dp, if (isDelivered) DriveeGreenDark else DriveeRed),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = if (isDelivered) DriveeGreenDark else DriveeRed),
              modifier = Modifier.padding(end = 12.dp)
            ) {
              Text(if (isDelivered) "Готово" else "Отменить", fontSize = 12.sp)
            }
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
      )
    },
    modifier = modifier
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .verticalScroll(rememberScrollState())
    ) {
      // Top Status Banner / Interactive Map
      if (isDelivered) {
        Surface(
          shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
          color = DriveeGreenContainer,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(DriveeGreenDark),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(38.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "Посылка успешно доставлена!",
              fontWeight = FontWeight.Bold,
              fontSize = 19.sp,
              color = DriveeGreenDark,
              textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Курьер передал заказ по адресу ${order.dropoffAddress}",
              fontSize = 13.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
              onClick = onCancelOrder,
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = DriveeGreen, contentColor = Color.Black),
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("delivered_new_order_btn")
            ) {
              Text("Создать новый заказ", fontWeight = FontWeight.Bold)
            }
          }
        }
      } else if (isCourierAssigned) {
        InteractiveRouteMap(
          modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .testTag("client_tracking_map"),
          distanceKm = order.distanceKm,
          etaMinutes = acceptedBid?.etaMinutes ?: 8,
          isDarkTheme = isDarkTheme,
          pickupAddress = order.pickupAddress,
          dropoffAddress = order.dropoffAddress,
          startLat = order.startLat.toDouble(),
          startLng = order.startLng.toDouble(),
          endLat = order.endLat.toDouble(),
          endLng = order.endLng.toDouble(),
          courierLat = order.courierLat,
          courierLng = order.courierLng,
          courierHeading = order.courierHeading,
          isLiveTracking = true
        )

        Surface(
          shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
          color = DriveeGreenContainer,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(DriveeGreenDark),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Курьер назначен!",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = DriveeGreenDark
              )
              Text(
                text = "Прибудет через ~${acceptedBid?.etaMinutes ?: 8} мин на ${acceptedBid?.vehicle ?: "авто"}",
                fontSize = 13.sp,
                color = DriveeGreenDark.copy(alpha = 0.8f)
              )
            }
          }
        }
      } else {
        // Radar pulse searching block
        Card(
          shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(DriveeGreenContainer),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Radar,
                contentDescription = null,
                tint = DriveeGreenDark,
                modifier = Modifier
                  .size(44.dp)
                  .rotate(rotation)
              )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
              text = "Ищем курьеров в Якутске...",
              fontWeight = FontWeight.Bold,
              fontSize = 17.sp,
              color = MaterialTheme.colorScheme.onSurface
            )

            Text(
              text = "Ваша начальная ставка: ${order.priceRub} ₽",
              fontSize = 13.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 15 seconds order response countdown chip
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (remainingSeconds <= 5) DriveeRedLight else DriveeOrangeLight,
              modifier = Modifier.testTag("client_waiting_timer_chip")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.Timer,
                  contentDescription = null,
                  tint = if (remainingSeconds <= 5) DriveeRed else DriveeOrange,
                  modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = if (remainingSeconds > 0) {
                    "Таймер отклика: $remainingSeconds сек (лимит 15 с)"
                  } else {
                    "Время ожидания (15 с) истекло"
                  },
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (remainingSeconds <= 5) DriveeRed else DriveeOrange
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Order Route Summary
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Text(text = "Откуда: ${order.pickupAddress}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text(text = "Куда: ${order.dropoffAddress}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DriveeGreenContainer,
                    modifier = Modifier.weight(1f)
                  ) {
                    Row(
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Icon(Icons.Default.Map, contentDescription = null, tint = DriveeGreenDark, modifier = Modifier.size(14.dp))
                      Spacer(modifier = Modifier.width(4.dp))
                      Text("OSM Карта активна", color = DriveeGreenDark, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                  }

                  OutlinedButton(
                    onClick = {
                      try {
                        val uri = Uri.parse("geo:${order.startLat},${order.startLng}?q=${Uri.encode(order.dropoffAddress + ", " + order.city)}")
                        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                        context.startActivity(mapIntent)
                      } catch (_: Exception) {
                        try {
                          val webUri = Uri.parse("https://www.openstreetmap.org/search?query=${Uri.encode(order.dropoffAddress + ", " + order.city)}")
                          context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                        } catch (_: Exception) {}
                      }
                    },
                    modifier = Modifier.testTag("waiting_open_external_maps_btn"),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                  ) {
                    Icon(Icons.Default.Navigation, contentDescription = null, tint = DriveeGreenDark, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Навигатор", fontSize = 11.sp)
                  }
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Section: Bidding Couriers or Assigned Driver Card
      Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        if (isDelivered) {
          // Driver review and delivery complete card
          Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = "Курьер: ${order.assignedCourierName ?: acceptedBid?.courierName ?: "Айсен В."}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
              )
              Text(
                text = acceptedBid?.vehicle ?: "Toyota Corolla • белый",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(12.dp))
              Text("Оцените качество доставки:", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
              Spacer(modifier = Modifier.height(8.dp))
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (star in 1..5) {
                  Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Star $star",
                    tint = if (star <= clientRating) DriveeOrange else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    modifier = Modifier
                      .size(36.dp)
                      .clickable { clientRating = star }
                  )
                }
              }
              Spacer(modifier = Modifier.height(16.dp))
              Button(
                onClick = onCancelOrder,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DriveeGreen, contentColor = Color.Black)
              ) {
                Text("Завершить и закрыть заказ", fontWeight = FontWeight.Bold)
              }
            }
          }
        } else if (isCourierAssigned) {
          // Driver details card
          Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(48.dp)
                      .clip(CircleShape)
                      .background(DriveeGreen),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.Black)
                  }
                  Spacer(modifier = Modifier.width(12.dp))
                  Column {
                    Text(
                      text = order.assignedCourierName ?: acceptedBid?.courierName ?: "Айсен В.",
                      fontWeight = FontWeight.Bold,
                      fontSize = 16.sp
                    )
                    Text(
                      text = acceptedBid?.vehicle ?: "Toyota Corolla • белый",
                      fontSize = 13.sp,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                      text = "★ ${acceptedBid?.courierRating ?: 4.98}",
                      fontWeight = FontWeight.Bold,
                      fontSize = 12.sp,
                      color = DriveeOrange
                    )
                  }
                }

                // Price badge
                Column(horizontalAlignment = Alignment.End) {
                  Text(text = "Итоговая цена", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                  Text(text = "${order.priceRub} ₽", fontSize = 20.sp, fontWeight = FontWeight.Black, color = DriveeGreenDark)
                }
              }

              Spacer(modifier = Modifier.height(16.dp))

              Button(
                onClick = {
                  try {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+79142809901"))
                    context.startActivity(intent)
                  } catch (_: Exception) {}
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DriveeGreen, contentColor = Color.Black)
              ) {
                Icon(Icons.Default.Call, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Позвонить курьеру", fontWeight = FontWeight.Bold)
              }
            }
          }
        } else {
          // Bids list
          Text(
            text = "Отклики курьеров (${order.bids.size}):",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(10.dp))

          if (order.bids.isEmpty()) {
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                CircularProgressIndicator(
                  modifier = Modifier.size(24.dp),
                  color = DriveeGreen,
                  strokeWidth = 2.5.dp
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                  text = "Ожидание ставок водителей (симуляция 3-5 сек)...",
                  fontSize = 13.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          } else {
            order.bids.forEach { bid ->
              Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 6.dp)
                  .testTag("bid_card_${bid.id}")
              ) {
                Column(modifier = Modifier.padding(14.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Box(
                        modifier = Modifier
                          .size(42.dp)
                          .clip(CircleShape)
                          .background(DriveeOrangeLight),
                        contentAlignment = Alignment.Center
                      ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = DriveeOrange)
                      }
                      Spacer(modifier = Modifier.width(10.dp))
                      Column {
                        Text(text = bid.courierName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(text = bid.vehicle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "★ ${bid.courierRating} • ${bid.etaMinutes} мин до вас", fontSize = 12.sp, color = DriveeOrange)
                      }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                      Text(
                        text = "${bid.bidPrice} ₽",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = if (bid.bidPrice > order.priceRub) DriveeOrange else DriveeGreenDark
                      )
                      if (bid.bidPrice > order.priceRub) {
                        Text(text = "+${bid.bidPrice - order.priceRub} ₽ торг", fontSize = 11.sp, color = DriveeOrange)
                      }
                    }
                  }

                  Spacer(modifier = Modifier.height(12.dp))

                  Button(
                    onClick = { onAcceptBid(bid.id) },
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(44.dp)
                      .testTag("btn_accept_bid_${bid.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                      containerColor = DriveeGreen,
                      contentColor = Color.Black
                    )
                  ) {
                    Text(
                      text = "Выбрать за ${bid.bidPrice} ₽",
                      fontWeight = FontWeight.Bold,
                      fontSize = 14.sp
                    )
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(32.dp))
      }
    }
  }
}
