package com.fauzan.kafeserver.model

/**
 * PCI DSS-compliant model representing an order refund request for kitchen/admin operations.
 */
data class RefundRequestModel @JvmOverloads constructor(
    var orderNumber: String? = null,
    var name: String? = null,
    var phone: String? = null,
    var amount: Double = 0.0,
    var reason: String? = null,
    var transactionId: String? = null,
    var status: String = "PENDING",
    var isDone: Boolean = false,
    var requestDate: Long = System.currentTimeMillis()
)
