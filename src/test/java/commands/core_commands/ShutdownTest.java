package commands.core_commands;

import org.junit.Before;
import org.junit.Test;
import provider.DataProvider;
import test_helper.GTestCase;
import test_helper.PTestCase;

import static org.junit.Assert.assertEquals;
import static test_helper.FakeStuff.*;

import java.util.ArrayList;
import java.util.List;

public class ShutdownTest {
    private List<GTestCase> gtests = new ArrayList<>();
    private List<PTestCase> ptests = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        String[] args = new String[] {};
        gtests.add(new GTestCase(getGMREvent(false, ""), args, "[Error] You aren't authorized to do this"));

        ptests.add(new PTestCase(getPMREvent("0", "", ""), args, "[Error] You aren't authorized to do this"));
    }

    @Test
    public void runCommand() throws Exception {
        Shutdown shutdown = new Shutdown();

        for (GTestCase testCase : gtests) {
            shutdown.runCommand(testCase.event, testCase.args);
            assertEquals(testCase.output, DataProvider.lastMessageSent);
        }

        for (PTestCase testCase : ptests) {
            shutdown.runCommand(testCase.event, testCase.args);
            assertEquals(testCase.output, DataProvider.lastMessageSent);
        }
    }

}