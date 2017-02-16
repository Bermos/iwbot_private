package iw_bot;

import commands.GuildCommand;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.impl.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.http.HttpHost;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class CommandsTest {

    @Test
    public void runTest() {
        Commands commands = new Commands();

        JDAImpl jda = new JDAImpl(AccountType.BOT, new HttpHost("0"), false, false, false, false);
        GuildImpl guildimpl = new GuildImpl(jda,"0");
        TextChannelImpl textimpl = new TextChannelImpl("0", guildimpl);

        User user = new UserImpl("1", jda);

        Message mess = new MessageImpl("0", textimpl, true).setAuthor(user);
        GuildMessageReceivedEvent event = new GuildMessageReceivedEvent(jda, 0, mess);

        for (Map.Entry<String, GuildCommand> entry : commands.guildCommands.entrySet()) {
            assertNotNull(entry.getValue().getHelp(event));
        }
    }
}