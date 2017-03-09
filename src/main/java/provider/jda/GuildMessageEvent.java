package provider.jda;

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import provider.jda.channel.GuildChannel;

public class GuildMessageEvent {
    private Discord discord;
    private GuildChannel channel;
    private User author;
    private String[] args;

    public GuildMessageEvent(GuildMessageReceivedEvent event,
                             Discord discord,
                             String[] args) {
        this.discord = discord;
        this.channel = new GuildChannel(event.getChannel().getId(), discord);
        this.author  = new User(event.getAuthor().getId(), discord);
        this.args    = args;

    }

    public GuildMessageEvent(Discord discord) {
        this.discord = discord;
    }

    public Discord getDiscord() {
        return discord;
    }
}
