package provider.jda.channel;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import provider.jda.Discord;
import provider.jda.Guild;

public class GuildChannel implements Channel {
    private String id;
    private Guild guild;
    private Discord discord;

    public GuildChannel(String id, Guild guild, Discord discord) {
        this.id = id;
        this.guild = guild;
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

    public String getTopic() {
        return discord.getChannelTopic(id);
    }
}
