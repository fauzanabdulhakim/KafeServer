package com.fauzan.kafeserver.ui.food_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fauzan.kafeserver.common.Common
import com.fauzan.kafeserver.model.FoodModel

class FoodListViewModel : ViewModel() {

    private val _mutableLiveDataFoodList = MutableLiveData<List<FoodModel>>()
    val mutableLiveDataFoodList: LiveData<List<FoodModel>>
        get() {
            _mutableLiveDataFoodList.value = Common.categorySelected?.foods
            return _mutableLiveDataFoodList
        }
}
