package com.peterae86.copy.action;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

/**
 * Created by xiaorui.guo on 2016/6/28.
 */
public abstract class BaseAction extends EditorAction {
    protected BaseAction() {
        super(null);
        this.setupHandler(new EditorActionHandler() {
            @Override
            protected void doExecute(Editor editor, @Nullable Caret caret, DataContext dataContext) {
                SelectionModel selectionModel = editor.getSelectionModel();
                int selectionStart = selectionModel.getSelectionStart();
                int selectionEnd = selectionModel.getSelectionEnd();
                if (selectionStart == selectionEnd) {
                    return;
                }
                Document document = editor.getDocument();
                int startLine = 0;
                int endLine = 0;
                for (int i = 0; i < document.getLineCount(); i++) {
                    int lineStartOffset = document.getLineStartOffset(i);
                    int lineEndOffset = document.getLineEndOffset(i);
                    if (lineStartOffset <= selectionStart && selectionStart <= lineEndOffset) {
                        startLine = i;
                    }
                    if (lineStartOffset <= selectionEnd && selectionEnd <= lineEndOffset) {
                        endLine = i;
                    }
                }
                String content = getContent(editor, startLine, endLine);
                Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                Transferable tText = new StringSelection(content);
                systemClipboard.setContents(tText, null);
            }
        });
    }

    abstract String getContent(Editor editor, int startLine, int endLine);
}
