package com.fauzan.kafeserver.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fauzan.kafeserver.databinding.LayoutRefundItemBinding
import com.fauzan.kafeserver.model.RefundRequestModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * RecyclerView adapter displaying refund requests for kitchen and admin fulfillment.
 */
class MyRefundAdapter(
    private val context: Context,
    private var refundList: List<RefundRequestModel>,
    private val onActionClickListener: OnRefundActionListener
) : RecyclerView.Adapter<MyRefundAdapter.RefundViewHolder>() {

    interface OnRefundActionListener {
        fun onApprove(refundModel: RefundRequestModel)
        fun onReject(refundModel: RefundRequestModel)
    }

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    private val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

    fun updateData(newList: List<RefundRequestModel>) {
        this.refundList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RefundViewHolder {
        val binding = LayoutRefundItemBinding.inflate(
            LayoutInflater.from(context), parent, false
        )
        return RefundViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RefundViewHolder, position: Int) {
        val item = refundList[position]
        val b = holder.binding

        b.txtOrderNumber.text = "Order #${item.orderNumber ?: "-"}"
        b.txtCustomerInfo.text = "${item.name ?: "Unknown"} • ${item.phone ?: "-"}"
        b.txtAmount.text = "Refund: Rp ${String.format(Locale.getDefault(), "%,.0f", item.amount)}"
        b.txtReason.text = "Reason: ${item.reason ?: "-"}"

        val status = item.status.uppercase()
        b.txtStatus.text = status

        when (status) {
            "APPROVED" -> {
                b.txtStatus.setBackgroundColor(Color.parseColor("#E8F5E9"))
                b.txtStatus.setTextColor(Color.parseColor("#2E7D32"))
                b.layoutActions.visibility = View.GONE
            }
            "REJECTED" -> {
                b.txtStatus.setBackgroundColor(Color.parseColor("#FFEBEE"))
                b.txtStatus.setTextColor(Color.parseColor("#C62828"))
                b.layoutActions.visibility = View.GONE
            }
            else -> {
                b.txtStatus.setBackgroundColor(Color.parseColor("#FFF3E0"))
                b.txtStatus.setTextColor(Color.parseColor("#E65100"))
                b.layoutActions.visibility = View.VISIBLE
            }
        }

        b.btnApprove.setOnClickListener {
            onActionClickListener.onApprove(item)
        }

        b.btnReject.setOnClickListener {
            onActionClickListener.onReject(item)
        }
    }

    override fun getItemCount(): Int = refundList.size

    class RefundViewHolder(val binding: LayoutRefundItemBinding) :
        RecyclerView.ViewHolder(binding.root)
}
