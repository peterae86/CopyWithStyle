package com.peterae86.copy.style;

import com.google.common.escape.Escaper;
import com.google.common.html.HtmlEscapers;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.openapi.editor.impl.DocumentImpl;
import com.intellij.openapi.editor.impl.EditorFilteringMarkupModelEx;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xiaorui.guo on 2016/6/23.
 */
public class DocumentStyleParser {
    private static Escaper escaper = HtmlEscapers.htmlEscaper();
    private HtmlStyle defalutStyle;
    Map<TextRange, HtmlStyle> keywordStyle = new HashMap<>();
    Map<TextRange, HtmlStyle> syntaxStyle = new HashMap<>();
    List<List<Pair<TextRange, String>>> textLines = new ArrayList<>();

    public DocumentStyleParser(Editor editor) {
        parseDefaultStyle(editor);
        parseCodeAndStyle(editor);
    }

    private void parseDefaultStyle(Editor editor) {
        defalutStyle = new HtmlStyle();
        EditorColorsScheme colorsScheme = editor.getColorsScheme();
        defalutStyle.add(StyleType.BACKGROUND, color2String(colorsScheme.getDefaultBackground()));
        defalutStyle.add(StyleType.FOREGROUND, color2String(colorsScheme.getDefaultForeground()));
        defalutStyle.add(StyleType.SIZE, colorsScheme.getEditorFontSize() + "");
        defalutStyle.add(StyleType.LINE_SPACING, String.valueOf(colorsScheme.getLineSpacing()));
        defalutStyle.add(StyleType.FONT, colorsScheme.getFontPreferences().toString());
    }

    private void parseCodeAndStyle(Editor editor) {
        EditorImpl editorImpl = (EditorImpl) editor;
        DocumentImpl document = (DocumentImpl) editorImpl.getDocument();
        EditorFilteringMarkupModelEx filteredDocumentMarkupModel = (EditorFilteringMarkupModelEx) editorImpl.getFilteredDocumentMarkupModel();


        HighlighterIterator iterator = editorImpl.getHighlighter().createIterator(0);
        List<Pair<TextRange, String>> textLine = new ArrayList<>();
        textLines.add(textLine);
        while (!iterator.atEnd()) {
            TextAttributes textAttributes = iterator.getTextAttributes();
            int start = iterator.getStart();
            int end = iterator.getEnd();
            TextRange textRange = new TextRange(start, end);
            String text = document.getText(textRange);
            if (text.startsWith("\n")) {
                textLine = new ArrayList<>();
                textLines.add(textLine);
            }
            textLine.add(Pair.create(textRange, text));
            if (text.endsWith("\n")) {
                textLine = new ArrayList<>();
                textLines.add(textLine);
            }
            if (textAttributes.getForegroundColor() != null) {
                if (!keywordStyle.containsKey(textRange)) {
                    keywordStyle.put(textRange, new HtmlStyle());
                }
                keywordStyle.get(textRange).add(StyleType.FOREGROUND, color2String(textAttributes.getForegroundColor()));
            }
            iterator.advance();
        }


        RangeHighlighter[] allHighlighters = filteredDocumentMarkupModel.getDelegate().getAllHighlighters();
        for (RangeHighlighter allHighlighter : allHighlighters) {
            TextRange textRange = new TextRange(allHighlighter.getStartOffset(), allHighlighter.getEndOffset());
            TextAttributes textAttributes = allHighlighter.getTextAttributes();
            if (textAttributes != null && textAttributes.getForegroundColor() != null) {
                if (!syntaxStyle.containsKey(textRange)) {
                    syntaxStyle.put(textRange, new HtmlStyle());
                }
                syntaxStyle.get(textRange).add(StyleType.FOREGROUND, color2String(textAttributes.getForegroundColor()));
            }
        }
    }

    public String getHtmlContent(int startLine, int endLine, HtmlStyleCombiner htmlStyleCombiner) {
        StringBuilder sb = new StringBuilder();
        if (startLine > endLine || startLine < 0 || endLine >= textLines.size()) {
            return "error line";
        }
        sb.append(String.format("<div style=\"%s\">\n", defalutStyle.toString()));
        for (List<Pair<TextRange, String>> line : textLines.subList(startLine, endLine + 1)) {
            sb.append("<p>\n");
            for (Pair<TextRange, String> text : line) {
                sb.append(String.format("<span style=\"%s\">",
                        htmlStyleCombiner.combine(keywordStyle.get(text.getFirst()), syntaxStyle.get(text.getFirst()))));
                sb.append(escaper.escape(text.getSecond()).replace(" ","&ensp;").replace("\n", ""));
                sb.append("</span>");
            }
            sb.append("</p>\n");
        }
        sb.append("</div>\n");
        return sb.toString();
    }

    private String color2String(Color color) {
        String R = Integer.toHexString(color.getRed());
        R = R.length() < 2 ? ('0' + R) : R;
        String B = Integer.toHexString(color.getBlue());
        B = B.length() < 2 ? ('0' + B) : B;
        String G = Integer.toHexString(color.getGreen());
        G = G.length() < 2 ? ('0' + G) : G;
        return '#' + R + G + B;
    }
}
