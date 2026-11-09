package com.fauzan.kafeserver.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fauzan.kafeserver.callback.RecyclerClickListener
import com.fauzan.kafeserver.common.Common
import com.fauzan.kafeserver.databinding.LayoutFoodItemBinding
import com.fauzan.kafeserver.model.FoodModel

class MyFoodListAdapter(
    private val context: Context,
    private val foodModelList: List<FoodModel>
) : RecyclerView.Adapter<MyFoodListAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = LayoutFoodItemBinding.inflate(
            LayoutInflater.from(context), parent, false
        )
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val food = foodModelList[position]
        Glide.with(context).load(food.image).into(holder.binding.imgFoodImage)

        if (!food.isAvailable) {
            holder.binding.txtFoodName.text = "${food.name} [HABIS]"
            holder.binding.txtFoodPrice.text = "Rp${food.price} (Out of Stock)"
            holder.binding.txtFoodPrice.setTextColor(android.graphics.Color.RED)
        } else {
            holder.binding.txtFoodName.text = food.name
            holder.binding.txtFoodPrice.text = "Rp${food.price}"
            holder.binding.txtFoodPrice.setTextColor(android.graphics.Color.WHITE)
        }

        holder.setListener { _, pos ->
            Common.selectedFood = foodModelList[pos]
            Common.selectedFood?.key = pos.toString()
        }
    }

    override fun getItemCount(): Int = foodModelList.size

    fun getItemAtPosition(pos: Int): FoodModel = foodModelList[pos]

    class MyViewHolder(val binding: LayoutFoodItemBinding) :
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
}
