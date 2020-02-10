package com.fauzan.kafeserver.callback;

import com.fauzan.kafeserver.model.CategoryModel;

import java.util.List;

public interface CategoryCallBackListener {
    void onCategoryLoadSuccess(List<CategoryModel> categoryModelList);
    void onCategoryLoadFailed(String message);
}
