package commands.core_commands;

import fake_testing_classes.FakeTextChannel;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.impl.GuildImpl;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.entities.impl.MessageImpl;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.http.HttpHost;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import provider.DataProvider;
import test_helper.FakeStuff;
import test_helper.TestCase;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class TopicTest {
    private List<TestCase> tests = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        JDAImpl jda = FakeStuff.getJDA();
        Guild guild = FakeStuff.getGuild();

        FakeTextChannel channel = new FakeTextChannel("0", guild).setTopic("This is a topic");
        Message mess = new MessageImpl("0", channel, true);

        String[] args = new String[] {};

        tests.add(new TestCase(new GuildMessageReceivedEvent(jda, 0, mess), args, "This is a topic"));

        channel = new FakeTextChannel("", guild).setTopic("");
        mess = new MessageImpl("0", channel, true);
        tests.add(new TestCase(new GuildMessageReceivedEvent(jda, 0, mess), args, "This channel has no topic."));
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void runCommand() throws Exception {
        Topic topic = new Topic();

        for (TestCase test : tests) {
            topic.runCommand(test.event, test.args);
            assertEquals(test.output, DataProvider.lastMessageSent);
        }
    }

}