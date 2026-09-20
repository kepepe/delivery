package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CourierBid
import com.example.data.model.DeliveryOrder
import com.example.ui.theme.DriveeGreen
import com.example.ui.theme.DriveeGreenDark
import com.example.ui.theme.DriveeOrange
import com.example.ui.theme.DriveeOrangeLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BargainingSheet(
  order: DeliveryOrder,
  sheetState: SheetState,
  onDismiss: () -> Unit,
  onAcceptDirect: (DeliveryOrder) -> Unit,
  onSubmitCounterBid: (orderId: String, bid: CourierBid) -> Unit
) {
  var proposedPrice by remember(order) {
    mutableIntStateOf(order.priceRub + 30)
  }

  val quickChips = listOf(
    order.priceRub + 20,
    order.priceRub + 30,
    order.priceRub + 50,
    order.priceRub + 70
  )

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)
        .padding(bottom = 32.dp)
        .testTag("bargaining_sheet")
    ) {
      // Top bar with close button
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Предложение цены",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Закрыть")
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Customer Card Preview
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(DriveeOrangeLight),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Person,
              contentDescription = "Аватар",
              tint = DriveeOrange,
              modifier = Modifier.size(24.dp)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = order.clientName,
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Рейтинг ★ ${order.clientRating} • 48 заказов",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = "Заказчик дал",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = "${order.priceRub} ₽",
              fontWeight = FontWeight.ExtraBold,
              fontSize = 18.sp,
              color = DriveeGreenDark
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Direct Accept Button (One-tap consent at client's offered price)
      Button(
        onClick = {
          onAcceptDirect(order)
          onDismiss()
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("sheet_accept_direct_btn"),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = DriveeGreen,
          contentColor = Color.Black
        )
      ) {
        Text(
          text = "Принять за ${order.priceRub} ₽",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      Text(
        text = "Или предложите свою ставку:",
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Quick Chips (+20, +30, +50, +70)
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
              .testTag("quick_chip_$chipPrice")
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text(
                text = "$chipPrice ₽",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Manual Price Adjuster ( - / price field / + )
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        Surface(
          shape = CircleShape,
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.size(46.dp)
        ) {
          IconButton(
            onClick = {
              if (proposedPrice > order.priceRub) proposedPrice -= 10
            },
            modifier = Modifier.testTag("btn_price_minus")
          ) {
            Icon(Icons.Default.Remove, contentDescription = "Минус 10")
          }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Surface(
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          border = BorderStroke(2.dp, DriveeOrange),
          modifier = Modifier
            .width(160.dp)
            .height(52.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Text(
              text = "$proposedPrice ₽",
              fontWeight = FontWeight.Black,
              fontSize = 22.sp,
              color = DriveeOrange,
              textAlign = TextAlign.Center,
              modifier = Modifier.testTag("proposed_price_display")
            )
          }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Surface(
          shape = CircleShape,
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.size(46.dp)
        ) {
          IconButton(
            onClick = { proposedPrice += 10 },
            modifier = Modifier.testTag("btn_price_plus")
          ) {
            Icon(Icons.Default.Add, contentDescription = "Плюс 10")
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Submit Counter-Bid Button
      Button(
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
          onDismiss()
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("btn_submit_counter_bid"),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = DriveeOrange,
          contentColor = Color.White
        )
      ) {
        Text(
          text = "Предложить $proposedPrice ₽",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }
  }
}
