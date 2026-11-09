package com.fauzan.kafeserver.domain.usecase

import com.fauzan.kafeserver.domain.mapper.OrderStatusMapper
import com.fauzan.kafeserver.model.OrderModel
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for FilterOrdersByStatusUseCase kitchen pipeline categorization.
 */
class FilterOrdersByStatusUseCaseTest {

    private lateinit var useCase: FilterOrdersByStatusUseCase

    @Before
    fun setUp() {
        useCase = FilterOrdersByStatusUseCase()
    }

    @Test
    fun `invoke with empty or null list should return empty summary`() {
        val summaryNull = useCase(null)
        assertEquals(0, summaryNull.activeCount)
        assertEquals(0, summaryNull.placedOrders.size)

        val summaryEmpty = useCase(emptyList())
        assertEquals(0, summaryEmpty.activeCount)
        assertEquals(0, summaryEmpty.shippingOrders.size)
    }

    @Test
    fun `invoke should categorize orders and calculate active kitchen count`() {
        val order1 = OrderModel().apply {
            key = "ORD-1"
            orderStatus = OrderStatusMapper.STATUS_PLACED
            createDate = 1000L
        }
        val order2 = OrderModel().apply {
            key = "ORD-2"
            orderStatus = OrderStatusMapper.STATUS_SHIPPING
            createDate = 2000L
        }
        val order3 = OrderModel().apply {
            key = "ORD-3"
            orderStatus = OrderStatusMapper.STATUS_SHIPPED
            createDate = 3000L
        }
        val order4 = OrderModel().apply {
            key = "ORD-4"
            orderStatus = OrderStatusMapper.STATUS_CANCELLED
            createDate = 4000L
        }

        val summary = useCase(listOf(order4, order1, order3, order2))

        assertEquals(1, summary.placedOrders.size)
        assertEquals("ORD-1", summary.placedOrders[0].key)

        assertEquals(1, summary.shippingOrders.size)
        assertEquals("ORD-2", summary.shippingOrders[0].key)

        assertEquals(1, summary.shippedOrders.size)
        assertEquals("ORD-3", summary.shippedOrders[0].key)

        assertEquals(1, summary.cancelledOrders.size)
        assertEquals("ORD-4", summary.cancelledOrders[0].key)

        // Active count = placed (1) + shipping (1) = 2
        assertEquals(2, summary.activeCount)
    }
}
