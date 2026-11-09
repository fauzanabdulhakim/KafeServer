package com.fauzan.kafeserver.common

import android.graphics.Typeface
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.widget.TextView
import com.fauzan.kafeserver.domain.mapper.OrderStatusMapper
import com.fauzan.kafeserver.domain.session.ServerSessionManager
import com.fauzan.kafeserver.model.CategoryModel
import com.fauzan.kafeserver.model.FoodModel
import com.fauzan.kafeserver.model.ServerUserModel

/**
 * Common constants and backward-compatible bridge for legacy callers.
 * Business logic and mutable session management are delegated to domain classes.
 */
object Common {
    const val SERVER_REF: String = "Server"
    const val CATEGORY_REF: String = "Category"
    const val ORDER_REF: String = "Order"
    const val REQUEST_REFUND_REF: String = "RequestRefund"

    const val DEFAULT_COLUMN_COUNT: Int = 0
    const val FULL_WIDTH_COLUMN: Int = 1

    private val orderStatusMapper = OrderStatusMapper()

    // Legacy fields maintained for backward compatibility; getters/setters synchronized with ServerSessionManager
    @JvmField
    var currentServerUser: ServerUserModel? = null
    @JvmField
    var categorySelected: CategoryModel? = null
    @JvmField
    var selectedFood: FoodModel? = null

    /**
     * Synchronizes legacy public fields into the scoped ServerSessionManager.
     */
    @JvmStatic
    fun syncToSession() {
        val session = ServerSessionManager.getInstance()
        session.currentServerUser = currentServerUser
        session.selectedCategory = categorySelected
        session.selectedFood = selectedFood
    }

    /**
     * Formats greeting text with bold username for TextViews.
     */
    @JvmStatic
    fun setSpanString(welcome: String, name: String, textView: TextView) {
        val builder = SpannableStringBuilder()
        builder.append(welcome)
        val spannableString = SpannableString(name)
        val boldSpan = StyleSpan(Typeface.BOLD)
        spannableString.setSpan(boldSpan, 0, name.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        builder.append(spannableString)
        textView.setText(builder, TextView.BufferType.SPANNABLE)
    }

    /**
     * Formats greeting text with colored bold username for TextViews.
     */
    @JvmStatic
    fun setSpanStringColor(welcome: String, name: String, textView: TextView, color: Int) {
        val builder = SpannableStringBuilder()
        builder.append(welcome)
        val spannableString = SpannableString(name)
        val boldSpan = StyleSpan(Typeface.BOLD)
        spannableString.setSpan(boldSpan, 0, name.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(ForegroundColorSpan(color), 0, name.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        builder.append(spannableString)
        textView.setText(builder, TextView.BufferType.SPANNABLE)
    }

    /**
     * Delegates order status conversion to the domain OrderStatusMapper.
     */
    @JvmStatic
    fun convertStatusToString(orderStatus: Int): String {
        return orderStatusMapper.convertStatusToString(orderStatus)
    }
}
