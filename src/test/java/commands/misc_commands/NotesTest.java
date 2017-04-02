package commands.misc_commands;

import iw_bot.Listener;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class NotesTest {
    private List<TestCase> tests;
    class TestCase {
        TestCase(String u, String[] r, String[] i, String o) {
            userId = u; roleId = r; inputs = i; output = o;
        }
        String   userId;
        String[] roleId;
        String[] inputs;
        String   output;
    }

    @Before
    public void setUp() throws Exception {
        tests = new ArrayList<>();

        tests.add(new TestCase("0", new String[] {"0"}, new String[] {}, "'/note' help : [add|edit|del], [public], notes name, notes content"));
        tests.add(new TestCase("0", new String[] {"0"}, new String[] {""}, "Testname's note:\nString from sql"));

        tests.add(new TestCase("0", new String[] {"0"}, new String[] {"add", "", "1", ""}, "[Error] You are not authorised to make public notes."));
        tests.add(new TestCase("0", new String[] {"1"}, new String[] {"add", "", "1", ""}, "Saved"));
        tests.add(new TestCase("0", new String[] {"0"}, new String[] {"add", "", ""}, "Saved"));
        tests.add(new TestCase("0", new String[] {"1"}, new String[] {"add", "", ""}, "Saved"));

        tests.add(new TestCase("0", new String[] {"0"}, new String[] {"edit", ""}, "Seems like you forgot to put the name or the new content in your message."));
        tests.add(new TestCase("0", new String[] {"0"}, new String[] {"edit", "", ""}, "Edited"));

        tests.add(new TestCase("0", new String[] {"0"}, new String[] {"del", ""}, "Deleted"));
        Listener.isTest = true;
    }

    @Test
    public void notes() throws Exception {
        Notes notes = new Notes();

        for (TestCase testCase : tests) {
            assertEquals(testCase.output, notes.notes(testCase.userId, "Testname", testCase.roleId, testCase.inputs));
        }
    }

}