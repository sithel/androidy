package com.six.arm.studios.miscproject1;

import android.opengl.GLSurfaceView;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OpenGlAttemptActivity extends AppCompatActivity {
    @LayoutRes
    public static final int LAYOUT_ID = R.layout.activity_open_gl_attempt;

    @BindView(R.id.fancy_gl_surface) MyGLSurfaceView mGLView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View v = getLayoutInflater().inflate(LAYOUT_ID, null);
        setContentView(v);
        ButterKnife.bind(this);

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
//        mGLView = new MyGLSurfaceView(this);
//        setContentView(mGLView);
    }
}
