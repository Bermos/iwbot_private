package commands;

import provider.jda.Discord;
import provider.jda.events.GuildMessageEvent;

public interface GuildCommand {
    void runCommand(GuildMessageEvent event, Discord discord);
    String getHelp(GuildMessageEvent event);
}