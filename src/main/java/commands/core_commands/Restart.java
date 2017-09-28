package commands.core_commands;

import commands.GuildCommand;
import commands.PMCommand;
import core.JDAUtil;
import core.Listener;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import provider.DataProvider;

public class Restart implements PMCommand, GuildCommand {
    @Override
    public void runCommand(Listener listener, PrivateMessageReceivedEvent event, String[] args) {
        //Permission check
        if (!DataProvider.isBotAdmin(event)) {
            event.getChannel().sendMessage("[Error] You aren't authorized to do this").queue();
            return;
        }

        event.getChannel().sendMessage("Restarting...").complete();
        System.exit(1);
    }

    @Override
    public void runCommand(Listener listener, GuildMessageReceivedEvent event, String[] args) {
        //Permission check
        if (!DataProvider.isBotAdmin(event)) {
            event.getChannel().sendMessage("[Error] You aren't authorized to do this").queue();
            return;
        }

        event.getChannel().sendMessage("Restarting...").complete();
        System.exit(1);
    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        if (!DataProvider.isBotAdmin(event))
            return "";
        return "Restarts the bot. Use when he behaves strangely or throws a fit";
    }
}
