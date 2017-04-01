package studioes.arm.six.graphics3d.text;

import android.graphics.Canvas;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.Surface;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import studioes.arm.six.graphics3d.OpenGlUtil;

/**
 * Created by sithel on 3/26/17.
 */

public class TextSurfaceRenderer implements GLSurfaceView.Renderer, ITextRenderer {
    public static final String TAG = TextSurfaceRenderer.class.getSimpleName();

    private static final int DEFAULT_TEXTURE_WIDTH = 500;
    private static final int DEFAULT_TEXTURE_HEIGHT = 500;

    private int mTextureWidth = DEFAULT_TEXTURE_WIDTH;
    private int mTextureHeight = DEFAULT_TEXTURE_HEIGHT;

    private int mGlSurfaceTexture;
    private SurfaceTexture mSurfaceTexture;

    /**
     * Handed out for drawing to the Views
     */
    private Canvas mSurfaceCanvas;
    /**
     * Created when the surface changes
     */
    private Surface mSurface;

    // region Surface View renderer methods
    @Override public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        final String extensions = GLES30.glGetString(GLES30.GL_EXTENSIONS);
        Log.d(TAG, extensions);
    }

    @Override public void onSurfaceChanged(GL10 gl, int width, int height) {
        releaseSurface();
        mGlSurfaceTexture = createTexture();
        if (mGlSurfaceTexture > 0) {
            //attach the texture to a surface.
            //It's a clue class for rendering an android view to gl level
            mSurfaceTexture = new SurfaceTexture(mGlSurfaceTexture);
            mSurfaceTexture.setDefaultBufferSize(mTextureWidth, mTextureHeight);
            mSurface = new Surface(mSurfaceTexture);
        }
    }

    @Override public void onDrawFrame(GL10 gl) {
        synchronized (this) {
            // update texture
            mSurfaceTexture.updateTexImage();
        }
    }
    // endregion

    // region ITextRenderer

    public Canvas onDrawViewBegin() {
        mSurfaceCanvas = null;
        if (mSurface != null) {
            try {
                mSurfaceCanvas = mSurface.lockCanvas(null);
            } catch (Exception e) {
                Log.e(TAG, "error while rendering view to gl: " + e);
            }
        }
        return mSurfaceCanvas;
    }

    public void onDrawViewEnd() {
        if (mSurfaceCanvas != null) {
            mSurface.unlockCanvasAndPost(mSurfaceCanvas);
        }
        mSurfaceCanvas = null;
    }

    // endregion

    private int createTexture() {
        int[] textures = new int[1];

        // Generate the texture to where android view will be rendered
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glGenTextures(1, textures, 0);
        OpenGlUtil.checkGlError("Texture generate");

        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0]);
        OpenGlUtil.checkGlError("Texture bind");

        GLES30.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLES30.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

        return textures[0];
    }


    private void releaseSurface() {
        if (mSurface != null) {
            mSurface.release();
        }
        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
        }
        mSurface = null;
        mSurfaceTexture = null;
    }


    int getGLSurfaceTexture() {
        return mGlSurfaceTexture;
    }
}
