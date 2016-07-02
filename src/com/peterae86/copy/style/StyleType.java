package com.peterae86.copy.style;

/**
 * Created by xiaorui.guo on 2016/6/23.
 */
public enum StyleType {
    LINE_SPACING("line-height"),
    FONT("font-family"),
    BACKGROUND("background-color"),
    FOREGROUND("color"),
    SIZE("font-size"),
    WAVE_UNDERSCORE("text-decoration"),
    LINE_UNDERSCORE("border-bottom"),
    HEIGHT("height"),
    MARGIN("margin"),
    PADDING("padding"),
    DISPLAY("display"),
    POSITION("position"),
    CONTENT("content"),
    WIDTH("width"),
    TOP("top"),
    OVER_FLOW("overflow"),
    FONT_TYPE("font-style"),
    VERTICAL_ALIGN("vertical-align"), WHITE_SPACE("white-space");

    private String name;

    StyleType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
