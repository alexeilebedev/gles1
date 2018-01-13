package com.alexeilebedev.gles1;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.time.LocalDateTime;
import java.util.Date;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Glrend implements GLSurfaceView.Renderer {
    private int _mvpmat_handle;
    private int _position_handle;
    private int _color_handle;
    private float[] _projmat = new float[16];
    private float[] _modelmat = new float[16];
    private float[] _viewmat = new float[16];
    private float[] _mvpmat = new float[16];
    private FloatBuffer _tri1;
    Glview _view;

    final String vertexShader =
            "uniform mat4 u_MVPMatrix; "
                    + "attribute vec4 a_Position; "
                    + "attribute vec4 a_Color; "
                    + "varying vec4 v_Color; "
                    + "void main() {"
                    + "   v_Color = a_Color; "
                    + "   gl_Position = u_MVPMatrix * a_Position; "
                    + "}";

    final String fragmentShader =
            "precision mediump float;"
                    + "varying vec4 v_Color;"
                    + "void main() { "
                    + "   gl_FragColor = v_Color; "
                    + "} ";

    int compileShaderX(int type, String text) {
        int ret = GLES20.glCreateShader(type);
        if (ret != 0) {
            // Pass in the shader source.
            GLES20.glShaderSource(ret, text);
            GLES20.glCompileShader(ret);
            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(ret, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
            if (compileStatus[0] == 0){
                GLES20.glDeleteShader(ret);
                ret = 0;
            }
        }
        if (ret == 0)  {
            throw new RuntimeException("Error creating vertex shader.");
        }
        return ret;
    }

    int compileProg(int vshader, int fshader) {
        int prog = GLES20.glCreateProgram();
        if (prog != 0) {
            GLES20.glAttachShader(prog, vshader);
            GLES20.glAttachShader(prog, fshader);
            GLES20.glBindAttribLocation(prog, 0, "a_Position");
            GLES20.glBindAttribLocation(prog, 1, "a_Color");
            GLES20.glLinkProgram(prog);
            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(prog, GLES20.GL_LINK_STATUS, linkStatus, 0);
            // If the link failed, delete the program.
            if (linkStatus[0] == 0) {
                GLES20.glDeleteProgram(prog);
                prog = 0;
            }
        }
        if (prog == 0) {
            throw new RuntimeException("Error creating program.");
        }
        return prog;
    }

    private void drawTriangle(final FloatBuffer tribuf) {
        int stride_bytes = 7 * 4;
        tribuf.position(0);
        GLES20.glVertexAttribPointer(_position_handle, 3, GLES20.GL_FLOAT, false, stride_bytes, tribuf);
        GLES20.glEnableVertexAttribArray(_position_handle);
        tribuf.position(3);
        GLES20.glVertexAttribPointer(_color_handle, 4, GLES20.GL_FLOAT, false, stride_bytes, tribuf);
        GLES20.glEnableVertexAttribArray(_color_handle);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
    }

    Glrend(Glview view) {
        _view=view;
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 0.9f);
        //7=elements per vertex, 3=vertices
        _tri1 = ByteBuffer.allocateDirect(7*3* 4).order(ByteOrder.nativeOrder()).asFloatBuffer();

        // eyez must be >= 1.0 to work
        Matrix.setLookAtM(_viewmat, 0
                , 0.0f, 0.0f, 2.0f
                , 0.0f, 0.0f, -5.0f
                , 0.0f, 1.0f, 0.0f);

        int vshader = compileShaderX(GLES20.GL_VERTEX_SHADER, vertexShader);
        int fshader = compileShaderX(GLES20.GL_FRAGMENT_SHADER, fragmentShader);
        int prog = compileProg(vshader, fshader);
        _mvpmat_handle = GLES20.glGetUniformLocation(prog, "u_MVPMatrix");
        _position_handle = GLES20.glGetAttribLocation(prog, "a_Position");
        _color_handle = GLES20.glGetAttribLocation(prog, "a_Color");
        GLES20.glUseProgram(prog);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        final float ratio = (float) width / height;
        Matrix.frustumM(_projmat, 0, -ratio, ratio, -1.0f, 1.0f, 1.0f, 10.0f);
    }


    public void putVertex(float x, float y, float z) {
        _tri1.put(x);
        _tri1.put(y);
        _tri1.put(z);
    }

    public void putColor(int r, int g, int b) {
        _tri1.put(r/255.f);
        _tri1.put(g/255.f);
        _tri1.put(b/255.f);
        _tri1.put(1.f);// alpha
    }

    public void putRad(float angle, float radius) {
        // clocks go clockwise from PI/2
        angle = (float)(Math.PI * 0.5 - angle * Math.PI * 2);
        float x = (float)(Math.cos(angle) *radius);
        float y = (float)(Math.sin(angle) *radius);
        putVertex(x,y,0.f);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        Matrix.setIdentityM(_modelmat, 0);
        //Matrix.rotateM(_modelmat, 0, _angle, 0.0f, 0.0f, 1.0f);

        Matrix.multiplyMM(_mvpmat, 0, _viewmat, 0, _modelmat, 0);
        Matrix.multiplyMM(_mvpmat, 0, _projmat, 0, _mvpmat, 0);
        GLES20.glUniformMatrix4fv(_mvpmat_handle, 1, false, _mvpmat, 0);

        LocalDateTime ldt = LocalDateTime.now();
        long millis = System.currentTimeMillis() % 1000;
        float secs = ldt.getSecond() + (float)millis*0.001f;
        int min = ldt.getMinute();
        int hour = ldt.getHour();

        // compose triangle vertices
        _tri1.clear();
        putRad(secs / 60.0f, 1.f);
        putColor(255,0,0);

        putRad((float)min / 60, 1.0f);
        putColor(0,255,0);

        putRad((float)(hour % 12) / 12, 1.0f);
        putColor(0,0, 255);

        _tri1.position(0);
        drawTriangle(_tri1);
    }
}
