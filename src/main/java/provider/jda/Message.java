package provider.jda;

import provider.jda.channel.Channel;

import java.util.List;

public class Message {
    private String id;
    private Channel channel;
    private Discord discord;

    public Message(String id,
                   Channel channel,
                   Discord discord) {
        this.id = id;
        this.channel = channel;
        this.discord = discord;
    }

    public List<Channel> getMentionedChannels() {
        return discord.getMentionedChannels(id, channel);
    }

    public List<User> getMentionedUsers() {
        return discord.getMentionedUsers(id, channel);
    }

    public List<Role> getMentionedRoles() {
        return discord.getMentionedRoles(id, channel);
    }
}
