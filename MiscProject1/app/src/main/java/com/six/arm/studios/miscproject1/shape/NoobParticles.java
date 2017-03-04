package com.six.arm.studios.miscproject1.shape;

import android.opengl.GLES20;
import android.opengl.GLES31;
import android.util.Log;

import com.six.arm.studios.miscproject1.MyGLRenderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;

/**
 * Created by sithel on 3/2/17.
 * Looking at http://www.opengl-tutorial.org/intermediate-tutorials/billboards-particles/particles-instancing/
 */

public class NoobParticles {
    public static final String TAG = NoobParticles.class.getName();

    private final int mProgram;
    final String vertexShaderCode =
            "uniform mat4 u_MVPMatrix;      \n"
                    + "attribute vec4 a_Position;     \n"
                    + "void main()                    \n"
                    + "{                              \n"
                    + "   gl_Position = u_MVPMatrix   \n"
                    + "               * a_Position;   \n"
                    + "   gl_PointSize = 50.0;       \n"
                    + "}                              \n";

    final String fragmentShaderCode =
            "precision mediump float;       \n"
                    + "void main()                    \n"
                    + "{                              \n"
                    + "   float dist = distance( vec2(0.5,0.5), gl_PointCoord );\n"
                    + "   float zdist = abs(gl_FragCoord[2]*4.0); \n"
                    + "   float r = min(0.5, zdist); \n"
                    + "  if ( dist > r)\n"
                    + "       discard;\n"
                    + "   gl_FragColor = vec4(gl_PointCoord[0],gl_PointCoord[1], min(1.0, zdist*1.0), min(1.0,dist*1.0) );             \n"
                    + "}                              \n";
    private static float dotCoords[] = {   // in counterclockwise order:
            0.01f, 0.05f, 1f,
            0.01f, 0.05f, 1.1f,
            0.8f, 0.02f, 1.2f,
            0.09f, 0.2f, 0.8f,
    };

    List<RPoint> points = Arrays.asList(
            new RPoint(0, 0, 0, 1)
    );
    float[] vertices = {
            0.0f, 0.0f, 1.0f
    };

    // number of coordinates per vertex in this array
    private static final int COORDS_PER_VERTEX = 3;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    // buffers
    private FloatBuffer vertexBuffer;
    FloatBuffer vertexBuf;

    // handles
    private int mPositionHandle;
    private int mMVPMatrixHandle;


    public NoobParticles() {
        points = new ArrayList<>();
        for (int i = 0; i < 500; ++i) {
            points.add(new RPoint(i, 0, 1, 1));
        }
        dotCoords = new float[points.size() * 3];
        ByteBuffer bb = ByteBuffer.allocateDirect(dotCoords.length * 4); // (number of coordinate values * 4 bytes per float)
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(dotCoords);
        vertexBuffer.position(0);


        vertexBuf = ByteBuffer.allocateDirect(vertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexBuf.put(vertices).position(0);

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
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "a_Position");
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "u_MVPMatrix");

        // No culling of back faces
        GLES20.glDisable(GLES20.GL_CULL_FACE);

        // No depth testing
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        // http://www.learnopengles.com/android-lesson-five-an-introduction-to-blending/
        GLES20.glEnable(GLES20.GL_BLEND);
//        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE);     // Additive
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);     //Interpolative

        Observable
                .interval(10, TimeUnit.MILLISECONDS)
                .flatMap(new Func1<Long, Observable<RPoint>>() {
                    @Override
                    public Observable<RPoint> call(final Long count) {
                        return Observable.from(points)
                                .map(new Func1<RPoint, RPoint>() {
                                    @Override
                                    public RPoint call(RPoint rPoint) {
                                        rPoint.nudge(count);
//                                        if (rPoint.i == 0) {
//                                            Log.i(TAG, rPoint.toString());
//                                        }
                                        return rPoint;
                                    }
                                });
                    }
                })
                .subscribe()
        ;

    }

    class RPoint {
        long lifetime;
        long start;
        float[] startPoint;

        float xVel;
        float yVel;

        int i;
        float x;
        float y;
        float z;

        public RPoint(int index, float x, float y, float z) {
            start = 0;
            lifetime = (long) (Math.random() * 1000) + 500;
            startPoint = new float[]{x, y, z};
            xVel = 0;
            yVel = 0;
            i = index;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        private void reset(long count) {
            xVel = (float) (2*(Math.random() - 0.5) / 100);
            yVel = 0.01f;
            x = startPoint[0];
            y = startPoint[1];
            z = startPoint[2];
            start = count;
        }

        public void nudge(long count) {
            double r = Math.random();
            long age = count - start;
            if (age > lifetime) {
                reset(count);
                return;
            }
            if (count % 18 == 1) {
                xVel = (float) (Math.random() - 0.5) / 100;
            } else if (count % 10 == 1) {
                yVel = (float) (Math.random() - 0.85)/100;
            }
            x += xVel;
            y += yVel;
            z = startPoint[2] - 4f * age / (lifetime);
        }

        public void updateArray(float[] dots) {
            dots[3 * i] = x;
            dots[3 * i + 1] = y;
            dots[3 * i + 2] = z;
        }

        @Override
        public String toString() {
            return "[(" + i + ") x: " + x + ", y: " + y + ", z: " + z + ", start: " + start + ", lifetime: " + lifetime + "]";
        }
    }

    public void draw(float[] mvpMatrix) {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram);

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);


        // Prepare the triangle coordinate data
        for (RPoint rp : points) {
            rp.updateArray(dotCoords);
//            Log.i(TAG, rp.toString());
        }
//        vertexBuffer.clear();
        vertexBuffer.put(dotCoords);
        vertexBuffer.position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, dotCoords.length / COORDS_PER_VERTEX);

        //Send the vertex
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuf);
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        //Draw the point
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mMVPMatrixHandle);

    }
}
