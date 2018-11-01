package com.xiaomi.mace.demo.camera;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.View;

import static com.xiaomi.mace.demo.Constant.CAMERA_PERMISSION_REQ;

/**
 * @author dhb
 * @date 2018/10/30
 */

public abstract class Engage {

    //view:TextureView or GLSurfaceView
    private View view;
    private SurfaceTexture mSurfaceTexture;

    /**
     * preview height and width
     */
    private int mPreviewHeight;
    private int mPreviewWidth;

    public Engage() {
    }

    public Engage(View view, SurfaceTexture mSurfaceTexture) {
        this.view = view;
        this.mSurfaceTexture = mSurfaceTexture;
    }

    public abstract void openCamera(int width, int height);

    public abstract void autoFocus();

    public abstract void closeCamera();

    public abstract String getCameraId();

    public abstract void startPreview();

    public abstract boolean isFlipHorizontal();

    public abstract int getOrientation();

    public abstract void onResume();

    public abstract void onPause();

    public abstract void setModel(int mode);

    public boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(view.getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(view.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(view.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ((Activity) view.getContext()).requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_PERMISSION_REQ);
            }
            return false;
        }
        return true;
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public SurfaceTexture getmSurfaceTexture() {
        return mSurfaceTexture;
    }

    public void setmSurfaceTexture(SurfaceTexture mSurfaceTexture) {
        this.mSurfaceTexture = mSurfaceTexture;
    }

    public int getmPreviewHeight() {
        return mPreviewHeight;
    }

    public void setmPreviewHeight(int mPreviewHeight) {
        this.mPreviewHeight = mPreviewHeight;
    }

    public int getmPreviewWidth() {
        return mPreviewWidth;
    }

    public void setmPreviewWidth(int mPreviewWidth) {
        this.mPreviewWidth = mPreviewWidth;
    }
}
