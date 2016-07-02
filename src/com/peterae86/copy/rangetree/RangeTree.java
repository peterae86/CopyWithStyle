package com.peterae86.copy.rangetree;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.TreeRangeMap;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by test on 2016/7/1.
 */
public class RangeTree {
    private int l[];
    private int r[];
    private long v[];

    public RangeTree(int left, int right) {
        int size = (right - left) << 2;
        l = new int[size];
        r = new int[size];
        v = new long[size];
        init(1, left, right);
    }

    private void init(int i, int left, int right) {
        l[i] = left;
        r[i] = right;
        v[i] = 0;
        if (left == right) {
            return;
        }
        int mid = (left + right) >> 1;
        init(i << 1, left, mid);
        init(i << 1 | 1, mid + 1, right);
    }

    public void update(long mark, int left, int right) {
        lazyUpdate(1, mark, left, right);
    }

    private void lazyUpdate(int i, long value, int left, int right) {
        if (l[i] > right || r[i] < left) {
            return;
        }
        if (l[i] >= left && r[i] <= right) {
            v[i] |= value;
            return;
        }
        lazyUpdate(i << 1, value | v[i], left, right);
        lazyUpdate(i << 1 | 1, value | v[i], left, right);
    }

    public List<Pair<TextRange, Long>> queryRanges(int left, int right) {
        List<Pair<Integer, Long>> points = Lists.newArrayListWithExpectedSize(right - left + 1);
        queryRanges(1, v[1], left, right, points);
        List<Pair<TextRange, Long>> res = new ArrayList<>();
        int start = points.get(0).first;
        long mark = points.get(0).second;
        for (Pair<Integer, Long> point : points) {
            if (point.second != mark) {
                res.add(Pair.create(new TextRange(start, point.first), mark));
                start = point.first;
                mark = point.second;
            }
        }
        res.add(Pair.create(new TextRange(start, right), mark));
        return res;
    }

    private void queryRanges(int i, long mark, int left, int right, List<Pair<Integer, Long>> points) {
        if (l[i] > right || r[i] < left) {
            return;
        }
        if (l[i] == r[i] && l[i] >= left && r[i] <= right) {
            points.add(Pair.create(l[i], v[i] | mark));
            return;
        }
        queryRanges(i << 1, mark | v[i], left, right, points);
        queryRanges(i << 1 | 1, mark | v[i], left, right, points);
    }
}
