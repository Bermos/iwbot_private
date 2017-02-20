package test_helper;

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class GTestCase {
    public GuildMessageReceivedEvent event;
    public String[] args;
    public String output;

    public GTestCase(GuildMessageReceivedEvent event, String[] args, String output) {
        this.event = event;
        this.args = args;
        this.output = output;
    }
}
