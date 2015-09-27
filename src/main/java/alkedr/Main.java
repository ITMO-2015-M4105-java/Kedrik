package alkedr;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

public class Main {
    private static final Pattern LINE_SPLIT_PATTERN = Pattern.compile("\\s");

    public static void main(String... args) throws IOException {
        writeColoredToys("out/toys.txt", generateColoredToys(readToys("in/toys"), readColors("in/colors.txt")));
    }

    private static Stream<Toy> readToys(String dirName) throws IOException {
        return Files.walk(Paths.get(dirName))
                .filter(Files::isRegularFile)
                .flatMap(Main::readLines)
                .map(LINE_SPLIT_PATTERN::split)
                .filter(split -> split.length == 2)
                .collect(groupingBy(split -> split[0]))
                .entrySet().stream()
                .map(entry -> new Toy(
                        entry.getKey(),
                        entry.getValue().stream()
                                .mapToDouble(split -> Double.valueOf(split[1]))
                                .average().getAsDouble()
                ));
    }

    private static Map<String, Double> readColors(String fileName) throws IOException {
        return Files.lines(Paths.get(fileName))
                .map(LINE_SPLIT_PATTERN::split)
                .filter(split -> split.length == 2)
                .collect(toMap(
                        split -> split[0],
                        split -> Double.parseDouble(split[1])
                ));
    }

    private static Stream<Toy> generateColoredToys(Stream<Toy> toys, Map<String, Double> colorNameToPrice) {
        return toys.flatMap(toy -> generateColoredToys(toy, colorNameToPrice));
    }

    public static Stream<Toy> generateColoredToys(Toy toy, Map<String, Double> colorNameToPrice) {
        return colorNameToPrice.entrySet().stream()
                .map(colorEntry -> new Toy(toy.name + '_' + colorEntry.getKey(), toy.price + colorEntry.getValue()));
    }

    private static void writeColoredToys(String fileName, Stream<Toy> coloredToys) throws IOException {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(fileName), StandardCharsets.UTF_8)) {
            coloredToys
                    .map(coloredToy -> coloredToy.name + ' ' + coloredToy.price)
                    .forEach(s -> {
                        try {
                            writer.append(s).append('\n');
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }

    public static Stream<String> readLines(Path path) {
        try {
            return Files.lines(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static class Toy {
        public final String name;
        public final double price;

        private Toy(String name, double price) {
            this.name = name;
            this.price = price;
        }
    }
}
