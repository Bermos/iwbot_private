import commands.RunCoreCmds;
import commands.RunMiscCmds;
import iw_bot.CommandsTest;
import iw_bot.Listener;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        RunCoreCmds.class,
        RunMiscCmds.class,
        CommandsTest.class})

public class RunAll {
    @Before
    public void setUp() {
        Listener.isTest = true;
    }

}