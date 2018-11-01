package com.xiaomi.mace.demo.camera.GL;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;

import com.xiaomi.mace.demo.CameraFactory;
import com.xiaomi.mace.demo.ViewModeEnum;
import com.xiaomi.mace.demo.camera.Engage;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by dhb on 2018/10/29.
 */

public class CameraGLSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer,SurfaceTexture.OnFrameAvailableListener {

    private final String TAG = "CameraGLSurfaceView";
    private Context mContext;

    private SurfaceTexture surfaceTexture;
    private GLDrawer drawer;
    private Engage engage;
    private Handler handler;

    //opengl params
    private int texture_id = -1;
    private int[] mFrameBuffers;
    private int[] mFrameBufferTextures;
    private int mWidth = -1;
    private int mHeight = -1;

    private int tag = -1;


    public CameraGLSurfaceView(Context context) {
        super(context);
    }

    public CameraGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        Log.i(TAG, "onSurfaceCreated begin ... ");

        GLES20.glEnable(GL10.GL_DITHER);
        GLES20.glClearColor(0, 0, 0, 0);
        GLES20.glEnable(GL10.GL_DEPTH_TEST);
        //oes texture id
        texture_id = GLUtil.genTextureOES();

        //surface texture
        surfaceTexture = new SurfaceTexture(texture_id);
        surfaceTexture.setOnFrameAvailableListener(this);

        Log.i(TAG, "onSurfaceCreated end ... ");
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        Log.i(TAG, "onSurfaceChanged begin ... ");

        //使用view分辨率设置视口
        GLES20.glViewport(0, 0, width, height);
        Log.i(TAG, "onSurfaceChanged create view port width : " + width + ", height : " + height);

        mWidth = width;
        mHeight = height;

        if (engage == null) {
            engage = CameraFactory.genCameEngage(this, ViewModeEnum.GLSURFACE_VIEW.getValue());

            if (handler != null) {
                Message message = Message.obtain();
                message.what = 1;
                message.obj = engage;
                handler.sendMessage(message);
                handler.sendEmptyMessage(0);
            }

            //可以使用目标分辨率打开摄像头，但实际上摄像头预览的分辨率为摄像头
            // 本身支持的分辨率对指定分辨率的适配，在这里使用view的宽高打开
            engage.openCamera(width, height);
            //create drawer
            drawer = new GLDrawer(engage, width, height);
            Log.i(TAG, "onSurfaceChanged camera preview width : " + engage.getmPreviewWidth() + ", height : " + engage.getmPreviewHeight());

        }
        //adjust viewport
        drawer.adjustViewPort(width, height);
        Log.i(TAG, "onSurfaceChanged end ... ");
    }

    @Override
    public void onDrawFrame(GL10 gl10) {

        surfaceTexture.updateTexImage();

//        surfaceTexture
//        float[] mtx = new float[16];
//        surfaceTexture.getTransformMatrix(mtx);
        if (drawer != null) {
            int preTextureId = drawer.preProcess(texture_id);
            drawer.draw(preTextureId);
        }

    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        if (tag == -1) {
            requestRender();
        }
//        tag++;
    }

    public SurfaceTexture getSurfaceTexture() {
        return surfaceTexture;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }
}
