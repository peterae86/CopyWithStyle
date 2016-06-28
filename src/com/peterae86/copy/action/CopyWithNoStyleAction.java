package com.peterae86.copy.action;

import com.intellij.openapi.editor.Editor;
import com.peterae86.copy.style.DocumentStyleParser;
import com.peterae86.copy.style.HtmlStyle;
import com.peterae86.copy.style.HtmlStyleCombiner;

public class CopyWithNoStyleAction extends BaseAction {

    @Override
    String getContent(Editor editor, int startLine, int endLine) {
        String res = new DocumentStyleParser(editor).getHtmlContent(startLine, endLine, new HtmlStyleCombiner() {
            @Override
            public HtmlStyle combine(HtmlStyle keyword, HtmlStyle syntax) {
                return new HtmlStyle();
            }
        });
        return res.replaceFirst("<div.*?>", "<div>");
    }
}