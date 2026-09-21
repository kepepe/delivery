package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CourierBid
import com.example.data.model.OrderStatus
import com.example.data.repository.LocalPropDeliveryRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Drivee Delivery", appName)
  }

  @Test
  fun `repository loads initial orders in Yakutsk`() = runBlocking {
    val repo = LocalPropDeliveryRepository()
    val orders = repo.orders.value
    assertTrue(orders.isNotEmpty())
    assertTrue(orders.any { it.pickupAddress.contains("Ленина") || it.pickupAddress.contains("Якутск") })
  }

  @Test
  fun `courier can accept order directly`() = runBlocking {
    val repo = LocalPropDeliveryRepository()
    val firstOrder = repo.orders.value.first()
    val success = repo.acceptOrderDirectly(firstOrder.id, "c_test", "Тест Курьер")
    assertTrue(success)
    val updated = repo.orders.value.find { it.id == firstOrder.id }
    assertNotNull(updated)
    assertEquals(OrderStatus.ACCEPTED_BY_COURIER, updated?.status)
  }

  @Test
  fun `client can create order and courier can submit bid`() = runBlocking {
    val repo = LocalPropDeliveryRepository()
    val order = repo.createOrder(
      pickupAddress = "ул. Кирова, 12",
      dropoffAddress = "ул. Дзержинского, 30",
      priceRub = 350,
      comment = "Хрупкий груз",
      tags = listOf("Курьер", "Срочно"),
      senderPhone = "+7 (914) 000-11-22"
    )

    assertNotNull(order.id)
    assertEquals(350, order.priceRub)
    assertEquals("ул. Кирова, 12", order.pickupAddress)

    // Courier submits a counter bid
    val bidSuccess = repo.submitCounterBid(
      orderId = order.id,
      courierBid = CourierBid(
        id = "bid_999",
        courierId = "c_555",
        courierName = "Алексей Курьер",
        courierRating = 4.9,
        vehicle = "Велосипед",
        bidPrice = 400,
        etaMinutes = 15
      )
    )
    assertTrue(bidSuccess)

    val updatedOrder = repo.orders.value.find { it.id == order.id }
    assertNotNull(updatedOrder)
    assertEquals(1, updatedOrder?.bids?.size)
    assertEquals(400, updatedOrder?.bids?.first()?.bidPrice)

    // Client accepts courier bid
    val acceptSuccess = repo.clientAcceptBid(order.id, "bid_999")
    assertTrue(acceptSuccess)
    val finalOrder = repo.orders.value.find { it.id == order.id }
    assertEquals(OrderStatus.ACCEPTED_BY_COURIER, finalOrder?.status)
    assertEquals("Алексей Курьер", finalOrder?.assignedCourierName)
  }

  @Test
  fun `order lifecycle reaches delivered status and removes from active feed`() = runBlocking {
    val repo = LocalPropDeliveryRepository()
    val initial = repo.orders.value.first()
    repo.acceptOrderDirectly(initial.id, "c_1", "Курьер")
    repo.updateOrderStatus(initial.id, OrderStatus.DELIVERED)
    val finalOrder = repo.orders.value.find { it.id == initial.id }
    // As requested: delivered orders are permanently removed from the active feed
    assertNull(finalOrder)
    // And recorded in courier shift history
    assertTrue(repo.courierProfile.value.orderHistory.any { it.id == initial.id })
  }
}

