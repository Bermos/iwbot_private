import commands.RunCoreCmds;
import commands.RunMiscCmds;
import iw_bot.CommandsTest;
import iw_bot.Listener;
import iw_bot.ListenerTest;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        RunCoreCmds.class,
        RunMiscCmds.class,
        CommandsTest.class,
        ListenerTest.class})

public class RunAll {
    @Before
    public void setUp() {
        System.out.println("hey");
        Listener.isTest = true;
    }

}