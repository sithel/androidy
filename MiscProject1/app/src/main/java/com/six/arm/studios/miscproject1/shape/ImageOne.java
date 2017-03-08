package com.six.arm.studios.miscproject1.shape;

import android.content.Context;
import android.opengl.GLES20;

import com.six.arm.studios.miscproject1.R;
import com.six.arm.studios.miscproject1.Utils;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by sithel on 3/6/17.
 */

public class ImageOne {

    private int mProgramHandle;

    private static float triangleCoords[] = {   // in counterclockwise order:
            0.0f, 0.622008459f, 0.0f, // top
            -0.5f, -0.311004243f, 0.0f, // bottom left
            0.5f, -0.311004243f, 0.0f,  // bottom right
            0.5f, -0.622008459f, 0.0f  // bottom
    };


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
            {
                    // Front face
                    0.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 1.0f,
                    1.0f, 0.0f
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

    public ImageOne(Context context) {
        mProgramHandle = Utils.createAndLinkProgram(
                Utils.compileShader(GLES20.GL_VERTEX_SHADER, Utils.readTextFileFromRawResource(context, R.raw.vertexShader)),
                Utils.compileShader(GLES20.GL_FRAGMENT_SHADER, Utils.readTextFileFromRawResource(context, R.raw.fragmentShader)),
                new String[]{"a_Position", "a_Color", "a_Normal", "a_TexCoordinate"});
// Load the texture
        mTextureDataHandle = Utils.loadTexture(context, R.drawable.greenish_texture_1);
        mVertexBuffer = Utils.createVertexBuffer(triangleCords);
        mCubeTextureCoordinates = Utils.createVertexBuffer(cubeTextureCoordinateData);

    }

    public void draw() {
        // Set our per-vertex lighting program.
        GLES20.glUseProgram(mProgramHandle);

        mTextureUniformHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_Texture");
        mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_TexCoordinate");

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0);


        mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Position");
        // Pass in the position information
        mVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
                0, mVertexBuffer);

        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Pass in the texture coordinate information
        mCubeTextureCoordinates.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false,
                0, mCubeTextureCoordinates);

        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);
    }

}
