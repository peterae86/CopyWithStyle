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
import java.util.function.IntFunction;

/**
 * Created by xiaorui.guo on 2016/6/23.
 */
public class DocumentStyleParser {
    private static Escaper escaper = HtmlEscapers.htmlEscaper();
    private static Joiner joiner = Joiner.on(",");
    private HtmlStyle defalutStyle;

    Map<HtmlStyle, Set<String>> styleLayer2000 = new HashMap<>();
    Map<HtmlStyle, Set<String>> styleLayer3000 = new HashMap<>();
    Map<HtmlStyle, Set<String>> styleLayer4000 = new HashMap<>();

    private Integer[] codeIntervalStartPoints;
    private List<List<Pair<TextRange, String>>> textLines = new ArrayList<>();


    public DocumentStyleParser(Editor editor) {
        parseDefaultStyle(editor);
        parseCodeInterval(editor);
        parseStyle(editor);
    }

    private void parseCodeInterval(Editor editor) {
        EditorImpl editorImpl = (EditorImpl) editor;
        DocumentImpl document = (DocumentImpl) editorImpl.getDocument();

        HighlighterIterator iterator = editorImpl.getHighlighter().createIterator(0);
        List<Pair<TextRange, String>> textLine = new ArrayList<>();
        textLines.add(textLine);
        List<Integer> startPoints = new ArrayList<>();
        while (!iterator.atEnd()) {
            int start = iterator.getStart();
            int end = iterator.getEnd();
            startPoints.add(start);
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
            iterator.advance();
        }
        codeIntervalStartPoints = startPoints.stream().toArray(Integer[]::new);
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

    private void parseStyle(Editor editor) {
        EditorImpl editorImpl = (EditorImpl) editor;
        DocumentImpl document = (DocumentImpl) editorImpl.getDocument();
        EditorFilteringMarkupModelEx filteredDocumentMarkupModel = (EditorFilteringMarkupModelEx) editorImpl.getFilteredDocumentMarkupModel();


        HighlighterIterator iterator = editorImpl.getHighlighter().createIterator(0);
        while (!iterator.atEnd()) {
            TextAttributes textAttributes = iterator.getTextAttributes();
            int start = iterator.getStart();
            if (textAttributes.getForegroundColor() != null) {
                HtmlStyle htmlStyle = new HtmlStyle();
                htmlStyle.add(StyleType.FOREGROUND, color2String(textAttributes.getForegroundColor()));
                if (!styleLayer2000.containsKey(htmlStyle)) {
                    styleLayer2000.put(htmlStyle, new HashSet<>());
                }
                styleLayer2000.get(htmlStyle).add(".code_" + start);
            }
            iterator.advance();
        }

        RangeHighlighter[] allHighlighters = filteredDocumentMarkupModel.getAllHighlighters();

        for (RangeHighlighter highlighter : allHighlighters) {
            TextAttributes textAttributes = highlighter.getTextAttributes();
            if (textAttributes != null) {
                HtmlStyle htmlStyle = new HtmlStyle();
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
                Map<HtmlStyle, Set<String>> map;
                if (highlighter.getLayer() == 3000) {
                    map = styleLayer3000;
                } else {
                    map = styleLayer4000;
                }
                if (!map.containsKey(htmlStyle)) {
                    map.put(htmlStyle, new HashSet<>());
                }
                int start = Arrays.binarySearch(codeIntervalStartPoints, highlighter.getStartOffset());
                if (start < 0) {
                    start = -start - 1;
                }
                int end = Arrays.binarySearch(codeIntervalStartPoints, highlighter.getEndOffset());
                if (end < 0) {
                    end = -end - 1;
                }
                for (int i = start; i < end; i++) {
                    map.get(htmlStyle).add(".code_" + i);
                }
            }
        }
    }

    public static void main(String[] args) {
        System.out.println(Arrays.binarySearch(new int[]{1, 3, 5, 7, 9}, 4));
    }

    public String getHtmlContent(int startLine, int endLine) {
        StringBuilder sb = new StringBuilder();
        if (startLine > endLine || startLine < 0 || endLine >= textLines.size()) {
            return "error line";
        }
        sb.append("<div>\n");
        sb.append("<style>\n");
        sb.append(String.format(".line{%s}\n", defalutStyle));
        for (Map.Entry<HtmlStyle, Set<String>> entry : styleLayer2000.entrySet()) {
            sb.append(String.format("%s{%s}\n", joiner.join(entry.getValue()), entry.getKey()));
        }
        sb.append("</style>\n");
        for (List<Pair<TextRange, String>> line : textLines.subList(startLine, endLine + 1)) {
            sb.append("<p class=\"line\">\n");
            for (Pair<TextRange, String> text : line) {
                sb.append(String.format("<span class=\"code_%s\">", text.getFirst().getStartOffset()));
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
