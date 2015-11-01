package alkedr.lab2;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class MapTest {
    private final IterativeParallelism ip = new IterativeParallelismImpl();

    private final int threads;

    public MapTest(int threads) {
        this.threads = threads;
    }

    @Parameterized.Parameters(name = "threads={0}")
    public static Object[][] parameters() {
        return new Object[][]{{1},{2},{3},{4},{5},{6},{7},{8},{9},{10}};
    }

    private void check(List<Integer> input, List<String> expectedOutput) {
        List<String> actualOutput = ip.map(threads, input, String::valueOf);
        assertEquals(expectedOutput.size(), actualOutput.size());
        for (int i = 0; i < expectedOutput.size(); i++) {
            assertEquals(expectedOutput.get(i), actualOutput.get(i));
        }
    }


    @Test
    public void emptyInput() {
        check(emptyList(), emptyList());
    }

    @Test
    public void oneElement() {
        check(asList(1), asList("1"));
        check(asList(2), asList("2"));
    }

    @Test
    public void twoElements() {
        check(asList(1, 2), asList("1", "2"));
    }

    @Test
    public void oneHundredElements() {
        List<Integer> integers = new ArrayList<>();
        List<String> strings = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            integers.add(i);
            strings.add(String.valueOf(i));
        }
        check(integers, strings);
    }
}
