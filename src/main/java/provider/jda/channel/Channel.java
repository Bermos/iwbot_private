package provider.jda.channel;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import provider.jda.Discord;

public interface Channel {

    String getId();

    Discord getDiscord();

    void sendMessageAsync(String message);

    void sendMessageAsync(MessageEmbed embed);

    Message sendMessage(String message);

    Message sendMessage(MessageEmbed embed);

    void sendTyping();
}
