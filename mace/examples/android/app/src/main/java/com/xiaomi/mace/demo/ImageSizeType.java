package com.xiaomi.mace.demo;

/**
 * @author dhb
 * @date 2018/11/1
 */

public enum ImageSizeType {

    ORIGIN_SIZE("origin_size", 0),
    RESIZE_1("resize_1", 1);

    private String name;
    private int value;

    ImageSizeType(String name, int value) {
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
        for (ImageSizeType v : ImageSizeType.values()) {
            if (name.equalsIgnoreCase(v.getName())) {
                return v.getValue();
            }
        }
        return 0;
    }
}
