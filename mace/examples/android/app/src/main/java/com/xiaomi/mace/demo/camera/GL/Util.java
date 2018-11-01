package com.xiaomi.mace.demo.camera.GL;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by dhb on 2018/10/29.
 */

public class Util {
    public static Bitmap getImageFromAssetsFile(Context context, String fileName){
        Bitmap image = null;
        AssetManager am = context.getResources().getAssets();
        try{
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        return image;
    }

    public static boolean pushImageToSdCard(String fileName, Bitmap img) {
        boolean isOk = true;
        String sdCardDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        String filePath = sdCardDir + "/" + fileName;
        Log.i("Util", "save file to path : " + filePath);
        File filePic = null;
        try {
            filePic = new File(filePath);
            if (!filePic.exists()) {
                filePic.getParentFile().mkdirs();
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            img.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return isOk;
    }

}
