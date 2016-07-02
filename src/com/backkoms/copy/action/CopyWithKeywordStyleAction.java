package com.backkoms.copy.action;

import com.intellij.openapi.editor.Editor;
import com.backkoms.copy.style.DocumentStyleParser;

public class CopyWithKeywordStyleAction extends BaseAction {

    @Override
    String getContent(Editor editor, int startLine, int endLine) {
        return new DocumentStyleParser(editor, startLine, endLine).getHtmlContent(100);
    }
}