package com.example.chatapp.utils.swipeUpButtonListener

import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.DragEvent
import android.view.View
import android.widget.Toast


open class OnSwipeTouchListener(
    val context: Context,
    val v: View
) :
    View.OnDragListener {
    private var mLastTouchX: Float = 0f
    private var mLastTouchY: Float = 0f
    private var mPosX: Float = 0f
    private var mPosY: Float = 0f
    private var mScaleFactor = 1f
    override fun onDrag(view: View?, event: DragEvent?): Boolean {
        when (event?.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                // Determines if this View can accept the dragged data
                if (event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                    // As an example of what your application might do,
                    // applies a blue color tint to the View to indicate that it can accept
                    // data.
                    v.setBackgroundColor(Color.BLUE)

                    // Invalidate the view to force a redraw in the new tint
                    v.invalidate()

                    // returns true to indicate that the View can accept the dragged data.
                }
            }
                DragEvent.ACTION_DRAG_ENTERED -> {
                    // Applies a green tint to the View. Return true; the return value is ignored.
                    v.setBackgroundColor(Color.GREEN)

                    // Invalidate the view to force a redraw in the new tint
                    v.invalidate()
                }

                DragEvent.ACTION_DRAG_LOCATION ->{}
                // Ignore the event

                DragEvent.ACTION_DRAG_EXITED -> {
//                     Re-sets the color tint to blue. Returns true; the return value is ignored.
                    v.setBackgroundColor(Color.BLUE)

                    // Invalidate the view to force a redraw in the new tint
                    v.invalidate()
                }
                DragEvent.ACTION_DROP -> {
                    // Gets the item containing the dragged data
                    val item: ClipData.Item = event.clipData.getItemAt(0)

                    // Gets the text data from the item.
                    val dragData = item.text

                    // Displays a message containing the dragged data.
                    Toast.makeText(context, "Dragged data is " + dragData, Toast.LENGTH_LONG).show()

                    // Turns off any color tints
                    v.background.clearColorFilter()

                    // Invalidates the view to force a redraw
                    v.invalidate()

                    // Returns true. DragEvent.getResult() will return true.
                }

                DragEvent.ACTION_DRAG_ENDED -> {
                    // Turns off any color tinting
                    (v.background.clearColorFilter())

                    // Invalidates the view to force a redraw
                    v.invalidate()

                    // Does a getResult(), and displays what happened.
                    when (event.result) {
                        true ->
                            Toast.makeText(context, "The drop was handled.", Toast.LENGTH_LONG)
                        else ->
                            Toast.makeText(context, "The drop didn't work.", Toast.LENGTH_LONG)
                    }.show()

                    // returns true; the value is ignored.
                }
                else -> {
                    // An unknown action type was received.
                    Log.e("DragDrop Example", "Unknown action type received by OnDragListener.")
                }
            }
            return true
        }
    }