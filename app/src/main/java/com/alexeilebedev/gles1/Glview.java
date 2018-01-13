package com.alexeilebedev.gles1;

import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

public class Glview extends GLSurfaceView {
    Home _home;
    Glrend _glrend;

    Glview(Home home) {
        super(home);
        _home=home;
        setEGLContextClientVersion(2);
        _glrend = new Glrend(this);
        setRenderer(_glrend);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        boolean invalidate = false;
        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:
                invalidate = true;
                break;
            case MotionEvent.ACTION_BUTTON_PRESS:
                break;
            case MotionEvent.ACTION_DOWN:
                invalidate = true;
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        if (invalidate) {
            requestRender();
        }
        return true;
    }
}
