package com.fauzan.kafeserver.ui.category;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.fauzan.kafeserver.callback.CategoryCallBackListener;
import com.fauzan.kafeserver.common.Common;
import com.fauzan.kafeserver.model.CategoryModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CategoryViewModel extends ViewModel implements CategoryCallBackListener {

    private MutableLiveData<List<CategoryModel>> categoryListMultable;
    private MutableLiveData<String> messageError = new MutableLiveData<>();
    private CategoryCallBackListener categoryCallBackListener;

    public CategoryViewModel() {
        categoryCallBackListener = this;
    }

    public MutableLiveData<List<CategoryModel>> getCategoryListMultable() {
        if (categoryListMultable == null)
        {
            categoryListMultable = new MutableLiveData<>();
            messageError = new MutableLiveData<>();
            loadCategories();
        }
        return categoryListMultable;
    }

    public void loadCategories() {
        List<CategoryModel> temList = new ArrayList<>();
        DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference(Common.CATEGORY_REF);
        categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot itemSnapShot:dataSnapshot.getChildren())
                {
                    CategoryModel categoryModel = itemSnapShot.getValue(CategoryModel.class);
                    categoryModel.setMenu_id(itemSnapShot.getKey());
                    temList.add(categoryModel);
                }
                categoryCallBackListener.onCategoryLoadSuccess(temList);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                categoryCallBackListener.onCategoryLoadFailed(databaseError.getMessage());

            }
        });
    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    @Override
    public void onCategoryLoadSuccess(List<CategoryModel> categoryModelList) {
        categoryListMultable.setValue(categoryModelList);
    }

    @Override
    public void onCategoryLoadFailed(String message) {
        messageError.setValue(message);
    }
}