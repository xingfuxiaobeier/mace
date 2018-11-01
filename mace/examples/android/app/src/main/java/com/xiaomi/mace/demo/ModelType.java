package com.xiaomi.mace.demo;

/**
 * Created by dhb on 2018/10/25.
 */
public enum ModelType {

    MOBILE_NET_V1("mobilenet_v1", 0),
    MOBILE_NET_V2("mobilenet_v2", 1),
    SEMANTIC_SEGMENT_NET("deeplab_v3_plus_mobilenet_v2", 2);

    private String name;
    int value;

    ModelType(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    public int getValue() {
        return value;
    }

    public static int getValueByName(String name) {
        for (ModelType model : ModelType.values()) {
            if (name.equalsIgnoreCase(model.getName())) {
                return model.getValue();
            }
        }
        return 0;
    }
}
