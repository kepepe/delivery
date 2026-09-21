package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.DriveeGreen
import com.example.ui.theme.DriveeNavy
import com.example.ui.theme.DriveeOrange
import com.example.ui.theme.DriveeRed

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun InteractiveRouteMap(
  modifier: Modifier = Modifier,
  distanceKm: Double = 3.5,
  etaMinutes: Int = 13,
  isDarkTheme: Boolean = false,
  pickupAddress: String = "ул. Ленина, 14",
  dropoffAddress: String = "203 мкрн, корп. 6",
  startLat: Double = 62.0281,
  startLng: Double = 129.7325,
  endLat: Double = 62.0395,
  endLng: Double = 129.7540,
  courierLat: Double? = null,
  courierLng: Double? = null,
  courierHeading: Float = 0f,
  isLiveTracking: Boolean = false
) {
  val context = LocalContext.current
  var useOsmMap by remember { mutableStateOf(true) }
  var webViewInstance by remember { mutableStateOf<WebView?>(null) }

  // Fallback vector canvas schematic pan and zoom
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

  // Midpoint waypoint calculation
  val midLat = startLat + (endLat - startLat) * 0.5
  val midLng = startLng + (endLng - startLng) * 0.2

  val safeCourierLat = courierLat ?: (startLat + (endLat - startLat) * 0.4)
  val safeCourierLng = courierLng ?: (startLng + (endLng - startLng) * 0.4)

  // Build high-performance Leaflet OpenStreetMap HTML
  val osmHtml = remember(startLat, startLng, endLat, endLng, isDarkTheme) {
    """
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="utf-8" />
      <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
      <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
      <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
      <style>
        body, html, #map { margin: 0; padding: 0; width: 100%; height: 100%; background: ${if (isDarkTheme) "#111827" else "#f0f3f6"}; }
        .custom-pin {
          display: flex; align-items: center; justify-content: center;
          width: 32px; height: 32px; border-radius: 50%; color: white;
          font-weight: 800; font-family: -apple-system, BlinkMacSystemFont, sans-serif; font-size: 14px;
          box-shadow: 0 4px 10px rgba(0,0,0,0.4); border: 2.5px solid white;
        }
        .pin-a { background: #00AA44; }
        .pin-b { background: #E53935; }
        .courier-marker {
          width: 38px; height: 38px; border-radius: 50%;
          background: #00AA44; border: 3px solid #ffffff;
          box-shadow: 0 0 16px rgba(0,170,68,0.85);
          display: flex; align-items: center; justify-content: center;
          font-size: 18px; color: white;
          transition: transform 0.4s ease-out;
        }
        .pulse-ring {
          position: absolute; width: 52px; height: 52px;
          border-radius: 50%; background: rgba(0,170,68,0.3);
          animation: pulse 1.8s infinite ease-out;
          top: -7px; left: -7px; pointer-events: none;
        }
        @keyframes pulse {
          0% { transform: scale(0.6); opacity: 1; }
          100% { transform: scale(1.6); opacity: 0; }
        }
      </style>
    </head>
    <body>
    <div id="map"></div>
    <script>
      var centerLat = ${(startLat + endLat) / 2.0};
      var centerLng = ${(startLng + endLng) / 2.0};
      var map = L.map('map', { zoomControl: false, attributionControl: false }).setView([centerLat, centerLng], 14);
      
      L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19
      }).addTo(map);

      // Pin A
      var pinAIcon = L.divIcon({
        className: '',
        html: '<div class="custom-pin pin-a">A</div>',
        iconSize: [32, 32],
        iconAnchor: [16, 16]
      });
      L.marker([$startLat, $startLng], { icon: pinAIcon }).addTo(map)
        .bindPopup("<b>Точка забора (A):</b><br/>${pickupAddress.replace("\"", "\\\"")}");

      // Pin B
      var pinBIcon = L.divIcon({
        className: '',
        html: '<div class="custom-pin pin-b">Б</div>',
        iconSize: [32, 32],
        iconAnchor: [16, 16]
      });
      L.marker([$endLat, $endLng], { icon: pinBIcon }).addTo(map)
        .bindPopup("<b>Точка вручения (Б):</b><br/>${dropoffAddress.replace("\"", "\\\"")}");

      // Green Route Polyline
      var routeCoords = [
        [$startLat, $startLng],
        [$midLat, $startLng],
        [$midLat, $endLng],
        [$endLat, $endLng]
      ];
      L.polyline(routeCoords, { color: '#00AA44', weight: 6, opacity: 0.85, lineJoin: 'round' }).addTo(map);

      // Courier live Marker
      var courierMarker = null;
      function updateCourierPosition(lat, lng, heading) {
        if (!courierMarker) {
          var cIcon = L.divIcon({
            className: '',
            html: '<div style="position:relative;"><div class="pulse-ring"></div><div id="c-icon" class="courier-marker" style="transform: rotate(' + heading + 'deg);">🚗</div></div>',
            iconSize: [38, 38],
            iconAnchor: [19, 19]
          });
          courierMarker = L.marker([lat, lng], { icon: cIcon }).addTo(map).bindPopup("<b>Курьер в пути</b><br/>г. Якутск");
        } else {
          courierMarker.setLatLng([lat, lng]);
          var iconEl = document.getElementById('c-icon');
          if (iconEl) {
            iconEl.style.transform = 'rotate(' + heading + 'deg)';
          }
        }
      }

      // Initial courier spawn
      updateCourierPosition($safeCourierLat, $safeCourierLng, $courierHeading);

      function zoomIn() { map.zoomIn(); }
      function zoomOut() { map.zoomOut(); }
      function centerOn(lat, lng, zoom) { map.setView([lat, lng], zoom || 15); }
    </script>
    </body>
    </html>
    """.trimIndent()
  }

  // Smoothly update courier location in OpenStreetMap via JS bridge
  LaunchedEffect(courierLat, courierLng, courierHeading) {
    if (courierLat != null && courierLng != null && webViewInstance != null) {
      webViewInstance?.evaluateJavascript("updateCourierPosition($courierLat, $courierLng, $courierHeading);", null)
    }
  }

  Box(modifier = modifier) {
    if (useOsmMap) {
      // 1. OpenStreetMap (OSM) Live View
      AndroidView(
        factory = { ctx ->
          WebView(ctx).apply {
            layoutParams = ViewGroup.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.MATCH_PARENT
            )
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            webViewClient = WebViewClient()
            loadDataWithBaseURL("https://openstreetmap.org", osmHtml, "text/html", "UTF-8", null)
            webViewInstance = this
          }
        },
        update = { webView ->
          webViewInstance = webView
          if (courierLat != null && courierLng != null) {
            webView.evaluateJavascript("updateCourierPosition($courierLat, $courierLng, $courierHeading);", null)
          }
        },
        modifier = Modifier
          .fillMaxSize()
          .testTag("osm_interactive_map")
      )
    } else {
      // 2. Vector Canvas Schematic (Graceful offline/schematic backup)
      val mapBg = if (isDarkTheme) Color(0xFF0D1B2A) else Color(0xFFE8ECEF)
      val roadColor = if (isDarkTheme) Color(0xFF1B2A4A) else Color(0xFFFFFFFF)
      val mainRoadColor = if (isDarkTheme) Color(0xFF2B3A5A) else Color(0xFFDDE3EA)
      val blockColor = if (isDarkTheme) Color(0xFF132238) else Color(0xFFDFE5EB)
      val riverColor = if (isDarkTheme) Color(0xFF0F3057) else Color(0xFFBCD4E6)

      Box(
        modifier = Modifier
          .fillMaxSize()
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

          // River Lena curvature
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

          // City blocks
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

          // Main Avenues
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

          // Route Path
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

          drawPath(
            path = routePath,
            color = Color(0x3300D26A),
            style = Stroke(width = 16f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
          )
          drawPath(
            path = routePath,
            color = DriveeGreen,
            style = Stroke(width = 9f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
          )
          drawPath(
            path = routePath,
            color = Color.White,
            style = Stroke(
              width = 3f * scale,
              cap = StrokeCap.Round,
              pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
            )
          )

          // Courier vehicle
          val courierPos = Offset(corner1.x, corner1.y - 65f * scale)
          drawCircle(color = DriveeGreen.copy(alpha = pulseAlpha), radius = pulseRadius * scale, center = courierPos)
          drawCircle(color = DriveeNavy, radius = 12f * scale, center = courierPos)
          drawCircle(color = DriveeGreen, radius = 7f * scale, center = courierPos)

          // Pins
          drawCircle(color = Color.White, radius = 15f * scale, center = pA)
          drawCircle(color = DriveeGreen, radius = 12f * scale, center = pA)
          drawCircle(color = Color.White, radius = 15f * scale, center = pB)
          drawCircle(color = DriveeRed, radius = 12f * scale, center = pB)
        }
      }
    }

    // Top Floating ETA & Live GPS telemetry Badge
    Surface(
      modifier = Modifier
        .align(Alignment.TopCenter)
        .padding(top = 12.dp)
        .testTag("map_eta_badge"),
      shape = RoundedCornerShape(24.dp),
      color = if (isDarkTheme) Color(0xEE1E293B) else Color(0xF5FFFFFF),
      tonalElevation = 6.dp,
      shadowElevation = 8.dp
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
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
          fontSize = 14.sp,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "•",
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = "$distanceKm км",
          fontWeight = FontWeight.Medium,
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = "•",
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = if (useOsmMap) "🗺️ OpenStreetMap" else "📐 Схема",
          fontWeight = FontWeight.SemiBold,
          fontSize = 12.sp,
          color = DriveeGreen
        )
      }
    }

    // Map control buttons (Toggle OSM/Scheme, Navigation, Zoom, Center)
    Column(
      modifier = Modifier
        .align(Alignment.CenterEnd)
        .padding(end = 12.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Toggle OSM / Schematic Canvas
      Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
      ) {
        IconButton(
          onClick = { useOsmMap = !useOsmMap },
          modifier = Modifier.size(44.dp).testTag("map_toggle_type")
        ) {
          Icon(
            imageVector = Icons.Default.Layers,
            contentDescription = "Переключить вид карты",
            tint = if (useOsmMap) DriveeGreen else DriveeNavy
          )
        }
      }

      // Open in 2GIS / external Maps
      Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
      ) {
        IconButton(
          onClick = {
            try {
              val uri = Uri.parse("geo:${startLat},${startLng}?q=${Uri.encode("$dropoffAddress, Якутск")}")
              val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
              }
              context.startActivity(mapIntent)
            } catch (e: Exception) {
              try {
                val osmUri = Uri.parse("https://www.openstreetmap.org/directions?engine=fossgis_osrm_car&route=${startLat}%2C${startLng}%3B${endLat}%2C${endLng}")
                context.startActivity(Intent(Intent.ACTION_VIEW, osmUri))
              } catch (_: Exception) {
                Toast.makeText(context, "Не удалось открыть навигатор", Toast.LENGTH_SHORT).show()
              }
            }
          },
          modifier = Modifier.size(44.dp).testTag("map_open_external_nav")
        ) {
          Icon(Icons.Default.Navigation, contentDescription = "Внешний навигатор", tint = DriveeOrange)
        }
      }

      // Zoom in
      Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
      ) {
        IconButton(
          onClick = {
            if (useOsmMap) {
              webViewInstance?.evaluateJavascript("zoomIn();", null)
            } else {
              scale = (scale + 0.3f).coerceAtMost(3.0f)
            }
          },
          modifier = Modifier.size(44.dp).testTag("map_zoom_in")
        ) {
          Icon(Icons.Default.Add, contentDescription = "Приблизить", tint = MaterialTheme.colorScheme.onSurface)
        }
      }

      // Zoom out
      Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
      ) {
        IconButton(
          onClick = {
            if (useOsmMap) {
              webViewInstance?.evaluateJavascript("zoomOut();", null)
            } else {
              scale = (scale - 0.3f).coerceAtLeast(0.7f)
            }
          },
          modifier = Modifier.size(44.dp).testTag("map_zoom_out")
        ) {
          Icon(Icons.Default.Remove, contentDescription = "Отдалить", tint = MaterialTheme.colorScheme.onSurface)
        }
      }

      // Center on Courier / Order
      Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
      ) {
        IconButton(
          onClick = {
            if (useOsmMap) {
              webViewInstance?.evaluateJavascript("centerOn($safeCourierLat, $safeCourierLng, 15);", null)
            } else {
              offsetX = 0f
              offsetY = 0f
              scale = 1.0f
            }
          },
          modifier = Modifier.size(44.dp).testTag("map_center_gps")
        ) {
          Icon(Icons.Default.MyLocation, contentDescription = "Центрировать", tint = DriveeGreen)
        }
      }
    }

    // Bottom OSM Attribution & Live badge
    Surface(
      modifier = Modifier
        .align(Alignment.BottomStart)
        .padding(start = 12.dp, bottom = 12.dp),
      shape = RoundedCornerShape(12.dp),
      color = if (isDarkTheme) Color(0xCC000000) else Color(0xCCFFFFFF)
    ) {
      Text(
        text = "© OpenStreetMap contributors",
        fontSize = 10.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
      )
    }
  }
}
