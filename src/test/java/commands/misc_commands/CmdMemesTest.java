package commands.misc_commands;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.impl.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.http.HttpHost;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class CmdMemesTest {
    @org.junit.Before
    public void setUp() throws Exception {

    }

    @Test
    public void getHelp() {
        Memes memes = new Memes(); // Class to be tested

        JDAImpl jda = new JDAImpl(AccountType.BOT, new HttpHost("0"), false, false, false, false);
        GuildImpl guildimpl = new GuildImpl(jda,"0");
        TextChannelImpl textimpl = new TextChannelImpl("0", guildimpl);

        User user = new UserImpl("50", jda);

        Message mess = new MessageImpl("Hi", textimpl, true).setAuthor(user);
        GuildMessageReceivedEvent event = new GuildMessageReceivedEvent(jda, 0, mess);

        System.out.println(memes.getHelp(event));
        assertNotNull(memes.getHelp(event));
    }

}