package com.isidroid.b21.utils.views

import android.os.Handler
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.RecyclerView

class YRecyclerScrollListener(private val child: View) : RecyclerView.OnScrollListener() {
    private var isVisible = true
    private val handler = Handler()

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        if (newState == RecyclerView.SCROLL_STATE_DRAGGING && isVisible) hide()
        else if (newState == RecyclerView.SCROLL_STATE_IDLE && !isVisible) show()
    }

    private fun show() = synchronized(isVisible) {
        handler.postDelayed({
            isVisible = true
            animate(child, 0)
        }, 1_000)
    }

    private fun hide() = synchronized(isVisible) {
        isVisible = false
        handler.removeMessages(0)

        val layoutParams = child.layoutParams as CoordinatorLayout.LayoutParams
        val fabBottomMargin = layoutParams.bottomMargin
        animate(child, child.height + fabBottomMargin)
    }

    private fun animate(child: View, y: Int) {
        child.animate().translationY(y.toFloat()).setInterpolator(LinearInterpolator()).start()
    }
}