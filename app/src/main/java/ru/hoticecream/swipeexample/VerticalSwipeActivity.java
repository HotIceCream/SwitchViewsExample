package ru.hoticecream.swipeexample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;


public class VerticalSwipeActivity extends Activity implements GestureDetector.OnGestureListener {

    private static final String TAG = "SwipeExample";
    private GestureDetector mGestureDetector;
    private View mBlackView;
    private View mRedView;
    private FlingAnimator mCurrentFlingAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vertical_swipe);
        mGestureDetector = new GestureDetector(this, this);
        mRedView = findViewById(R.id.layout_red);
        mBlackView = findViewById(R.id.layout_black);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP) {
            onUp();
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        stopAnimation();
        return true;
    }

    private void onUp() {
        Log.d(TAG, "onUp");
        synchronized (mutex) {
            if (mCurrentFlingAnimation == null) {
                Log.d(TAG, "onUp start Animation");
                if (mRedView.getHeight() > mBlackView.getHeight()) {
                    mCurrentFlingAnimation = new FlingAnimator(mBlackView, mRedView, 0, mFlingAnimationListener);
                } else {
                    mCurrentFlingAnimation = new FlingAnimator(mRedView, mBlackView, 0, mFlingAnimationListener);
                }
                mCurrentFlingAnimation.start();
            }
        }
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float distanceX, float distanceY) {
        Log.d(TAG, "onScroll " + distanceY);
        View minimized;
        View maximized;

        if (distanceY < 0) {
            // move down
            minimized = mBlackView;
            maximized = mRedView;
        } else {
            // move up
            minimized = mRedView;
            maximized = mBlackView;
        }

        if (minimized.getHeight() > 0) {
            int delta = minimized.getHeight() > Math.abs(distanceY) ? (int) Math.abs(distanceY) : minimized.getHeight();
            ViewGroup.LayoutParams redParams = minimized.getLayoutParams();
            redParams.height -= delta;
            minimized.setLayoutParams(redParams);

            ViewGroup.LayoutParams blackParams = maximized.getLayoutParams();
            blackParams.height += delta;
            maximized.setLayoutParams(blackParams);
            return true;
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
        Log.d(TAG, "onFling " + v2);
        stopAnimation();
        synchronized (mutex) {
            if (v2 > 0) {
                mCurrentFlingAnimation = new FlingAnimator(mRedView, mBlackView, v2, mFlingAnimationListener);
            } else {
                mCurrentFlingAnimation = new FlingAnimator(mBlackView, mRedView, v2, mFlingAnimationListener);
            }
            mCurrentFlingAnimation.start();
        }
        return false;
    }

    private static final Object mutex = new Object();
    private void stopAnimation() {
        synchronized (mutex) {
            if (mCurrentFlingAnimation != null) {
                mCurrentFlingAnimation.interrupt();
                mCurrentFlingAnimation = null;
            }
        }
    }

    private FlingAnimationListener mFlingAnimationListener = new FlingAnimationListener() {

        @Override
        public void onFinish(FlingAnimator instance) {
            synchronized (mutex) {
                if (instance == mCurrentFlingAnimation) {
                    mCurrentFlingAnimation = null;
                }
            }
        }
    };

    private static class FlingAnimator extends Thread {
        private static final float GRAVITY = 30;
        private static final long DELAY = 10;
        private static final long ONE_SECOND = 1000;
        private final View mFirst;
        private final View mSecond;
        private final FlingAnimationListener mListener;
        private float mSpeed;


        /**
         * Direction is from first to second.
         * @param first
         * @param second
         * @param speed
         */
        public FlingAnimator(View first, View second, float speed, FlingAnimationListener listener) {
            mFirst = first;
            mSecond = second;
            mSpeed = Math.abs(speed) * DELAY / ONE_SECOND;
            mListener = listener;
        }

        @Override
        public void run() {
            while (!isInterrupted()) {
                if (mSpeed > 0) {
                    if (mSecond.getHeight() <= 0) {
                        break;
                    }
                } else {
                    if (mFirst.getHeight() <= 0) {
                        break;
                    }
                }
                int delta;
                if (mSpeed > 0) {
                    delta = (mSecond.getHeight() > mSpeed) ? (int) mSpeed : mSecond.getHeight();
                } else {
                    delta = (mFirst.getHeight() > mSpeed) ? (int) mSpeed : -mFirst.getHeight();
                }

                final ViewGroup.LayoutParams redParams = mSecond.getLayoutParams();
                redParams.height -= delta;
                mSecond.post(new Runnable() {
                    @Override
                    public void run() {
                        mSecond.setLayoutParams(redParams);
                    }
                });


                final ViewGroup.LayoutParams blackParams = mFirst.getLayoutParams();
                blackParams.height += delta;
                mFirst.post(new Runnable() {
                    @Override
                    public void run() {
                        mFirst.setLayoutParams(blackParams);
                    }
                });

                if (mFirst.getHeight() > mSecond.getHeight()) {
                    mSpeed += GRAVITY * DELAY / ONE_SECOND;
                } else {
                    mSpeed -= GRAVITY * DELAY / ONE_SECOND;
                }

                try {
                    sleep(DELAY);
                } catch (InterruptedException e) {
                    break;
                }
            }
            mListener.onFinish(this);
        }
    }

    private interface FlingAnimationListener {
        void onFinish(FlingAnimator instance);
    }


}
