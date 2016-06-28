package com.peterae86.copy.style;

import com.google.common.base.Joiner;
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
import java.util.*;

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
        defalutStyle.add(StyleType.FONT, Joiner.on(",").join(colorsScheme.getFontPreferences().getEffectiveFontFamilies()));
        defalutStyle.add(StyleType.HEIGHT, String.valueOf(editor.getLineHeight()));
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


        RangeHighlighter[] allHighlighters = filteredDocumentMarkupModel.getAllHighlighters();
        Arrays.sort(allHighlighters, (o1, o2) -> Integer.compare(o1.getLayer(), o2.getLayer()));

        for (RangeHighlighter allHighlighter : allHighlighters) {
            TextRange textRange = new TextRange(allHighlighter.getStartOffset(), allHighlighter.getEndOffset());
            TextAttributes textAttributes = allHighlighter.getTextAttributes();
            if (textAttributes != null) {
                if (!syntaxStyle.containsKey(textRange)) {
                    syntaxStyle.put(textRange, new HtmlStyle());
                }
                HtmlStyle htmlStyle = syntaxStyle.get(textRange);
                switch (textAttributes.getEffectType()) {
                    case WAVE_UNDERSCORE:
                        htmlStyle.add(StyleType.WAVE_UNDERSCORE, "1px solid " + color2String(textAttributes.getEffectColor()));
                        break;
                }
                if (textAttributes.getForegroundColor() != null) {
                    htmlStyle.add(StyleType.FOREGROUND, color2String(textAttributes.getForegroundColor()));
                }
                if (textAttributes.getBackgroundColor() != null) {
                    htmlStyle.add(StyleType.BACKGROUND, color2String(textAttributes.getBackgroundColor()));
                }
            }
        }
    }

    public String getHtmlContent(int startLine, int endLine, HtmlStyleCombiner htmlStyleCombiner) {
        StringBuilder sb = new StringBuilder();
        if (startLine > endLine || startLine < 0 || endLine >= textLines.size()) {
            return "error line";
        }
        sb.append(String.format("<div style=\"margin:0;padding:0;%s\">\n", defalutStyle.toString()));
        for (List<Pair<TextRange, String>> line : textLines.subList(startLine, endLine + 1)) {
            sb.append("<p style=\"margin:0;padding:0;\">\n");
            for (Pair<TextRange, String> text : line) {
                HtmlStyle combine = htmlStyleCombiner.combine(keywordStyle.get(text.getFirst()), syntaxStyle.get(text.getFirst()));
                sb.append(String.format("<span style=\"%s\">", combine == null ? "" : combine));
                sb.append(escaper.escape(text.getSecond()).replace(" ", "&ensp;").replace("\n", ""));
                sb.append("</span>");
            }
            sb.append("</p>\n");
        }
        sb.append("</div>\n");
        return sb.toString();
    }

    private String color2String(Color color) {
        if (color == null) {
            return "";
        }
        String R = Integer.toHexString(color.getRed());
        R = R.length() < 2 ? ('0' + R) : R;
        String B = Integer.toHexString(color.getBlue());
        B = B.length() < 2 ? ('0' + B) : B;
        String G = Integer.toHexString(color.getGreen());
        G = G.length() < 2 ? ('0' + G) : G;
        return '#' + R + G + B;
    }
}
