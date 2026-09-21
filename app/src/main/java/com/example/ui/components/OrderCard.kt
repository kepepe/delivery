package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeliveryOrder
import com.example.data.model.OrderStatus
import com.example.ui.theme.DriveeGreen
import com.example.ui.theme.DriveeGreenContainer
import com.example.ui.theme.DriveeGreenDark
import com.example.ui.theme.DriveeOrange
import com.example.ui.theme.DriveeOrangeLight
import com.example.ui.theme.DriveeRed
import com.example.ui.theme.DriveeRedLight

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OrderCard(
  order: DeliveryOrder,
  onAcceptDirect: (DeliveryOrder) -> Unit,
  onBargainClick: (DeliveryOrder) -> Unit,
  onCardClick: (DeliveryOrder) -> Unit,
  modifier: Modifier = Modifier,
  compactView: Boolean = false
) {
  val context = LocalContext.current
  val isTaken = order.status == OrderStatus.TAKEN_BY_OTHER
  val isAccepted = order.status == OrderStatus.ACCEPTED_BY_COURIER

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(18.dp))
      .clickable { onCardClick(order) }
      .testTag("order_card_${order.id}"),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    border = BorderStroke(
      width = 1.dp,
      color = if (isTaken) DriveeRed.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(if (compactView) 12.dp else 16.dp)
    ) {
      // Header: Price, Status & Distance
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "${order.priceRub} ₽",
            fontWeight = FontWeight.ExtraBold,
            fontSize = if (compactView) 20.sp else 24.sp,
            color = if (isTaken) DriveeRed else DriveeGreenDark,
            modifier = Modifier.testTag("order_price_${order.id}")
          )
          Text(
            text = "${order.distanceKm} км • ~${order.etaMinutes} мин",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        if (isTaken) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = DriveeRedLight
          ) {
            Text(
              text = "Заказ уже взят",
              color = DriveeRed,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        } else if (isAccepted) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = DriveeGreenContainer
          ) {
            Text(
              text = "В работе",
              color = DriveeGreenDark,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        } else {
          // Tag badge: e.g. "г. Якутск"
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
          ) {
            Text(
              text = order.city,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 12.sp,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }
      }

      val isNew = order.status == OrderStatus.NEW
      var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
      LaunchedEffect(isNew) {
        if (isNew) {
          while (true) {
            delay(1000)
            currentTime = System.currentTimeMillis()
          }
        }
      }
      val remainingSeconds = order.remainingAcceptanceSeconds(currentTime)

      if (isNew) {
        Spacer(modifier = Modifier.height(8.dp))
        if (order.bids.isEmpty()) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (remainingSeconds <= 10) DriveeRedLight else DriveeOrangeLight,
            border = BorderStroke(1.dp, if (remainingSeconds <= 10) DriveeRed else DriveeOrange),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = null,
                tint = if (remainingSeconds <= 10) DriveeRed else DriveeOrange,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Таймер отклика: $remainingSeconds сек (не успеете — заказ исчезнет)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (remainingSeconds <= 10) DriveeRed else DriveeOrange
              )
            }
          }
        } else {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = DriveeGreenContainer,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = null,
                tint = DriveeGreenDark,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Курьер откликнулся • Заказ сохранен",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = DriveeGreenDark
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Route A -> B
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
      ) {
        // Timeline indicator
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(top = 4.dp, end = 12.dp)
        ) {
          Box(
            modifier = Modifier
              .size(10.dp)
              .background(DriveeGreen, CircleShape)
          )
          Box(
            modifier = Modifier
              .width(2.dp)
              .height(24.dp)
              .background(MaterialTheme.colorScheme.surfaceVariant)
          )
          Box(
            modifier = Modifier
              .size(10.dp)
              .background(DriveeOrange, CircleShape)
          )
        }

        // Addresses
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = order.pickupAddress,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = order.dropoffAddress,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      // Comment / parcel details
      if (order.comment.isNotEmpty()) {
        Spacer(modifier = Modifier.height(10.dp))
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.ChatBubbleOutline,
              contentDescription = null,
              modifier = Modifier.size(14.dp),
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = order.comment,
              fontSize = 13.sp,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }

      // Tags
      if (!compactView && order.tags.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          order.tags.forEach { tag ->
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = if (tag == "Срочно") DriveeOrangeLight else MaterialTheme.colorScheme.surfaceVariant
            ) {
              Text(
                text = tag,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (tag == "Срочно") DriveeOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
              )
            }
          }
        }
      }

      // Sender Phone & Client Info
      if (!compactView) {
        Spacer(modifier = Modifier.height(10.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = order.clientName,
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = " • ★ ${order.clientRating}",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = DriveeOrange
            )
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .clickable {
                try {
                  val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${order.senderPhone}"))
                  context.startActivity(intent)
                } catch (_: Exception) {}
              }
              .padding(4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Call,
              contentDescription = "Позвонить",
              tint = DriveeGreenDark,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = order.senderPhone,
              fontSize = 12.sp,
              color = DriveeGreenDark,
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      }

      // Buttons: Accept & Bargain
      if (!isTaken && !isAccepted) {
        Spacer(modifier = Modifier.height(12.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Button(
            onClick = { onAcceptDirect(order) },
            modifier = Modifier
              .weight(1.2f)
              .height(44.dp)
              .testTag("btn_accept_${order.id}"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = DriveeGreen,
              contentColor = Color.Black
            )
          ) {
            Text(
              text = "Принять ${order.priceRub} ₽",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
          }

          OutlinedButton(
            onClick = { onBargainClick(order) },
            modifier = Modifier
              .weight(0.8f)
              .height(44.dp)
              .testTag("btn_bargain_${order.id}"),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, DriveeOrange),
            colors = ButtonDefaults.outlinedButtonColors(
              contentColor = DriveeOrange
            )
          ) {
            Text(
              text = "Торг",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
          }
        }
      }
    }
  }
}
