package com.fauzan.kafeserver.model.EventBus

data class ToastEvent(
    var isUpdate: Boolean = false,
    var isFromFoodList: Boolean = false
)
