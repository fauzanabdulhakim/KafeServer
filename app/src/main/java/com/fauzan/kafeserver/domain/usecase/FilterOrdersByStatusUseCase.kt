package com.fauzan.kafeserver.domain.usecase

import com.fauzan.kafeserver.domain.mapper.OrderStatusMapper
import com.fauzan.kafeserver.model.OrderModel

/**
 * Domain UseCase that organizes kitchen orders by lifecycle stage.
 */
class FilterOrdersByStatusUseCase(
    private val mapper: OrderStatusMapper = OrderStatusMapper()
) {

    data class KitchenPipelineSummary(
        val allOrders: List<OrderModel>,
        val placedOrders: List<OrderModel>,
        val shippingOrders: List<OrderModel>,
        val shippedOrders: List<OrderModel>,
        val cancelledOrders: List<OrderModel>,
        val activeCount: Int
    )

    /**
     * Categorizes a raw list of orders into pipeline stages sorted by creation time (oldest first).
     *
     * @param orders raw list of incoming orders
     * @return KitchenPipelineSummary
     */
    operator fun invoke(orders: List<OrderModel>?): KitchenPipelineSummary {
        if (orders.isNullOrEmpty()) {
            return KitchenPipelineSummary(
                allOrders = emptyList(),
                placedOrders = emptyList(),
                shippingOrders = emptyList(),
                shippedOrders = emptyList(),
                cancelledOrders = emptyList(),
                activeCount = 0
            )
        }

        val sorted = orders.sortedBy { it.createDate }

        val placed = sorted.filter { it.orderStatus == OrderStatusMapper.STATUS_PLACED }
        val shipping = sorted.filter { it.orderStatus == OrderStatusMapper.STATUS_SHIPPING }
        val shipped = sorted.filter { it.orderStatus == OrderStatusMapper.STATUS_SHIPPED }
        val cancelled = sorted.filter { it.orderStatus == OrderStatusMapper.STATUS_CANCELLED }

        return KitchenPipelineSummary(
            allOrders = sorted,
            placedOrders = placed,
            shippingOrders = shipping,
            shippedOrders = shipped,
            cancelledOrders = cancelled,
            activeCount = placed.size + shipping.size
        )
    }
}
