package iw_bot;

import commands.GuildCommand;
import commands.PMCommand;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import provider.DataProvider;
import test_helper.GTestCase;
import test_helper.PTestCase;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static test_helper.FakeStuff.getGMREvent;
import static test_helper.FakeStuff.getPMREvent;

public class ListenerTest {
    List<PTestCase> pTestCases = new ArrayList<>();
    List<GTestCase> gTestCases = new ArrayList<>();
    PrintStream og;

    @Before
    public void setUp() throws Exception {
        Commands cmd = Listener.getCommands();
        cmd.pmCommands.put("test", new PMCommand() {
            @Override
            public void runCommand(PrivateMessageReceivedEvent event, String[] args) {
                if (args.length == 0)
                    event.getChannel().sendMessage("Result: null").queue();
                else
                    event.getChannel().sendMessage("Result: " + args[0]).queue();
            }
        });
        cmd.guildCommands.put("test", new GuildCommand() {
            @Override
            public void runCommand(GuildMessageReceivedEvent event, String[] args) {
                if (args.length == 0)
                    event.getChannel().sendMessage("Result: null").queue();
                else
                    event.getChannel().sendMessage("Result: " + args[0]).queue();
            }

            @Override
            public String getHelp(GuildMessageReceivedEvent event) {
                return "";
            }
        });

        PrintStream fake = new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {

            }
        });
        og = System.out;
        System.setOut(fake);

        // Add PM test cases
        pTestCases.add(new PTestCase(getPMREvent("0", "0", "/test"),   new String[]{}, "Result: null"));
        pTestCases.add(new PTestCase(getPMREvent("0", "0", "/test 0"), new String[]{}, "Result: 0"));
        pTestCases.add(new PTestCase(getPMREvent("0", "0", "/test 1"), new String[]{}, "Result: 1"));

        // Add guild tests cases
        gTestCases.add(new GTestCase(getGMREvent(true, "0", "/test"),   new String[]{}, "Result: null"));
        gTestCases.add(new GTestCase(getGMREvent(true, "0", "/test 0"), new String[]{}, "Result: 0"));
        gTestCases.add(new GTestCase(getGMREvent(true, "0", "/test 1"), new String[]{}, "Result: 1"));
    }

    @After
    public void tearDown() throws Exception {
        Commands cmd = Listener.getCommands();
        cmd.pmCommands.remove("test");
        cmd.guildCommands.remove("test");

        System.setOut(og);
    }

    @Test
    public void onPrivateMessageReceived() throws Exception {
        Listener listener = new Listener();

        for (PTestCase tCase : pTestCases) {
            listener.onPrivateMessageReceived(tCase.event);
            assertEquals(tCase.output, DataProvider.lastMessageSent);
        }
    }

    @Test
    public void onGuildMessageReceived() throws Exception {
        Listener listener = new Listener();

        for (GTestCase tCase : gTestCases) {
            System.out.println(tCase.event.getMember().getEffectiveName());
            listener.onGuildMessageReceived(tCase.event);
            assertEquals(tCase.output, DataProvider.lastMessageSent);
        }
    }

}