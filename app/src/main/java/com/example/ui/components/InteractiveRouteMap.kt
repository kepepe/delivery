package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DriveeGreen
import com.example.ui.theme.DriveeNavy
import com.example.ui.theme.DriveeOrange
import com.example.ui.theme.DriveeRed

@Composable
fun InteractiveRouteMap(
  modifier: Modifier = Modifier,
  distanceKm: Double = 3.5,
  etaMinutes: Int = 13,
  isDarkTheme: Boolean = false,
  pickupAddress: String = "ул. Ленина, 14",
  dropoffAddress: String = "203 мкрн, корп. 6"
) {
  val context = LocalContext.current
  var scale by remember { mutableFloatStateOf(1.0f) }
  var offsetX by remember { mutableFloatStateOf(0f) }
  var offsetY by remember { mutableFloatStateOf(0f) }

  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseRadius by infiniteTransition.animateFloat(
    initialValue = 12f,
    targetValue = 28f,
    animationSpec = infiniteRepeatable(
      animation = tween(1500, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "pulseRadius"
  )
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.8f,
    targetValue = 0.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(1500, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "pulseAlpha"
  )

  val mapBg = if (isDarkTheme) Color(0xFF0D1B2A) else Color(0xFFE8ECEF)
  val roadColor = if (isDarkTheme) Color(0xFF1B2A4A) else Color(0xFFFFFFFF)
  val mainRoadColor = if (isDarkTheme) Color(0xFF2B3A5A) else Color(0xFFDDE3EA)
  val blockColor = if (isDarkTheme) Color(0xFF132238) else Color(0xFFDFE5EB)
  val riverColor = if (isDarkTheme) Color(0xFF0F3057) else Color(0xFFBCD4E6)

  Box(
    modifier = modifier
      .background(mapBg)
      .pointerInput(Unit) {
        detectTransformGestures { _, pan, zoom, _ ->
          scale = (scale * zoom).coerceIn(0.7f, 3.0f)
          offsetX += pan.x
          offsetY += pan.y
        }
      }
  ) {
    Canvas(modifier = Modifier.fillMaxSize().testTag("interactive_canvas_map")) {
      val w = size.width
      val h = size.height
      val cx = w / 2f + offsetX
      val cy = h / 2f + offsetY

      // 1. Draw River Lena curvature on right side
      val riverPath = Path().apply {
        moveTo(w * 0.82f * scale + offsetX, 0f)
        cubicTo(
          w * 0.78f * scale + offsetX, h * 0.35f * scale + offsetY,
          w * 0.88f * scale + offsetX, h * 0.7f * scale + offsetY,
          w * 0.84f * scale + offsetX, h
        )
        lineTo(w, h)
        lineTo(w, 0f)
        close()
      }
      drawPath(riverPath, riverColor)

      // 2. Draw city blocks
      val blockCols = 6
      val blockRows = 8
      val blockW = (w * 0.75f) / blockCols * scale
      val blockH = h / blockRows * scale

      for (r in 0 until blockRows) {
        for (c in 0 until blockCols) {
          val bx = c * (blockW + 16f * scale) + (offsetX * 0.5f)
          val by = r * (blockH + 16f * scale) + (offsetY * 0.5f)
          drawRoundRect(
            color = blockColor,
            topLeft = Offset(bx, by),
            size = Size(blockW - 6f, blockH - 6f),
            cornerRadius = CornerRadius(8f, 8f)
          )
        }
      }

      // 3. Main Avenues (Pr. Lenina, Dzerzhinskogo)
      drawLine(
        color = mainRoadColor,
        start = Offset(0f, cy - 30f * scale),
        end = Offset(w, cy - 30f * scale),
        strokeWidth = 24f * scale,
        cap = StrokeCap.Round
      )
      drawLine(
        color = mainRoadColor,
        start = Offset(cx - 40f * scale, 0f),
        end = Offset(cx - 40f * scale, h),
        strokeWidth = 22f * scale,
        cap = StrokeCap.Round
      )
      drawLine(
        color = roadColor,
        start = Offset(0f, cy + 120f * scale),
        end = Offset(w, cy + 120f * scale),
        strokeWidth = 16f * scale,
        cap = StrokeCap.Round
      )

      // 4. Delivery Route Path (Point A -> Turn -> Turn -> Point B)
      val pA = Offset(cx - 100f * scale, cy + 80f * scale)
      val corner1 = Offset(cx - 40f * scale, cy + 80f * scale)
      val corner2 = Offset(cx - 40f * scale, cy - 50f * scale)
      val pB = Offset(cx + 90f * scale, cy - 50f * scale)

      val routePath = Path().apply {
        moveTo(pA.x, pA.y)
        lineTo(corner1.x, corner1.y)
        lineTo(corner2.x, corner2.y)
        lineTo(pB.x, pB.y)
      }

      // Route shadow / casing
      drawPath(
        path = routePath,
        color = Color(0x3300D26A),
        style = Stroke(width = 16f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
      )

      // Route main line
      drawPath(
        path = routePath,
        color = DriveeGreen,
        style = Stroke(width = 9f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
      )

      // Animated dashed progress overlay
      drawPath(
        path = routePath,
        color = Color.White,
        style = Stroke(
          width = 3f * scale,
          cap = StrokeCap.Round,
          pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
        )
      )

      // 5. Courier animated vehicle location (mid-way between corner1 and corner2)
      val courierPos = Offset(corner1.x, corner1.y - 65f * scale)

      // Courier pulse ripple
      drawCircle(
        color = DriveeGreen.copy(alpha = pulseAlpha),
        radius = pulseRadius * scale,
        center = courierPos
      )
      drawCircle(
        color = DriveeNavy,
        radius = 12f * scale,
        center = courierPos
      )
      drawCircle(
        color = DriveeGreen,
        radius = 7f * scale,
        center = courierPos
      )

      // 6. Point A Pin (Pickup)
      drawCircle(color = Color.White, radius = 15f * scale, center = pA)
      drawCircle(color = DriveeGreen, radius = 12f * scale, center = pA)

      // 7. Point B Pin (Destination)
      drawCircle(color = Color.White, radius = 15f * scale, center = pB)
      drawCircle(color = DriveeRed, radius = 12f * scale, center = pB)
    }

    // Top Floating ETA & Traffic Badge
    Surface(
      modifier = Modifier
        .align(Alignment.TopCenter)
        .padding(top = 16.dp)
        .testTag("map_eta_badge"),
      shape = RoundedCornerShape(24.dp),
      color = if (isDarkTheme) Color(0xDD1E293B) else Color(0xF0FFFFFF),
      tonalElevation = 6.dp,
      shadowElevation = 8.dp
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Box(
          modifier = Modifier
            .size(10.dp)
            .background(DriveeGreen, CircleShape)
        )
        Text(
          text = "$etaMinutes мин",
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "•",
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = "$distanceKm км в пути",
          fontWeight = FontWeight.Medium,
          fontSize = 14.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    // Map control buttons (Google Maps, Zoom in, Zoom out, Center)
    Column(
      modifier = Modifier
        .align(Alignment.CenterEnd)
        .padding(end = 12.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
      ) {
        IconButton(
          onClick = {
            try {
              val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&origin=${Uri.encode(pickupAddress + ", Якутск")}&destination=${Uri.encode(dropoffAddress + ", Якутск")}&travelmode=driving")
              val mapIntent = Intent(Intent.ACTION_VIEW, uri)
              context.startActivity(mapIntent)
            } catch (e: Exception) {
              Toast.makeText(context, "Не удалось открыть Google Карты", Toast.LENGTH_SHORT).show()
            }
          },
          modifier = Modifier.size(44.dp).testTag("map_open_google_maps")
        ) {
          Icon(Icons.Default.Navigation, contentDescription = "Открыть в Google Картах", tint = DriveeOrange)
        }
      }

      Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
      ) {
        IconButton(
          onClick = { scale = (scale + 0.3f).coerceAtMost(3.0f) },
          modifier = Modifier.size(44.dp).testTag("map_zoom_in")
        ) {
          Icon(Icons.Default.Add, contentDescription = "Приблизить")
        }
      }

      Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
      ) {
        IconButton(
          onClick = { scale = (scale - 0.3f).coerceAtLeast(0.7f) },
          modifier = Modifier.size(44.dp).testTag("map_zoom_out")
        ) {
          Icon(Icons.Default.Remove, contentDescription = "Отдалить")
        }
      }

      Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
      ) {
        IconButton(
          onClick = {
            scale = 1.0f
            offsetX = 0f
            offsetY = 0f
          },
          modifier = Modifier.size(44.dp).testTag("map_center")
        ) {
          Icon(Icons.Default.MyLocation, contentDescription = "По центру", tint = DriveeGreen)
        }
      }
    }
  }
}
