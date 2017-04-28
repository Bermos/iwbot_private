package commands.core_commands;

import commands.GuildCommand;
import provider.jda.Discord;
import provider.jda.events.GuildMessageEvent;

public class Topic implements GuildCommand {
    @Override
    public void runCommand(GuildMessageEvent event, Discord discord) {
        String topic = event.getChannel().getTopic().isEmpty() ? "This channel has no topic." : event.getChannel().getTopic();
        event.replyAsync(topic);
    }

    @Override
    public String getHelp(GuildMessageEvent event) {
        return "Shows the channel details";
    }
}
