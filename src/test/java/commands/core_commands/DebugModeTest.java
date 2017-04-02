package commands.core_commands;

import iw_bot.Listener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import provider.DataProvider;
import test_helper.GTestCase;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static test_helper.FakeStuff.getGMREvent;

public class DebugModeTest {
    private List<GTestCase> tests = new ArrayList<>();
    private boolean previousDebugValue;

    @Before
    public void setUp() throws Exception {
        String[] args = new String[] {};
        tests.add(new GTestCase(getGMREvent(false, ""), args, "[Error] You aren't authorized to do this"));
        tests.add(new GTestCase(getGMREvent(true,  ""), args, "Debug mode: true"));
        tests.add(new GTestCase(getGMREvent(false, ""), args, "[Error] You aren't authorized to do this"));
        tests.add(new GTestCase(getGMREvent(true,  ""), args, "Debug mode: false"));

        previousDebugValue = Listener.isDebug;
        Listener.isDebug = false;
    }

    @Test
    public void runCommand() throws Exception {
        DebugMode debugMode = new DebugMode();

        for (GTestCase tc : tests) {
            debugMode.runCommand(tc.event, tc.args);
            assertEquals(tc.output, DataProvider.lastMessageSent);
        }
    }

    @After
    public void tearDown() throws Exception {
        Listener.isDebug = previousDebugValue;
    }

}