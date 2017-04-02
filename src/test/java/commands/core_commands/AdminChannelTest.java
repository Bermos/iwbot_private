package commands.core_commands;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import provider.DataProvider;
import test_helper.GTestCase;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static test_helper.FakeStuff.getGMREvent;

public class AdminChannelTest {
    private List<GTestCase> tests = new ArrayList<>();
    private DataProvider.Info infoBackup;

    @Before
    public void setUp() throws Exception {
        infoBackup = DataProvider.getInfoBackup();

        tests.add(new GTestCase(getGMREvent("0", "0", ""), new String[] {},"[Error] You aren't authorized to do this"));
    }

    @After
    public void tearDown() throws Exception {
        DataProvider.revertToBackup(infoBackup);
    }

    @Test
    public void runCommand() throws Exception {
        AdminChannel adminChannel = new AdminChannel();

        for (GTestCase tc : tests) {
            //adminChannel.runCommand(tc.event, tc.args);
            //assertEquals(tc.output, DataProvider.lastMessageSent);
        }
    }

}