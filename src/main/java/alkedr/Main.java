package alkedr;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import static java.nio.file.Files.lines;
import static java.nio.file.Files.walk;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class Main {
    public static void main(String... args) {
        try {
            Path toysDirectoryPath = Paths.get(args[0]);
            Path colorsFilePath = Paths.get(args[1]);
            Path resultFilePath = Paths.get("result.txt");
            generateResultFile(toysDirectoryPath, colorsFilePath, resultFilePath);
        } catch (ToyProcessingException e) {
            System.err.println(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private static void generateResultFile(Path toysDirectoryPath, Path colorsFilePath, Path resultFilePath) {
        Collection<Path> toysFilesPaths = findToysFiles(toysDirectoryPath);
        if (toysFilesPaths.isEmpty()) {
            throw new ThereAreNoFilesWithToysException();
        }
        Collection<Collection<String>> toyFilesLines = readToysFilesLines(toysFilesPaths);
        if (toyFilesLines.stream().allMatch(Collection::isEmpty)) {
            throw new AllFilesWithToysAreEmptyException();
        }
        Collection<String> colorsFileLines = readFileLines(colorsFilePath);
        Collection<String> resultFileLines = generateResultFileLines(toyFilesLines, colorsFileLines);
        writeLines(resultFilePath, resultFileLines);
    }

    private static Collection<Path> findToysFiles(Path toysDirectoryPath) {
        try {
            return walk(toysDirectoryPath, 1)
                    .filter(Files::isRegularFile)
                    .collect(toList());
        } catch (IOException e) {
            throw new InvalidToysDirectoryException(e);
        }
    }

    private static Collection<Collection<String>> readToysFilesLines(Collection<Path> toysFilesPaths) {
        return toysFilesPaths.stream()
                .map(Main::readFileLines)
                .collect(toList());
    }

    private static Collection<String> readFileLines(Path colorsFilePath) {
        try {
            return lines(colorsFilePath).collect(toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static Collection<String> generateResultFileLines(Collection<Collection<String>> toyFilesLines,
                                                      Collection<String> colorsFileLines) {
        Collection<Collection<Item>> toys = parseToyFilesLines(toyFilesLines);
        if (toys.stream().allMatch(Collection::isEmpty)) {
            throw new ThereAreNoValidLinesInFilesWithToysException();
        }
        Collection<Item> mergedToys = mergeAndSortToys(toys);
        Collection<Item> colors = parseItemFileLines(colorsFileLines);
        if (colors.isEmpty()) {
            return generateResultFileLines(mergedToys);
        }
        Collection<Item> coloredToys = generateColoredToys(mergedToys, colors);
        return generateResultFileLines(coloredToys);
    }

    private static Collection<Collection<Item>> parseToyFilesLines(Collection<Collection<String>> toyFilesLines) {
        return toyFilesLines.stream()
                .map(Main::parseItemFileLines)
                .collect(toList());
    }

    private static Collection<Item> parseItemFileLines(Collection<String> lines) {
        return lines.stream()
                .map(Main::parseItem)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }

    private static Optional<Item> parseItem(String s) {
        String[] split = s.split(" ");
        if (split.length != 2) {
            return Optional.empty();
        }
        try {
            return Optional.of(new Item(split[0], Integer.parseInt(split[1])));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    private static Collection<String> generateResultFileLines(Collection<Item> toys) {
        return toys.stream()
                .map(Main::toyToResultFileLine)
                .collect(toList());
    }

    private static String toyToResultFileLine(Item toy) {
        return toy.name + ' ' + toy.price;
    }

    private static Collection<Item> mergeAndSortToys(Collection<Collection<Item>> toys) {
        return mergeToysWithTheSameNameAcrossAllFilesAndSort(mergeToysWithTheSameNamesInEachFile(toys));
    }

    private static Collection<Item> mergeToysWithTheSameNamesInEachFile(Collection<Collection<Item>> toys) {
        return toys.stream()
                .flatMap(Main::mergeToysWithTheSameNamesInFile)
                .collect(toList());
    }

    private static Stream<Item> mergeToysWithTheSameNamesInFile(Iterable<Item> toys) {
        Map<String, Item> map = new HashMap<>();
        for (Item toy : toys) {
            map.putIfAbsent(toy.name, toy);
        }
        return map.values().stream();
    }

    static Collection<Item> mergeToysWithTheSameNameAcrossAllFilesAndSort(Collection<Item> lines) {
        return lines.stream()
                .collect(toMap(toy -> toy.name, ToyGroup::new, ToyGroup::add, TreeMap::new))  // TODO: case-insensitive?
                .values().stream()
                .map(ToyGroup::toToy)
                .collect(toList());
    }

    private static Collection<Item> generateColoredToys(Iterable<Item> toys, Iterable<Item> colors) {
        Collection<Item> result = new ArrayList<>();
        for (Item toy : toys) {
            for (Item color : colors) {
                result.add(new Item(toy.name + '_' + color.name, toy.price + color.price));
            }
        }
        return result;
    }

    private static void writeLines(Path resultFilePath, Iterable<String> resultFileLines) {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(resultFilePath.toFile()), StandardCharsets.UTF_8)) {
            for (String line : resultFileLines) {
                writer.append(line).append('\n');
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    static class Item {
        public final String name;
        public final double price;

        Item(String name, double price) {
            this.name = name;
            this.price = price;
        }
    }

    private static class ToyGroup {
        private final String name;
        private double priceSum;
        private long count;

        ToyGroup(Item toy) {
            this.name = toy.name;
            this.priceSum = toy.price;
            this.count = 1;
        }

        public ToyGroup add(ToyGroup other) {
            if (!Objects.equals(name, other.name)) {
                throw new RuntimeException("Баг (попытались посчитать среднюю цену для игрушек с разными названиями)");
            }
            priceSum += other.priceSum;
            count += other.count;
            return this;
        }

        public Item toToy() {
            return new Item(name, priceSum / count);
        }
    }


    static class ToyProcessingException extends RuntimeException {
        ToyProcessingException(String message) {
            super(message);
        }

        ToyProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    static class InvalidToysDirectoryException extends ToyProcessingException {
        InvalidToysDirectoryException(Throwable cause) {
            super("Ошибка получения списка файлов с игрушками", cause);
        }
    }

    static class ThereAreNoFilesWithToysException extends ToyProcessingException {
        ThereAreNoFilesWithToysException() {
            super("Нет ни одного файла с игрушками");
        }
    }

    static class AllFilesWithToysAreEmptyException extends ToyProcessingException {
        AllFilesWithToysAreEmptyException() {
            super("Нет ни одного непустого файла с игрушками");
        }
    }

    static class ThereAreNoValidLinesInFilesWithToysException extends ToyProcessingException {
        ThereAreNoValidLinesInFilesWithToysException() {
            super("В файлах с игрушками нет ни одной валидной игрушки");
        }
    }
}
