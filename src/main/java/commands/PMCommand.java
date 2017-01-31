package commands;

import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;

public interface PMCommand {
    void runCommand(PrivateMessageReceivedEvent event, String[] args);
}
