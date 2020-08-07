package com.fauzan.kafeserver.callback;

import com.fauzan.kafeserver.model.OrderModel;

import java.util.List;

public interface OrderCallbackListener {
    void onOrderLoadSuccess(List<OrderModel> orderModelList);
    void onOrderLoadFailed(String message);
}
