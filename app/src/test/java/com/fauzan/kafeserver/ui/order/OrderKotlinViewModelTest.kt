package com.fauzan.kafeserver.ui.order

import com.fauzan.kafeserver.domain.mapper.OrderStatusMapper
import com.fauzan.kafeserver.domain.model.UiState
import com.fauzan.kafeserver.model.OrderModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OrderKotlinViewModelTest {

    private lateinit var viewModel: OrderKotlinViewModel

    @Before
    fun setUp() {
        viewModel = OrderKotlinViewModel()
    }

    @Test
    fun `initial state should be Loading`() {
        assertTrue(viewModel.pipelineState.value is UiState.Loading)
    }

    @Test
    fun `processOrders with valid orders should update pipeline state to Success`() {
        val order1 = OrderModel().apply {
            key = "ORD-1"
            orderStatus = OrderStatusMapper.STATUS_PLACED
        }
        val order2 = OrderModel().apply {
            key = "ORD-2"
            orderStatus = OrderStatusMapper.STATUS_SHIPPING
        }

        viewModel.processOrders(listOf(order1, order2))

        val state = viewModel.pipelineState.value
        assertTrue(state is UiState.Success)
        val data = (state as UiState.Success).data
        assertEquals(2, data.activeCount)
        assertEquals(1, data.placedOrders.size)
        assertEquals(1, data.shippingOrders.size)
    }

    @Test
    fun `processOrders with null or empty list should emit Empty state`() {
        viewModel.processOrders(null)
        assertTrue(viewModel.pipelineState.value is UiState.Empty)

        viewModel.processOrders(emptyList())
        assertTrue(viewModel.pipelineState.value is UiState.Empty)
    }

    @Test
    fun `setError should update state to Error`() {
        viewModel.setError("Database connection error")

        val state = viewModel.pipelineState.value
        assertTrue(state is UiState.Error)
        assertEquals("Database connection error", (state as UiState.Error).message)
    }

    @Test
    fun `refresh should reset pipeline state to Loading`() {
        viewModel.setError("Temporary error")
        viewModel.refresh()
        assertTrue(viewModel.pipelineState.value is UiState.Loading)
    }
}
