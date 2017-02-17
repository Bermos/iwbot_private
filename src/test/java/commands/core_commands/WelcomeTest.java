package commands.core_commands;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class WelcomeTest {
    private List<TestCase> tests;
    class TestCase {
        String   name;
        String[] roleIds;
        String[] inputs;
        String   output;

        TestCase(String name, String[] roleIds, String[] inputs, String output) {
            this.name = name;
            this.roleIds = roleIds;
            this.inputs = inputs;
            this.output = output;
        }
    }

    @Before
    public void setUp() throws Exception {
        tests = new ArrayList<>();

        tests.add(new TestCase("", new String[] {"0"}, new String[] {}, "[Error] You aren't authorized to do this"));
        tests.add(new TestCase("", new String[] {"0"}, new String[] {""}, "[Error] You aren't authorized to do this"));
        tests.add(new TestCase("", new String[] {"0"}, new String[] {"Hey", "<user>"}, "[Error] You aren't authorized to do this"));

        tests.add(new TestCase("Testname", new String[] {"1"}, new String[] {}, "[Error] No welcome message set."));

        tests.add(new TestCase("Testname", new String[] {"1"}, new String[] {"Welcome <user>"}, "[Success] New member message changed"));
        tests.add(new TestCase("Testname", new String[] {"1"}, new String[] {}, "Welcome Testname"));

        tests.add(new TestCase("Testname", new String[] {"1"}, new String[] {"Welcome", "<user>"}, "[Success] New member message changed"));
        tests.add(new TestCase("Testname", new String[] {"1"}, new String[] {}, "Welcome, Testname"));

        tests.add(new TestCase("Testname", new String[] {"1"}, new String[] {""}, "[Success] New member message changed"));
        tests.add(new TestCase("Testname", new String[] {"1"}, new String[] {}, "[Error] No welcome message set."));
    }

    @Test
    public void welcome() throws Exception {
        Welcome welcome = new Welcome();

        for (TestCase tc : tests) {
            assertEquals(tc.output, welcome.welcome(tc.name, tc.roleIds, tc.inputs));
        }
    }

}