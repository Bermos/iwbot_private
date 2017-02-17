package commands;

import commands.misc_commands.MemesTest;
import commands.misc_commands.NotesTest;
import commands.misc_commands.ReminderTest;
import commands.misc_commands.RollDiceTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        MemesTest.class,
        NotesTest.class,
        ReminderTest.class,
        RollDiceTest.class })

public class RunMiscCmds {
}
