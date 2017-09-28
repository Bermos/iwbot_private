package commands;

import commands.core_commands.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        MessageChannelTest.class,
        DebugModeTest.class,
        ShutdownTest.class,
        //StatusTest.class,
        TopicTest.class,
        UTCTimeTest.class,
        WelcomeTest.class })

public class RunCoreCmds {
}
