package com.fauzan.kafeserver.model.EventBus

import com.fauzan.kafeserver.model.CategoryModel

data class CategoryClick(
    var isSuccess: Boolean = false,
    var categoryModel: CategoryModel? = null
)
