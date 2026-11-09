package com.fauzan.kafeserver.ui.order

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.fauzan.kafeserver.R
import com.fauzan.kafeserver.adapter.MyOrderAdapter
import com.fauzan.kafeserver.databinding.FragmentOrderBinding
import com.fauzan.kafeserver.domain.model.UiState
import com.fauzan.kafeserver.domain.usecase.FilterOrdersByStatusUseCase

/**
 * Kitchen Order fulfillment screen built with Kotlin, ViewBinding, and reactive StateFlow observation.
 * Automatically adapts between Phone (single-column) and Tablet Kitchen Display System (multi-column grid).
 */
class OrderFragment : Fragment() {

    private var _binding: FragmentOrderBinding? = null
    private val binding get() = _binding!!

    private lateinit var orderViewModel: OrderKotlinViewModel
    private var layoutAnimationController: LayoutAnimationController? = null
    private var adapter: MyOrderAdapter? = null

    private var currentFilter = -1 // -1 = All
    private var lastSummary: FilterOrdersByStatusUseCase.KitchenPipelineSummary? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        orderViewModel = ViewModelProvider(this).get(OrderKotlinViewModel::class.java)
        _binding = FragmentOrderBinding.inflate(inflater, container, false)

        initViews()
        setupFilterChips()
        observeViewModel()

        orderViewModel.loadOrders()

        return binding.root
    }

    private fun initViews() {
        context?.let { ctx ->
            binding.recyclerOrder.setHasFixedSize(true)

            // Responsive Layout: Use GridLayout for Kitchen Display tablets (>=600dp), LinearLayout for phones
            val isTablet = resources.configuration.smallestScreenWidthDp >= 600
            if (isTablet) {
                binding.recyclerOrder.layoutManager = GridLayoutManager(ctx, 2)
            } else {
                binding.recyclerOrder.layoutManager = LinearLayoutManager(ctx)
            }

            layoutAnimationController = AnimationUtils.loadLayoutAnimation(ctx, R.anim.layout_item_from_left)
        }
    }

    private fun setupFilterChips() {
        binding.chipAll.setOnClickListener {
            currentFilter = -1
            updateDisplayedOrders()
        }

        binding.chipPlaced.setOnClickListener {
            currentFilter = 0
            updateDisplayedOrders()
        }

        binding.chipShipping.setOnClickListener {
            currentFilter = 1
            updateDisplayedOrders()
        }

        binding.chipShipped.setOnClickListener {
            currentFilter = 2
            updateDisplayedOrders()
        }
    }

    private fun observeViewModel() {
        orderViewModel.pipelineState.asLiveData().observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    // Loading placeholder
                }
                is UiState.Success -> {
                    lastSummary = state.data
                    updateChipCounters(state.data)
                    updateDisplayedOrders()
                }
                is UiState.Empty -> {
                    lastSummary = null
                    binding.txtEmptyOrder.visibility = View.VISIBLE
                    binding.recyclerOrder.visibility = View.GONE
                    updateChipCounters(null)
                }
                is UiState.Error -> {
                    context?.let { ctx ->
                        Toast.makeText(ctx, state.message, Toast.LENGTH_SHORT).show()
                    }
                }
                is UiState.Idle -> {
                    // Initial idle state
                }
            }
        }
    }

    private fun updateChipCounters(summary: FilterOrdersByStatusUseCase.KitchenPipelineSummary?) {
        val placedCount = summary?.placedOrders?.size ?: 0
        val shippingCount = summary?.shippingOrders?.size ?: 0
        val shippedCount = summary?.shippedOrders?.size ?: 0

        binding.chipPlaced.text = "Placed ($placedCount)"
        binding.chipShipping.text = if (resources.configuration.smallestScreenWidthDp >= 600) "Preparing ($shippingCount)" else "Shipping ($shippingCount)"
        binding.chipShipped.text = "Shipped ($shippedCount)"
    }

    private fun updateDisplayedOrders() {
        val summary = lastSummary
        val ctx = context ?: return

        if (summary == null) {
            binding.txtEmptyOrder.visibility = View.VISIBLE
            binding.recyclerOrder.visibility = View.GONE
            return
        }

        val ordersToShow = orderViewModel.getFilteredOrders(summary, currentFilter)

        if (ordersToShow.isEmpty()) {
            binding.txtEmptyOrder.visibility = View.VISIBLE
            binding.recyclerOrder.visibility = View.GONE
        } else {
            binding.txtEmptyOrder.visibility = View.GONE
            binding.recyclerOrder.visibility = View.VISIBLE

            adapter = MyOrderAdapter(ctx, ordersToShow)
            binding.recyclerOrder.adapter = adapter
            layoutAnimationController?.let {
                binding.recyclerOrder.layoutAnimation = it
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        adapter = null
        super.onDestroyView()
    }
}
