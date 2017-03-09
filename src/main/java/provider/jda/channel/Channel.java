package provider.jda.channel;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import provider.jda.Discord;

public interface Channel {

    public String getId();

    public Discord getDiscord();

    public void sendMessageAsync(String message);

    public void sendMessageAsync(MessageEmbed embed);

    public Message sendMessage(String message);

    public Message sendMessage(MessageEmbed embed);

}
