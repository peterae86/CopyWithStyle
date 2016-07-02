package com.peterae86.copy.style;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.escape.Escaper;
import com.google.common.html.HtmlEscapers;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.openapi.editor.impl.DocumentImpl;
import com.intellij.openapi.editor.impl.EditorFilteringMarkupModelEx;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.peterae86.copy.rangetree.RangeTree;

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

    TreeMap<Integer, Map<HtmlStyle, Set<TextRange>>> styleLayerMap = new TreeMap<>();

    private RangeTree rangeTree;
    private Editor editor;
    private int startLine;
    private int endLine;
    private Document document;

    public DocumentStyleParser(Editor editor, int startLine, int endLine) {
        this.editor = editor;
        this.startLine = startLine;
        this.endLine = endLine;
        this.document = editor.getDocument();
        buildLineRangeTrees(document, startLine, endLine);
        parseDefaultStyle(editor);
        parseStyle(editor);
    }


    private void buildLineRangeTrees(Document document, int startLine, int endLine) {
        rangeTree = new RangeTree(document.getLineStartOffset(startLine), document.getLineEndOffset(endLine));
    }

    private void parseDefaultStyle(Editor editor) {
        EditorColorsScheme colorsScheme = editor.getColorsScheme();
        defaultStyle = new HtmlStyle();
        defaultStyle.add(StyleType.BACKGROUND, color2String(colorsScheme.getDefaultBackground()));

        lineStyle = new HtmlStyle();
        lineStyle.add(StyleType.LINE_SPACING, String.valueOf(colorsScheme.getLineSpacing()));
        lineStyle.add(StyleType.HEIGHT, String.valueOf(editor.getLineHeight()) + "px");
        lineStyle.add(StyleType.PADDING, "0");
        lineStyle.add(StyleType.FOREGROUND, color2String(colorsScheme.getDefaultForeground()));
        lineStyle.add(StyleType.SIZE, colorsScheme.getEditorFontSize() + "px");
        lineStyle.add(StyleType.FONT, Joiner.on(",").join(colorsScheme.getFontPreferences().getEffectiveFontFamilies()) + ",serif");
        lineStyle.add(StyleType.MARGIN, "0");


        spanStyle = new HtmlStyle();
        spanStyle.add(StyleType.DISPLAY, "inline-block");
        spanStyle.add(StyleType.VERTICAL_ALIGN, "top");
        spanStyle.add(StyleType.POSITION, "relative");
        spanStyle.add(StyleType.MARGIN, "0");
        spanStyle.add(StyleType.PADDING, "0");
        spanStyle.add(StyleType.LINE_SPACING, String.valueOf(editor.getLineHeight()) + "px");
        spanStyle.add(StyleType.HEIGHT, String.valueOf(editor.getLineHeight()) + "px");
    }

    private void parseStyle(Editor editor) {
        EditorImpl editorImpl = (EditorImpl) editor;
        RangeHighlighter[] allHighlighters1 = editor.getMarkupModel().getAllHighlighters();
        CaretModel caretModel = editor.getCaretModel();
        FoldingModel foldingModel = editor.getFoldingModel();
        SoftWrapModel softWrapModel = editor.getSoftWrapModel();
        ScrollingModel scrollingModel = editor.getScrollingModel();
        EditorFilteringMarkupModelEx filteredDocumentMarkupModel = (EditorFilteringMarkupModelEx) editorImpl.getFilteredDocumentMarkupModel();
        HighlighterIterator iterator = editorImpl.getHighlighter().createIterator(0);
        Map<HtmlStyle, Set<TextRange>> styleLayer2000 = new HashMap<>();
        styleLayerMap.put(2000, styleLayer2000);
        while (!iterator.atEnd()) {
            TextAttributes textAttributes = iterator.getTextAttributes();
            int start = iterator.getStart();
            int end = iterator.getEnd();
            if (textAttributes.getForegroundColor() != null) {
                HtmlStyle htmlStyle = new HtmlStyle();
                htmlStyle.add(StyleType.FOREGROUND, color2String(textAttributes.getForegroundColor()));
                if (textAttributes.getFontType() == 2) {
                    htmlStyle.add(StyleType.FONT_TYPE, "oblique");
                }
                if (!styleLayer2000.containsKey(htmlStyle)) {
                    styleLayer2000.put(htmlStyle, new HashSet<>());
                }
                styleLayer2000.get(htmlStyle).add(new TextRange(start, end));
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
                                htmlStyle.add(StyleType.CONTENT, "\"" + Strings.repeat("~", 100) + "\"");
                                htmlStyle.add(StyleType.SIZE, editor.getLineHeight() - ((EditorImpl) editor).getFontSize() - 1 + "px");
                                htmlStyle.add(StyleType.FOREGROUND, color2String(textAttributes.getEffectColor()));
                                htmlStyle.add(StyleType.WIDTH, "100%");
                                htmlStyle.add(StyleType.POSITION, "absolute");
                                htmlStyle.add(StyleType.TOP, ((EditorImpl) editor).getFontSize() - 1 + "px");
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
                Map<HtmlStyle, Set<TextRange>> map;
                if (!styleLayerMap.containsKey(highlighter.getLayer())) {
                    map = new HashMap<>();
                    styleLayerMap.put(highlighter.getLayer(), map);
                } else {
                    map = styleLayerMap.get(highlighter.getLayer());
                }
                if (!map.containsKey(htmlStyle)) {
                    map.put(htmlStyle, new HashSet<>());
                }
                map.get(htmlStyle).add(new TextRange(highlighter.getStartOffset(), highlighter.getEndOffset()));
            }
        }
    }

    public String getHtmlContent(int maxLayer) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div>\n");
        sb.append("<style>\n");
        sb.append(String.format("div{%s}\n", defaultStyle));
        sb.append(String.format(".line{%s}\n", lineStyle));
        sb.append(String.format(".span{%s}\n", spanStyle));
        int styleIndex = 0;
        for (Integer layer : styleLayerMap.navigableKeySet()) {
            if (layer <= maxLayer) {
                sb.append("/* layer:").append(layer).append("  */\n");
                for (Map.Entry<HtmlStyle, Set<TextRange>> entry : styleLayerMap.get(layer).entrySet()) {
                    if (!entry.getKey().isEmpty()) {
                        if (entry.getKey().isBefore()) {
                            sb.append(String.format("style_%s:before{%s}\n", styleIndex, entry.getKey()));
                        } else {
                            sb.append(String.format("style_%s{%s}\n", styleIndex, entry.getKey()));
                        }
                        for (TextRange textRange : entry.getValue()) {
                            rangeTree.update(1L << styleIndex, textRange.getStartOffset(), textRange.getEndOffset());
                        }
                        styleIndex++;
                    }
                }
            }
        }
        sb.append("</style>\n");
        sb.append("<div>\n");
        for (int i = startLine; i <= endLine; i++) {
            int lineStartOffset = document.getLineStartOffset(i);
            int lineEndOffset = document.getLineEndOffset(i);
            List<Pair<TextRange, Long>> ranges = rangeTree.queryRanges(lineStartOffset, lineEndOffset);
            sb.append("<p class=\"line\">\n");
            for (Pair<TextRange, Long> range : ranges) {
                String text = document.getText(range.first);
                sb.append(String.format("<span class=\"span %s\">", getStyleClassByMark(range.second)));
                sb.append(escaper.escape(text).replace(" ", "&ensp;").replace("\n", ""));
                sb.append("</span>");
            }
            sb.append("</p>\n");
        }
        sb.append("</div>\n");
        sb.append("</div>\n");
        return sb.toString();
    }

    private String getStyleClassByMark(Long mark) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 64; i++)
            if ((mark & (1L << i)) > 0) {
                sb.append("style_").append(i).append(" ");
            }
        return sb.toString();
    }

    private String color2String(Color color) {
        if (color == null) {
            return "";
        }
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }
}
