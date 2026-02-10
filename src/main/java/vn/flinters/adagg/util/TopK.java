package vn.flinters.adagg.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public final class TopK<T> {
    private final int k;
    private final PriorityQueue<T> heap;
    private final Comparator<T> comparator;
    private final boolean keepMax; // true => keep K largest, heap is min-heap; false => keep K smallest, heap is max-heap

    private TopK(int k, Comparator<T> comparator, boolean keepMax) {
        this.k = k;
        this.comparator = comparator;
        this.keepMax = keepMax;

        // For keepMax: heap root is "worst among kept" => smallest
        // For keepMin: heap root is "worst among kept" => largest
        Comparator<T> heapCmp = keepMax ? comparator : comparator.reversed();
        this.heap = new PriorityQueue<>(k + 1, heapCmp);
    }

    public static <T> TopK<T> maxK(int k, Comparator<T> comparator) {
        return new TopK<>(k, comparator, true);
    }

    public static <T> TopK<T> minK(int k, Comparator<T> comparator) {
        return new TopK<>(k, comparator, false);
    }

    public void offer(T value) {
        if (k <= 0) return;

        if (heap.size() < k) {
            heap.offer(value);
            return;
        }

        T root = heap.peek();
        // If keepMax: replace root if value > root
        // If keepMin: replace root if value < root
        int cmp = comparator.compare(value, root);
        boolean better = keepMax ? (cmp > 0) : (cmp < 0);

        if (better) {
            heap.poll();
            heap.offer(value);
        }
    }

    public List<T> toSortedListDescending() {
        ArrayList<T> list = new ArrayList<>(heap);
        list.sort(comparator.reversed());
        return list;
    }

    public List<T> toSortedListAscending() {
        ArrayList<T> list = new ArrayList<>(heap);
        list.sort(comparator);
        return list;
    }
}
