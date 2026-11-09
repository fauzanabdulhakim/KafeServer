package com.fauzan.kafeserver

import com.fauzan.kafeserver.domain.mapper.OrderStatusMapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests verifying OrderStatusMapper business logic and status pipeline.
 */
class OrderStatusMapperTest {

    private lateinit var mapper: OrderStatusMapper

    @Before
    fun setUp() {
        mapper = OrderStatusMapper()
    }

    @Test
    fun convertStatusToString_whenStatusPlaced_shouldReturnPlaced() {
        assertEquals("Placed", mapper.convertStatusToString(0))
    }

    @Test
    fun convertStatusToString_whenStatusShipping_shouldReturnShipping() {
        assertEquals("Shipping", mapper.convertStatusToString(1))
    }

    @Test
    fun convertStatusToString_whenStatusShipped_shouldReturnShipped() {
        assertEquals("Shipped", mapper.convertStatusToString(2))
    }

    @Test
    fun convertStatusToString_whenStatusCancelled_shouldReturnCancelled() {
        assertEquals("Cancelled", mapper.convertStatusToString(-1))
    }

    @Test
    fun convertStatusToString_whenUnknownStatus_shouldReturnError() {
        assertEquals("Error", mapper.convertStatusToString(99))
        assertEquals("Error", mapper.convertStatusToString(-99))
    }

    @Test
    fun isTerminalStatus_shouldReturnTrueOnlyForShippedAndCancelled() {
        assertTrue(mapper.isTerminalStatus(OrderStatusMapper.STATUS_SHIPPED))
        assertTrue(mapper.isTerminalStatus(OrderStatusMapper.STATUS_CANCELLED))
        assertFalse(mapper.isTerminalStatus(OrderStatusMapper.STATUS_PLACED))
        assertFalse(mapper.isTerminalStatus(OrderStatusMapper.STATUS_SHIPPING))
    }

    @Test
    fun canAdvanceStatus_shouldReturnTrueForPlacedAndShipping() {
        assertTrue(mapper.canAdvanceStatus(OrderStatusMapper.STATUS_PLACED))
        assertTrue(mapper.canAdvanceStatus(OrderStatusMapper.STATUS_SHIPPING))
        assertFalse(mapper.canAdvanceStatus(OrderStatusMapper.STATUS_SHIPPED))
        assertFalse(mapper.canAdvanceStatus(OrderStatusMapper.STATUS_CANCELLED))
    }

    @Test
    fun getNextStatus_shouldFollowFulfillmentPipeline() {
        assertEquals(OrderStatusMapper.STATUS_SHIPPING, mapper.getNextStatus(OrderStatusMapper.STATUS_PLACED))
        assertEquals(OrderStatusMapper.STATUS_SHIPPED, mapper.getNextStatus(OrderStatusMapper.STATUS_SHIPPING))
        assertEquals(OrderStatusMapper.STATUS_SHIPPED, mapper.getNextStatus(OrderStatusMapper.STATUS_SHIPPED))
        assertEquals(OrderStatusMapper.STATUS_CANCELLED, mapper.getNextStatus(OrderStatusMapper.STATUS_CANCELLED))
    }
}
