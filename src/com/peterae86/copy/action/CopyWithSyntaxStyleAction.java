package com.peterae86.copy.action;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.text.StringUtil;
import com.peterae86.copy.style.DocumentStyleParser;
import com.peterae86.copy.style.HtmlStyle;
import com.peterae86.copy.style.HtmlStyleCombiner;
import com.peterae86.copy.style.StyleType;

/**
 * Created by xiaorui.guo on 2016/6/28.
 */
public class CopyWithSyntaxStyleAction extends BaseAction {

    @Override
    String getContent(Editor editor, int startLine, int endLine) {
        return new DocumentStyleParser(editor).getHtmlContent(startLine, endLine, new HtmlStyleCombiner() {
            @Override
            public HtmlStyle combine(HtmlStyle keyword, HtmlStyle syntax) {
                if(syntax==null){
                    return keyword;
                }
                HtmlStyle res = new HtmlStyle();
                if (StringUtil.isEmpty(syntax.get(StyleType.FOREGROUND))) {
                    res.add(StyleType.FOREGROUND, keyword.get(StyleType.FOREGROUND));
                } else {
                    res.add(StyleType.FOREGROUND, syntax.get(StyleType.FOREGROUND));
                }
                return res;
            }
        });
    }
}
