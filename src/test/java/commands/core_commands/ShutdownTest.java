package commands.core_commands;

import org.junit.Before;
import org.junit.Test;
import test_helper.GTestCase;
import static test_helper.FakeStuff.*;

import java.util.ArrayList;
import java.util.List;

public class ShutdownTest {
    private List<GTestCase> tests = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        String[] args = new String[] {};
        tests.add(new GTestCase(getGMREvent(false, ""), args, ""));
    }

    @Test
    public void runCommand() throws Exception {
        //TODO
    }

}