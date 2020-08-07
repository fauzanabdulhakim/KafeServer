package com.fauzan.kafeserver.ui.food_list;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.ImageView;

import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.fauzan.kafeserver.R;
import com.fauzan.kafeserver.SizeAddonEditActivity;
import com.fauzan.kafeserver.adapter.MyFoodListAdapter;
import com.fauzan.kafeserver.common.Common;
import com.fauzan.kafeserver.common.MySwipeHelper;
import com.fauzan.kafeserver.model.EventBus.AddonSizeEditEvent;
import com.fauzan.kafeserver.model.EventBus.ChangeMenuClick;
import com.fauzan.kafeserver.model.EventBus.ToastEvent;
import com.fauzan.kafeserver.model.FoodModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

public class FoodListFragment extends Fragment {

    //Image Upload
    private static final int PICK_IMAGE_REQUEST = 1234;
    private ImageView img_food;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private android.app.AlertDialog dialog;

    private FoodListViewModel foodListViewModel;

    private List<FoodModel> foodModelList;

    Unbinder unbinder;
    @BindView(R.id.recycler_food_list)
    RecyclerView recycler_food_list;

    LayoutAnimationController layoutAnimationController;
    MyFoodListAdapter adapter;
    private Uri imageUri=null;

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.food_list_menu,menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);

        SearchManager searchManager = (SearchManager)getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView)menuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

        //Event
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                startSearchFood(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        // Clear text when click to clear button on seach view
        ImageView closeButton = (ImageView)searchView.findViewById(R.id.search_close_btn);
        closeButton.setOnClickListener(view -> {
            EditText ed = (EditText)searchView.findViewById(R.id.search_src_text);
            //Clear text
            ed.setText("");
            //Clear query
            searchView.setQuery("",false);
            //Collapse the action view
            searchView.onActionViewCollapsed();
            //Collapse the search widget
            menuItem.collapseActionView();
            //Restore result to original
            foodListViewModel.getMutableLiveDataFoodList().setValue(Common.categorySelected.getFoods());
        });
    }

    private void startSearchFood(String s) {
        List<FoodModel> resultFood =  new ArrayList<>();
        for (int i=0;i<Common.categorySelected.getFoods().size();i++) {
            FoodModel foodModel = Common.categorySelected.getFoods().get(i);
            if (foodModel.getName().toLowerCase().contains(s.toLowerCase()))
            {
                foodModel.setPositionInList(i); //Save index
                resultFood.add(foodModel);
            }
        }

            foodListViewModel.getMutableLiveDataFoodList().setValue(resultFood); // Set search result
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        foodListViewModel =
                ViewModelProviders.of(this).get(FoodListViewModel.class);
        View root = inflater.inflate(R.layout.fragment_food_list, container, false);
        unbinder = ButterKnife.bind(this,root);
        initViews();
        foodListViewModel.getMutableLiveDataFoodList().observe(this, foodModels -> {
            if (foodModels != null){
                foodModelList = foodModels;
                adapter = new MyFoodListAdapter(getContext(), foodModelList);
                recycler_food_list.setAdapter(adapter);
                recycler_food_list.setLayoutAnimation(layoutAnimationController);
            }
        });
        return root;
    }

    private void initViews() {

        setHasOptionsMenu(true); // enable menu in fragment

        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        ((AppCompatActivity)getActivity())
                .getSupportActionBar()
                .setTitle(Common.categorySelected.getName());

        recycler_food_list.setHasFixedSize(true);
        recycler_food_list.setLayoutManager(new LinearLayoutManager(getContext()));

        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(),R.anim.layout_item_from_left);

        //Get Size
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        MySwipeHelper mySwipeHelper = new MySwipeHelper(getContext(),recycler_food_list,width/6) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(getContext(),"Delete",30,0, Color.parseColor("#9b0000"),
                        pos -> {

                        if (foodModelList != null)
                            Common.selectedFood = foodModelList.get(pos);
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle("DELETE")
                                    .setMessage("Do you want to delete this food ?")
                                    .setNegativeButton("CANCEL",((dialogInterface, i) -> dialogInterface.dismiss()))
                                    .setPositiveButton("DELETE",((dialogInterface, i) -> {
                                        FoodModel foodModel = adapter.getItemAtPosition(pos); // Get item in adapter
                                        if (foodModel.getPositionInList() == -1) // if == -1 default , do nothing
                                            Common.categorySelected.getFoods().remove(pos);
                                        else
                                            Common.categorySelected.getFoods().remove(foodModel.getPositionInList()); // Remove by index we was save
                                        updateFood(Common.categorySelected.getFoods(),true);
                                    }));
                            AlertDialog deleteDialog = builder.create();
                            deleteDialog.show();

                            }));

                buf.add(new MyButton(getContext(),"Update",30,0, Color.parseColor("#560027"),
                        pos -> {
                    //similar
                            FoodModel foodModel = adapter.getItemAtPosition(pos);
                            if (foodModel.getPositionInList() == -1)
                                showUpdateDialog(pos,foodModel);
                            else
                                showUpdateDialog(foodModel.getPositionInList(),foodModel);

                        }));

                buf.add(new MyButton(getContext(),"Size",30,0, Color.parseColor("#12005e"),
                        pos -> {
                            FoodModel foodModel = adapter.getItemAtPosition(pos);
                            if (foodModel.getPositionInList() == -1)
                                Common.selectedFood = foodModelList.get(pos);
                            else
                                Common.selectedFood = foodModel;
                            startActivity(new Intent(getContext(), SizeAddonEditActivity.class));
                            //Change pos
                            if (foodModel.getPositionInList() == -1)
                                EventBus.getDefault().postSticky(new AddonSizeEditEvent(false,pos));
                            else
                                EventBus.getDefault().postSticky(new AddonSizeEditEvent(false,foodModel.getPositionInList()));
                        }));

                buf.add(new MyButton(getContext(),"Addon",30,0, Color.parseColor("#336699"),
                        pos -> {
                            FoodModel foodModel = adapter.getItemAtPosition(pos);
                            if (foodModel.getPositionInList() == -1)
                                Common.selectedFood = foodModelList.get(pos);
                            else
                                Common.selectedFood = foodModel;
                            startActivity(new Intent(getContext(), SizeAddonEditActivity.class));
                            if (foodModel.getPositionInList() == -1)
                                EventBus.getDefault().postSticky(new AddonSizeEditEvent(true,pos));
                            else
                                EventBus.getDefault().postSticky(new AddonSizeEditEvent(true,foodModel.getPositionInList()));
                        }));
            }
        };
    }

    private void showUpdateDialog(int pos,FoodModel foodModel) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Update");
        builder.setMessage("Please fill information");

        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_update_food,null);
        EditText edt_food_name = (EditText)itemView.findViewById(R.id.edt_food_name);
        EditText edt_food_price= (EditText)itemView.findViewById(R.id.edt_food_price);
        EditText edt_food_description = (EditText)itemView.findViewById(R.id.edt_food_description);
        img_food = (ImageView)itemView.findViewById(R.id.img_food_image);

        //Set data
        edt_food_name.setText(new StringBuilder("")
                .append(foodModel.getName()));
        edt_food_price.setText(new StringBuilder("")
                .append(foodModel.getPrice()));
        edt_food_description.setText(new StringBuilder("")
                .append(foodModel.getDescription()));

        Glide.with(getContext()).load(foodModel.getImage()).into(img_food);

        //Set event
        img_food.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,"Select Picture"),PICK_IMAGE_REQUEST);
        });

        builder.setNegativeButton("CANCEL",((dialogInterface, i) -> dialogInterface.dismiss()))
                .setPositiveButton("UPDATE",((dialogInterface, i) -> {

                    FoodModel updateFood = foodModel;
                    updateFood.setName(edt_food_name.getText().toString());
                    updateFood.setDescription(edt_food_description.getText().toString());
                    updateFood.setPrice(TextUtils.isEmpty(edt_food_price.getText()) ? 0 :
                            Long.parseLong(edt_food_price.getText().toString()));

                    if (imageUri != null)
                    {
                        //In this , we will use Firebase Storage to upload image
                        dialog.setMessage("Uploading...");
                        dialog.show();

                        String unique_name = UUID.randomUUID().toString();
                        StorageReference imageFolder = storageReference.child("images/"+unique_name);

                        imageFolder.putFile(imageUri)
                                .addOnFailureListener(e -> {
                                    dialog.dismiss();
                                    Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }).addOnCompleteListener(task -> {
                            dialog.dismiss();
                            imageFolder.getDownloadUrl().addOnSuccessListener(uri -> {
                                updateFood.setImage(uri.toString());
                                Common.categorySelected.getFoods().set(pos,updateFood);
                                updateFood(Common.categorySelected.getFoods(),false);
                            });
                        }).addOnProgressListener(taskSnapshot -> {
                            double progress = (100.0* taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            dialog.setMessage(new StringBuilder("Uploading: ").append(progress).append("&"));
                        });
                    }
                    else
                    {
                        Common.categorySelected.getFoods().set(pos,updateFood);
                        updateFood(Common.categorySelected.getFoods(),false);
                    }

                }));

        builder.setView(itemView);
        AlertDialog updateDialog = builder.create();
        updateDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK)
        {
            if (data != null && data.getData() != null)
            {
                imageUri = data.getData();
                img_food.setImageURI(imageUri);
            }
        }
    }

    private void updateFood(List<FoodModel> foods,boolean isDelete) {
        Map<String,Object> updateData = new HashMap<>();
        updateData.put("foods",foods);

        FirebaseDatabase.getInstance()
                .getReference(Common.CATEGORY_REF)
                .child(Common.categorySelected.getMenu_id())
                .updateChildren(updateData)
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                    {
                        foodListViewModel.getMutableLiveDataFoodList();
                        EventBus.getDefault().postSticky(new ToastEvent(!isDelete, true));

                    }
                });
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new ChangeMenuClick(true));
        super.onDestroy();
    }
}