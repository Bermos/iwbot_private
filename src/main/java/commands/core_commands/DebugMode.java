package commands.core_commands;

import commands.GuildCommand;
import iw_bot.Listener;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import provider.DataProvider;

/**
 * This class is for switching debug mode
 * on and off. Just use debug to do it.
 */
public class DebugMode implements GuildCommand {
    @Override
    public void runCommand(GuildMessageReceivedEvent event, String[] args) {
        //Permission check
        if (!(DataProvider.isOwner(event))) {
            event.getChannel().sendMessage("[Error] You aren't authorized to do this").queue();
            return;
        }

        Listener.isDebug = !Listener.isDebug;

        event.getChannel().sendMessage("Debug mode: " + Listener.isDebug).queue();
    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        return "";
    }
}
