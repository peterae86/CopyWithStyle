package com.peterae86.copy.style;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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
    private static Joiner joiner = Joiner.on(",");
    private HtmlStyle defaultStyle;
    private HtmlStyle lineStyle;
    private HtmlStyle spanStyle;

    TreeMap<Integer, Map<HtmlStyle, Set<String>>> styleLayerMap = new TreeMap<>();

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
        EditorColorsScheme colorsScheme = editor.getColorsScheme();
        defaultStyle = new HtmlStyle();
        defaultStyle.add(StyleType.BACKGROUND, color2String(colorsScheme.getDefaultBackground()));
        defaultStyle.add(StyleType.FOREGROUND, color2String(colorsScheme.getDefaultForeground()));
        defaultStyle.add(StyleType.SIZE, colorsScheme.getEditorFontSize() + "px");
        defaultStyle.add(StyleType.FONT, Joiner.on(",").join(colorsScheme.getFontPreferences().getEffectiveFontFamilies()) + ",serif");
        defaultStyle.add(StyleType.MARGIN, " -1px 0 0");
        defaultStyle.add(StyleType.PADDING, "0");
        defaultStyle.add(StyleType._WEBKIT_MARGIN_BEFORE, "0");
        defaultStyle.add(StyleType._WEBKIT_MARGIN_AFTER, "0");
        lineStyle = new HtmlStyle();
        lineStyle.add(StyleType.LINE_SPACING, String.valueOf(colorsScheme.getLineSpacing()));
        lineStyle.add(StyleType.HEIGHT, String.valueOf(editor.getLineHeight()) + "px");
        lineStyle.add(StyleType.PADDING, "0");

        spanStyle = new HtmlStyle();
        spanStyle.add(StyleType.DISPLAY, "inline-block");
        spanStyle.add(StyleType.POSITION, "relative");
        spanStyle.add(StyleType.PADDING, "0");
        lineStyle.add(StyleType.LINE_SPACING, String.valueOf(editor.getLineHeight()) + "px");
        spanStyle.add(StyleType.HEIGHT, String.valueOf(editor.getLineHeight()) + "px");

        HashMap<HtmlStyle, Set<String>> map = Maps.newHashMapWithExpectedSize(2);
        map.put(lineStyle, Sets.newHashSet(".line"));
        map.put(spanStyle, Sets.newHashSet(".span"));
        map.put(defaultStyle, Sets.newHashSet("p"));
        styleLayerMap.put(1000, map);
    }

    private void parseStyle(Editor editor) {
        EditorImpl editorImpl = (EditorImpl) editor;
        EditorFilteringMarkupModelEx filteredDocumentMarkupModel = (EditorFilteringMarkupModelEx) editorImpl.getFilteredDocumentMarkupModel();
        HighlighterIterator iterator = editorImpl.getHighlighter().createIterator(0);
        Map<HtmlStyle, Set<String>> styleLayer2000 = new HashMap<>();
        styleLayerMap.put(2000, styleLayer2000);
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
                if (textAttributes.getEffectType() != null) {
                    switch (textAttributes.getEffectType()) {
                        case WAVE_UNDERSCORE:
                            if (textAttributes.getEffectColor() != null) {
                                htmlStyle.setBefore(true);
                                htmlStyle.add(StyleType.CONTENT, "\"" + Strings.repeat("~", (highlighter.getEndOffset() - highlighter.getStartOffset()) * 2) + "\"");
                                htmlStyle.add(StyleType.SIZE, ((EditorImpl) editor).getFontSize() / 2.0 + "px");
                                htmlStyle.add(StyleType.FOREGROUND, color2String(textAttributes.getEffectColor()));
                                htmlStyle.add(StyleType.WIDTH, "100%");
                                htmlStyle.add(StyleType.POSITION, "absolute");
                                htmlStyle.add(StyleType.TOP, ((EditorImpl) editor).getFontSize() * 3 / 4.0 + "px");
                                htmlStyle.add(StyleType.OVER_FLOW, "hidden");
                            }
                            break;
                        case LINE_UNDERSCORE:
                            if (textAttributes.getEffectColor() != null) {
                                htmlStyle.add(StyleType.LINE_UNDERSCORE, "1px solid " + color2String(textAttributes.getEffectColor()));
                            }
                            break;
                        case STRIKEOUT:
                            if (textAttributes.getEffectColor() != null) {
                                htmlStyle.add(StyleType.WAVE_UNDERSCORE, "line-through");
                            }
                            break;
                    }
                }
                if (textAttributes.getFontType() == 2) {
                    htmlStyle.add(StyleType.FONT_TYPE, "oblique");
                }
                if (textAttributes.getForegroundColor() != null) {
                    htmlStyle.add(StyleType.FOREGROUND, color2String(textAttributes.getForegroundColor()));
                }
                if (textAttributes.getBackgroundColor() != null) {
                    htmlStyle.add(StyleType.BACKGROUND, color2String(textAttributes.getBackgroundColor()));
                }
                Map<HtmlStyle, Set<String>> map;
                if (!styleLayerMap.containsKey(highlighter.getLayer())) {
                    map = new HashMap<>();
                    styleLayerMap.put(highlighter.getLayer(), map);
                } else {
                    map = styleLayerMap.get(highlighter.getLayer());
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
                    map.get(htmlStyle).add(".code_" + codeIntervalStartPoints[i]);
                }
            }
        }
    }

    public static void main(String[] args) {
        System.out.println(Arrays.binarySearch(new int[]{1, 3, 5, 7, 9}, 4));
    }

    public String getHtmlContent(int startLine, int endLine, int maxLayer) {
        StringBuilder sb = new StringBuilder();
        if (startLine > endLine || startLine < 0 || endLine >= textLines.size()) {
            return "error line";
        }
        sb.append("<div>\n");
        sb.append("<style>\n");
        for (Integer layer : styleLayerMap.navigableKeySet()) {
            if (layer <= maxLayer) {
                sb.append("/* layer:").append(layer).append("  */\n");
                for (Map.Entry<HtmlStyle, Set<String>> entry : styleLayerMap.get(layer).entrySet()) {
                    if (!entry.getKey().isEmpty()) {
                        if (entry.getKey().isBefore()) {
                            for (String s : entry.getValue()) {
                                sb.append(String.format("%s:before{%s}\n", s, entry.getKey()));
                            }
                        } else {
                            sb.append(String.format("%s{%s}\n", joiner.join(entry.getValue()), entry.getKey()));
                        }
                    }
                }
            }
        }
        sb.append("</style>\n");
        for (List<Pair<TextRange, String>> line : textLines.subList(startLine, endLine + 1)) {
            sb.append("<p class=\"line\">\n");
            for (Pair<TextRange, String> text : line) {
                sb.append(String.format("<span class=\"span code_%s\">", text.getFirst().getStartOffset()));
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
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }
}
