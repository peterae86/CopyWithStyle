//package com.peterae86.export;
//
//import com.google.common.escape.Escaper;
//import com.google.common.html.HtmlEscapers;
//import com.intellij.openapi.actionSystem.DataContext;
//import com.intellij.openapi.editor.*;
//import com.intellij.openapi.editor.actionSystem.EditorAction;
//import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
//import com.intellij.openapi.editor.colors.EditorColorsScheme;
//import com.intellij.openapi.editor.colors.FontPreferences;
//import com.intellij.openapi.editor.highlighter.HighlighterIterator;
//import com.intellij.openapi.editor.impl.DocumentImpl;
//import com.intellij.openapi.editor.impl.EditorFilteringMarkupModelEx;
//import com.intellij.openapi.editor.impl.EditorImpl;
//import com.intellij.openapi.editor.markup.*;
//import com.intellij.openapi.util.TextRange;
//import com.peterae86.export.style.StyleKey;
//import org.jetbrains.annotations.Nullable;
//
//import java.awt.*;
//import java.lang.reflect.Field;
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * Created by xiaorui.guo on 2016/6/3.
// */
//public class ExportToHtmlAction extends EditorAction {
//
//
//    protected ExportToHtmlAction() {
//        this(true);
//    }
//
//    private <T> T getField(Object obj, String name) {
//        try {
//            Field field = obj.getClass().getDeclaredField(name);
//            field.setAccessible(true);
//            return (T) field.get(obj);
//        } catch (Exception e) {
//            try {
//                Field field = obj.getClass().getSuperclass().getDeclaredField(name);
//                field.setAccessible(true);
//                return (T) field.get(obj);
//            } catch (Exception e1) {
//                throw new RuntimeException(e1);
//            }
//        }
//    }
//
//    public ExportToHtmlAction(boolean setupHandler) {
//        super(null);
//        this.setupHandler(new EditorActionHandler() {
//            @Override
//            protected void doExecute(Editor editor, @Nullable Caret caret, DataContext dataContext) {
//                final SelectionModel selectionModel = editor.getSelectionModel();
//                EditorImpl editorImpl = (EditorImpl) editor;
//                DocumentImpl document = (DocumentImpl) editorImpl.getDocument();
//                EditorFilteringMarkupModelEx filteredDocumentMarkupModel = (EditorFilteringMarkupModelEx) editorImpl.getFilteredDocumentMarkupModel();
//                Map<TextRange, Map<StyleKey, String>> styleMap = new HashMap<>();
//
//
//
//
//
//                StringBuilder sb = new StringBuilder();
//                EditorColorsScheme colorsScheme = editor.getColorsScheme();
//                colorsScheme.getDefaultBackground();
//                colorsScheme.getDefaultForeground();
//                colorsScheme.getEditorFontSize();
//                float lineSpacing = colorsScheme.getLineSpacing();
//                FontPreferences fontPreferences = colorsScheme.getFontPreferences();
//
//                editorImpl.getFontSize();
//                editorImpl.getLineHeight();
//                sb.append(String.format("<div style=\"line-height: %s;font-family: '%s';font-size: %spx;color: %s;background-color: %s\">\n"
//                        , lineSpacing, fontPreferences.getFontFamily(), colorsScheme.getEditorFontSize(), color2String(colorsScheme.getDefaultForeground()), color2String(colorsScheme.getDefaultBackground())));
//                sb.append("<p>\n");
//                iterator = editorImpl.getHighlighter().createIterator(0);
//                while (!iterator.atEnd()) {
//                    int start = iterator.getStart();
//                    int end = iterator.getEnd();
//                    TextRange textRange = new TextRange(start, end);
//                    String text = document.getText(textRange);
//                    String color = null;
//                    if (styleMap.get(textRange) != null) {
//                        color = styleMap.get(textRange).get(StyleKey.Color);
//                    }
//                    if (text.startsWith("\n")) {
//                        sb.append("</p><p>\n");
//                    }
//                    if (color != null) {
//                        sb.append("<span style=\"" + "color:" + color + ";\">");
//                    } else {
//                        sb.append("<span>");
//                    }
//                    sb.append(escaper.escape(text).replace(" ","&ensp;").replace("\n", "")).append("</span>");
//
//
//                    if (text.endsWith("\n") && !text.equals("\n")) {
//                        sb.append("</p><p>\n");
//                    }
//                    iterator.advance();
//                }
//                sb.append("</p>\n");
//                sb.append("</div>\n");
//                System.out.println(sb.toString());
//            }
//        });
//    }
//
//
//
//}
