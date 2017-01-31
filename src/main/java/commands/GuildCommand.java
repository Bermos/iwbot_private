package commands;

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public interface GuildCommand {
    void runCommand(GuildMessageReceivedEvent event, String[] args);
    String getHelp(GuildMessageReceivedEvent event);
}