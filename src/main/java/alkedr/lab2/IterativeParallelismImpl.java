package alkedr.lab2;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class IterativeParallelismImpl implements IterativeParallelism {
    @Override
    public <T> T minimum(int threads, List<T> list, Comparator<T> comparator) {
        return run(
                threads,
                list,
                part -> part.stream().min(comparator),
                results -> results.stream()
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .min(comparator).orElse(null)
        );
    }

    @Override
    public <T> T maximum(int threads, List<T> list, Comparator<T> comparator) {
        return run(
                threads,
                list,
                part -> part.stream().max(comparator),
                results -> results.stream()
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .max(comparator).orElse(null)
        );
    }

    @Override
    public <T> boolean all(int threads, List<T> list, Predicate<T> predicate) {
        return run(
                threads,
                list,
                part -> part.stream().allMatch(predicate),
                results -> results.stream().allMatch(b -> b)
        );
    }

    @Override
    public <T> boolean any(int threads, List<T> list, Predicate<T> predicate) {
        return run(
                threads,
                list,
                part -> part.stream().anyMatch(predicate),
                results -> results.stream().anyMatch(b -> b)
        );
    }

    @Override
    public <T> List<T> filter(int threads, List<T> list, Predicate<T> predicate) {
        return run(
                threads,
                list,
                part -> part.stream().filter(predicate).collect(toList()),
                results -> results.stream().flatMap(Collection::stream).collect(toList())
        );
    }

    @Override
    public <T, R> List<R> map(int threads, List<T> list, Function<T, R> function) {
        return run(
                threads,
                list,
                part -> part.stream().map(function).collect(toList()),
                results -> results.stream().flatMap(Collection::stream).collect(toList())
        );
    }


    private static <T, U, R> R run(int threads, List<T> list, Function<List<T>, U> threadFunction, Function<List<U>, R> combineFunction) {
        return combineFunction.apply(run(split(threads, list), threadFunction));
    }

    static <T> List<List<T>> split(int threads, List<T> list) {
        List<List<T>> result = new ArrayList<>();
        while (threads > 1) {
            int itemsPerThread = list.size() / threads;
            result.add(list.subList(0, itemsPerThread));
            list = list.subList(itemsPerThread, list.size());
            threads--;
        }
        result.add(list);
        return result;
    }

    private static <T, U> List<U> run(Collection<T> inputs, Function<T, U> threadFunction) {
        Collection<Worker<T, U>> workers = inputs.stream()
                .map(input -> new Worker<>(input, threadFunction))
                .collect(toList());

        Collection<Thread> threads = workers.stream()
                .map(Thread::new)
                .collect(toList());

        threads.forEach(Thread::start);
        threads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        return workers.stream()
                .map(worker -> worker.output)
                .collect(toList());
    }


    private static class Worker<T, U> implements Runnable {
        private final T input;
        private final Function<T, U> threadFunction;
        public U output;

        Worker(T input, Function<T, U> threadFunction) {
            this.input = input;
            this.threadFunction = threadFunction;
        }

        @Override
        public void run() {
            output = threadFunction.apply(input);
        }
    }
}
