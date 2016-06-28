package com.peterae86.copy.style;

/**
 * Created by xiaorui.guo on 2016/6/23.
 */
public enum StyleType {
    LINE_SPACING("line-height"),
    FONT("font-family"),
    BACKGROUND("background-color"),
    FOREGROUND("color"),
    SIZE("font-size"),;

    private String name;

    StyleType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
