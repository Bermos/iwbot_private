package commands;

import provider.jda.Discord;
import provider.jda.PrivateMessageEvent;

public interface PMCommand {
    void runCommand(PrivateMessageEvent event, Discord discord);
}
