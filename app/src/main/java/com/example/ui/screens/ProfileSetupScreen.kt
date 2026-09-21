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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.ui.theme.DriveeGreen
import com.example.ui.theme.DriveeGreenDark
import com.example.ui.theme.DriveeNavy
import com.example.ui.theme.DriveeOrange
import com.example.ui.theme.DriveeOrangeLight

/**
 * Screen for entering user personal details upon first Google Account sign-in.
 * Collects full name, phone number, city, and vehicle type for couriers.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
  initialProfile: UserProfile?,
  onProfileCompleted: (UserProfile) -> Unit,
  modifier: Modifier = Modifier
) {
  var fullName by remember(initialProfile) {
    mutableStateOf(initialProfile?.fullName ?: "")
  }
  var phone by remember(initialProfile) {
    mutableStateOf(initialProfile?.phone.takeIf { !it.isNullOrBlank() } ?: "+7 (914) ")
  }
  var city by remember(initialProfile) {
    mutableStateOf(initialProfile?.city.takeIf { !it.isNullOrBlank() } ?: "Якутск")
  }
  var selectedRole by remember(initialProfile) {
    mutableStateOf(initialProfile?.role ?: UserRole.COURIER)
  }
  var selectedVehicle by remember(initialProfile) {
    mutableStateOf(initialProfile?.vehicleType ?: "Велосипед")
  }

  val vehicleOptions = listOf("Пешком", "Велосипед", "Самокат", "Автомобиль")

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Регистрация профиля",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
          )
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
        .padding(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Icon Header
      Box(
        modifier = Modifier
          .size(68.dp)
          .clip(CircleShape)
          .background(if (selectedRole == UserRole.COURIER) DriveeGreen else DriveeOrange),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Badge,
          contentDescription = null,
          tint = if (selectedRole == UserRole.COURIER) Color.Black else Color.White,
          modifier = Modifier.size(36.dp)
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = "Добро пожаловать в сервис!",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )

      Text(
        text = "Для работы заполните личные контактные данные",
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(20.dp))

      // Card with form fields
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(
            text = "1. Основная информация",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Full Name
          OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Имя и Фамилия") },
            leadingIcon = {
              Icon(Icons.Default.Person, contentDescription = null, tint = DriveeGreenDark)
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("setup_input_fullname")
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Phone Number
          OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Номер телефона для связи") },
            leadingIcon = {
              Icon(Icons.Default.Phone, contentDescription = null, tint = DriveeGreenDark)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("setup_input_phone")
          )

          Spacer(modifier = Modifier.height(12.dp))

          // City
          OutlinedTextField(
            value = city,
            onValueChange = { city = it },
            label = { Text("Город") },
            leadingIcon = {
              Icon(Icons.Default.LocationCity, contentDescription = null, tint = DriveeGreenDark)
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("setup_input_city")
          )

          if (initialProfile?.email?.isNotEmpty() == true) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Google: ${initialProfile.email}",
                  fontSize = 12.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Role Selection Card
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(
            text = "2. Выберите статус",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface
          )

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (selectedRole == UserRole.COURIER) DriveeGreen else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              border = BorderStroke(
                1.5.dp,
                if (selectedRole == UserRole.COURIER) DriveeGreenDark else Color.Transparent
              ),
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .clickable { selectedRole = UserRole.COURIER }
                .testTag("setup_select_courier")
            ) {
              Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Icon(
                  imageVector = Icons.Default.LocalShipping,
                  contentDescription = null,
                  tint = if (selectedRole == UserRole.COURIER) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "Курьер",
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp,
                  color = if (selectedRole == UserRole.COURIER) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (selectedRole == UserRole.CLIENT) DriveeOrange else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              border = BorderStroke(
                1.5.dp,
                if (selectedRole == UserRole.CLIENT) DriveeOrange else Color.Transparent
              ),
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .clickable { selectedRole = UserRole.CLIENT }
                .testTag("setup_select_client")
            ) {
              Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Icon(
                  imageVector = Icons.Default.Person,
                  contentDescription = null,
                  tint = if (selectedRole == UserRole.CLIENT) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "Заказчик",
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp,
                  color = if (selectedRole == UserRole.CLIENT) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }

          if (selectedRole == UserRole.COURIER) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = "Транспорт для доставок:",
              fontWeight = FontWeight.SemiBold,
              fontSize = 13.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              vehicleOptions.forEach { vehicle ->
                FilterChip(
                  selected = selectedVehicle == vehicle,
                  onClick = { selectedVehicle = vehicle },
                  label = { Text(vehicle, fontSize = 12.sp) },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = DriveeGreen,
                    selectedLabelColor = Color.Black
                  )
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Complete Profile Button
      val isValid = fullName.isNotBlank() && phone.trim().length >= 8
      Button(
        onClick = {
          if (isValid) {
            val updated = (initialProfile ?: UserProfile()).copy(
              fullName = fullName.trim(),
              phone = phone.trim(),
              city = city.trim(),
              role = selectedRole,
              vehicleType = selectedVehicle,
              isProfileComplete = true
            )
            onProfileCompleted(updated)
          }
        },
        enabled = isValid,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (selectedRole == UserRole.COURIER) DriveeGreen else DriveeOrange,
          contentColor = if (selectedRole == UserRole.COURIER) Color.Black else Color.White
        ),
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("btn_complete_profile_setup")
      ) {
        Icon(Icons.Default.CheckCircle, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (isValid) "Завершить регистрацию и войти" else "Заполните имя и телефон",
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp
        )
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}
