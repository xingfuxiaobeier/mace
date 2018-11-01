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

package com.xiaomi.mace.demo;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.xiaomi.mace.JniMaceUtils;
import com.xiaomi.mace.demo.camera.MessageEvent;
import com.xiaomi.mace.demo.result.DeepLibV3ResData;
import com.xiaomi.mace.demo.result.InitData;
import com.xiaomi.mace.demo.result.LabelCache;
import com.xiaomi.mace.demo.result.ResultData;

import org.greenrobot.eventbus.EventBus;

public class AppModel {

    private Handler mJniThread;
    public static AppModel instance = new AppModel();
    private AppModel() {
        HandlerThread thread = new HandlerThread("jniThread");
        thread.start();
        mJniThread = new Handler(thread.getLooper());
    }


    //classification
    public void maceMobilenetSetAttrs(final InitData initData) {
        mJniThread.post(new Runnable() {
            @Override
            public void run() {
                int result = JniMaceUtils.maceMobilenetSetAttrs(
                        initData.getOmpNumThreads(), initData.getCpuAffinityPolicy(),
                        initData.getGpuPerfHint(), initData.getGpuPriorityHint(),
                        initData.getKernelPath());
                Log.i("APPModel", "maceMobilenetSetAttrs result = " + result);
            }
        });
    }

    public void maceMobilenetCreateEngine(final InitData initData) {
        mJniThread.post(new Runnable() {
            @Override
            public void run() {
                JniMaceUtils.maceMobilenetCreateEngine(initData.getModel(), initData.getDevice());
            }
        });
    }

    public void maceMobilenetClassify(final float[] input) {
        mJniThread.post(new Runnable() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                float[] result = JniMaceUtils.maceMobilenetClassify(input);
                Log.i("APPModel", "maceMobilenetClassify length : " + result.length);
                printResArray(result);

                final ResultData resultData = LabelCache.instance().getResultFirst(result);
                resultData.costTime = System.currentTimeMillis() - start;
                EventBus.getDefault().post(new MessageEvent.MaceResultEvent(resultData));
            }
        });
    }

    private void printResArray(float[] result) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < result.length; i++) {
            if (i != result.length - 1) {
                builder.append(result[i] + ",");
            } else {
                builder.append(result[i]);
            }
        }
        builder.append("]");

        Log.i("APPModel", "mace model res : " + builder.toString());
    }


    //semantic segment
    public void maceDeepLabv3SetAttrs(final InitData initData) {
        mJniThread.post(new Runnable() {
            @Override
            public void run() {
                int result = JniMaceUtils.maceDeepLibnetSetAttrs(
                        initData.getOmpNumThreads(), initData.getCpuAffinityPolicy(),
                        initData.getGpuPerfHint(), initData.getGpuPriorityHint(),
                        initData.getKernelPath());
                Log.i("APPModel", "maceDeepLabv3SetAttrs result = " + result);
            }
        });
    }

    public void maceDeepLabv3CreateEngine(final InitData initData) {
        mJniThread.post(new Runnable() {
            @Override
            public void run() {
                Log.i("APPModel", "maceDeepLabv3CreateEngine create engine begin ... ");
                int res = JniMaceUtils.maceDeepLibnetCreateEngine(initData.getModel(), initData.getDevice());
                Log.i("APPModel", "maceDeepLabv3CreateEngine create engine result : " + res);
            }
        });
    }

    public void maceDeepLabv3Segment(final float[] input) {
        mJniThread.post(new Runnable() {
            @Override
            public void run() {
                Log.i("APPModel", "maceDeepLabv3Segment execute segment begin ... ");
                long start = System.currentTimeMillis();
                float[] result = JniMaceUtils.maceDeepLibnetSegment(input);
                if (result != null) {
                    Log.i("APPModel", "maceDeepLabv3Segment length : " + result.length);
//                    printResArray(result);
                }

                DeepLibV3ResData resultData = LabelCache.instance().getDeepLibResData(result, 65, 65, 21);
                printImageData(resultData.getImg());
                resultData.costTime = System.currentTimeMillis() - start;
                EventBus.getDefault().post(new MessageEvent.MaceResultEvent(resultData));
                Log.i("APPModel", "maceDeepLabv3Segment execute segment end ... ");
            }
        });
    }

    private void printImageData(int[] img) {
        Log.i("APPModel", "image data length : " + img.length);
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < img.length; i++) {
            if (i != img.length - 1) {
                builder.append(img[i] + ",");
            } else {
                builder.append(img[i] + "]");
            }

        }

        Log.i("APPModel", "image data : " + builder.toString());

    }

}
