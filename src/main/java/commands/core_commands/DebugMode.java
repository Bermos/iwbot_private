package commands.core_commands;

import core.JDAUtil;
import core.Listener;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import provider.DataProvider;

/**
 * This class is for switching debug mode
 * on and off. Just use debug to do it.
 */
public class DebugMode implements commands.GuildCommand {
    @Override
    public void runCommand(Listener listener, GuildMessageReceivedEvent event, String[] args) {
        //Permission check
        if (!JDAUtil.isAuthorized(event)) {
            event.getChannel().sendMessage("[Error] You aren't authorized to do this").queue();
            return;
        }

        listener.isDebug = !listener.isDebug;

        event.getChannel().sendMessage("Debug mode: " + listener.isDebug).queue();
    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        //Permission check
        if (!JDAUtil.isAuthorized(event))
            return "";
        return "Switches debug mode on/off";
    }
}
