package com.six.arm.studios.miscproject1;

import android.opengl.EGLConfig;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import com.six.arm.studios.miscproject1.shape.NoobParticles;
import com.six.arm.studios.miscproject1.shape.Square;
import com.six.arm.studios.miscproject1.shape.Triangle;

import java.util.concurrent.TimeUnit;

import javax.microedition.khronos.opengles.GL10;

import rx.Observable;
import rx.functions.Action1;

/**
 * Created by sithel on 2/28/17.
 * https://developer.android.com/training/graphics/opengl/environment.html
 */

public class MyGLRenderer implements GLSurfaceView.Renderer {
    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];

    private float[] mRotationMatrix = new float[16];
    private float[] mRotationMatrixX = new float[16];
    private float[] mRotationMatrixY = new float[16];
    private float[] mRotationMatrixZ = new float[16];

    public Triangle mTriangle;
    private Square mSquare;
    private NoobParticles mParts;

    // Since the renderer code is running on a separate thread from the main user
    // interface thread of your application, you must declare this public variable
    // as volatile.
    public volatile float mAngleX;
    public volatile float mAngleY;


    public void setAngle(float angleX, float angleY) {
        mAngleX += angleX/100f;
        mAngleY += angleY/100f;
    }


    @Override
    public void onDrawFrame(GL10 unused) {
        float[] scratch = new float[16];

        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);



        // Create a rotation transformation for the triangle
        long time = SystemClock.uptimeMillis() % 4000L;
        float angle = 0.090f * ((int) time);
        Matrix.setRotateM(mRotationMatrix, 0, 0, 0, 0, -1.0f);
//        Matrix.setRotateM(mRotationMatrix, 0, angle, 0, 0, -1.0f);
//        Matrix.setRotateM(mRotationMatr   ix, 0, angle, -1.0f, 0, 0);
//        Matrix.setRotateM(mRotationMatrixX, 0, mAngleX, -1.0f, 0, 0);
//        Matrix.setRotateM(mRotationMatrixY, 0, mAngleY, 0, -1.0f, 0.0f);

//        Matrix.multiplyMM(mRotationMatrix, 0, mRotationMatrixY, 0, mRotationMatrixX, 0);
//        Matrix.multiplyMM(mRotationMatrix, 0, mRotationMatrix, 0, mRotationMatrixZ, 0);

        // Combine the rotation matrix with the projection and camera view
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mRotationMatrix, 0);


        // Draw shape
        mTriangle.draw(scratch);
        mParts.draw(scratch);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, javax.microedition.khronos.egl.EGLConfig config) {
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        mParts = new NoobParticles();
        // initialize a triangle
        mTriangle = new Triangle();
        // initialize a square
        mSquare = new Square();

    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }


    /**
     * Note: Compiling OpenGL ES shaders and linking programs is expensive in terms of CPU
     * cycles and processing time, so you should avoid doing this more than once. If you
     * do not know the content of your shaders at runtime, you should build your code such
     * that they only get created once and then cached for later use.
     *
     * https://developer.android.com/training/graphics/opengl/draw.html
     * @param type
     * @param shaderCode
     * @return
     */
    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }
}