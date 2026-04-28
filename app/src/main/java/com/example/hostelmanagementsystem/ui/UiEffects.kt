package com.example.hostelmanagementsystem.ui

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator

object UiEffects {

    fun animateScreenIn(activity: Activity) {
        val content = activity.findViewById<ViewGroup>(android.R.id.content)
        val root = content.getChildAt(0) ?: return

        root.alpha = 0f
        root.translationY = 12f

        root.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(180)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    fun pulse(view: View, endAction: (() -> Unit)? = null) {
        endAction?.invoke()

        view.animate()
            .scaleX(0.985f)
            .scaleY(0.985f)
            .setDuration(45)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(90)
                    .start()
            }
            .start()
    }
}
