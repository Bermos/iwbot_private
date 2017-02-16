package commands.core_commands;

import commands.misc_commands.RollDice;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class UTCTimeTest {
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

        tests.add(new TestCase(new String[] {}, "(UTC time:\\n)[0-9]{2}\\/[0-9]{2}\\/[0-9]{4} [0-9]{2}:[0-9]{2}:[0-9]{2}"));
    }

    @Test
    public void time() throws Exception {
        UTCTime time = new UTCTime();

        for (TestCase testCase : tests) {
            assertTrue(time.time().matches(testCase.output));
        }
    }

}