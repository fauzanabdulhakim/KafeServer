package com.fauzan.kafeserver.adapter

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fauzan.kafeserver.common.Common
import com.fauzan.kafeserver.databinding.LayoutOrderItemBinding
import com.fauzan.kafeserver.domain.mapper.OrderStatusMapper
import com.fauzan.kafeserver.model.OrderModel
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * RecyclerView Adapter for merchant order management.
 * Migrated to Android Jetpack ViewBinding with interactive order status controls.
 */
class MyOrderAdapter(
    private val context: Context,
    val orderModelList: List<OrderModel>
) : RecyclerView.Adapter<MyOrderAdapter.MyViewHolder>() {

    private val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
    private val orderStatusMapper = OrderStatusMapper()

    fun interface OnOrderStatusChangeListener {
        fun onOrderStatusChanged(order: OrderModel, newStatus: Int)
    }

    private var statusChangeListener: OnOrderStatusChangeListener? = null

    fun setOnOrderStatusChangeListener(listener: OnOrderStatusChangeListener) {
        this.statusChangeListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = LayoutOrderItemBinding.inflate(
            LayoutInflater.from(context), parent, false
        )
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val order = orderModelList[position]

        // Safe image loading: avoid IndexOutOfBoundsException if cart items are empty
        if (!order.cartItemList.isNullOrEmpty()) {
            Glide.with(context)
                .load(order.cartItemList!![0].foodImage)
                .into(holder.binding.imgFoodImage)
        }

        holder.binding.txtOrderNumber.text = "Order #${order.key ?: ""}"
        holder.binding.txtTime.text = "Date: ${simpleDateFormat.format(order.createDate)}"
        holder.binding.txtName.text = "Customer: ${order.userName ?: ""}"

        val currentStatus = order.orderStatus
        val statusText = orderStatusMapper.convertStatusToString(currentStatus)
        holder.binding.txtOrderStatus.text = "Status: $statusText"

        // Color coding for status
        when (currentStatus) {
            OrderStatusMapper.STATUS_PLACED -> {
                holder.binding.txtOrderStatus.setTextColor(Color.parseColor("#E65100")) // Orange
                holder.binding.btnUpdateStatus.text = "Advance to Shipping"
                holder.binding.btnUpdateStatus.isEnabled = true
            }
            OrderStatusMapper.STATUS_SHIPPING -> {
                holder.binding.txtOrderStatus.setTextColor(Color.parseColor("#0288D1")) // Blue
                holder.binding.btnUpdateStatus.text = "Mark as Shipped"
                holder.binding.btnUpdateStatus.isEnabled = true
            }
            OrderStatusMapper.STATUS_SHIPPED -> {
                holder.binding.txtOrderStatus.setTextColor(Color.parseColor("#2E7D32")) // Green
                holder.binding.btnUpdateStatus.text = "Completed"
                holder.binding.btnUpdateStatus.isEnabled = false
            }
            else -> {
                holder.binding.txtOrderStatus.setTextColor(Color.parseColor("#C62828")) // Red
                holder.binding.btnUpdateStatus.text = "Cancelled"
                holder.binding.btnUpdateStatus.isEnabled = false
            }
        }

        val itemCount = order.cartItemList?.size ?: 0
        holder.binding.txtNumItem.text = "Items: $itemCount"

        holder.binding.btnUpdateStatus.setOnClickListener {
            showStatusUpdateDialog(order, position)
        }
    }

    private fun showStatusUpdateDialog(order: OrderModel, position: Int) {
        val currentStatus = order.orderStatus
        if (orderStatusMapper.isTerminalStatus(currentStatus)) {
            Toast.makeText(context, "Order is already finalized", Toast.LENGTH_SHORT).show()
            return
        }

        val options: Array<String>
        val targetStatuses: IntArray

        when (currentStatus) {
            OrderStatusMapper.STATUS_PLACED -> {
                options = arrayOf("Start Shipping (Process Order)", "Cancel Order")
                targetStatuses = intArrayOf(OrderStatusMapper.STATUS_SHIPPING, OrderStatusMapper.STATUS_CANCELLED)
            }
            OrderStatusMapper.STATUS_SHIPPING -> {
                options = arrayOf("Mark as Shipped (Completed)", "Cancel Order")
                targetStatuses = intArrayOf(OrderStatusMapper.STATUS_SHIPPED, OrderStatusMapper.STATUS_CANCELLED)
            }
            else -> return
        }

        AlertDialog.Builder(context)
            .setTitle("Update Order #${order.key}")
            .setItems(options) { _, which ->
                val selectedNewStatus = targetStatuses[which]
                updateOrderStatusInFirebase(order, position, selectedNewStatus)
            }
            .setNegativeButton("Dismiss") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun updateOrderStatusInFirebase(order: OrderModel, position: Int, newStatus: Int) {
        val orderKey = order.key ?: return

        val updateData = HashMap<String, Any>()
        updateData["orderStatus"] = newStatus

        FirebaseDatabase.getInstance()
            .getReference(Common.ORDER_REF)
            .child(orderKey)
            .updateChildren(updateData)
            .addOnSuccessListener {
                order.orderStatus = newStatus
                notifyItemChanged(position)
                Toast.makeText(
                    context,
                    "Order updated to ${orderStatusMapper.convertStatusToString(newStatus)}",
                    Toast.LENGTH_SHORT
                ).show()
                statusChangeListener?.onOrderStatusChanged(order, newStatus)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to update: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun getItemCount(): Int = orderModelList.size

    class MyViewHolder(val binding: LayoutOrderItemBinding) :
        RecyclerView.ViewHolder(binding.root)
}
