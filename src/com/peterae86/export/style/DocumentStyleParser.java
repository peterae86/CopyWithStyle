package com.peterae86.export.style;

import com.google.common.escape.Escaper;
import com.google.common.html.HtmlEscapers;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.openapi.editor.impl.DocumentImpl;
import com.intellij.openapi.editor.impl.EditorFilteringMarkupModelEx;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.util.TextRange;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by xiaorui.guo on 2016/6/23.
 */
public class DocumentStyleParser {
    private static Escaper escaper = HtmlEscapers.htmlEscaper();
    private HtmlStyle defalutStyle;
    Map<TextRange, HtmlStyle> keywordStyle = new HashMap<>();
    Map<TextRange, HtmlStyle> syntaxStyle = new HashMap<>();

    public DocumentStyleParser(Editor editor) {
        parseDefaultStyle(editor);
        parseCodeAndStyle(editor);
    }

    private void parseDefaultStyle(Editor editor) {

    }

    private void parseCodeAndStyle(Editor editor) {
        EditorImpl editorImpl = (EditorImpl) editor;
        DocumentImpl document = (DocumentImpl) editorImpl.getDocument();
        EditorFilteringMarkupModelEx filteredDocumentMarkupModel = (EditorFilteringMarkupModelEx) editorImpl.getFilteredDocumentMarkupModel();

        HighlighterIterator iterator = editorImpl.getHighlighter().createIterator(0);
        while (!iterator.atEnd()) {
            TextAttributes textAttributes = iterator.getTextAttributes();
            int start = iterator.getStart();
            int end = iterator.getEnd();
            if (textAttributes.getForegroundColor() != null) {
                if (!keywordStyle.containsKey(new TextRange(start, end))) {
                    keywordStyle.put(new TextRange(start, end), new HashMap<>());
                }
                keywordStyle.get(new TextRange(start, end)).put(StyleType.Color, color2String(textAttributes.getForegroundColor()));
            }
            iterator.advance();
        }


        RangeHighlighter[] allHighlighters = filteredDocumentMarkupModel.getDelegate().getAllHighlighters();
        for (RangeHighlighter allHighlighter : allHighlighters) {
            TextRange textRange = new TextRange(allHighlighter.getStartOffset(), allHighlighter.getEndOffset());
            TextAttributes textAttributes = allHighlighter.getTextAttributes();
            if (textAttributes != null && textAttributes.getForegroundColor() != null) {
                if (!syntaxStyle.containsKey(textRange)) {
                    syntaxStyle.put(textRange, new HashMap<>());
                }
                syntaxStyle.get(textRange).put(StyleType.Color, color2String(textAttributes.getForegroundColor()));
            }
        }
    }

    public String getContent() {
        return "";
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
