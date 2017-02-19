package commands.core_commands;

import org.junit.Before;
import org.junit.Test;
import test_helper.TestCase;
import static test_helper.FakeStuff.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ShutdownTest {
    private List<TestCase> tests = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        String[] args = new String[] {};
        tests.add(new TestCase(getGMREvent(false, "0", ""), args, ""));
    }

    @Test
    public void runCommand() throws Exception {

    }

}