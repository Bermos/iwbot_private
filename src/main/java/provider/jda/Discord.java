package provider.jda;

import iw_bot.Listener;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.Role;
import provider.jda.channel.Channel;
import provider.jda.channel.GuildChannel;
import provider.jda.channel.PrivateChannel;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This is the core of the jda wrapper. It primarily wraps the JDA class but has some additional stuff
 */
public class Discord {
    private JDA jda;
    private Listener listener;

    public Discord(JDA jda, Listener listener) {
        this.jda = jda;
        this.listener = listener;
    }

    public Listener getListener() {
        return listener;
    }

    // Guild messages
    public void sendGMessageAsync(String chanId, String message) {
        jda.getTextChannelById(chanId).sendMessage(message).queue();
    }

    public void sendGMessageAsync(String chanId, MessageEmbed embed) {
        jda.getTextChannelById(chanId).sendMessage(embed).queue();
    }

    public Message sendGMessageNow(String chanId, String message) {
        return jda.getTextChannelById(chanId).sendMessage(message).complete();
    }

    public Message sendGMessageNow(String chanId, MessageEmbed embed) {
        return jda.getTextChannelById(chanId).sendMessage(embed).complete();
    }

    // Private messages
    public void sendPMessageAsync(String chanId, String message) {
        jda.getPrivateChannelById(chanId).sendMessage(message).queue();
    }

    public void sendPMessageAsync(String chanId, MessageEmbed embed) {
        jda.getPrivateChannelById(chanId).sendMessage(embed).queue();
    }

    public Message sendPMessageNow(String chanId, String message) {
        return jda.getPrivateChannelById(chanId).sendMessage(message).complete();
    }

    public Message sendPMessageNow(String chanId, MessageEmbed embed) {
        return jda.getPrivateChannelById(chanId).sendMessage(embed).complete();
    }

    // More Stuff
    public Guild getGuildById(String id) {
        return new Guild(id, this);
    }

    public void shutdown() {
        jda.shutdown();
    }

    public Member getMember(String guildId, String id) {
        return new Member(id, this, guildId);
    }

    public String[] getRoleIdsForMember(String id, String guildId) {
        List<Role> roles = jda.getGuildById(guildId).getMember(jda.getUserById(id)).getRoles();
        return (String[]) roles.stream().map(role -> role.getId()).collect(Collectors.toList()).toArray();
    }

    public String getName(String id) {
        return jda.getUserById(id).getName();
    }

    public String getEffectiveName(String id, String guildId) {
        return jda.getGuildById(guildId).getMemberById(id).getEffectiveName();
    }

    public void sendPTyping(String id) {
        jda.getPrivateChannelById(id).sendTyping().queue();
    }

    public void sendGTyping(String id) {
        jda.getTextChannelById(id).sendTyping().queue();
    }

    public Iterable<? extends provider.jda.Role> getRolesByName(String id, String name, boolean exact) {
        return jda.getGuildById(id).getRolesByName(name, !exact).stream().map(role -> new provider.jda.Role(role.getId(), id, this)).collect(Collectors.toList());
    }

    // Other stuff

    public String getRoleName(String id, String guildId) {
        return jda.getGuildById(guildId).getRoleById(id).getName();
    }

    public void deleteRoleAsync(String id, String guildId) {
        jda.getGuildById(guildId).getRoleById(id).delete().queue();
    }

    public List<Channel> getMentionedChannels(String id, Channel channel) {
        if (channel instanceof PrivateChannel)
            return null;

        return jda.getTextChannelById(channel.getId()).getMessageById(id).complete().getMentionedChannels().stream()
                    .map(chan -> new GuildChannel(chan.getId(), this)).collect(Collectors.toList());
    }
}
