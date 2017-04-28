package provider.jda.events;

import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import provider.jda.Discord;
import provider.jda.Message;
import provider.jda.User;
import provider.jda.channel.PrivateChannel;

public class PrivateMessageEvent implements MessageEvent {
    private Discord discord;
    private PrivateChannel channel;
    private Message message;
    private User author;
    private String[] args;

    public PrivateMessageEvent(PrivateMessageReceivedEvent event,
                               Discord discord,
                               String[] args) {
        this.discord = discord;
        this.channel = new PrivateChannel(event.getChannel().getId(), discord);
        this.message = new Message(event.getMessage().getId(), this.channel, discord);
        this.author = new User(event.getAuthor().getId(), discord);
        this.args = args;
    }

    public PrivateMessageEvent(Discord discord,
                               PrivateChannel channel,
                               User author,
                               String[] args) {
        this.discord = discord;
        this.channel = channel;
        this.author = author;
        this.args = args;
    }

    /**
     * Get the discord object associated with the event
     *
     * @return discord object
     */
    public Discord getDiscord() {
        return discord;
    }

    /**
     * Get the channel in which the messsage was sent
     *
     * @return channel
     */
    public PrivateChannel getChannel() {
        return channel;
    }

    /**
     * Get the user that wrote the message
     *
     * @return user/author
     */
    public User getAuthor() {
        return author;
    }

    /**
     * Get the split args that were passed with the command
     *
     * @return the args as String array
     */
    public String[] getArgs() {
        return args;
    }

    @Override
    public Message getMessage() {
        return message;
    }

    /**
     * Sends a message to the channel in the event
     *
     * @param message the message to send
     */
    public void replyAsync(String message) {
        discord.sendPMessageAsync(channel.getId(), message);
    }

    /**
     * Sends a message to the channel in the event
     *
     * @param embed the embed to send
     */
    public void replyAsync(MessageEmbed embed) {
        discord.sendPMessageAsync(channel.getId(), embed);
    }

    public Message replyNow(String message) {
        return discord.sendPMessageNow(channel.getId(), message);
    }

    public Message replyNow(MessageEmbed embed) {
        return discord.sendPMessageNow(channel.getId(), embed);
    }
}
