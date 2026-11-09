package com.fauzan.kafeserver.model.EventBus

import com.fauzan.kafeserver.model.AddonModel

data class UpdateAddonModel(
    var addonModel: MutableList<AddonModel>? = null
)
