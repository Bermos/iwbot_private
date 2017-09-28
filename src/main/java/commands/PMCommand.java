package commands;

import core.Listener;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;

public interface PMCommand {
    void runCommand(Listener listener, PrivateMessageReceivedEvent event, String[] args);
}
