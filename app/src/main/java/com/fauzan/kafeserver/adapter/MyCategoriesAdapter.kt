package com.fauzan.kafeserver.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fauzan.kafeserver.callback.RecyclerClickListener
import com.fauzan.kafeserver.common.Common
import com.fauzan.kafeserver.databinding.LayoutCategoryItemBinding
import com.fauzan.kafeserver.model.CategoryModel
import com.fauzan.kafeserver.model.EventBus.CategoryClick
import org.greenrobot.eventbus.EventBus

class MyCategoriesAdapter(
    private val context: Context,
    val categoryModelList: List<CategoryModel>
) : RecyclerView.Adapter<MyCategoriesAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = LayoutCategoryItemBinding.inflate(
            LayoutInflater.from(context), parent, false
        )
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val category = categoryModelList[position]
        Glide.with(context).load(category.image).into(holder.binding.imgCategory)
        holder.binding.txtCategory.text = category.name

        holder.setListener { _, pos ->
            Common.categorySelected = categoryModelList[pos]
            EventBus.getDefault().postSticky(CategoryClick(true, categoryModelList[pos]))
        }
    }

    override fun getItemCount(): Int = categoryModelList.size

    class MyViewHolder(val binding: LayoutCategoryItemBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        private var listener: RecyclerClickListener? = null

        fun setListener(listener: RecyclerClickListener) {
            this.listener = listener
        }

        init {
            binding.root.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            listener?.onItemCLickListener(view, adapterPosition)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (categoryModelList.size == 1) {
            Common.DEFAULT_COLUMN_COUNT
        } else {
            if (categoryModelList.size % 2 == 0) {
                Common.DEFAULT_COLUMN_COUNT
            } else {
                if (position > 1 && position == categoryModelList.size - 1)
                    Common.FULL_WIDTH_COLUMN
                else
                    Common.DEFAULT_COLUMN_COUNT
            }
        }
    }
}
