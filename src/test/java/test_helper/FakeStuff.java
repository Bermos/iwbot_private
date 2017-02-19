package test_helper;

import fake_testing_classes.FakeTextChannel;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.impl.GuildImpl;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.entities.impl.MessageImpl;
import net.dv8tion.jda.core.entities.impl.UserImpl;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.http.HttpHost;

public class FakeStuff {
    public static JDAImpl getJDA() {
        return new JDAImpl(AccountType.BOT, new HttpHost("0"), false, false, false, false);
    }

    public static Guild getGuild() {
        return new GuildImpl(getJDA(),"0");
    }

    public static Guild getGuild(JDAImpl jda) {
        return new GuildImpl(jda,"0");
    }

    public static FakeTextChannel getTextChannel(String id) {
        return new FakeTextChannel(id, getGuild());
    }

    public static FakeTextChannel getTextChannel(String id, Guild guild) {
        return new FakeTextChannel(id, guild);
    }

    public static Message getMessage(String id, String content) {
        return new MessageImpl(id, getTextChannel("0"), true).setContent(content);
    }

    public static Message getMessage(String id, String content, boolean isAdmin) {
        return new MessageImpl(id, getTextChannel("0"), true).setContent(content).setAuthor(new UserImpl(isAdmin ? "1" : "0", getJDA()));
    }

    public static Message getMessage(String mesId, String chanId, String content) {
        return new MessageImpl(mesId, getTextChannel(chanId), true).setContent(content);
    }

    public static Message getMessage(String mesId, FakeTextChannel chan, String content) {
        return new MessageImpl(mesId, chan, true).setContent(content);
    }

    public static GuildMessageReceivedEvent getGMREvent(String channelId, String content) {
        return new GuildMessageReceivedEvent(getJDA(), 0, getMessage("0", getTextChannel(channelId), content));
    }

    public static GuildMessageReceivedEvent getGMREvent(boolean isAdmin, String channelId, String content) {
        return new GuildMessageReceivedEvent(getJDA(), 0, getMessage("0", content, isAdmin));
    }

    public static String[] getArgs() {
        return new String[] {};
    }
}