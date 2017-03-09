package provider.jda;

import iw_bot.Listener;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.Role;

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

    public String[] getRolesForMember(String id, String guildId) {
        List<Role> roles = jda.getGuildById(guildId).getMember(jda.getUserById(id)).getRoles();
        return (String[]) roles.stream().map(role -> role.getId()).collect(Collectors.toList()).toArray();
    }

    // Other stuff
}
