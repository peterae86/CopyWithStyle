package com.backkoms.copy.action;

import com.intellij.openapi.editor.Editor;
import com.backkoms.copy.style.DocumentStyleParser;

/**
 * Created by xiaorui.guo on 2016/6/28.
 */
public class CopyWithSyntaxStyleAction extends BaseAction {

    @Override
    String getContent(Editor editor, int startLine, int endLine) {
        return new DocumentStyleParser(editor, startLine, endLine).getHtmlContent(3000);
    }
}
