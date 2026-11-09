package com.fauzan.kafeserver.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fauzan.kafeserver.callback.RecyclerClickListener
import com.fauzan.kafeserver.databinding.LayoutSizeAddonDisplayBinding
import com.fauzan.kafeserver.model.EventBus.SelectSizeModel
import com.fauzan.kafeserver.model.EventBus.UpdateSizeModel
import com.fauzan.kafeserver.model.SizeModel
import org.greenrobot.eventbus.EventBus

class MySizeAdapter(
    private val context: Context,
    val sizeModelList: MutableList<SizeModel>
) : RecyclerView.Adapter<MySizeAdapter.MyViewHolder>() {

    private val updateSizeModel: UpdateSizeModel = UpdateSizeModel()
    var editPos: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = LayoutSizeAddonDisplayBinding.inflate(
            LayoutInflater.from(context), parent, false
        )
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val size = sizeModelList[position]
        holder.binding.txtName.text = size.name
        holder.binding.txtPrice.text = size.price.toString()

        holder.binding.imgDelete.setOnClickListener {
            sizeModelList.removeAt(position)
            notifyItemRemoved(position)
            updateSizeModel.sizeModelList = sizeModelList
            EventBus.getDefault().postSticky(updateSizeModel)
        }

        holder.setListener { _, pos ->
            editPos = position
            EventBus.getDefault().postSticky(SelectSizeModel(sizeModelList[pos]))
        }
    }

    override fun getItemCount(): Int = sizeModelList.size

    fun addNewSize(sizeModel: SizeModel) {
        sizeModelList.add(sizeModel)
        notifyItemInserted(sizeModelList.size - 1)
        updateSizeModel.sizeModelList = sizeModelList
        EventBus.getDefault().postSticky(updateSizeModel)
    }

    fun editSize(sizeModel: SizeModel) {
        if (editPos != -1) {
            sizeModelList[editPos] = sizeModel
            notifyItemChanged(editPos)
            editPos = -1
            updateSizeModel.sizeModelList = sizeModelList
            EventBus.getDefault().postSticky(updateSizeModel)
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
