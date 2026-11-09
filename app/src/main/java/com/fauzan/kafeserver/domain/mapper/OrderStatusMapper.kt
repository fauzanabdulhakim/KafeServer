package com.fauzan.kafeserver.domain.mapper

/**
 * Pure Kotlin Domain mapper responsible for order status resolution and business rules.
 * Decoupled from Android UI and database layers for testability.
 */
class OrderStatusMapper {

    companion object {
        const val STATUS_CANCELLED: Int = -1
        const val STATUS_PLACED: Int = 0
        const val STATUS_SHIPPING: Int = 1
        const val STATUS_SHIPPED: Int = 2

        const val TEXT_PLACED: String = "Placed"
        const val TEXT_SHIPPING: String = "Shipping"
        const val TEXT_SHIPPED: String = "Shipped"
        const val TEXT_CANCELLED: String = "Cancelled"
        const val TEXT_ERROR: String = "Error"
    }

    /**
     * Converts numeric order status code into human-readable text.
     */
    fun convertStatusToString(orderStatus: Int): String {
        return when (orderStatus) {
            STATUS_PLACED -> TEXT_PLACED
            STATUS_SHIPPING -> TEXT_SHIPPING
            STATUS_SHIPPED -> TEXT_SHIPPED
            STATUS_CANCELLED -> TEXT_CANCELLED
            else -> TEXT_ERROR
        }
    }

    /**
     * Checks if the given order status is in a terminal state (Shipped or Cancelled).
     */
    fun isTerminalStatus(orderStatus: Int): Boolean {
        return orderStatus == STATUS_SHIPPED || orderStatus == STATUS_CANCELLED
    }

    /**
     * Returns whether an order status can transition to the next stage.
     */
    fun canAdvanceStatus(currentStatus: Int): Boolean {
        return currentStatus == STATUS_PLACED || currentStatus == STATUS_SHIPPING
    }

    /**
     * Computes the next logical status in the order fulfillment pipeline.
     */
    fun getNextStatus(currentStatus: Int): Int {
        return when (currentStatus) {
            STATUS_PLACED -> STATUS_SHIPPING
            STATUS_SHIPPING -> STATUS_SHIPPED
            else -> currentStatus
        }
    }
}
