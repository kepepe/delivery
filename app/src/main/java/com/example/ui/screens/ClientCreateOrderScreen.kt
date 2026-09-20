package com.example.ui.screens

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.InteractiveRouteMap
import com.example.ui.theme.DriveeGreen
import com.example.ui.theme.DriveeGreenContainer
import com.example.ui.theme.DriveeGreenDark
import com.example.ui.theme.DriveeOrange
import com.example.ui.theme.DriveeOrangeLight

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ClientCreateOrderScreen(
  onCreateOrder: (pickup: String, dropoff: String, price: Int, comment: String, tags: List<String>, phone: String) -> Unit,
  onSwitchRole: () -> Unit,
  modifier: Modifier = Modifier
) {
  var pickupAddress by remember { mutableStateOf("") }
  var dropoffAddress by remember { mutableStateOf("") }
  var priceRub by remember { mutableIntStateOf(200) }
  var comment by remember { mutableStateOf("") }
  var senderPhone by remember { mutableStateOf("") }

  val selectedTags = remember { mutableStateListOf("Курьер", "Перевод") }

  val yakutskShortcutsA = listOf(
    "ул. Ленина, 14 (Гостиница Лена)",
    "ТЦ Туймаада (Орджоникидзе, 38)",
    "ул. Дзержинского, 18 (Главпочтамт)",
    "Аэропорт Якутск"
  )

  val yakutskShortcutsB = listOf(
    "203 мкрн, корп. 6",
    "ул. Ойунского, 24 (СВФУ)",
    "ул. Ярославского, 20",
    "Сергеляхское шоссе, 5 км"
  )

  val availableTags = listOf("Курьер", "Перевод", "От двери до двери", "Срочно", "Наличные")

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text("Создание заказа", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Заказчик • г. Якутск", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        },
        actions = {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = DriveeGreen.copy(alpha = 0.15f),
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .clickable { onSwitchRole() }
              .padding(end = 12.dp)
              .testTag("client_switch_to_courier_btn")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = DriveeGreenDark, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Курьер", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DriveeGreenDark)
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
      // Live Route Map Preview directly inside the screen
      InteractiveRouteMap(
        modifier = Modifier
          .fillMaxWidth()
          .height(200.dp)
          .testTag("create_order_embedded_map"),
        distanceKm = 4.2,
        etaMinutes = 18,
        isDarkTheme = false,
        pickupAddress = if (pickupAddress.isNotBlank()) pickupAddress else "ул. Ленина, 14",
        dropoffAddress = if (dropoffAddress.isNotBlank()) dropoffAddress else "203 мкрн, корп. 6"
      )

      Column(modifier = Modifier.padding(16.dp)) {
        // 1. Addresses Section
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(text = "Маршрут доставки", fontWeight = FontWeight.Bold, fontSize = 15.sp)
          Spacer(modifier = Modifier.height(12.dp))

          // Point A Field
          OutlinedTextField(
            value = pickupAddress,
            onValueChange = { pickupAddress = it },
            label = { Text("Точка А (Откуда забрать)") },
            leadingIcon = {
              Icon(Icons.Default.Place, contentDescription = null, tint = DriveeGreen)
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("input_pickup_address")
          )

          Spacer(modifier = Modifier.height(6.dp))

          // Quick shortcuts for Point A
          FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            yakutskShortcutsA.forEach { shortcut ->
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .clickable { pickupAddress = shortcut }
                  .padding(vertical = 2.dp)
              ) {
                Text(
                  text = shortcut.substringBefore("(").trim(),
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Point B Field
          OutlinedTextField(
            value = dropoffAddress,
            onValueChange = { dropoffAddress = it },
            label = { Text("Точка Б (Куда доставить)") },
            leadingIcon = {
              Icon(Icons.Default.LocationOn, contentDescription = null, tint = DriveeOrange)
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("input_dropoff_address")
          )

          Spacer(modifier = Modifier.height(6.dp))

          // Quick shortcuts for Point B
          FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            yakutskShortcutsB.forEach { shortcut ->
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .clickable { dropoffAddress = shortcut }
                  .padding(vertical = 2.dp)
              ) {
                Text(
                  text = shortcut.substringBefore("(").trim(),
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 2. Offered Price Selection
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
            text = "Ваша начальная цена в рублях",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            modifier = Modifier.fillMaxWidth()
          )
          Text(
            text = "Курьеры увидят эту ставку и смогут предложить торг",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(14.dp))

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(46.dp)) {
              IconButton(onClick = { if (priceRub > 150) priceRub -= 10 }, modifier = Modifier.testTag("client_price_minus")) {
                Icon(Icons.Default.Remove, contentDescription = null)
              }
            }

            Spacer(modifier = Modifier.width(20.dp))

            Surface(
              shape = RoundedCornerShape(14.dp),
              color = DriveeGreenContainer,
              modifier = Modifier.width(150.dp).height(56.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Text(
                  text = "$priceRub ₽",
                  fontWeight = FontWeight.Black,
                  fontSize = 24.sp,
                  color = DriveeGreenDark,
                  modifier = Modifier.testTag("client_price_display")
                )
              }
            }

            Spacer(modifier = Modifier.width(20.dp))

            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(46.dp)) {
              IconButton(onClick = { priceRub += 10 }, modifier = Modifier.testTag("client_price_plus")) {
                Icon(Icons.Default.Add, contentDescription = null)
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Suggested quick chips
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(200, 250, 300, 350).forEach { p ->
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (priceRub == p) DriveeGreen else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .clickable { priceRub = p }
              ) {
                Text(
                  text = "$p ₽",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (priceRub == p) Color.Black else MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 3. Comment, Tags & Phone
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(text = "Детали посылки", fontWeight = FontWeight.Bold, fontSize = 15.sp)
          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = comment,
            onValueChange = { comment = it },
            label = { Text("Комментарий курьеру") },
            placeholder = { Text("например: два пакета, купить лекарство...") },
            leadingIcon = { Icon(Icons.Default.ChatBubbleOutline, contentDescription = null) },
            maxLines = 3,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("input_order_comment")
          )

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = senderPhone,
            onValueChange = { senderPhone = it },
            label = { Text("Телефон для связи") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("input_sender_phone")
          )

          Spacer(modifier = Modifier.height(12.dp))

          Text(text = "Теги заказа:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Spacer(modifier = Modifier.height(6.dp))

          FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            availableTags.forEach { tag ->
              val isSelected = selectedTags.contains(tag)
              FilterChip(
                selected = isSelected,
                onClick = {
                  if (isSelected) selectedTags.remove(tag) else selectedTags.add(tag)
                },
                label = { Text(tag, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = DriveeOrange,
                  selectedLabelColor = Color.White
                )
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Submit Button
      val canSubmit = pickupAddress.isNotBlank() && dropoffAddress.isNotBlank() && priceRub >= 50
      Button(
        onClick = {
          if (canSubmit) {
            onCreateOrder(pickupAddress, dropoffAddress, priceRub, comment, selectedTags.toList(), senderPhone)
          }
        },
        enabled = canSubmit,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = DriveeGreen, contentColor = Color.Black),
        modifier = Modifier
          .fillMaxWidth()
          .height(54.dp)
          .testTag("btn_create_order_submit")
      ) {
        Icon(Icons.Default.ShoppingBag, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (canSubmit) "Найти курьера за $priceRub ₽" else "Укажите точки А и Б",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold
        )
      }

      Spacer(modifier = Modifier.height(32.dp))
      }
    }
  }
}
