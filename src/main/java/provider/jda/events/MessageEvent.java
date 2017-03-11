package provider.jda.events;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import provider.jda.Discord;
import provider.jda.User;
import provider.jda.channel.Channel;

public interface MessageEvent {

    /**
     * Get the discord object associated with the event
     *
     * @return discord object
     */
    Discord getDiscord();

    /**
     * Get the channel in which the messsage was sent
     *
     * @return channel
     */
    Channel getChannel();

    /**
     * Get the user that wrote the message
     *
     * @return user/author
     */
    User getAuthor();

    /**
     * Get the split args that were passed with the command
     *
     * @return the args as String array
     */
    String[] getArgs();

    Message getMessage();

    /**
     * Sends a message to the channel in the event
     *
     * @param message the message to send
     */
    void replyAsync(String message);

    /**
     * Sends a message to the channel in the event
     *
     * @param embed the embed to send
     */
    void replyAsync(MessageEmbed embed);

    Message replyNow(String message);

    Message replyNow(MessageEmbed embed);
}
