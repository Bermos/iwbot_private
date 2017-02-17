package commands.core_commands;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class StatusTest {
    private List<TestCase> tests;
    class TestCase {
        TestCase(String o) {
            output = o;
        }
        String   output;
    }

    @Before
    public void setUp() throws Exception {
        tests = new ArrayList<>();

        String uptime   = "```Uptime              \\| ([0-9]*d )?[0-9]{2}:[0-9]{2}:[0-9]{2}\n" +
            "# Threads           \\| [0-9]*\n" +
            "Memory usage        \\| [0-9]*\\.[0-9]{2}\\/[0-9]*\\.[0-9]{2} MB\n" +
            "Unique AI Datasets  \\| ([0-9]{1,3}('[0-9]{3})*)\n" +
            "Total AI Datasets   \\| ([0-9]{1,3}('[0-9]{3})*)\n" +
            "Version             \\| [0-9]*\\.[0-9]*\\.[0-9]*_[0-9]*```";

        tests.add(new TestCase(uptime));
    }

    @Test
    public void status() throws Exception {
        Status status = new Status();

        for (TestCase testCase : tests) {
            assertTrue(status.status().matches(testCase.output));
        }
    }

}