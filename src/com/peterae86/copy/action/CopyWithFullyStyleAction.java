package com.peterae86.copy.action;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.text.StringUtil;
import com.peterae86.copy.style.DocumentStyleParser;
import com.peterae86.copy.style.HtmlStyle;
import com.peterae86.copy.style.HtmlStyleCombiner;
import com.peterae86.copy.style.StyleType;

public class CopyWithFullyStyleAction extends BaseAction {

    @Override
    String getContent(Editor editor, int startLine, int endLine) {
        return new DocumentStyleParser(editor).getHtmlContent(startLine, endLine, new HtmlStyleCombiner() {
            @Override
            public HtmlStyle combine(HtmlStyle keyword, HtmlStyle syntax) {
                if (StringUtil.isEmpty(syntax.get(StyleType.FOREGROUND))) {
                    syntax.add(StyleType.FOREGROUND, keyword.get(StyleType.FOREGROUND));
                }
                return syntax;
            }
        });
    }
}