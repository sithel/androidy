package studioes.arm.six.graphics3d.text;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by sithel on 3/26/17.
 */

public class RenderableTextView extends TextView implements IRenderableView {
    public static final String TAG = RenderableTextView.class.getSimpleName();
    ITextRenderer mRenderer;

    // region view methods
    public RenderableTextView(Context context) {
        super(context);
    }

    public RenderableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RenderableTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public RenderableTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void draw(Canvas canvas) {
        //returns canvas attached to gl texture to draw on
        Log.v(TAG, "onDraw called...");
        Canvas glAttachedCanvas = mRenderer.onDrawViewBegin();
        if (glAttachedCanvas != null) {
            //translate canvas to reflect view scrolling
//            float xScale = glAttachedCanvas.getWidth() / (float)canvas.getWidth();
//            glAttachedCanvas.scale(xScale, xScale);
//            glAttachedCanvas.translate(-getScrollX(), -getScrollY());
            //draw the view to provided canvas
            super.draw(glAttachedCanvas);
        }
        // notify the canvas is updated
        mRenderer.onDrawViewEnd();
    }
    // endregion

    // region renderable stuff

    @Override public void setText(String title) {
        super.setText(title);
    }

    public void setTextRenderer(ITextRenderer renderer) {
        mRenderer = renderer;
    }
    // end region


}
