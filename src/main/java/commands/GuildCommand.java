package commands;

import core.Listener;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public interface GuildCommand {
    void runCommand(Listener listener, GuildMessageReceivedEvent event, String[] args);
    String getHelp(GuildMessageReceivedEvent event);
}