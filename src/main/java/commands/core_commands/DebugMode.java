package commands.core_commands;

import commands.GuildCommand;
import iw_bot.Listener;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import provider.DataProvider;
import provider.jda.Discord;
import provider.jda.events.GuildMessageEvent;

/**
 * This class is for switching debug mode
 * on and off. Just use debug to do it.
 */
public class DebugMode implements GuildCommand {
    @Override
    public void runCommand(GuildMessageEvent event, Discord discord) {
        //Permission check
        if (!(DataProvider.isOwner(event.getAuthor().getId()))) {
            event.replyAsync("[Error] You aren't authorized to do this");
            return;
        }

        Listener.isDebug = !Listener.isDebug;

        event.replyAsync("Debug mode: " + Listener.isDebug);
    }

    @Override
    public String getHelp(GuildMessageEvent event) {
        return "";
    }
}
