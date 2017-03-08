package com.six.arm.studios.miscproject1.shape;

import android.opengl.GLES20;
import android.util.Log;

import com.six.arm.studios.miscproject1.MyGLRenderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Date;

/**
 * Created by sithel on 2/28/17.
 */

public class Triangle {

    static private final String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "" +
                    "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "attribute vec4 SourceColor;" + // hack
                    "varying vec4 DestinationColor;" + // hack
                    "void main() {" +
                    // the matrix must be included as a modifier of gl_Position
                    // Note that the uMVPMatrix factor *must be first* in order
                    // for the matrix multiplication product to be correct.
                    "  DestinationColor = SourceColor;" +   //hack
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "}";

    // Use to access and set the view transformation
    static private int mMVPMatrixHandle;


    private ShortBuffer drawListBuffer;


    static private final String fragmentShaderCode =
            "" +
                    "varying lowp vec4 DestinationColor;" + //hack
                    "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "uniform float u_time;" +
                    "uniform vec2 u_mouse;" +
                    "void main() {" +
//                    "  gl_FragColor = vec4(u_mouse[1], 0,DestinationColor[2],1.0);" +
                    "  gl_FragColor = vec4(DestinationColor[0] * u_mouse[0],abs(sin(u_time * DestinationColor[1])),DestinationColor[2] * u_mouse[1],1.0);" +
//                    "  gl_FragColor = DestinationColor;" + //hack
//                    "  gl_FragColor = vColor;" +  nuked for hack
                    "}";


    private FloatBuffer vertexBuffer;
    private FloatBuffer colorHackBuffer;

    private final int mProgram;

    private int mPositionHandle;
    private int mTimeHandle;
    private int mMouseHandle;
    private int mColorHandle;
    private int mColorHandleHack;

    private final int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    private final int colorStride = COORDS_PER_COLOR * 4; // 4 bytes per vertex


    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    private static float triangleCoords[] = {   // in counterclockwise order:
            0.0f, 0.622008459f, 0.0f, // top
            -0.5f, -0.311004243f, 0.0f, // bottom left
            0.5f, -0.311004243f, 0.0f,  // bottom right
            0.5f, -0.622008459f, 0.0f  // bottom
    };


    static float squareCoords[] = {
            -0.5f, 0.5f, 3.0f,   // top left
            -0.5f, -0.5f, 3.0f,   // bottom left
            0.5f, -0.5f, 3.0f,   // bottom right
            0.5f, 0.5f, 3.0f}; // top right

    private short drawOrder[] = {0, 1, 2, 0, 2, 3}; // order to draw vertices

    // Set color with red, green, blue and alpha (opacity) values
    static final int COORDS_PER_COLOR = 4;
    float color[] = {
            1.0f, 0.0f, 0.0f, 1.0f, // top right
            0.0f, 1.0f, 0.0f, 1.0f, // bottom right
            0.0f, 0.0f, 1.0f, 1.0f, // bottom left
            1.0f, 1.0f, 1.0f, 1.0f  // top left
    };
    float mouse[] = {0f, 0f};

    public Triangle() {
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                triangleCoords.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        vertexBuffer.put(triangleCoords);
//        vertexBuffer.put(squareCoords);
        // set the buffer to read the first coordinate
        vertexBuffer.position(0);


        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);


        ByteBuffer bbHack = ByteBuffer.allocateDirect(color.length * 4);
        bbHack.order(ByteOrder.nativeOrder());
        colorHackBuffer = bbHack.asFloatBuffer();
        colorHackBuffer.put(color);
        colorHackBuffer.position(0);
        Log.i("Rebecca", "boop1");


        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        // create empty OpenGL ES Program
        mProgram = GLES20.glCreateProgram();

        // add the vertex shader to program
        GLES20.glAttachShader(mProgram, vertexShader);

        // add the fragment shader to program
        GLES20.glAttachShader(mProgram, fragmentShader);

        // creates OpenGL ES program executables
        GLES20.glLinkProgram(mProgram);
    }

    public void draw(float[] mvpMatrix) {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        mColorHandleHack = GLES20.glGetAttribLocation(mProgram, "SourceColor");
        GLES20.glEnableVertexAttribArray(mColorHandleHack);
        GLES20.glVertexAttribPointer(mColorHandleHack, 4, GLES20.GL_FLOAT, false, colorStride, colorHackBuffer);

        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        // book
        mTimeHandle = GLES20.glGetUniformLocation(mProgram, "u_time");
        GLES20.glUniform1f(mTimeHandle, mTime);
        mMouseHandle = GLES20.glGetUniformLocation(mProgram, "u_mouse");
        GLES20.glUniform2fv(mMouseHandle, 1, mouse, 0);

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);


        // Draw the square
//        GLES20.glDrawElements(
//                GLES20.GL_TRIANGLES, drawOrder.length,
//                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mMVPMatrixHandle);
        GLES20.glDisableVertexAttribArray(mMouseHandle);
        GLES20.glDisableVertexAttribArray(mColorHandle);
        GLES20.glDisableVertexAttribArray(mColorHandleHack);

        color[0] += 0.001;
        if (color[0] > 1) {
            color[0] = 0;
        }
    }

    float mDelta = 0.005f;
    float mTime = mDelta;
    boolean isInc = true;

    public static final String TAG = Triangle.class.getName();
    public void bumpTime(Long time) {
        mTime = isInc ? mTime + mDelta : mTime - mDelta;
        if (mTime <= mDelta || mTime >= 100) {
            isInc = !isInc;
        }
        GLES20.glUniform1f(mTimeHandle, mTime);
    }

    public void bumpMouse(float x, float y) {
//        GLES20.glUniform1f(mTimeHandle, x);
        mouse = new float[] {x, y};
//        GLES20.glUniform2fv(mMouseHandle, 1, mouse, 0);
//        Log.i(TAG, "and now with x/y "+x+", "+y);
    }
}
