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

import android.graphics.SurfaceTexture;
import android.os.Build;
import android.view.View;

import com.xiaomi.mace.demo.camera.CameraApiLessM;
import com.xiaomi.mace.demo.camera.CameraTextureView;
import com.xiaomi.mace.demo.camera.Engage;
import com.xiaomi.mace.demo.camera.GL.CameraApiNew;
import com.xiaomi.mace.demo.camera.GL.CameraGLSurfaceView;

public class CameraFactory {

    public static Engage genCameEngage(View view, int mode) {
        Engage cameraEngage = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (mode == ViewModeEnum.TEXTURE_VIEW.getValue()) {
                cameraEngage = new CameraApiLessM((CameraTextureView) view);
            } else if (mode == ViewModeEnum.GLSURFACE_VIEW.getValue()) {
                SurfaceTexture texture = ((CameraGLSurfaceView)view).getSurfaceTexture();
                cameraEngage = new CameraApiNew((CameraGLSurfaceView) view, texture);
            }
        } else {
            if (mode == ViewModeEnum.TEXTURE_VIEW.getValue()) {
                cameraEngage = new CameraApiLessM((CameraTextureView) view);
            } else if (mode == ViewModeEnum.GLSURFACE_VIEW.getValue()) {
                SurfaceTexture texture = ((CameraGLSurfaceView)view).getSurfaceTexture();
                cameraEngage = new CameraApiNew((CameraGLSurfaceView) view, texture);
            }
        }
        return cameraEngage;
    }
}
