package com.fauzan.kafeserver.model.EventBus

import com.fauzan.kafeserver.model.SizeModel

data class UpdateSizeModel(
    var sizeModelList: MutableList<SizeModel>? = null
)
