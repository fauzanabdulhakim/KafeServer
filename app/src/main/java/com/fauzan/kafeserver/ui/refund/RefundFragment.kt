package com.fauzan.kafeserver.ui.refund

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.fauzan.kafeserver.adapter.MyRefundAdapter
import com.fauzan.kafeserver.common.Common
import com.fauzan.kafeserver.databinding.FragmentRefundBinding
import com.fauzan.kafeserver.model.RefundRequestModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * Screen enabling merchants and managers to review, approve, or reject customer refund requests.
 * Fully migrated to Android Jetpack ViewBinding with proper lifecycle listener disposal.
 */
class RefundFragment : Fragment(), MyRefundAdapter.OnRefundActionListener {

    private var _binding: FragmentRefundBinding? = null
    private val binding get() = _binding!!

    private var refundRef: DatabaseReference? = null
    private var refundListener: ValueEventListener? = null
    private var adapter: MyRefundAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRefundBinding.inflate(inflater, container, false)

        initViews()
        loadRefundRequests()

        return binding.root
    }

    private fun initViews() {
        val ctx = requireContext()
        binding.recyclerRefund.setHasFixedSize(true)
        binding.recyclerRefund.layoutManager = LinearLayoutManager(ctx)
        adapter = MyRefundAdapter(ctx, emptyList(), this)
        binding.recyclerRefund.adapter = adapter
    }

    private fun loadRefundRequests() {
        val ref = FirebaseDatabase.getInstance().getReference(Common.REQUEST_REFUND_REF)
        refundRef = ref

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<RefundRequestModel>()
                for (item in snapshot.children) {
                    val model = item.getValue(RefundRequestModel::class.java)
                    if (model != null) {
                        if (model.orderNumber.isNullOrBlank()) {
                            model.orderNumber = item.key
                        }
                        list.add(model)
                    }
                }
                list.sortByDescending { it.requestDate }

                val currentBinding = _binding ?: return
                if (list.isEmpty()) {
                    currentBinding.txtEmptyRefund.visibility = View.VISIBLE
                    currentBinding.recyclerRefund.visibility = View.GONE
                } else {
                    currentBinding.txtEmptyRefund.visibility = View.GONE
                    currentBinding.recyclerRefund.visibility = View.VISIBLE
                    adapter?.updateData(list)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                context?.let {
                    Toast.makeText(it, error.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        refundListener = listener
        ref.addValueEventListener(listener)
    }

    override fun onApprove(refundModel: RefundRequestModel) {
        val orderNum = refundModel.orderNumber ?: return
        val ctx = context ?: return

        AlertDialog.Builder(ctx)
            .setTitle("Approve Refund")
            .setMessage("Approve refund for Order #$orderNum in the amount of Rp ${String.format(java.util.Locale.getDefault(), "%,.0f", refundModel.amount)}?")
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Approve") { _, _ ->
                val updates = hashMapOf<String, Any>(
                    "status" to "APPROVED",
                    "done" to true
                )
                FirebaseDatabase.getInstance().getReference(Common.REQUEST_REFUND_REF)
                    .child(orderNum)
                    .updateChildren(updates)
                    .addOnSuccessListener {
                        Toast.makeText(ctx, "Refund for Order #$orderNum APPROVED", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(ctx, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .show()
    }

    override fun onReject(refundModel: RefundRequestModel) {
        val orderNum = refundModel.orderNumber ?: return
        val ctx = context ?: return

        AlertDialog.Builder(ctx)
            .setTitle("Reject Refund")
            .setMessage("Are you sure you want to reject the refund request for Order #$orderNum?")
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Reject") { _, _ ->
                val updates = hashMapOf<String, Any>(
                    "status" to "REJECTED",
                    "done" to true
                )
                FirebaseDatabase.getInstance().getReference(Common.REQUEST_REFUND_REF)
                    .child(orderNum)
                    .updateChildren(updates)
                    .addOnSuccessListener {
                        Toast.makeText(ctx, "Refund for Order #$orderNum REJECTED", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(ctx, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .show()
    }

    override fun onDestroyView() {
        refundListener?.let { listener ->
            refundRef?.removeEventListener(listener)
        }
        refundListener = null
        refundRef = null
        adapter = null
        _binding = null
        super.onDestroyView()
    }
}
