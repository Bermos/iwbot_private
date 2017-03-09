package provider.jda.channel;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import provider.jda.Discord;

public class PrivateChannel implements Channel {
    String id;
    Discord discord;

    public PrivateChannel(String id, Discord discord) {
        this.id = id;
        this.discord = discord;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Discord getDiscord() {
        return discord;
    }

    @Override
    public void sendMessageAsync(String message) {
        discord.sendPMessageAsync(id, message);
    }

    @Override
    public void sendMessageAsync(MessageEmbed embed) {
        discord.sendPMessageAsync(id, embed);
    }

    @Override
    public Message sendMessage(String message) {
        return discord.sendPMessageNow(id, message);
    }

    @Override
    public Message sendMessage(MessageEmbed embed) {
        return discord.sendPMessageNow(id, embed);
    }
}
