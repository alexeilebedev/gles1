package com.alexeilebedev.gles1;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

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
    private float _angle = 0.f;
    private boolean _rotating = false;
    long _basetime;
    Glview _view;
    final float[] _tri1data = {
            // X, Y, Z, R, G, B, A
            -0.5f, -0.25f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f,
            0.5f, -0.25f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.559016994f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f
    };

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
        int mStrideBytes = 7 * 4;
        int mPositionOffset = 0;
        int mPositionDataSize = 3;
        int mColorOffset = 3;
        int mColorDataSize = 4;
        // Pass in the position information
        tribuf.position(mPositionOffset);
        GLES20.glVertexAttribPointer(_position_handle, mPositionDataSize, GLES20.GL_FLOAT, false,mStrideBytes, tribuf);
        GLES20.glEnableVertexAttribArray(_position_handle);
        tribuf.position(mColorOffset);
        GLES20.glVertexAttribPointer(_color_handle, mColorDataSize, GLES20.GL_FLOAT, false,mStrideBytes, tribuf);
        GLES20.glEnableVertexAttribArray(_color_handle);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
    }

    Glrend(Glview view) {
        _view=view;
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        // create a triangle <yawn>
        // glBegin(GL_TRIANGLES);
        // ...
        // glVertex3f(-0.5f, -0.25f, 0.0f);
        // glColor3f(1.0f, 0.0f, 0.0f);
        // glEnd();
        _tri1 = ByteBuffer.allocateDirect(_tri1data.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        _tri1.put(_tri1data).position(0);

        // Set the background clear color to gray.
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 0.5f);

        // Position the eye behind the origin.
        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = 1.5f;

        // We are looking toward the distance
        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = -5.0f;

        // Set our up vector. This is where our head would be pointing were we holding the camera.
        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;

        // Set the view matrix. This matrix can be said to represent the camera position.
        // NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
        // view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
        Matrix.setLookAtM(_viewmat, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

        int vshader = compileShaderX(GLES20.GL_VERTEX_SHADER, vertexShader);
        int fshader = compileShaderX(GLES20.GL_FRAGMENT_SHADER, fragmentShader);

        // Create a program object and store the handle to it.
        int prog = compileProg(vshader, fshader);

        // Set program handles. These will later be used to pass in values to the program.
        _mvpmat_handle = GLES20.glGetUniformLocation(prog, "u_MVPMatrix");
        _position_handle = GLES20.glGetAttribLocation(prog, "a_Position");
        _color_handle = GLES20.glGetAttribLocation(prog, "a_Color");

        // Tell OpenGL to use this program when rendering.
        GLES20.glUseProgram(prog);
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        // Create a new perspective projection matrix. The height will stay the same
        // while the width will vary as per aspect ratio.
        final float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 1.0f;
        final float far = 10.0f;

        Matrix.frustumM(_projmat, 0, left, right, bottom, top, near, far);
    }

    public void beginRotate() {
        _rotating = true;
        _basetime = SystemClock.uptimeMillis();
    }
    public void endRotate() {
        _rotating=false;
    }
    @Override
    public void onDrawFrame(GL10 glUnused) {
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        long now = SystemClock.uptimeMillis();
        _angle += (now - _basetime) * 360.f / 10000.f;
        _basetime = now;

        // Draw the triangle facing straight on.
        Matrix.setIdentityM(_modelmat, 0);
        Matrix.rotateM(_modelmat, 0, _angle, 0.0f, 0.0f, 1.0f);

        Matrix.multiplyMM(_mvpmat, 0, _viewmat, 0, _modelmat, 0);
        Matrix.multiplyMM(_mvpmat, 0, _projmat, 0, _mvpmat, 0);
        GLES20.glUniformMatrix4fv(_mvpmat_handle, 1, false, _mvpmat, 0);

        drawTriangle(_tri1);
        if (_rotating) {
            _view.requestRender();
        }
    }
}
