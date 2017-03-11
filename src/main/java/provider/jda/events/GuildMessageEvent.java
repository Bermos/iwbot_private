package provider.jda.events;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import provider.jda.Discord;
import provider.jda.Guild;
import provider.jda.Member;
import provider.jda.User;
import provider.jda.channel.Channel;
import provider.jda.channel.GuildChannel;
import provider.jda.channel.PrivateChannel;
import provider.jda.events.MessageEvent;

public class GuildMessageEvent implements MessageEvent {
    private Discord discord;
    private GuildChannel channel;
    private Guild guild;
    private User author;
    private String[] args;

    public GuildMessageEvent(GuildMessageReceivedEvent event,
                             Discord discord,
                             String[] args) {
        this.discord = discord;
        this.channel = new GuildChannel(event.getChannel().getId(), discord);
        this.guild   = new Guild(event.getGuild().getId(), discord);
        this.author  = new User(event.getAuthor().getId(), discord);
        this.args    = args;

    }

    public GuildMessageEvent(Discord discord) {
        this.discord = discord;
    }

    @Override
    public Discord getDiscord() {
        return discord;
    }

    @Override
    public Channel getChannel() {
        return channel;
    }

    @Override
    public User getAuthor() {
        return author;
    }

    @Override
    public String[] getArgs() {
        return args;
    }

    @Override
    public void replyAsync(String message) {
        discord.sendGMessageAsync(channel.getId(), message);
    }

    @Override
    public void replyAsync(MessageEmbed embed) {
        discord.sendGMessageAsync(channel.getId(), embed);
    }

    @Override
    public Message replyNow(String message) {
        return discord.sendGMessageNow(channel.getId(), message);
    }

    @Override
    public Message replyNow(MessageEmbed embed) {
        return discord.sendGMessageNow(channel.getId(), embed);
    }

    public Member getMember() {
        return discord.getMember(guild.getId(), author.getId());
    }

    public Guild getGuild() {
        return guild;
    }
}
