package com.xiaomi.mace.demo;

/**
 * Created by dhb on 2018/10/29.
 */

public enum ViewModeEnum {

    TEXTURE_VIEW("texture_view", 0),
    GLSURFACE_VIEW("glsurface_view", 1);

    private String name;
    private int value;

    ViewModeEnum(String name, int value) {
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
        for (ViewModeEnum model : ViewModeEnum.values()) {
            if (name.equalsIgnoreCase(model.getName())) {
                return model.getValue();
            }
        }
        return 0;
    }
}
