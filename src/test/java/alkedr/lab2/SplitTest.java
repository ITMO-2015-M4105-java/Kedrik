package alkedr.lab2;

import org.junit.Test;

import java.util.Collection;
import java.util.List;

import static alkedr.lab2.IterativeParallelismImpl.split;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

public class SplitTest {
    private static <T> void check(List<T> list, Integer... partSizes) {
        List<List<T>> result = split(partSizes.length, list);
        assertEquals(asList(partSizes), result.stream().map(Collection::size).collect(toList()));
        assertEquals(list, result.stream().flatMap(Collection::stream).collect(toList()));
    }


    private static final List<Integer> L0 = emptyList();
    private static final List<Integer> L1 = asList(1);
    private static final List<Integer> L2 = asList(1, 2);
    private static final List<Integer> L3 = asList(1, 2, 3);
    private static final List<Integer> L7 = asList(1, 2, 3, 4, 5, 6, 7);


    @Test
    public void emptyInput() {
        check(L0, 0);
        check(L0, 0, 0);
        check(L0, 0, 0, 0);
    }

    @Test
    public void oneElement() {
        check(L1, 1);
        check(L1, 0, 1);
    }

    @Test
    public void twoElements() {
        check(L2, 2);
        check(L2, 1, 1);
        check(L2, 0, 1, 1);
    }

    @Test
    public void threeElements() {
        check(L3, 3);
        check(L3, 1, 2);
        check(L3, 1, 1, 1);
        check(L3, 0, 1, 1, 1);
    }

    @Test
    public void sevenElements() {
        check(L7, 7);
        check(L7, 3, 4);
        check(L7, 2, 2, 3);
        check(L7, 1, 2, 2, 2);
        check(L7, 1, 1, 1, 2, 2);
        check(L7, 1, 1, 1, 1, 1, 2);
        check(L7, 1, 1, 1, 1, 1, 1, 1);
        check(L7, 0, 1, 1, 1, 1, 1, 1, 1);
    }
}
