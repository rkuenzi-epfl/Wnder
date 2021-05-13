package com.github.wnder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * A custom implementation of the OnTouchListener to allow specific action when a "swipe" is performed on a particular view.
 * The code is entierly based on this discussion from stackoverflow: https://stackoverflow.com/questions/4139288/android-how-to-handle-right-to-left-swipe-gestures
 */
public class OnSwipeTouchListener implements View.OnTouchListener {
    private final GestureDetector gestureDetector;
    private enum movementType {VERTICAL, HORIZONTAL};

    public OnSwipeTouchListener(Context ctx){
        gestureDetector = new GestureDetector(ctx, new GestureListener());
    }

    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouch(final View v, final MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        private boolean movementTypeDispatchRightBottom(movementType type){
            return type == movementType.HORIZONTAL ? onSwipeRight() : onSwipeBottom();
        }

        private boolean movementTypeDispatchLeftTop(movementType type){
            return type == movementType.HORIZONTAL ? onSwipeLeft() : onSwipeTop();
        }

        private boolean movementTypeDispatch(float diff, movementType type){
            if (diff > 0) {
                return movementTypeDispatchRightBottom(type);
            } else {
                return movementTypeDispatchLeftTop(type);
            }
        }

        private boolean multiDirectionalSwipe(float diff, float velocity, movementType type){
            boolean result = false;
            if (Math.abs(diff) > SWIPE_THRESHOLD && Math.abs(velocity) > SWIPE_VELOCITY_THRESHOLD) {
                result = movementTypeDispatch(diff, type);
            }
            return result;
        }

        /**
         * Retrieve the movement being done by checking the MotionEvents and the velocities associated to it.
         * @param e1 the beginning of the global movement
         * @param e2 The end of the global movement
         * @param velocityX The velocity in the x axis
         * @param velocityY The velocity in the y axis
         * @return true if the function has correctly determined the movement and it was an implemented one, false otherwise.
         */
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    result = multiDirectionalSwipe(diffX, velocityX, movementType.HORIZONTAL);
                } else {
                    result = multiDirectionalSwipe(diffY, velocityY, movementType.VERTICAL);
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }

    public boolean onSwipeRight() {
        return false;
    }

    public boolean onSwipeLeft() {
        return false;
    }

    public boolean onSwipeTop() {
        return false;
    }

    public boolean onSwipeBottom() {
        return false;
    }
}
