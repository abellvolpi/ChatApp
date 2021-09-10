package com.example.chatapp.utils.swipeUpButtonListener

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import android.view.View

open class OnSwipeTouchListener(val context: Context, val action : (Int) -> Unit) : View.OnTouchListener {
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
        if (motionEvent != null && view!=null) {

            if (motionEvent.action == MotionEvent.ACTION_UP
                || motionEvent.action == MotionEvent.ACTION_CANCEL
            ) {

                if (motionEvent.action == MotionEvent.ACTION_UP) {
                    action.invoke(motionEvent.action)
                }

            }
            view.onTouchEvent(motionEvent)
            return true
        }
        return false
    }


}