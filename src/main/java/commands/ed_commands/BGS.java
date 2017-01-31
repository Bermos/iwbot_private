package commands.ed_commands;

import commands.GuildCommand;
import commands.PMCommand;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;

/**
 * Created by Bermos on 31.01.2017.
 */
public class BGS implements PMCommand, GuildCommand {
    @Override
    public void runCommand(PrivateMessageReceivedEvent event, String[] args) {

    }

    @Override
    public void runCommand(GuildMessageReceivedEvent event, String[] args) {

    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        return null;
    }

    //TODO
}
