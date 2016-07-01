package com.peterae86.copy.action;

import com.intellij.openapi.editor.Editor;
import com.peterae86.copy.style.DocumentStyleParser;

/**
 * Created by xiaorui.guo on 2016/6/28.
 */
public class CopyWithSyntaxStyleAction extends BaseAction {

    @Override
    String getContent(Editor editor, int startLine, int endLine) {
        return new DocumentStyleParser(editor, startLine, endLine).getHtmlContent(startLine, endLine, 3000);
    }
}
