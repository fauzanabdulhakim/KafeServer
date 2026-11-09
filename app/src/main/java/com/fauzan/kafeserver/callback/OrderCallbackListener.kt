package com.fauzan.kafeserver.callback

import com.fauzan.kafeserver.model.OrderModel

interface OrderCallbackListener {
    fun onOrderLoadSuccess(orderModelList: List<OrderModel>)
    fun onOrderLoadFailed(message: String)
}
