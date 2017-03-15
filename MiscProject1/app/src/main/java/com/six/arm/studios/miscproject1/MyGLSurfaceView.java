package com.six.arm.studios.miscproject1;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Action1;

/**
 * Created by sithel on 2/28/17.
 * https://developer.android.com/training/graphics/opengl/environment.html
 */

class MyGLSurfaceView extends GLSurfaceView {
    public static final String TAG = MyGLSurfaceView.class.getName();
    private MyGLRenderer mRenderer;
    private final float TOUCH_SCALE_FACTOR = 180.0f / 520;
    private float mPreviousX;
    private float mPreviousY;

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:

                float dx = x - mPreviousX;
                float dy = y - mPreviousY;

                // reverse direction of rotation above the mid-line
                if (y > getHeight() / 2) {
                    dx = dx * -1;
                }

                // reverse direction of rotation to left of the mid-line
                if (x < getWidth() / 2) {
                    dy = dy * -1;
                }

                mRenderer.setAngle((dx* TOUCH_SCALE_FACTOR), (dy*TOUCH_SCALE_FACTOR));
//                requestRender();
                if (mRenderer == null) {
//                    Log.i(TAG, "wtf, null render?");
                } else if (mRenderer.mTriangle == null) {
//                    Log.i(TAG, "wtf, null triangle?");
                } else {
                    mRenderer.mTriangle.bumpMouse(x / MyGLSurfaceView.this.getWidth(), y / MyGLSurfaceView.this.getHeight());
                }
        }

        mPreviousX = x;
        mPreviousY = y;
        return false;
    }

//    @Override
//    protected void onFinishInflate() {
//        super.onFinishInflate();
//    }

    public MyGLSurfaceView(Context context) {
        super(context);
    }

    public MyGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onFinishInflate() {

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        mRenderer = new MyGLRenderer(getContext());

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(mRenderer);

        // Render the view only when there is a change in the drawing data
        // To allow the triangle to rotate automatically, this line is commented out:
//        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        Observable.interval(10, TimeUnit.MICROSECONDS).subscribe(new Action1<Long>() {
            @Override
            public void call(Long aLong) {
                if (mRenderer == null) {
//                    Log.i(TAG, "wtf, null render?");
                } else if (mRenderer.mTriangle == null) {
//                    Log.i(TAG, "wtf, null triangle?");
                } else {
                    mRenderer.mTriangle.bumpTime(aLong);
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Log.e("Rebecca", "Rebecca, saw bad shet : " + throwable);
//                requestRender();
            }
        });

    }
}