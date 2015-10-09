package alkedr;

import org.junit.Test;

import static alkedr.Main.generateResultFileLines;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

/* alkedr 09.10.15 */
public class GenerateResultFileLinesTest {
    @Test(expected = Main.ThereAreNoValidLinesInFilesWithToysException.class)
    public void noToys() {
        generateResultFileLines(asList(), asList());
    }

    @Test(expected = Main.ThereAreNoValidLinesInFilesWithToysException.class)
    public void onlyInvalidToys() {
        generateResultFileLines(asList(asList("", "name", "name 1 1", "name 1.2")), asList());
    }

    @Test
    public void noColors() {
        assertEquals(asList("name1 1.0", "name2 2.0"), generateResultFileLines(asList(asList("name1 1", "name2 2")), asList()));
    }

    @Test
    public void normal() {
        assertEquals(
                asList("name1_red 2.0", "name1_blue 3.0", "name2_red 3.0", "name2_blue 4.0"),
                generateResultFileLines(asList(asList("name1 1", "name2 2")), asList("red 1", "blue 2"))
        );
    }

    @Test
    public void twoToysWithTheSameNameInTheSameFile() {
        assertEquals(
                asList("name1_red 2.0", "name1_blue 3.0", "name2_red 3.0", "name2_blue 4.0"),
                generateResultFileLines(asList(asList("name1 1", "name1 100", "name2 2")), asList("red 1", "blue 2"))
        );
    }

    @Test
    public void twoToysWithTheSameNameInDifferentFiles() {
        assertEquals(
                asList("name1_red 2.5", "name1_blue 3.5", "name2_red 3.0", "name2_blue 4.0"),
                generateResultFileLines(asList(asList("name1 1", "name2 2"), asList("name1 2")), asList("red 1", "blue 2"))
        );
    }
}
