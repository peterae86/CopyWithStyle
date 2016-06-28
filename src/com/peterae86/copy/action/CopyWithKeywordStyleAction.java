package com.peterae86.copy.action;

import com.intellij.openapi.editor.Editor;
import com.peterae86.copy.style.DocumentStyleParser;
import com.peterae86.copy.style.HtmlStyle;
import com.peterae86.copy.style.HtmlStyleCombiner;

public class CopyWithKeywordStyleAction extends BaseAction {

    @Override
    String getContent(Editor editor, int startLine, int endLine) {
        return new DocumentStyleParser(editor).getHtmlContent(startLine, endLine, new HtmlStyleCombiner() {
            @Override
            public HtmlStyle combine(HtmlStyle keyword, HtmlStyle syntax) {
                return keyword;
            }
        });
    }
}