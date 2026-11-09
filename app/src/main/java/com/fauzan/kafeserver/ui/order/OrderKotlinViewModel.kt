package com.fauzan.kafeserver.ui.order

import androidx.lifecycle.ViewModel
import com.fauzan.kafeserver.common.Common
import com.fauzan.kafeserver.domain.model.UiState
import com.fauzan.kafeserver.domain.usecase.FilterOrdersByStatusUseCase
import com.fauzan.kafeserver.model.OrderModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Modernized Kitchen Order ViewModel utilizing Kotlin StateFlow and
 * FilterOrdersByStatusUseCase to manage the real-time fulfillment pipeline.
 */
class OrderKotlinViewModel(
    private val filterOrdersUseCase: FilterOrdersByStatusUseCase = FilterOrdersByStatusUseCase()
) : ViewModel() {

    private val _pipelineState = MutableStateFlow<UiState<FilterOrdersByStatusUseCase.KitchenPipelineSummary>>(UiState.Loading)
    val pipelineState: StateFlow<UiState<FilterOrdersByStatusUseCase.KitchenPipelineSummary>> = _pipelineState.asStateFlow()

    private val _currentStatusFilter = MutableStateFlow(-1) // -1 for All
    val currentStatusFilter: StateFlow<Int> = _currentStatusFilter.asStateFlow()

    private var activeOrders: List<OrderModel> = emptyList()
    private var orderRef: DatabaseReference? = null
    private var orderEventListener: ValueEventListener? = null

    /**
     * Loads live orders from Firebase Realtime Database with real-time updates.
     */
    fun loadOrders() {
        _pipelineState.value = UiState.Loading
        try {
            val ref = FirebaseDatabase.getInstance().getReference(Common.ORDER_REF)
            orderRef = ref

            // Remove existing listener if present to prevent multiple registrations
            orderEventListener?.let { existingListener ->
                ref.removeEventListener(existingListener)
            }

            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val orders = mutableListOf<OrderModel>()
                    for (item in snapshot.children) {
                        val order = item.getValue(OrderModel::class.java)
                        if (order != null) {
                            order.key = item.key
                            orders.add(order)
                        }
                    }
                    orders.sortBy { it.createDate }
                    activeOrders = orders
                    processOrders(orders)
                }

                override fun onCancelled(error: DatabaseError) {
                    setError(error.message)
                }
            }
            orderEventListener = listener
            ref.addValueEventListener(listener)
        } catch (e: Exception) {
            setError(e.message ?: "Failed to connect to Firebase")
        }
    }

    /**
     * Sets status filter (-1 for All, 0 for Placed, 1 for Shipping, 2 for Shipped, -1 for Cancelled)
     */
    fun setStatusFilter(status: Int) {
        _currentStatusFilter.value = status
    }

    /**
     * Updates kitchen pipeline with latest incoming order records.
     */
    fun processOrders(orders: List<OrderModel>?) {
        if (orders.isNullOrEmpty()) {
            activeOrders = emptyList()
            _pipelineState.value = UiState.Empty
        } else {
            activeOrders = orders
            val summary = filterOrdersUseCase(orders)
            _pipelineState.value = UiState.Success(summary)
        }
    }

    /**
     * Returns orders matching the active filter.
     */
    fun getFilteredOrders(summary: FilterOrdersByStatusUseCase.KitchenPipelineSummary, filter: Int): List<OrderModel> {
        return when (filter) {
            0 -> summary.placedOrders
            1 -> summary.shippingOrders
            2 -> summary.shippedOrders
            -1 -> summary.allOrders
            else -> summary.allOrders
        }
    }

    /**
     * Emits error state for kitchen display.
     */
    fun setError(message: String) {
        _pipelineState.value = UiState.Error(message)
    }

    fun refresh() {
        _pipelineState.value = UiState.Loading
    }

    override fun onCleared() {
        orderEventListener?.let { listener ->
            orderRef?.removeEventListener(listener)
        }
        orderEventListener = null
        orderRef = null
        super.onCleared()
    }
}
