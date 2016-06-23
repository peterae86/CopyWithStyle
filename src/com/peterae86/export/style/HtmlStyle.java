package com.peterae86.export.style;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xiaorui.guo on 2016/6/23.
 */
public class HtmlStyle {
    private Map<StyleType, String> styleMap;

    public HtmlStyle() {
        this.styleMap = new HashMap<>();
    }

    public HtmlStyle add(StyleType type, String value) {
        styleMap.put(type, value);
        return this;
    }

    public String toString() {
        return "";
    }
}

