package com.fauzan.kafeserver.callback

import com.fauzan.kafeserver.model.CategoryModel

interface CategoryCallBackListener {
    fun onCategoryLoadSuccess(categoryModelList: List<CategoryModel>)
    fun onCategoryLoadFailed(message: String)
}
