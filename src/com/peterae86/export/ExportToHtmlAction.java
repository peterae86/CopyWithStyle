package com.peterae86.export;

import com.google.common.escape.Escaper;
import com.google.common.html.HtmlEscapers;
import com.intellij.codeInsight.highlighting.HighlightUsagesHandler;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.FontPreferences;
import com.intellij.openapi.editor.ex.DocumentEx;
import com.intellij.openapi.editor.ex.MarkupModelEx;
import com.intellij.openapi.editor.ex.RangeHighlighterEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.openapi.editor.impl.DocumentImpl;
import com.intellij.openapi.editor.impl.EditorFilteringMarkupModelEx;
import com.intellij.openapi.editor.impl.EditorHighlighterCache;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.editor.impl.view.EditorView;
import com.intellij.openapi.editor.markup.*;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xiaorui.guo on 2016/6/3.
 */
public class ExportToHtmlAction extends EditorAction {


    protected ExportToHtmlAction() {
        this(true);
    }

    private <T> T getField(Object obj, String name) {
        try {
            Field field = obj.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return (T) field.get(obj);
        } catch (Exception e) {
            try {
                Field field = obj.getClass().getSuperclass().getDeclaredField(name);
                field.setAccessible(true);
                return (T) field.get(obj);
            } catch (Exception e1) {
                throw new RuntimeException(e1);
            }
        }
    }

    static enum StyleKey {
        Color,
        Size,

    }

    public ExportToHtmlAction(boolean setupHandler) {
        super(null);
        this.setupHandler(new EditorActionHandler() {
            @Override
            protected void doExecute(Editor editor, @Nullable Caret caret, DataContext dataContext) {
                final SelectionModel selectionModel = editor.getSelectionModel();
                EditorImpl editorImpl = (EditorImpl) editor;
                DocumentImpl document = (DocumentImpl) editorImpl.getDocument();
                EditorFilteringMarkupModelEx filteredDocumentMarkupModel = (EditorFilteringMarkupModelEx) editorImpl.getFilteredDocumentMarkupModel();
                Map<TextRange, Map<StyleKey, String>> styleMap = new HashMap<>();

                Escaper escaper = HtmlEscapers.htmlEscaper();
                HighlighterIterator iterator = editorImpl.getHighlighter().createIterator(0);
                while (!iterator.atEnd()) {
                    TextAttributes textAttributes = iterator.getTextAttributes();
                    int start = iterator.getStart();
                    int end = iterator.getEnd();
                    if (textAttributes.getForegroundColor() != null) {
                        if (!styleMap.containsKey(new TextRange(start, end))) {
                            styleMap.put(new TextRange(start, end), new HashMap<>());
                        }
                        styleMap.get(new TextRange(start, end)).put(StyleKey.Color, color2String(textAttributes.getForegroundColor()));
                    }
                    iterator.advance();
                }

                RangeHighlighter[] allHighlighters = filteredDocumentMarkupModel.getDelegate().getAllHighlighters();

                for (RangeHighlighter allHighlighter : allHighlighters) {
                    TextRange textRange = new TextRange(allHighlighter.getStartOffset(), allHighlighter.getEndOffset());
                    TextAttributes textAttributes = allHighlighter.getTextAttributes();
                    if (textAttributes != null && textAttributes.getForegroundColor() != null) {
                        if (!styleMap.containsKey(textRange)) {
                            styleMap.put(textRange, new HashMap<>());
                        }
                        styleMap.get(textRange).put(StyleKey.Color, color2String(textAttributes.getForegroundColor()));
                    }
                }


                StringBuilder sb = new StringBuilder();
                EditorColorsScheme colorsScheme = editor.getColorsScheme();
                colorsScheme.getDefaultBackground();
                colorsScheme.getDefaultForeground();
                colorsScheme.getEditorFontSize();
                float lineSpacing = colorsScheme.getLineSpacing();
                FontPreferences fontPreferences = colorsScheme.getFontPreferences();

                editorImpl.getFontSize();
                editorImpl.getLineHeight();
                sb.append(String.format("<div style=\"line-height: %s;font-family: '%s';font-size: %spx;color: %s;background-color: %s\">\n"
                        , lineSpacing, fontPreferences.getFontFamily(), colorsScheme.getEditorFontSize(), color2String(colorsScheme.getDefaultForeground()), color2String(colorsScheme.getDefaultBackground())));
                sb.append("<p>\n");
                iterator = editorImpl.getHighlighter().createIterator(0);
                while (!iterator.atEnd()) {
                    int start = iterator.getStart();
                    int end = iterator.getEnd();
                    TextRange textRange = new TextRange(start, end);
                    String text = document.getText(textRange);
                    String color = null;
                    if (styleMap.get(textRange) != null) {
                        color = styleMap.get(textRange).get(StyleKey.Color);
                    }
                    if (text.startsWith("\n")) {
                        sb.append("</p><p>\n");
                    }
                    if (color != null) {
                        sb.append("<span style=\"" + "color:" + color + ";\">");
                    } else {
                        sb.append("<span>");
                    }
                    sb.append(escaper.escape(text).replace(" ","&ensp;").replace("\n", "")).append("</span>");


                    if (text.endsWith("\n") && !text.equals("\n")) {
                        sb.append("</p><p>\n");
                    }
                    iterator.advance();
                }
                sb.append("</p>\n");
                sb.append("</div>\n");
                System.out.println(sb.toString());
            }
        });
    }


    public static String color2String(Color color) {
        String R = Integer.toHexString(color.getRed());
        R = R.length() < 2 ? ('0' + R) : R;
        String B = Integer.toHexString(color.getBlue());
        B = B.length() < 2 ? ('0' + B) : B;
        String G = Integer.toHexString(color.getGreen());
        G = G.length() < 2 ? ('0' + G) : G;
        return '#' + R + G + B;
    }
}
