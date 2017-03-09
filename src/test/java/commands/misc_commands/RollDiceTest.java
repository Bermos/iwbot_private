package commands.misc_commands;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class RollDiceTest {
    private List<TestCase> tests;
    class TestCase {
        TestCase(String[] i, String o) {
            inputs = i; output = o;
        }
        String[] inputs;
        String   output;
    }

    @Before
    public void setUp() throws Exception {
        tests = new ArrayList<>();

        tests.add(new TestCase(new String[] {"", ""}, "(What do you want me to do with all those arguments\\?)"));

        tests.add(new TestCase(new String[] {}, "(You rolled a )[1-6]"));
        tests.add(new TestCase(new String[] {}, "(You rolled a )[1-6]"));
        tests.add(new TestCase(new String[] {}, "(You rolled a )[1-6]"));
        tests.add(new TestCase(new String[] {}, "(You rolled a )[1-6]"));
        tests.add(new TestCase(new String[] {}, "(You rolled a )[1-6]"));

        tests.add(new TestCase(new String[] {"-6"}, "(You rolled a -)[1-6]"));
        tests.add(new TestCase(new String[] {"-6"}, "(You rolled a -)[1-6]"));
        tests.add(new TestCase(new String[] {"-6"}, "(You rolled a -)[1-6]"));
        tests.add(new TestCase(new String[] {"-6"}, "(You rolled a -)[1-6]"));
        tests.add(new TestCase(new String[] {"-6"}, "(You rolled a -)[1-6]"));

        tests.add(new TestCase(new String[] {"0"}, "(You rolled a 0. Surprise, dumbass!)"));

        tests.add(new TestCase(new String[] {"1000"}, "(You rolled a )[0-9]{1,4}"));
        tests.add(new TestCase(new String[] {"1000"}, "(You rolled a )[0-9]{1,4}"));
        tests.add(new TestCase(new String[] {"1000"}, "(You rolled a )[0-9]{1,4}"));
        tests.add(new TestCase(new String[] {"1000"}, "(You rolled a )[0-9]{1,4}"));
        tests.add(new TestCase(new String[] {"1000"}, "(You rolled a )[0-9]{1,4}"));

        tests.add(new TestCase(new String[] {"a"}, "(\\[Error\\] a is not a valid number\\.)"));
        tests.add(new TestCase(new String[] {"1a0"}, "(\\[Error\\] 1a0 is not a valid number\\.)"));
        tests.add(new TestCase(new String[] {"a0"}, "(\\[Error\\] a0 is not a valid number\\.)"));
    }
    @Test
    public void roll() throws Exception {
        RollDice rollDice = new RollDice();

        for (TestCase testCase : tests) {
            assertTrue(rollDice.roll(testCase.inputs).matches(testCase.output));
        }
    }

}