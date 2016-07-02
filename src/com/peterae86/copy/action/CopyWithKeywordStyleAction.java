package com.peterae86.copy.action;

import com.intellij.openapi.editor.Editor;
import com.peterae86.copy.style.DocumentStyleParser;

public class CopyWithKeywordStyleAction extends BaseAction {

    @Override
    String getContent(Editor editor, int startLine, int endLine) {
        return new DocumentStyleParser(editor, startLine, endLine).getHtmlContent(2000);
    }
}