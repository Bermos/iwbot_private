package commands.misc_commands;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CmdMemesTest {
    private List<TestCase> tests;
    class TestCase {
        TestCase(String u, String[] i, String o) {
            userId = u; inputs = i; output = o;
        }
        String   userId;
        String[] inputs;
        String   output;
    }

    @org.junit.Before
    public void setUp() throws Exception {
        tests = new ArrayList<>();
        tests.add(new TestCase("1", new String[] {"update"},  "Memes updated from file."));
        tests.add(new TestCase("1", new String[] {"upgrade"}, "[Error] Wrong arguments"));
        tests.add(new TestCase("0", new String[] {"update"},  "[Error] You aren't authorized to do this"));
    }

    @Test
    public void getHelp() {
        Memes memes = new Memes(); // Class to be tested

        for (TestCase testCase : tests) {
            assertEquals(memes.memes(testCase.userId, testCase.inputs), testCase.output);
        }
    }

}