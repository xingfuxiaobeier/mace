// Copyright 2018 Xiaomi, Inc.  All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.xiaomi.mace.demo.camera.GL;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class CameraApiNew extends CameraEngage implements Camera.AutoFocusCallback {

    private Camera mCamera;
    private Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
    private String[] supportSize = new String[]{
            "1280x720", "640x480", "720x720"};
    private final String dstResolution = "640x480";

    public CameraApiNew(CameraGLSurfaceView glSurfaceView, SurfaceTexture texture) {
        super(glSurfaceView, texture);
    }

    @Override
    public void openCamera(int width, int height) {
        Log.i("dhb", "test step 3");
        if (!checkCameraPermission()) {
            return;
        }
        super.openCamera(width, height);
        closeCamera();
        String cameraId = getCameraId();
        if (TextUtils.isEmpty(cameraId)) {
            return;
        }

        mCamera = Camera.open(Integer.parseInt(cameraId));
        Camera.getCameraInfo(Integer.parseInt(cameraId), cameraInfo);
        setOutputConfig(width, height);
        startPreview();
    }

    @Override
    public void autoFocus() {
        doAutoFocus();
    }

    @Override
    public void closeCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public String getCameraId() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int cameraFacing = facingFrontPreview() ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK;
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == cameraFacing) {
                return String.valueOf(i);
            }
        }
        return "";
    }

    @Override
    public void startPreview() {
        try {
            doAutoFocus();
            mCamera.setPreviewTexture(getmSurfaceTexture());
            mCamera.startPreview();
            mCamera.setDisplayOrientation(90);
        } catch (Exception e) {
            Log.e(getClass().getName(), "startPreview error = " + e);
        }
    }

    @Override
    public boolean isFlipHorizontal() {
        if(cameraInfo != null) {
            return cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT ? true : false;
        }
        return false;
    }

    @Override
    public int getOrientation() {
        if (cameraInfo != null) {
            return cameraInfo.orientation;
        }
        return 0;
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        if (success) {
            mCamera.cancelAutoFocus();
        }
    }

    private void doAutoFocus() {
        try {
            mCamera.autoFocus(this);
        } catch (Throwable e) {
            Log.e(this.getClass().getName(), "auto focus error = " + e);
        }
    }

    private void setOutputConfigOrigin(int width, int height) {
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        List<String> supportSizes = getSupportedPreviewSize(supportSize);
        String curResolution = null;
        if (supportSizes.contains(dstResolution)) {

        } else {
            //dedault
            if (supportSizes.size() > 0) {
                curResolution = supportSizes.get(0);
            } else {
                //TODO:
            }
        }
    }

    private ArrayList<String> getSupportedPreviewSize(String[] previewSizes)
    {
        ArrayList<String> result = new ArrayList<String>();
        if(mCamera != null)
        {
            List<Camera.Size> sizes = mCamera.getParameters().getSupportedPreviewSizes();
            for(String candidate : previewSizes)
            {
                int index = candidate.indexOf('x');
                if (index == -1) continue;
                int width = Integer.parseInt(candidate.substring(0, index));
                int height = Integer.parseInt(candidate.substring(index + 1));
                for(Camera.Size s : sizes){
                    if((s.width == width) && (s.height == height)){
                        result.add(candidate);
                    }
                }
            }
        }
        return result;
    }


    private void setOutputConfig(int width, int height) {
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        Camera.Size size = getOptimalSize(parameters.getSupportedPreviewSizes(), width, height);
        Log.i("Camera api", "setOutputConfig sie : " + size.width + ", " + size.height);
        setmPreviewWidth(size.height);
        setmPreviewHeight(size.width);
        parameters.setPreviewSize(size.width, size.height);
        parameters.setPictureSize(size.width, size.height);
        mCamera.setParameters(parameters);
    }

    private Camera.Size getOptimalSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.height / size.width;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
                continue;
            }
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }

        return optimalSize;
    }


}
