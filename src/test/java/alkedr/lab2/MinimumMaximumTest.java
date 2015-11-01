package alkedr.lab2;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Comparator.naturalOrder;
import static org.junit.Assert.assertSame;

@RunWith(Parameterized.class)
public class MinimumMaximumTest {
    private final IterativeParallelism ip = new IterativeParallelismImpl();

    private final int threads;

    public MinimumMaximumTest(int threads) {
        this.threads = threads;
    }

    @Parameterized.Parameters(name = "threads={0}")
    public static Object[][] parameters() {
        return new Object[][]{{1},{2},{3},{4},{5},{6},{7},{8},{9},{10}};
    }

    private <T extends Comparable<T>> void check(List<T> objects, T min, T max) {
        assertSame(min, ip.minimum(threads, objects, naturalOrder()));
        assertSame(max, ip.maximum(threads, objects, naturalOrder()));
    }


    @Test
    public void emptyInput() {
        check(Collections.<Integer>emptyList(), null, null);
    }

    @Test
    public void oneInteger() {
        Integer v = 1;
        check(asList(v), v, v);
    }

    @Test
    public void threeStrings() {
        String min = "abc";
        String max = "cde";
        check(asList(min, "bcd", max), min, max);
        check(asList(max, "bcd", min), min, max);
    }

    @Test
    public void twoEqualIntegers() {
        Integer a = new Integer(1);
        Integer b = new Integer(1);
        check(asList(a, b), a, a);
        check(asList(b, a), b, b);
    }

    @Test
    public void oneHundredElementsInRandomOrder() {
        Integer min = new Integer(0);
        Integer max = new Integer(99);
        List<Integer> integers = new ArrayList<>();
        integers.add(min);
        for (int i = 1; i < 99; i++) {
            integers.add(i);
        }
        integers.add(max);
        for (int i = 0; i < 1000; i++) {
            Collections.shuffle(integers);
            check(integers, min, max);
        }
    }
}
