package commands;

import commands.core_commands.StatusTest;
import commands.core_commands.TopicTest;
import commands.core_commands.UTCTimeTest;
import commands.core_commands.WelcomeTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        StatusTest.class,
        TopicTest.class,
        UTCTimeTest.class,
        WelcomeTest.class })

public class RunCoreCmds {
}
