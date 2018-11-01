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

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.xiaomi.mace.demo.AppModel;
import com.xiaomi.mace.demo.ModelType;
import com.xiaomi.mace.demo.camera.Engage;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public abstract class CameraEngage extends Engage{

    private final String TAG = "CameraEngage";

    //model enum
    private int model = 1;

    @Override
    public void setModel(int model) {
        synchronized (lock) {
            this.model = model;
            switch (model) {
                case 0:
                case 1:
                    FINAL_SIZE = 224;
                    break;
                case 2:
                    FINAL_SIZE = 513;
                    break;
                default:
                    break;
            }

            colorValues = new int[FINAL_SIZE * FINAL_SIZE];
            float[] floatValues = new float[FINAL_SIZE * FINAL_SIZE * 3];
            floatBuffer.clear();
            floatBuffer = FloatBuffer.wrap(floatValues, 0, FINAL_SIZE * FINAL_SIZE * 3);
        }
    }

    /**
     * switch camera use
     */
    private boolean mFacingFront = false;
    /**
     * camera background thread
     */
    private HandlerThread mBackgroundHandlerThread;
    private Handler mBackgroundHandler;

    /**
     * mace need data size width and height
     */
    private static int FINAL_SIZE = 224;

    /**
     * storage rgb value
     */
    private int[] colorValues;

    /**
     * mace float[] input
     */
    private FloatBuffer floatBuffer;
    private ByteBuffer byteBuffer;

    /**
     * Bitmap
     */
    private Bitmap bitmap = null;

    private final Object lock = new Object();
    private final Object bufferLock = new Object();

    private boolean isCapturePic = false;

    private Runnable mHandleCapturePicRunnable = new Runnable() {
        @Override
        public void run() {
//            synchronized (lock) {
//                if (isCapturePic) {
//                    handleCapturePic();
//                }
//            }
            handleCapturePic();
        }
    };

    private void handleCapturePic() {

        synchronized(bufferLock){
            Log.i(TAG, "handleCapturePic begin ... ");

            try {
                //savePic
                byte[] buffer = byteBuffer.array();
                Log.i(TAG, "handleCapturePic buffer length : " + buffer.length);
                if (bitmap != null) {
                    bitmap.copyPixelsFromBuffer(byteBuffer);
                    Util.pushImageToSdCard("test.jpg", bitmap);
                }

//        if (bitmap != null) {
//            bitmap.getPixels(colorValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
//            handleColorRgbs();
//            bitmap.recycle();
//        }
            } catch (Exception e) {
                Log.i(TAG, "handleCapturePic error !!! ", e);
            }
            Log.i(TAG, "handleCapturePic end ... ");
        }


    }


    public CameraEngage(CameraGLSurfaceView glSurfaceView, SurfaceTexture texture) {
        super(glSurfaceView, texture);
        colorValues = new int[FINAL_SIZE * FINAL_SIZE];
        float[] floatValues = new float[FINAL_SIZE * FINAL_SIZE * 3];
        floatBuffer = FloatBuffer.wrap(floatValues, 0, FINAL_SIZE * FINAL_SIZE * 3);
    }

    @Override
    public void openCamera(int width, int height) {
        Log.i("dhb", "test step 4");
        startCapturePic();
    }

    boolean facingFrontPreview() {
        return mFacingFront;
    }

    public void setFacingFront(boolean facingFront) {
        this.mFacingFront = facingFront;
        onResume();
    }


    private void handleColorRgbs() {
        floatBuffer.rewind();
        for (int i = 0; i < colorValues.length; i++) {
            int value = colorValues[i];
            floatBuffer.put((((value >> 16) & 0xFF) - 128f)/ 128f);
            floatBuffer.put((((value >> 8) & 0xFF) - 128f) / 128f);
            floatBuffer.put(((value & 0xFF) - 128f) / 128f);
        }

        if (model == ModelType.MOBILE_NET_V1.getValue() || model == ModelType.MOBILE_NET_V2.getValue() )  {
            AppModel.instance.maceMobilenetClassify(floatBuffer.array());
        } else if (model == ModelType.SEMANTIC_SEGMENT_NET.getValue()) {
            AppModel.instance.maceDeepLabv3Segment(floatBuffer.array());
        }
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume now");
    }

    //create handle frame thread
    private void startCapturePic() {
        mBackgroundHandlerThread = new HandlerThread("captureBackground");
        mBackgroundHandlerThread.start();
        mBackgroundHandler = new Handler(mBackgroundHandlerThread.getLooper());
//        synchronized (lock) {
//            isCapturePic = true;
//        }
    }

    private void stopBackgroundThread() {
        try {
            synchronized (lock) {
                mBackgroundHandlerThread.quitSafely();
                mBackgroundHandlerThread.join();
                mBackgroundHandler = null;
                mBackgroundHandlerThread = null;
                isCapturePic = false;
            }
        } catch (Exception e) {
            Log.e(this.getClass().getName(), "stopBackgroundThread" + e);
        }
    }

    @Override
    public void onPause() {
        closeCamera();
        stopBackgroundThread();
    }

    public void setByteBuffer(ByteBuffer byteBuffer) {
        synchronized (bufferLock) {
            this.byteBuffer = byteBuffer;
        }
    }

    public void signalHandleFrame() {
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(getmPreviewWidth(), getmPreviewHeight(), Bitmap.Config.ARGB_8888);
        }
        synchronized (lock) {
            mBackgroundHandler.post(mHandleCapturePicRunnable);
        }
    }
}
