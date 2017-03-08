package com.six.arm.studios.miscproject1.shape;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import com.six.arm.studios.miscproject1.MyGLRenderer;
import com.six.arm.studios.miscproject1.R;
import com.six.arm.studios.miscproject1.Utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by sithel on 3/6/17.
 */

public class ImageOne {
    public static final String TAG = "ImageOne";

    private int mProgramHandle;

    /**
     * This will be used to pass in the texture.
     */
    private int mTextureUniformHandle;

    /**
     * This will be used to pass in model texture coordinate information.
     */
    private int mTextureCoordinateHandle;

    /**
     * Size of the texture coordinate data in elements.
     */
    private final int mTextureCoordinateDataSize = 2;

    /**
     * This is a handle to our texture data.
     */
    private int mTextureDataHandle;

    // S, T (or X, Y)
// Texture coordinate data.
// Because images have a Y axis pointing downward (values increase as you move down the image) while
// OpenGL has a Y axis pointing upward, we adjust for that here by flipping the Y axis.
// What's more is that the texture coordinates are the same for every face.
    final float[] cubeTextureCoordinateData =
            {
                    // Front face
                    0.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 1.0f,
                    1.0f, 0.0f
            };
    FloatBuffer mCubeTextureCoordinates;

    final float[] triangleCords =
            {  // in counterclockwise order:
                    0.0f, 0.622008459f, 0.0f, // top
                    -0.5f, -0.311004243f, 0.0f, // bottom left
                    0.5f, -0.311004243f, 0.0f  // bottom right
            };
    FloatBuffer mVertexBuffer;
    /**
     * Size of the position data in elements.
     */
    private final int mPositionDataSize = 3;
    /**
     * This will be used to pass in model position information.
     */
    private int mPositionHandle;

    // Use to access and set the view transformation
    private int mMVPMatrixHandle;

    static private final String vertexString =
            "" +
                    "uniform mat4 u_MVPMatrix;" +
                    "attribute vec4 a_Position;" +
                    "void main() {" +
                    "  gl_Position = u_MVPMatrix * a_Position;" +
                    "}";


    static private final String fragString =
            "" +
                    "precision mediump float;" +
                    "void main() {" +
                    "  gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);" +
                    "}";


    public ImageOne(Context context, boolean x) {

        Log.i(TAG, "rebecca, does this run?");
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                triangleCords.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        mVertexBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
//        vertexBuffer.put(triangleCoords);
        mVertexBuffer.put(triangleCords);
        // set the buffer to read the first coordinate
        mVertexBuffer.position(0);


        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                vertexString);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragString);

        // create empty OpenGL ES Program
        mProgramHandle = GLES20.glCreateProgram();

        // add the vertex shader to program
        GLES20.glAttachShader(mProgramHandle, vertexShader);

        // add the fragment shader to program
        GLES20.glAttachShader(mProgramHandle, fragmentShader);

        // creates OpenGL ES program executables
        GLES20.glLinkProgram(mProgramHandle);
    }
    // remember: this needs to all happen AFTER the Renderer has gotten started...
    public ImageOne(Context context) {
//        String vertexString = ;
//        Log.d(TAG, vertexString);
//        String fragString = ;
//        Log.d(TAG, fragString);
        Log.e(TAG, "SHIT IS NOT RIGHT");
        mProgramHandle = Utils.createAndLinkProgram_v2(
                Utils.compileShader(GLES20.GL_VERTEX_SHADER, Utils.readTextFileFromRawResource(context, R.raw.vertex_shader)),
                Utils.compileShader(GLES20.GL_FRAGMENT_SHADER, Utils.readTextFileFromRawResource(context, R.raw.fragment_shader)),
                new String[]{"a_TexCoordinate"});
//                new String[]{});

        // Load the texture
        mTextureDataHandle = Utils.loadTexture(context, R.drawable.greenish_texture_2);
        mCubeTextureCoordinates = Utils.createVertexBuffer(cubeTextureCoordinateData);
        mVertexBuffer = Utils.createVertexBuffer(triangleCords);
    }

    public void draw(float[] mvpMatrix) {

        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgramHandle);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Position");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, 3,
                GLES20.GL_FLOAT, false,
                3*4, mVertexBuffer);


        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, triangleCords.length/3);

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVPMatrix");

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mMVPMatrixHandle);


        // Set our per-vertex lighting program.
        GLES20.glUseProgram(mProgramHandle);


        mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Position");
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVPMatrix");

        mTextureUniformHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_Texture");
        mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_TexCoordinate");

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0);

        // Pass in the position information
//        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false, 0, mVertexBuffer);
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false, mPositionDataSize * 4, mVertexBuffer);
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Pass in the texture coordinate information
        mCubeTextureCoordinates.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false, 0, mCubeTextureCoordinates);

        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
    }

}
