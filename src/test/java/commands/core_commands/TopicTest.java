package commands.core_commands;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.impl.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.http.HttpHost;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class TopicTest {
    private List<GuildMessageReceivedEvent> tests = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        JDAImpl jda = new JDAImpl(AccountType.BOT, new HttpHost("0"), false, false, false, false);
        Guild guild = new GuildImpl(jda,"0");

        TextChannel channel = new TextChannelImpl("0", guild).setTopic("This is a topic");
        Message mess = new MessageImpl("0", channel, true);

        tests.add(new GuildMessageReceivedEvent(jda, 0, mess));

        channel = new TextChannelImpl("", guild).setTopic("");
        mess = new MessageImpl("0", channel, true);
        tests.add(new GuildMessageReceivedEvent(jda, 0, mess));
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void runCommand() throws Exception {
        Topic topic = new Topic();
        //TODO redo, it's shit right now

        for (GuildMessageReceivedEvent test : tests) {
            topic.runCommand(test, new String[] {});
        }
    }

}