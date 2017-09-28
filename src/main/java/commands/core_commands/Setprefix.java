package commands.core_commands;

import commands.GuildCommand;
import core.JDAUtil;
import core.Listener;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class Setprefix implements GuildCommand {

    @Override
    public void runCommand(Listener listener, GuildMessageReceivedEvent event, String[] args) {
        //Permission check
        if (!JDAUtil.isAuthorized(event)) {
            event.getChannel().sendMessage("[Error] You aren't authorized to do this").queue();
            return;
        }

        if (args.length < 1) {
            event.getChannel().sendMessage("No prefix is not a prefix. Sorry").queue();
        }
        else if (args.length > 1) {
            event.getChannel().sendMessage("',' is not allowed. Sorry").queue();
        }
        else {
            listener.gh.setGuildPrefix(event.getGuild().getId(), args[0]);
            event.getChannel().sendMessage("Prefix set to '" + args[0] + "'").queue();
        }
    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        //Permission check
        if (!JDAUtil.isAuthorized(event)) {
            return "";
        }

        return "<prefix> - To set the desired prefix for this bot. Standard is '!!'";
    }
}
