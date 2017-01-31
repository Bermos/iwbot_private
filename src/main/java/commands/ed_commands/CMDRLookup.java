package commands.ed_commands;

import commands.GuildCommand;
import commands.PMCommand;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;

public class CMDRLookup implements PMCommand, GuildCommand {
    @Override
    public void runCommand(PrivateMessageReceivedEvent event, String[] args) {
       event.getChannel().sendMessage(lookup(args));
    }

    @Override
    public void runCommand(GuildMessageReceivedEvent event, String[] args) {
        event.getChannel().sendMessage(lookup(args));
    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        return "Finds all the information available on inara.cz and r/EliteCombatLoggers";
    }

    private String lookup(String[] args) {
        if (args.length == 1) {
            return misc.CMDRLookup.whois(args[0], false);
        }
        else if (args.length == 2) {
            return misc.CMDRLookup.whois(args[0], args[1].equalsIgnoreCase("update"));
        }

        return "";
    }

    //TODO merge with CMDRLookup file
}
