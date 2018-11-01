package com.xiaomi.mace.demo.result;

/**
 * Created by mtime on 2018/10/25.
 */

public class DeepLibV3ResData extends ResultData {
    int[] img;


    public DeepLibV3ResData() {
    }


    public DeepLibV3ResData(String name, float probability) {
        super(name, probability);
    }

    public DeepLibV3ResData(String name) {
        super(name);
    }

    public void updateData(int[] data) {
        img = data;
    }

    public int[] getImg() {
        return img;
    }
}
