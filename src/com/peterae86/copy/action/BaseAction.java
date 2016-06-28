package com.peterae86.copy.action;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.editor.VisualPosition;
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
                VisualPosition startPosition = selectionModel.getSelectionStartPosition();
                VisualPosition endPosition = selectionModel.getSelectionEndPosition();
                if (startPosition == null || endPosition == null) {
                    return;
                }
                String content = getContent(editor, startPosition.getLine(), endPosition.getLine());
                Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                Transferable tText = new StringSelection(content);
                systemClipboard.setContents(tText, null);
            }
        });
    }

    abstract String getContent(Editor editor, int startLine, int endLine);
}
