package com.fauzan.kafeserver.model

/**
 * Item in customer cart snapshot within an order.
 */
data class CartItem(
    var foodId: String? = null,
    var foodName: String? = null,
    var foodImage: String? = null,
    var foodPrice: Double? = 0.0,
    var foodQuantity: Int = 0,
    var userPhone: String? = null,
    var foodExtraPrice: Double? = 0.0,
    var foodAddon: String? = null,
    var foodSize: String? = null,
    var uid: String? = null
)
