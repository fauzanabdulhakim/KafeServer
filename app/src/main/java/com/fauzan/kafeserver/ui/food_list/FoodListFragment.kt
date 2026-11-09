package com.fauzan.kafeserver.ui.food_list

import android.app.Activity
import android.app.AlertDialog
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fauzan.kafeserver.R
import com.fauzan.kafeserver.SizeAddonEditActivity
import com.fauzan.kafeserver.adapter.MyFoodListAdapter
import com.fauzan.kafeserver.common.Common
import com.fauzan.kafeserver.common.MySwipeHelper
import com.fauzan.kafeserver.databinding.FragmentFoodListBinding
import com.fauzan.kafeserver.databinding.LayoutUpdateFoodBinding
import com.fauzan.kafeserver.domain.usecase.ValidateFoodInputUseCase
import com.fauzan.kafeserver.model.EventBus.AddonSizeEditEvent
import com.fauzan.kafeserver.model.EventBus.ChangeMenuClick
import com.fauzan.kafeserver.model.EventBus.ToastEvent
import com.fauzan.kafeserver.model.FoodModel
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dmax.dialog.SpotsDialog
import org.greenrobot.eventbus.EventBus
import java.util.Locale
import java.util.UUID

/**
 * Fragment managing merchant food items, item modifications, pricing, and addon/size navigation.
 * Migrated to Android Jetpack ViewBinding with nullified bindings in onDestroyView.
 */
class FoodListFragment : Fragment() {

    private var imgFood: ImageView? = null
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private var dialog: AlertDialog? = null

    private lateinit var foodListViewModel: FoodListViewModel
    private var foodModelList: List<FoodModel>? = null
    private var _binding: FragmentFoodListBinding? = null
    private val binding get() = _binding!!

    private var layoutAnimationController: LayoutAnimationController? = null
    private var adapter: MyFoodListAdapter? = null
    private var imageUri: Uri? = null

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.food_list_menu, menu)

        val menuItem = menu.findItem(R.id.action_search)
        val act = activity
        if (act != null && menuItem != null) {
            val searchManager = act.getSystemService(Context.SEARCH_SERVICE) as? SearchManager
            val searchView = menuItem.actionView as? SearchView
            if (searchView != null && searchManager != null) {
                searchView.setSearchableInfo(searchManager.getSearchableInfo(act.componentName))
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(s: String): Boolean {
                        startSearchFood(s)
                        return true
                    }

                    override fun onQueryTextChange(s: String): Boolean = false
                })

                val closeButton = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
                closeButton?.setOnClickListener {
                    val ed = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
                    ed?.setText("")
                    searchView.setQuery("", false)
                    searchView.onActionViewCollapsed()
                    menuItem.collapseActionView()
                    Common.categorySelected?.foods?.let { foods ->
                        foodModelList = foods
                        adapter = context?.let { ctx -> MyFoodListAdapter(ctx, foods) }
                        binding.recyclerFoodList.adapter = adapter
                    }
                }
            }
        }
    }

    private fun startSearchFood(s: String) {
        val foods = Common.categorySelected?.foods ?: return
        val resultFood = ArrayList<FoodModel>()
        for (i in foods.indices) {
            val foodModel = foods[i]
            if (foodModel.name?.lowercase(Locale.getDefault())?.contains(s.lowercase(Locale.getDefault())) == true) {
                foodModel.positionInList = i
                resultFood.add(foodModel)
            }
        }
        foodModelList = resultFood
        context?.let { ctx ->
            adapter = MyFoodListAdapter(ctx, resultFood)
            binding.recyclerFoodList.adapter = adapter
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        foodListViewModel = ViewModelProvider(this).get(FoodListViewModel::class.java)
        _binding = FragmentFoodListBinding.inflate(inflater, container, false)

        initViews()

        foodListViewModel.mutableLiveDataFoodList.observe(viewLifecycleOwner) { foodModels ->
            if (foodModels != null && context != null && _binding != null) {
                foodModelList = foodModels
                adapter = MyFoodListAdapter(requireContext(), foodModels)
                binding.recyclerFoodList.adapter = adapter
                layoutAnimationController?.let { binding.recyclerFoodList.layoutAnimation = it }
            }
        }

        return binding.root
    }

    private fun initViews() {
        setHasOptionsMenu(true)

        val ctx = context ?: return
        dialog = SpotsDialog.Builder().setContext(ctx).setCancelable(true).build()
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference

        (activity as? AppCompatActivity)?.supportActionBar?.let { ab ->
            Common.categorySelected?.let { cat ->
                ab.title = cat.name
            }
        }

        binding.recyclerFoodList.setHasFixedSize(true)
        binding.recyclerFoodList.layoutManager = LinearLayoutManager(ctx)
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(ctx, R.anim.layout_item_from_left)

        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        val width = if (displayMetrics.widthPixels > 0) displayMetrics.widthPixels else 600

        object : MySwipeHelper(ctx, binding.recyclerFoodList, width / 6) {
            override fun instantiateMyButton(
                viewHolder: RecyclerView.ViewHolder,
                buf: MutableList<MyButton>
            ) {
                buf.add(
                    MyButton(ctx, "Delete", 30, 0, Color.parseColor("#9b0000")) { pos ->
                        val currentList = foodModelList
                        if (currentList != null && pos < currentList.size) {
                            Common.selectedFood = currentList[pos]
                            val builder = androidx.appcompat.app.AlertDialog.Builder(ctx)
                            builder.setTitle("DELETE")
                                .setMessage("Do you want to delete this food ?")
                                .setNegativeButton("CANCEL") { dialogInterface, _ -> dialogInterface.dismiss() }
                                .setPositiveButton("DELETE") { _, _ ->
                                    val foodModel = adapter?.getItemAtPosition(pos) ?: return@setPositiveButton
                                    val foods = Common.categorySelected?.foods ?: return@setPositiveButton
                                    if (foodModel.positionInList == -1) {
                                        foods.removeAt(pos)
                                    } else {
                                        foods.removeAt(foodModel.positionInList)
                                    }
                                    updateFood(foods, true)
                                }
                            builder.create().show()
                        }
                    }
                )

                buf.add(
                    MyButton(ctx, "Update", 30, 0, Color.parseColor("#560027")) { pos ->
                        val foodModel = adapter?.getItemAtPosition(pos) ?: return@MyButton
                        val targetPos = if (foodModel.positionInList == -1) pos else foodModel.positionInList
                        showUpdateDialog(targetPos, foodModel)
                    }
                )

                buf.add(
                    MyButton(ctx, "Size", 30, 0, Color.parseColor("#12005e")) { pos ->
                        val foodModel = adapter?.getItemAtPosition(pos) ?: return@MyButton
                        Common.selectedFood = if (foodModel.positionInList == -1) foodModelList?.get(pos) else foodModel
                        startActivity(Intent(ctx, SizeAddonEditActivity::class.java))
                        val targetPos = if (foodModel.positionInList == -1) pos else foodModel.positionInList
                        EventBus.getDefault().postSticky(AddonSizeEditEvent(isAddon = false, pos = targetPos))
                    }
                )

                buf.add(
                    MyButton(ctx, "Addon", 30, 0, Color.parseColor("#336699")) { pos ->
                        val foodModel = adapter?.getItemAtPosition(pos) ?: return@MyButton
                        Common.selectedFood = if (foodModel.positionInList == -1) foodModelList?.get(pos) else foodModel
                        startActivity(Intent(ctx, SizeAddonEditActivity::class.java))
                        val targetPos = if (foodModel.positionInList == -1) pos else foodModel.positionInList
                        EventBus.getDefault().postSticky(AddonSizeEditEvent(isAddon = true, pos = targetPos))
                    }
                )
            }
        }
    }

    private fun showUpdateDialog(pos: Int, foodModel: FoodModel) {
        val ctx = context ?: return

        val builder = androidx.appcompat.app.AlertDialog.Builder(ctx)
        builder.setTitle("Update")
        builder.setMessage("Please fill information")

        val dialogBinding = LayoutUpdateFoodBinding.inflate(layoutInflater)
        imgFood = dialogBinding.imgFoodImage

        dialogBinding.edtFoodName.setText(foodModel.name)
        dialogBinding.edtFoodPrice.setText(foodModel.price.toString())
        dialogBinding.edtFoodDescription.setText(foodModel.description)
        dialogBinding.switchIsAvailable.isChecked = foodModel.isAvailable

        Glide.with(ctx).load(foodModel.image).into(dialogBinding.imgFoodImage)

        dialogBinding.imgFoodImage.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
        }

        builder.setNegativeButton("CANCEL") { dialogInterface, _ -> dialogInterface.dismiss() }
            .setPositiveButton("UPDATE") { _, _ ->
                val price = if (TextUtils.isEmpty(dialogBinding.edtFoodPrice.text)) 0L else
                    dialogBinding.edtFoodPrice.text.toString().toLongOrNull() ?: 0L

                val validator = ValidateFoodInputUseCase()
                val validationResult = validator.invoke(
                    dialogBinding.edtFoodName.text.toString(),
                    price,
                    dialogBinding.edtFoodDescription.text.toString()
                )

                if (!validationResult.isValid) {
                    Toast.makeText(ctx, validationResult.errorMessage, Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val updateFood = foodModel
                updateFood.name = validationResult.trimmedName
                updateFood.description = validationResult.trimmedDescription
                updateFood.price = validationResult.sanitizedPrice
                updateFood.isAvailable = dialogBinding.switchIsAvailable.isChecked

                val uri = imageUri
                if (uri != null) {
                    dialog?.apply {
                        setMessage("Uploading...")
                        show()
                    }

                    val uniqueName = UUID.randomUUID().toString()
                    val imageFolder = storageReference.child("images/$uniqueName")

                    imageFolder.putFile(uri)
                        .addOnFailureListener { e ->
                            if (dialog?.isShowing == true) {
                                dialog?.dismiss()
                            }
                            Toast.makeText(ctx, "${e.message}", Toast.LENGTH_SHORT).show()
                        }
                        .addOnCompleteListener {
                            if (dialog?.isShowing == true) {
                                dialog?.dismiss()
                            }
                            imageFolder.downloadUrl.addOnSuccessListener { downloadUri ->
                                updateFood.image = downloadUri.toString()
                                Common.categorySelected?.foods?.set(pos, updateFood)
                                Common.categorySelected?.foods?.let { updateFood(it, false) }
                            }
                        }
                        .addOnProgressListener { taskSnapshot ->
                            val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                            dialog?.setMessage("Uploading: ${String.format(Locale.getDefault(), "%.1f", progress)}%")
                        }
                } else {
                    Common.categorySelected?.foods?.set(pos, updateFood)
                    Common.categorySelected?.foods?.let { updateFood(it, false) }
                }
            }

        builder.setView(dialogBinding.root)
        val updateDialog = builder.create()
        updateDialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data?.data != null) {
            imageUri = data.data
            imgFood?.setImageURI(imageUri)
        }
    }

    private fun updateFood(foods: MutableList<FoodModel>, isDelete: Boolean) {
        val catId = Common.categorySelected?.menu_id ?: return

        val updateData = HashMap<String, Any>()
        updateData["foods"] = foods

        FirebaseDatabase.getInstance()
            .getReference(Common.CATEGORY_REF)
            .child(catId)
            .updateChildren(updateData)
            .addOnFailureListener { e ->
                Toast.makeText(context, "${e.message}", Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    foodModelList = foods
                    adapter = context?.let { ctx -> MyFoodListAdapter(ctx, foods) }
                    binding.recyclerFoodList.adapter = adapter
                    EventBus.getDefault().postSticky(ToastEvent(!isDelete, true))
                }
            }
    }

    override fun onDestroyView() {
        if (dialog?.isShowing == true) {
            dialog?.dismiss()
        }
        dialog = null
        _binding = null
        super.onDestroyView()
    }

    override fun onDestroy() {
        EventBus.getDefault().postSticky(ChangeMenuClick(true))
        super.onDestroy()
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 1234
    }
}
