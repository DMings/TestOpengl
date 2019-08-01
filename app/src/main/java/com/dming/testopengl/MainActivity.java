package com.dming.testopengl;

import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {

    private GLSurfaceView mGLSurfaceView;
    private TextureRenderer mRenderer;
    private ImageView mIvShowTx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGLSurfaceView = findViewById(R.id.gl_show);
        mIvShowTx = findViewById(R.id.iv_show_tx);
//        mGLSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
//        mGLSurfaceView.getHolder().setFormat(PixelFormat.RGBA_8888);
        // 请求一个OpenGL ES 2.0兼容的上下文
        mGLSurfaceView.setEGLContextClientVersion(2);
        // 设置渲染器(后面会着重讲这个渲染器的类)
        mRenderer = new TextureRenderer(this, mGLSurfaceView);
        mGLSurfaceView.setRenderer(mRenderer);
        // 设置渲染模式为连续模式(会以60fps的速度刷新)
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        initBtnListener();

        mRenderer.setRun(new TextureRenderer.Run() {
            @Override
            public void getData(int w, int h, ByteBuffer byteBuffer) {
                final Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                bitmap.copyPixelsFromBuffer(byteBuffer);
                DLog.i("bitmap: " + bitmap);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mIvShowTx.setImageBitmap(bitmap);
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLSurfaceView.onPause();
    }

    private void initBtnListener() {
        findViewById(R.id.btn_effect_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mRenderer.getHeight();
                mRenderer.setGetImage();
                mGLSurfaceView.requestRender();
            }
        });
    }

}