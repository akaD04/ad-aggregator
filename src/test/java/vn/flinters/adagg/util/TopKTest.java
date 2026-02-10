package vn.flinters.adagg.util;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TopKTest {

    @Test
    void offer_doesNothingWhenKIsZero() {
        TopK<Integer> top = TopK.<Integer>maxK(0, Comparator.naturalOrder());
        top.offer(10);
        assertTrue(top.toSortedListDescending().isEmpty());
    }

    @Test
    void maxK_keepsLargestK_andSortsDescending() {
        TopK<Integer> top = TopK.<Integer>maxK(3, Comparator.naturalOrder());
        top.offer(1);
        top.offer(5);
        top.offer(2);
        top.offer(10);
        top.offer(3);

        assertEquals(List.of(10, 5, 3), top.toSortedListDescending());
        assertEquals(List.of(3, 5, 10), top.toSortedListAscending());
    }

    @Test
    void minK_keepsSmallestK_andSortsAscending() {
        TopK<Integer> top = TopK.<Integer>minK(3, Comparator.naturalOrder());
        top.offer(10);
        top.offer(1);
        top.offer(5);
        top.offer(2);
        top.offer(3);

        assertEquals(List.of(1, 2, 3), top.toSortedListAscending());
        assertEquals(List.of(3, 2, 1), top.toSortedListDescending());
    }

    @Test
    void offer_replacesRootOnlyIfBetter() {
        TopK<Integer> top = TopK.<Integer>maxK(2, Comparator.naturalOrder());
        top.offer(5);
        top.offer(4);
        top.offer(4); // not better than root=4 -> no replace
        assertEquals(List.of(5, 4), top.toSortedListDescending());

        top.offer(6); // better than root=4 -> replace
        assertEquals(List.of(6, 5), top.toSortedListDescending());
    }
}
