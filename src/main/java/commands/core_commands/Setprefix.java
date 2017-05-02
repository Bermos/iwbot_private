package commands.core_commands;

import commands.GuildCommand;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import provider.DataProvider;

/**
 * Created by bermos on 02/05/17.
 */
public class Setprefix implements GuildCommand {

    @Override
    public void runCommand(GuildMessageReceivedEvent event, String[] args) {
        //Permission check
        boolean allowed = false;
        for (Role role : event.getMember().getRoles()) {
            if (role.hasPermission(Permission.ADMINISTRATOR))
                allowed = true;
        }
        if (!allowed) {
            event.getChannel().sendMessage("Sorry, you aren't allowed to do that. Please let someone with the Administrator permission do that.").queue();
            return;
        }

        if (args.length < 1) {
            event.getChannel().sendMessage("No prefix is not a prefix. Sorry").queue();
        }
        else if (args.length > 1) {
            event.getChannel().sendMessage("',' is not allowed. Sorry").queue();
        }
        else {
            DataProvider.setPrefix(event.getGuild().getId(), args[0]);
            event.getChannel().sendMessage("Prefix set to '" + args[0] + "'").queue();
        }
    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        //Permission check
        boolean allowed = false;
        for (Role role : event.getMember().getRoles()) {
            if (role.hasPermission(Permission.ADMINISTRATOR))
                allowed = true;
        }

        if(!allowed)
            return "";
        return "<prefix> - To set the desired prefix for this bot. Standard is '!!'";
    }
}
