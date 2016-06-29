package com.peterae86.copy.action;

import com.intellij.openapi.editor.Editor;
import com.peterae86.copy.style.DocumentStyleParser;

public class CopyWithNoStyleAction extends BaseAction {

    @Override
    String getContent(Editor editor, int startLine, int endLine) {
        String res = new DocumentStyleParser(editor).getHtmlContent(startLine, endLine, 0);
        return res.replaceFirst("<div.*?>", "<div>");
    }
}