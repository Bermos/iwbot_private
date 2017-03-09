package test_helper;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.impl.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import org.apache.http.HttpHost;

public class FakeStuff {
    public static JDAImpl getJDA() {
        return new JDAImpl(AccountType.BOT, new HttpHost("0"), false, false, false, false);
    }

    public static GuildImpl getGuild() {
        return new GuildImpl(getJDA(),"0");
    }

    public static Guild getGuild(JDAImpl jda) {
        return new GuildImpl(jda,"0");
    }

    public static FakeTextChannel getTextChannel(String id, User user, boolean isAdmin) {
        GuildImpl guild = getGuild();
        guild.getController().addRolesToMember(guild.getMember(user), new RoleImpl(isAdmin ? "1" : "0", guild)).complete();
        return new FakeTextChannel(id, getGuild());
    }

    public static FakeTextChannel getTextChannel(String id) {
        return new FakeTextChannel(id, getGuild());
    }

    public static FakePrivateChannel getPrivateChannel(String id, String userId) {
        return new FakePrivateChannel(id, new UserImpl(userId, getJDA()));
    }

    public static FakeTextChannel getTextChannel(String id, Guild guild) {
        return new FakeTextChannel(id, guild);
    }

    public static Message getMessage(String id, String content, boolean isOwner) {
        User user = new UserImpl(isOwner ? "1" : "0", getJDA());
        return new MessageImpl(id, getTextChannel("0"), true).setContent(content).setAuthor(user);
    }

    public static Message getMessage(String id, String content, boolean isOwner, boolean isAdmin) {
        User user = new UserImpl(isOwner ? "1" : "0", getJDA());
        return new MessageImpl(id, getTextChannel("0", user, isAdmin), true).setContent(content).setAuthor(user);
    }

    public static Message getMessage(String mesId, String chanId, String content, String userId, boolean isPrivate) {
        if (isPrivate)
            return new MessageImpl(mesId, getPrivateChannel(chanId, userId), true).setContent(content).setAuthor(new UserImpl(userId, getJDA()));
        else
            return new MessageImpl(mesId, getTextChannel(chanId), true).setContent(content).setAuthor(new UserImpl(userId, getJDA()));
    }

    public static Message getMessage(String userId, String content, String effName, boolean isPrivate) {
        JDAImpl jda = getJDA();
        GuildImpl guild = new GuildImpl(jda, "0");
        User user = new UserImpl(userId, jda).setName(effName);
        guild.getMembersMap().put(userId, new MemberImpl(guild, user));
        if (isPrivate)
            return new MessageImpl("0", getPrivateChannel("0", userId), true).setContent(content).setAuthor(user);
        else
            return new MessageImpl("0", new FakeTextChannel("0", guild), true).setContent(content).setAuthor(user);
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

    public static GuildMessageReceivedEvent getGMREvent(boolean isOwner, boolean isAdmin, String content) {
        return new GuildMessageReceivedEvent(getJDA(), 0, getMessage("0", content, isOwner, isAdmin));
    }

    public static GuildMessageReceivedEvent getGMREvent(boolean isOwner, String content) {
        return new GuildMessageReceivedEvent(getJDA(), 0, getMessage("0", content, isOwner));
    }

    public static GuildMessageReceivedEvent getGMREvent(boolean isOwner, String dunno, String content) {
        return new GuildMessageReceivedEvent(getJDA(), 0, getMessage(isOwner ? "1" : "0", content, "generic user", false));
    }

    public static GuildMessageReceivedEvent getGMREvent(String userId, String channelId, String content) {
        return new GuildMessageReceivedEvent(getJDA(), 0, getMessage("0", channelId, content, userId, false));
    }

    public static PrivateMessageReceivedEvent getPMREvent(String userId, String chanId, String content) {
        return new PrivateMessageReceivedEvent(getJDA(), 0, getMessage("0", chanId, content, userId, true));
    }

    public static PrivateMessageReceivedEvent getPMREvent(String userId, String content) {
        return new PrivateMessageReceivedEvent(getJDA(), 0, getMessage(userId, content, "generic user", true));
    }

    public static String[] getArgs() {
        return new String[] {};
    }
}
