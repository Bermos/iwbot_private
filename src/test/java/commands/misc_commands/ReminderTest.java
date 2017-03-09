package commands.misc_commands;

import iw_bot.Listener;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ReminderTest {
    private List<TestCase> tests;
    class TestCase {
        TestCase(String u, String[] i, String o) {
            userId = u; inputs = i; output = o;
        }
        String   userId;
        String[] inputs;
        String   output;
    }

    @Before
    public void setUp() throws Exception {
        tests = new ArrayList<>();

        tests.add(new TestCase("0", new String[] {}, "[Error] Please specify a time frame number(s|m|h|d|w|y)"));
        tests.add(new TestCase("0", new String[] {"0"}, "[Error] Please specify the time unit (s|m|h|d|w|y)"));
        tests.add(new TestCase("0", new String[] {"10"}, "[Error] Please specify the time unit (s|m|h|d|w|y)"));
        tests.add(new TestCase("0", new String[] {"d"}, "[Error] Incompatible time format, please use: number(s|m|h|d|w|y)"));

        tests.add(new TestCase("0", new String[] {"10d"}, "Reminder set"));
        tests.add(new TestCase("0", new String[] {"10d", "a reason"}, "Reminder set"));
        tests.add(new TestCase("0", new String[] {"10d", "a reason", "with a comma"}, "Reminder set"));
        Listener.isTest = true;
    }

    @Test
    public void reminder() throws Exception {
        Reminder reminder = new Reminder();

        for (TestCase testCase : tests) {
            assertEquals(testCase.output, reminder.reminder(testCase.userId, testCase.inputs));
        }
    }

}