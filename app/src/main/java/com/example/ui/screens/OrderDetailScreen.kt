package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OrderDetailScreen(
  order: DeliveryOrder,
  onBack: () -> Unit,
  onAcceptDirect: (DeliveryOrder) -> Unit,
  onSubmitCounterBid: (String, CourierBid) -> Unit,
  onMarkDelivered: (String) -> Unit,
  isDarkTheme: Boolean,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var proposedPrice by remember(order) {
    mutableIntStateOf(order.priceRub + 30)
  }

  val quickChips = listOf(
    order.priceRub + 20,
    order.priceRub + 30,
    order.priceRub + 50,
    order.priceRub + 70
  )

  var showDeliveredSuccessDialog by remember { mutableStateOf(false) }

  val isTaken = order.status == OrderStatus.TAKEN_BY_OTHER
  val isAccepted = order.status == OrderStatus.ACCEPTED_BY_COURIER || order.status == OrderStatus.IN_TRANSIT
  val isDelivered = order.status == OrderStatus.DELIVERED

  if (showDeliveredSuccessDialog) {
    AlertDialog(
      onDismissRequest = {
        showDeliveredSuccessDialog = false
        onBack()
      },
      icon = {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = DriveeGreen, modifier = Modifier.size(44.dp))
      },
      title = {
        Text("Заказ успешно выполнен!", fontWeight = FontWeight.Bold)
      },
      text = {
        Column {
          Text("Вы успешно доставили заказ #${order.id.takeLast(4)}.")
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            "+${order.priceRub} ₽ начислено на ваш баланс.",
            fontWeight = FontWeight.Bold,
            color = DriveeGreenDark
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            "Заказ закрыт и больше не отображается в активной ленте.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            showDeliveredSuccessDialog = false
            onBack()
          },
          colors = ButtonDefaults.buttonColors(containerColor = DriveeGreen, contentColor = Color.Black)
        ) {
          Text("К списку заказов", fontWeight = FontWeight.Bold)
        }
      }
    )
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Заказ #${order.id.takeLast(4)}",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
          )
        },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("detail_back_btn")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
          }
        },
        actions = {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isTaken) DriveeRedLight else DriveeGreenContainer,
            modifier = Modifier.padding(end = 12.dp)
          ) {
            Text(
              text = order.status.titleRu,
              color = if (isTaken) DriveeRed else DriveeGreenDark,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
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
      // 1. Interactive Route Map with Option B Google Maps
      InteractiveRouteMap(
        modifier = Modifier
          .fillMaxWidth()
          .height(250.dp)
          .testTag("detail_interactive_map"),
        distanceKm = order.distanceKm,
        etaMinutes = order.etaMinutes,
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
        isLiveTracking = isTaken || order.status == OrderStatus.ACCEPTED_BY_COURIER || order.status == OrderStatus.IN_TRANSIT
      )

      Column(modifier = Modifier.padding(16.dp)) {
        // Price & Customer Bar
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "${order.priceRub} ₽",
              fontSize = 28.sp,
              fontWeight = FontWeight.Black,
              color = if (isTaken) DriveeRed else DriveeGreenDark,
              modifier = Modifier.testTag("detail_price")
            )
            Text(
              text = "${order.distanceKm} км • ${order.etaMinutes} мин в пути",
              fontSize = 13.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          // Customer Avatar & Rating
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
              .padding(8.dp)
          ) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(DriveeOrangeLight),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = DriveeOrange,
                modifier = Modifier.size(22.dp)
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = order.clientName,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "★ ${order.clientRating} (48 отз.)",
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = DriveeOrange
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Route Card
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            // Point A
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(modifier = Modifier.size(12.dp).background(DriveeGreen, CircleShape))
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(text = "Откуда (Точка А)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = order.pickupAddress, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
              }
            }

            Box(
              modifier = Modifier
                .padding(start = 5.dp)
                .width(2.dp)
                .height(20.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            // Point B
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(modifier = Modifier.size(12.dp).background(DriveeOrange, CircleShape))
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(text = "Куда (Точка Б)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = order.dropoffAddress, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Interactive route hint & optional external navigation button
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = DriveeGreenContainer,
                modifier = Modifier.weight(1f)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(Icons.Default.Map, contentDescription = null, tint = DriveeGreenDark, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("OSM Карта активна", color = DriveeGreenDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                modifier = Modifier.testTag("detail_open_external_maps_btn"),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
              ) {
                Icon(Icons.Default.Navigation, contentDescription = null, tint = DriveeGreenDark, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Навигатор", fontSize = 12.sp)
              }
            }
          }
        }

        // Comment & Tags
        if (order.comment.isNotEmpty()) {
          Spacer(modifier = Modifier.height(12.dp))
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = DriveeGreenDark, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(text = order.comment, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Phone call card
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
          modifier = Modifier
            .fillMaxWidth()
            .clickable {
              try {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${order.senderPhone}"))
                context.startActivity(intent)
              } catch (_: Exception) {}
            }
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Call, contentDescription = null, tint = DriveeGreenDark, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(text = "Телефон отправителя", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = order.senderPhone, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
              }
            }
            Text(text = "Вызов", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DriveeGreenDark)
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Actions: Direct Accept & Bargaining
        if (isDelivered) {
          Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Surface(
              shape = RoundedCornerShape(16.dp),
              color = DriveeGreenContainer,
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = DriveeGreenDark, modifier = Modifier.size(38.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Заказ успешно доставлен!", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = DriveeGreenDark)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "+${order.priceRub} ₽ начислено на ваш баланс", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
              }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Button(
              onClick = onBack,
              modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("detail_return_to_feed_btn"),
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(containerColor = DriveeGreen, contentColor = Color.Black)
            ) {
              Text("Вернуться в ленту заказов", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
          }
        } else if (isAccepted) {
          Column {
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = DriveeGreenContainer,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = "Вы выполняете этот заказ. Сумма к оплате: ${order.priceRub} ₽",
                color = DriveeGreenDark,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(14.dp),
                textAlign = TextAlign.Center
              )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
              onClick = {
                onMarkDelivered(order.id)
                showDeliveredSuccessDialog = true
              },
              modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("detail_mark_delivered_btn"),
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(containerColor = DriveeGreen, contentColor = Color.Black)
            ) {
              Icon(Icons.Default.CheckCircle, contentDescription = null)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Завершить заказ и получить ${order.priceRub} ₽", fontWeight = FontWeight.Bold)
            }
          }
        } else if (!isTaken) {
          // Direct Consent Button
          Button(
            onClick = { onAcceptDirect(order) },
            modifier = Modifier
              .fillMaxWidth()
              .height(52.dp)
              .testTag("detail_accept_btn"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DriveeGreen, contentColor = Color.Black)
          ) {
            Text(text = "Принять за ${order.priceRub} ₽", fontSize = 16.sp, fontWeight = FontWeight.Bold)
          }

          Spacer(modifier = Modifier.height(16.dp))

          Text(
            text = "Плашки быстрого торга:",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(8.dp))

          // Quick Bargaining Chips
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            quickChips.forEach { chipPrice ->
              val isSelected = proposedPrice == chipPrice
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) DriveeOrange else MaterialTheme.colorScheme.surfaceVariant,
                border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                  .weight(1f)
                  .height(42.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .clickable { proposedPrice = chipPrice }
                  .testTag("detail_chip_$chipPrice")
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Text(
                    text = "$chipPrice ₽",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Manual Price Adjuster
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(42.dp)) {
              IconButton(onClick = { if (proposedPrice > order.priceRub) proposedPrice -= 10 }) {
                Icon(Icons.Default.Remove, contentDescription = "Минус 10")
              }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              border = BorderStroke(2.dp, DriveeOrange),
              modifier = Modifier.width(140.dp).height(46.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Text(text = "$proposedPrice ₽", fontWeight = FontWeight.Black, fontSize = 20.sp, color = DriveeOrange)
              }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(42.dp)) {
              IconButton(onClick = { proposedPrice += 10 }) {
                Icon(Icons.Default.Add, contentDescription = "Плюс 10")
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          OutlinedButton(
            onClick = {
              val bid = CourierBid(
                id = "bid_${System.currentTimeMillis()}",
                courierId = "c_user",
                courierName = "Вы (Курьер)",
                courierRating = 4.98,
                vehicle = "Автомобиль • Доставка",
                bidPrice = proposedPrice,
                etaMinutes = order.etaMinutes
              )
              onSubmitCounterBid(order.id, bid)
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("detail_send_counter_bid_btn"),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, DriveeOrange),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = DriveeOrange)
          ) {
            Text("Предложить свою цену $proposedPrice ₽", fontWeight = FontWeight.Bold)
          }
        } else {
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = DriveeRedLight,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = "Этот заказ уже взят другим курьером",
              color = DriveeRed,
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              modifier = Modifier.padding(16.dp),
              textAlign = TextAlign.Center
            )
          }
        }

        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }
}
