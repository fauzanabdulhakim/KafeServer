package com.fauzan.kafeserver.model.EventBus

data class AddonSizeEditEvent(
    var isAddon: Boolean = false,
    var pos: Int = 0
)
