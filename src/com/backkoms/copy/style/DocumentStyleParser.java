package com.backkoms.copy.style;

import com.backkoms.copy.rangetree.RangeTree;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.escape.Escaper;
import com.google.common.html.HtmlEscapers;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.openapi.editor.impl.EditorFilteringMarkupModelEx;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.editor.markup.AttributesFlyweight;
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
    private HtmlStyle defaultStyle;
    private HtmlStyle lineStyle;
    private HtmlStyle spanStyle;
    private static String waveLine = Strings.repeat("~", 500);
    private static String straightLine = Strings.repeat("_", 500);

    private TreeMap<Integer, Map<HtmlStyle, Set<TextRange>>> styleLayerMap = new TreeMap<>();

    private RangeTree rangeTree;
    private int startLine;
    private int endLine;
    private Document document;
    private TextRange allTextRange;

    public DocumentStyleParser(Editor editor, int startLine, int endLine) {
        this.startLine = startLine;
        this.endLine = endLine;
        this.document = editor.getDocument();
        this.allTextRange = new TextRange(document.getLineStartOffset(startLine), document.getLineEndOffset(endLine));
        this.rangeTree = new RangeTree(document.getLineStartOffset(startLine), document.getLineEndOffset(endLine));
        parseDefaultStyle(editor);
        parseStyle(editor);
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
        spanStyle.add(StyleType.WHITE_SPACE, "pre-wrap");
        spanStyle.add(StyleType.DISPLAY, "inline-block");
        spanStyle.add(StyleType.VERTICAL_ALIGN, "top");
        spanStyle.add(StyleType.POSITION, "relative");
        spanStyle.add(StyleType.MARGIN, "0");
        spanStyle.add(StyleType.PADDING, "0");
        spanStyle.add(StyleType.LINE_SPACING, String.valueOf(editor.getLineHeight()) + "px");
        spanStyle.add(StyleType.HEIGHT, String.valueOf(editor.getLineHeight()) + "px");
        spanStyle.add(StyleType.OVER_FLOW, "hidden");
    }

    public static void main(String[] args) {
        System.out.println(new TextRange(1, 5).intersects(new TextRange(6, 10)));
    }

    private void parseStyle(Editor editor) {
        EditorImpl editorImpl = (EditorImpl) editor;
        EditorFilteringMarkupModelEx filteredDocumentMarkupModel = (EditorFilteringMarkupModelEx) editorImpl.getFilteredDocumentMarkupModel();
        HighlighterIterator iterator = editorImpl.getHighlighter().createIterator(0);

        while (!iterator.atEnd()) {
            TextAttributes textAttributes = iterator.getTextAttributes();
            int start = iterator.getStart();
            int end = iterator.getEnd();
            TextRange textRange = new TextRange(start, end - 1);
            if (allTextRange.intersects(textRange) && textAttributes.getForegroundColor() != null) {
                HtmlStyle htmlStyle = new HtmlStyle();
                htmlStyle.add(StyleType.FOREGROUND, color2String(textAttributes.getForegroundColor()));
                if (textAttributes.getFontType() == 2) {
                    htmlStyle.add(StyleType.FONT_TYPE, "oblique");
                }else if(textAttributes.getFontType() == 1){
                    htmlStyle.add(StyleType.FONT_WEIGHT,"700");
                }else if(textAttributes.getFontType() == 3){
                    htmlStyle.add(StyleType.FONT_TYPE, "oblique");
                    htmlStyle.add(StyleType.FONT_WEIGHT,"700");
                }
                addStyle(100, textRange, htmlStyle);
            }
            iterator.advance();
        }

        RangeHighlighter[] allHighlighters = filteredDocumentMarkupModel.getAllHighlighters();

        for (RangeHighlighter highlighter : allHighlighters) {
            TextAttributes textAttributes = highlighter.getTextAttributes();
            TextRange textRange = new TextRange(highlighter.getStartOffset(), highlighter.getEndOffset() - 1);
            if (allTextRange.intersects(textRange) && textAttributes != null) {
                if (textAttributes.getEffectType() != null) {
                    switch (textAttributes.getEffectType()) {
                        case WAVE_UNDERSCORE:
                            if (textAttributes.getEffectColor() != null) {
                                HtmlStyle htmlStyle = new HtmlStyle();
                                htmlStyle.setBefore(true);
                                htmlStyle.add(StyleType.CONTENT, "\"" + waveLine + "\"");
                                htmlStyle.add(StyleType.SIZE, "5px");
                                htmlStyle.add(StyleType.FOREGROUND, color2String(textAttributes.getEffectColor()));
                                htmlStyle.add(StyleType.WIDTH, "100%");
                                htmlStyle.add(StyleType.POSITION, "absolute");
                                htmlStyle.add(StyleType.TOP, (editor.getLineHeight() / 2 + 3) * 100.0 / editor.getLineHeight() + "%");
                                addStyle(highlighter.getLayer(), textRange, htmlStyle);
                            }
                            break;
                        case LINE_UNDERSCORE:
                            if (textAttributes.getEffectColor() != null) {
                                HtmlStyle htmlStyle = new HtmlStyle();
                                htmlStyle.setBefore(true);
                                htmlStyle.add(StyleType.CONTENT, "\"" + straightLine + "\"");
                                htmlStyle.add(StyleType.SIZE, "0.5em");
                                htmlStyle.add(StyleType.FOREGROUND, color2String(textAttributes.getEffectColor()));
                                htmlStyle.add(StyleType.WIDTH, "100%");
                                htmlStyle.add(StyleType.POSITION, "absolute");
                                htmlStyle.add(StyleType.TOP, "13%");
                                addStyle(highlighter.getLayer(), textRange, htmlStyle);
                            }
                            break;
                        case STRIKEOUT:
                            if (textAttributes.getEffectColor() != null) {
                                HtmlStyle htmlStyle = new HtmlStyle();
                                htmlStyle.setBefore(true);
                                htmlStyle.add(StyleType.CONTENT, "\"" + straightLine + "\"");
                                htmlStyle.add(StyleType.SIZE, "0.5em");
                                htmlStyle.add(StyleType.FOREGROUND, color2String(textAttributes.getEffectColor()));
                                htmlStyle.add(StyleType.WIDTH, "100%");
                                htmlStyle.add(StyleType.POSITION, "absolute");
                                htmlStyle.add(StyleType.TOP, "-8%");
                                addStyle(highlighter.getLayer(), textRange, htmlStyle);
                            }
                            break;
                    }
                }
                AttributesFlyweight flyweight = textAttributes.getFlyweight();

                HtmlStyle htmlStyle = new HtmlStyle();
                if (textAttributes.getFontType() == 2) {
                    htmlStyle.add(StyleType.FONT_TYPE, "oblique");
                }else if(textAttributes.getFontType() == 1){
                    htmlStyle.add(StyleType.FONT_WEIGHT,"700");
                }else if(textAttributes.getFontType() == 3){
                    htmlStyle.add(StyleType.FONT_TYPE, "oblique");
                    htmlStyle.add(StyleType.FONT_WEIGHT,"700");
                }
                if (textAttributes.getForegroundColor() != null) {
                    htmlStyle.add(StyleType.FOREGROUND, color2String(textAttributes.getForegroundColor()));
                }
                if (textAttributes.getBackgroundColor() != null) {
                    htmlStyle.add(StyleType.BACKGROUND, color2String(textAttributes.getBackgroundColor()));
                }
                addStyle(highlighter.getLayer(), textRange, htmlStyle);
            }
        }
    }

    private void addStyle(int layer, TextRange textRange, HtmlStyle htmlStyle) {
        Map<HtmlStyle, Set<TextRange>> map;
        if (!styleLayerMap.containsKey(layer)) {
            map = new HashMap<>();
            styleLayerMap.put(layer, map);
        } else {
            map = styleLayerMap.get(layer);
        }
        if (!map.containsKey(htmlStyle)) {
            map.put(htmlStyle, new HashSet<>());
        }
        map.get(htmlStyle).add(textRange);
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
                            sb.append(String.format(".style_%s:before{%s}\n", styleIndex, entry.getKey()));
                        } else {
                            sb.append(String.format(".style_%s{%s}\n", styleIndex, entry.getKey()));
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
                sb.append(escaper.escape(text));
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
                sb.append(" style_").append(i);
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
