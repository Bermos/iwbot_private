package commands.core_commands;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import provider.DataProvider;
import provider.DataProvider.Info;
import test_helper.PTestCase;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static test_helper.FakeStuff.getPMREvent;

public class EditOwnerTest {
    private List<PTestCase> tests = new ArrayList<>();
    private Info infoBackup;

    @Before
    public void setUp() throws Exception {
        infoBackup = DataProvider.getInfoBackup();

        tests.add(new PTestCase(getPMREvent("0", "0", ""), new String[] {},           "[Error] You aren't authorized to do this"));

        tests.add(new PTestCase(getPMREvent("1", "0", ""), new String[] {},           "[Error] unexpected/no arguments provided"));
        tests.add(new PTestCase(getPMREvent("1", "0", ""), new String[] {""},         "[Error] unexpected/no arguments provided"));
        tests.add(new PTestCase(getPMREvent("1", "0", ""), new String[] {"", "", ""}, "[Error] unexpected/no arguments provided"));

        tests.add(new PTestCase(getPMREvent("1", "0", ""), new String[] {"add", "2"}, "Owner added"));
        tests.add(new PTestCase(getPMREvent("0", "0", ""), new String[] {},           "[Error] You aren't authorized to do this"));
        tests.add(new PTestCase(getPMREvent("2", "0", ""), new String[] {},           "[Error] unexpected/no arguments provided"));

        tests.add(new PTestCase(getPMREvent("2", "0", ""), new String[] {"del", "2"}, "Owner removed"));
        tests.add(new PTestCase(getPMREvent("2", "0", ""), new String[] {"del", "2"}, "[Error] You aren't authorized to do this"));
        tests.add(new PTestCase(getPMREvent("1", "0", ""), new String[] {"del", "2"}, "Id not found. Nothing changed"));

        tests.add(new PTestCase(getPMREvent("1", "0", ""), new String[] {"", ""},     "[Error] unexpected arguments provided"));
    }

    @After
    public void tearDown() throws Exception {
        DataProvider.revertToBackup(infoBackup);
    }

    @Test
    public void runCommand() throws Exception {
        EditOwner editOwner = new EditOwner();

        for (PTestCase tc : tests) {
            editOwner.runCommand(tc.event, tc.args);
            assertEquals(tc.output, DataProvider.lastMessageSent);
        }
    }

}