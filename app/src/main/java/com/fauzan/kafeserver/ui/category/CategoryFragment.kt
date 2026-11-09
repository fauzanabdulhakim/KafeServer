package com.fauzan.kafeserver.ui.category

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fauzan.kafeserver.R
import com.fauzan.kafeserver.adapter.MyCategoriesAdapter
import com.fauzan.kafeserver.common.Common
import com.fauzan.kafeserver.common.MySwipeHelper
import com.fauzan.kafeserver.databinding.FragmentCategoryBinding
import com.fauzan.kafeserver.databinding.LayoutUpdateCategoryBinding
import com.fauzan.kafeserver.model.CategoryModel
import com.fauzan.kafeserver.model.EventBus.ToastEvent
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dmax.dialog.SpotsDialog
import org.greenrobot.eventbus.EventBus
import java.util.Locale
import java.util.UUID

/**
 * Fragment rendering category administration and category modification dialogs.
 * Migrated to Android Jetpack ViewBinding with nullified bindings in onDestroyView.
 */
class CategoryFragment : Fragment() {

    private lateinit var categoryViewModel: CategoryViewModel
    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!

    private var dialog: AlertDialog? = null
    private var layoutAnimationController: LayoutAnimationController? = null
    private var adapter: MyCategoriesAdapter? = null

    private var categoryModels: List<CategoryModel>? = null
    private var imgCategory: ImageView? = null
    private var imageUri: Uri? = null

    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        categoryViewModel = ViewModelProvider(this).get(CategoryViewModel::class.java)
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)

        initViews()

        categoryViewModel.messageError.observe(viewLifecycleOwner) { s ->
            context?.let { Toast.makeText(it, s, Toast.LENGTH_SHORT).show() }
            if (dialog?.isShowing == true) {
                dialog?.dismiss()
            }
        }

        categoryViewModel.categoryListMultable.observe(viewLifecycleOwner) { categoryModelList ->
            if (dialog?.isShowing == true) {
                dialog?.dismiss()
            }
            categoryModels = categoryModelList
            context?.let { ctx ->
                _binding?.let { b ->
                    adapter = MyCategoriesAdapter(ctx, categoryModelList)
                    b.recyclerMenu.adapter = adapter
                    layoutAnimationController?.let { anim ->
                        b.recyclerMenu.layoutAnimation = anim
                    }
                }
            }
        }

        return binding.root
    }

    private fun initViews() {
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference

        context?.let { ctx ->
            dialog = SpotsDialog.Builder().setContext(ctx).setCancelable(true).build()
            layoutAnimationController = AnimationUtils.loadLayoutAnimation(ctx, R.anim.layout_item_from_left)

            val layoutManager = LinearLayoutManager(ctx)
            binding.recyclerMenu.layoutManager = layoutManager
            binding.recyclerMenu.addItemDecoration(DividerItemDecoration(ctx, layoutManager.orientation))

            object : MySwipeHelper(ctx, binding.recyclerMenu, 200) {
                override fun instantiateMyButton(
                    viewHolder: RecyclerView.ViewHolder,
                    buf: MutableList<MyButton>
                ) {
                    buf.add(
                        MyButton(
                            ctx, "Update", 30, 0, Color.parseColor("#560027")
                        ) { pos ->
                            categoryModels?.let { list ->
                                if (pos < list.size) {
                                    Common.categorySelected = list[pos]
                                    showUpdateDialog()
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    private fun showUpdateDialog() {
        val ctx = context ?: return
        val selectedCategory = Common.categorySelected ?: return

        val builder = androidx.appcompat.app.AlertDialog.Builder(ctx)
        builder.setTitle("Update")
        builder.setMessage("Please fill information")

        val dialogBinding = LayoutUpdateCategoryBinding.inflate(layoutInflater)
        imgCategory = dialogBinding.imgCategory

        // Populate initial data
        dialogBinding.edtCategoryName.setText(selectedCategory.name)
        Glide.with(ctx).load(selectedCategory.image).into(dialogBinding.imgCategory)

        // Image picker click
        dialogBinding.imgCategory.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
        }

        builder.setNegativeButton("CANCEL") { dialogInterface, _ -> dialogInterface.dismiss() }
        builder.setPositiveButton("UPDATE") { _, _ ->
            val updateData = HashMap<String, Any>()
            updateData["name"] = dialogBinding.edtCategoryName.text.toString()

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
                        Toast.makeText(context, "${e.message}", Toast.LENGTH_SHORT).show()
                    }
                    .addOnCompleteListener {
                        if (dialog?.isShowing == true) {
                            dialog?.dismiss()
                        }
                        imageFolder.downloadUrl.addOnSuccessListener { downloadUri ->
                            updateData["image"] = downloadUri.toString()
                            updateCategory(updateData)
                        }
                    }
                    .addOnProgressListener { taskSnapshot ->
                        val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                        dialog?.setMessage("Uploading: ${String.format(Locale.getDefault(), "%.1f", progress)}%")
                    }
            } else {
                updateCategory(updateData)
            }
        }

        builder.setView(dialogBinding.root)
        val updateDialog = builder.create()
        updateDialog.show()
    }

    private fun updateCategory(updateData: Map<String, Any>) {
        val categoryId = Common.categorySelected?.menu_id ?: return

        FirebaseDatabase.getInstance()
            .getReference(Common.CATEGORY_REF)
            .child(categoryId)
            .updateChildren(updateData)
            .addOnFailureListener { e ->
                Toast.makeText(context, "${e.message}", Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener {
                categoryViewModel.loadCategories()
                EventBus.getDefault().postSticky(ToastEvent(isUpdate = true, isFromFoodList = false))
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data?.data != null) {
            imageUri = data.data
            imgCategory?.setImageURI(imageUri)
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

    companion object {
        private const val PICK_IMAGE_REQUEST = 1234
    }
}
