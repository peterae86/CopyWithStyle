package com.backkoms.copy.style;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xiaorui.guo on 2016/6/23.
 */
public class HtmlStyle {
    private boolean isBefore = false;
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

    public boolean isEmpty() {
        return styleMap.size() == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HtmlStyle htmlStyle = (HtmlStyle) o;

        if (isBefore != htmlStyle.isBefore) return false;
        return styleMap != null ? styleMap.equals(htmlStyle.styleMap) : htmlStyle.styleMap == null;

    }

    @Override
    public int hashCode() {
        int result = (isBefore ? 1 : 0);
        result = 31 * result + (styleMap != null ? styleMap.hashCode() : 0);
        return result;
    }

    public boolean isBefore() {
        return isBefore;
    }

    public void setBefore(boolean before) {
        isBefore = before;
    }
}

