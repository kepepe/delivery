package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.ViewAgenda
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CourierProfile
import com.example.data.model.UserRole
import com.example.ui.theme.DriveeGreen
import com.example.ui.theme.DriveeGreenContainer
import com.example.ui.theme.DriveeGreenDark
import com.example.ui.theme.DriveeOrange
import com.example.ui.theme.DriveeOrangeLight
import com.example.ui.theme.DriveeRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourierProfileScreen(
  profile: CourierProfile,
  isDarkTheme: Boolean,
  onToggleDarkTheme: (Boolean) -> Unit,
  compactView: Boolean,
  onToggleCompactView: (Boolean) -> Unit,
  soundAlerts: Boolean,
  onToggleSoundAlerts: (Boolean) -> Unit,
  chainOrders: Boolean,
  onToggleChainOrders: (Boolean) -> Unit,
  onSwitchRole: () -> Unit,
  onBack: () -> Unit,
  onLogout: () -> Unit,
  modifier: Modifier = Modifier
) {
  var showWithdrawDialog by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Кабинет курьера", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("profile_back_btn")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
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
        .padding(16.dp)
    ) {
      // 1. Profile Header: Avatar, Name, Rating & Priority
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(DriveeGreen),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Person, contentDescription = null, tint = Color.Black, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = profile.name,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = profile.phone,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Rating & Priority Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = DriveeOrangeLight,
              modifier = Modifier.weight(1f)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.Star, contentDescription = null, tint = DriveeOrange, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                  Text(text = "Рейтинг", fontSize = 11.sp, color = Color(0xFF78350F))
                  Text(text = "★ ${profile.rating}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DriveeOrange)
                }
              }
            }

            Surface(
              shape = RoundedCornerShape(12.dp),
              color = DriveeGreenContainer,
              modifier = Modifier.weight(1.3f)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = DriveeGreenDark, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                  Text(text = "Приоритет заказов", fontSize = 11.sp, color = DriveeGreenDark)
                  Text(text = profile.priorityText, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DriveeGreenDark)
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 2. Financial Chips: Лицевой счет, Баланс, Вывод средств
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(text = "Финансы и баланс", fontWeight = FontWeight.Bold, fontSize = 16.sp)
          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(text = "Текущий баланс", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text(
                text = "${profile.balanceRub} ₽",
                fontWeight = FontWeight.Black,
                fontSize = 26.sp,
                color = DriveeGreenDark
              )
            }

            Button(
              onClick = { showWithdrawDialog = true },
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = DriveeGreen, contentColor = Color.Black),
              modifier = Modifier.testTag("btn_withdraw_funds")
            ) {
              Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Вывод средств", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.CreditCard, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(text = "Лицевой счет", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = profile.accountNumber, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 3. Shift Breakdown & History (Доход, вычет комиссии 9.5%, налог 4% НПД)
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.History, contentDescription = null, tint = DriveeOrange)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Итоги смены (г. Якутск)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Calculations
          val gross = profile.shiftGrossRub
          val fee = (gross * (profile.serviceFeePercent / 100)).toInt()
          val tax = (gross * (profile.taxPercent / 100)).toInt()
          val net = gross - fee - tax

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Сумма за смену (грязными):", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("$gross ₽", fontWeight = FontWeight.Bold, fontSize = 13.sp)
          }
          Spacer(modifier = Modifier.height(6.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Комиссия сервиса (${profile.serviceFeePercent}%):", fontSize = 13.sp, color = DriveeRed)
            Text("-$fee ₽", fontWeight = FontWeight.Medium, fontSize = 13.sp, color = DriveeRed)
          }
          Spacer(modifier = Modifier.height(6.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Налог самозанятого НПД (${profile.taxPercent}%):", fontSize = 13.sp, color = DriveeRed)
            Text("-$tax ₽", fontWeight = FontWeight.Medium, fontSize = 13.sp, color = DriveeRed)
          }
          Spacer(modifier = Modifier.height(8.dp))
          Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.surfaceVariant))
          Spacer(modifier = Modifier.height(8.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Чистый доход на карту:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("$net ₽", fontWeight = FontWeight.Black, fontSize = 16.sp, color = DriveeGreenDark)
          }

          Spacer(modifier = Modifier.height(14.dp))
          Text(text = "Закрытые заказы сегодня:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Spacer(modifier = Modifier.height(8.dp))

          profile.orderHistory.forEach { hist ->
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
              modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
            ) {
              Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(text = hist.routeText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                  Text(text = hist.timeText, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(text = "+${hist.netAmountRub} ₽", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DriveeGreenDark)
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 4. Options & Settings: Dark mode, Compact view, Sound, Chaining
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(text = "Настройки приложения", fontWeight = FontWeight.Bold, fontSize = 16.sp)
          Spacer(modifier = Modifier.height(12.dp))

          // Dark Theme Toggle
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
              Spacer(modifier = Modifier.width(10.dp))
              Text(text = "Ночной режим / Темная тема", fontSize = 14.sp)
            }
            Switch(
              checked = isDarkTheme,
              onCheckedChange = onToggleDarkTheme,
              colors = SwitchDefaults.colors(checkedThumbColor = DriveeGreen, checkedTrackColor = DriveeGreenDark),
              modifier = Modifier.testTag("toggle_dark_mode")
            )
          }

          // Compact View
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.ViewAgenda, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
              Spacer(modifier = Modifier.width(10.dp))
              Text(text = "Компактный вид ленты", fontSize = 14.sp)
            }
            Switch(
              checked = compactView,
              onCheckedChange = onToggleCompactView,
              colors = SwitchDefaults.colors(checkedThumbColor = DriveeGreen, checkedTrackColor = DriveeGreenDark),
              modifier = Modifier.testTag("toggle_compact_view")
            )
          }

          // Sound Alerts
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
              Spacer(modifier = Modifier.width(10.dp))
              Text(text = "Звуковые уведомления", fontSize = 14.sp)
            }
            Switch(
              checked = soundAlerts,
              onCheckedChange = onToggleSoundAlerts,
              colors = SwitchDefaults.colors(checkedThumbColor = DriveeGreen, checkedTrackColor = DriveeGreenDark),
              modifier = Modifier.testTag("toggle_sound_alerts")
            )
          }

          // Chain Orders
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
              Spacer(modifier = Modifier.width(10.dp))
              Text(text = "Цепочка заказов", fontSize = 14.sp)
            }
            Switch(
              checked = chainOrders,
              onCheckedChange = onToggleChainOrders,
              colors = SwitchDefaults.colors(checkedThumbColor = DriveeGreen, checkedTrackColor = DriveeGreenDark),
              modifier = Modifier.testTag("toggle_chain_orders")
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 5. Customer Reviews
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(text = "Отзывы клиентов", fontWeight = FontWeight.Bold, fontSize = 16.sp)
          Spacer(modifier = Modifier.height(10.dp))

          profile.reviews.forEach { rev ->
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
              modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(text = rev.authorName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                  Text(text = "★".repeat(rev.rating), color = DriveeOrange, fontSize = 13.sp)
                }
                Text(text = rev.dateText, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = rev.commentText, fontSize = 12.sp)
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Switch Role & Logout Actions
      Button(
        onClick = onSwitchRole,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = DriveeOrange, contentColor = Color.White),
        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("profile_switch_to_client_btn")
      ) {
        Icon(Icons.Default.SwapHoriz, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Переключить роль на Заказчика", fontWeight = FontWeight.Bold)
      }

      Spacer(modifier = Modifier.height(10.dp))

      OutlinedButton(
        onClick = onLogout,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, DriveeRed),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = DriveeRed),
        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("profile_logout_btn")
      ) {
        Text("Выйти из аккаунта", fontWeight = FontWeight.SemiBold)
      }

      Spacer(modifier = Modifier.height(32.dp))
    }

    // Payout Dialog
    if (showWithdrawDialog) {
      AlertDialog(
        onDismissRequest = { showWithdrawDialog = false },
        title = { Text("Вывод средств") },
        text = {
          Text("Баланс ${profile.balanceRub} ₽ будет отправлен по СБП на карту, привязанную к номеру ${profile.phone}.")
        },
        confirmButton = {
          Button(
            onClick = { showWithdrawDialog = false },
            colors = ButtonDefaults.buttonColors(containerColor = DriveeGreen, contentColor = Color.Black)
          ) {
            Text("Подтвердить")
          }
        },
        dismissButton = {
          OutlinedButton(onClick = { showWithdrawDialog = false }) {
            Text("Отмена")
          }
        }
      )
    }
  }
}
