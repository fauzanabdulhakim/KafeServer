package com.fauzan.kafeserver.callback

import android.view.View

fun interface RecyclerClickListener {
    fun onItemCLickListener(view: View, pos: Int)
}
