package com.peterae86.copy.style;

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

    public String get(StyleType type) {
        return styleMap.get(type);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<StyleType, String> entry : styleMap.entrySet()) {
            sb.append(entry.getKey().getName()).append(":").append(entry.getValue()).append(";");
        }
        return sb.toString();
    }
}

