package alkedr.lab2;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class AllAnyFilterTest {
    private final IterativeParallelism ip = new IterativeParallelismImpl();

    private final int threads;

    public AllAnyFilterTest(int threads) {
        this.threads = threads;
    }

    @Parameterized.Parameters(name = "threads={0}")
    public static Object[][] parameters() {
        return new Object[][]{{1},{2},{3},{4},{5},{6},{7},{8},{9},{10}};
    }

    private void check(List<Boolean> objects, boolean all, boolean any, int filterCount) {
        Predicate<Boolean> predicate = object -> object;
        assertEquals(all, ip.all(threads, objects, predicate));
        assertEquals(any, ip.any(threads, objects, predicate));
        List<Boolean> filterResult = ip.filter(threads, objects, predicate);
        assertEquals(filterCount, filterResult.size());
        filterResult.forEach(Assert::assertTrue);
    }


    @Test
    public void emptyInput() {
        check(Collections.<Boolean>emptyList(), true, false, 0);
    }

    @Test
    public void oneElement() {
        check(asList(false), false, false, 0);
        check(asList(true), true, true, 1);
    }

    @Test
    public void twoElements() {
        check(asList(false, false), false, false, 0);
        check(asList(false, true), false, true, 1);
        check(asList(true, false), false, true, 1);
        check(asList(true, true), true, true, 2);
    }

    @Test
    public void oneHundredElements() {
        for (int i = 0; i < 100; i++) {
            List<Boolean> b1 = new ArrayList<>();
            List<Boolean> b2 = new ArrayList<>();
            for (int j = 0; j < 100; j++) {
                b1.add(false);
                b2.add(true);
            }
            b1.set(i, true);
            b2.set(i, false);
            check(b1, false, true, 1);
            check(b2, false, true, 99);
        }
    }
}
