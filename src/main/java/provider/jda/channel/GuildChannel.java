package provider.jda.channel;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import provider.jda.Discord;

public class GuildChannel implements Channel {
    private String id;
    private Discord discord;

    public GuildChannel(String id, Discord discord) {
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
        discord.sendGMessageAsync(id, message);
    }

    @Override
    public void sendMessageAsync(MessageEmbed embed) {
        discord.sendGMessageAsync(id, embed);
    }

    @Override
    public Message sendMessage(String message) {
        return discord.sendGMessageNow(id, message);
    }

    @Override
    public Message sendMessage(MessageEmbed embed) {
        return discord.sendGMessageNow(id, embed);
    }

    @Override
    public void sendTyping() {
        discord.sendGTyping(id);
    }
}
