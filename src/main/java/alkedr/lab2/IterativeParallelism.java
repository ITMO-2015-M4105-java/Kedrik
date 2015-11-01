package alkedr.lab2;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public interface IterativeParallelism {
    <T> T minimum(int threads, List<T> list, Comparator<T> comparator);
    <T> T maximum(int threads, List<T> list, Comparator<T> comparator);
    <T> boolean all(int threads, List<T> list, Predicate<T> predicate);
    <T> boolean any(int threads, List<T> list, Predicate<T> predicate);
    <T> List<T> filter(int threads, List<T> list, Predicate<T> predicate);
    <T, R> List<R> map(int threads, List<T> list, Function<T, R> function);
}
