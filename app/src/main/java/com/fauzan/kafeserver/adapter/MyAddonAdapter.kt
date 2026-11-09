package com.fauzan.kafeserver.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fauzan.kafeserver.callback.RecyclerClickListener
import com.fauzan.kafeserver.databinding.LayoutSizeAddonDisplayBinding
import com.fauzan.kafeserver.model.AddonModel
import com.fauzan.kafeserver.model.EventBus.SelectAddonModel
import com.fauzan.kafeserver.model.EventBus.UpdateAddonModel
import org.greenrobot.eventbus.EventBus

class MyAddonAdapter(
    private val context: Context,
    val addonModels: MutableList<AddonModel>
) : RecyclerView.Adapter<MyAddonAdapter.MyViewHolder>() {

    private val updateAddonModel: UpdateAddonModel = UpdateAddonModel()
    var editPos: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = LayoutSizeAddonDisplayBinding.inflate(
            LayoutInflater.from(context), parent, false
        )
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val addon = addonModels[position]
        holder.binding.txtName.text = addon.name
        holder.binding.txtPrice.text = addon.price.toString()

        holder.binding.imgDelete.setOnClickListener {
            addonModels.removeAt(position)
            notifyItemRemoved(position)
            updateAddonModel.addonModel = addonModels
            EventBus.getDefault().postSticky(updateAddonModel)
        }

        holder.setListener { _, pos ->
            editPos = position
            EventBus.getDefault().postSticky(SelectAddonModel(addonModels[pos]))
        }
    }

    override fun getItemCount(): Int = addonModels.size

    fun addNewSize(addonModel: AddonModel) {
        addonModels.add(addonModel)
        notifyItemInserted(addonModels.size - 1)
        updateAddonModel.addonModel = addonModels
        EventBus.getDefault().postSticky(updateAddonModel)
    }

    fun editSize(addonModel: AddonModel) {
        if (editPos != -1) {
            addonModels[editPos] = addonModel
            notifyItemChanged(editPos)
            editPos = -1
            updateAddonModel.addonModel = addonModels
            EventBus.getDefault().postSticky(updateAddonModel)
        }
    }

    class MyViewHolder(val binding: LayoutSizeAddonDisplayBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private var listener: RecyclerClickListener? = null

        fun setListener(listener: RecyclerClickListener) {
            this.listener = listener
        }

        init {
            binding.root.setOnClickListener { view ->
                listener?.onItemCLickListener(view, adapterPosition)
            }
        }
    }
}
