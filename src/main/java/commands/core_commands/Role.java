package commands.core_commands;

import commands.GuildCommand;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import provider.DataProvider;

import java.awt.*;

public class Role implements GuildCommand {
    @Override
    public void runCommand(GuildMessageReceivedEvent event, String[] args) {
        event.getChannel().sendTyping();
        //Permission check
        if (!(DataProvider.isOwner(event.getAuthor().getId()) || DataProvider.isAdmin(event.getGuild().getMember(event.getAuthor()).getRoles()))) {
            event.getChannel().sendMessage("[Error] You aren't authorized to do this").queue();
            return;
        }

        if (args.length == 0) {
            event.getChannel().sendMessage("[Error] No name stated").queue();
        } else if (args.length == 1) {
            event.getChannel().sendMessage("[Error] No action selected").queue();
        } else {
            if (args[0].equalsIgnoreCase("add")) {
                event.getGuild().getController().createRole().setName(args[1]).queue();
                event.getChannel().sendMessage("[Success] role '" + args[1] + "' created").queue();
            }
            if (args[0].equalsIgnoreCase("del")) {
                for (net.dv8tion.jda.core.entities.Role role : event.getGuild().getRolesByName(args[1], true)) {
                    String oldName = role.getName();
                    role.delete().queue();
                    event.getChannel().sendMessage("[Success] role '" + oldName + "' deleted").queue();
                }
            }
            if (args[0].equalsIgnoreCase("color") || args[0].equalsIgnoreCase("colour")) {
                if (args.length < 5) {
                    event.getChannel().sendMessage("[Error] you need to specify the RGB values for the new color. '0, 0, 0' for example").queue();
                    return;
                }

                for (net.dv8tion.jda.core.entities.Role role : event.getGuild().getRolesByName(args[1], true)) {
                    role.getManager().setColor(new Color(Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]))).queue();
                    event.getChannel().sendMessage("[Success] colour of role '" + args[1] + "' changed").queue();
                }
            }
            if (args[0].equalsIgnoreCase("rename")) {
                if (args.length < 3) {
                    event.getChannel().sendMessage("[Error] no new name set").queue();
                    return;
                }
                for (net.dv8tion.jda.core.entities.Role role : event.getGuild().getRolesByName(args[1], true)) {
                    String oldName = role.getName();
                    role.getManager().setName(args[2]).queue();
                    event.getChannel().sendMessage("[Success] role '" + oldName + "' renamed to '" + args[2] + "'").queue();
                }
            }
        }
    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        //Permission check
        if (!(DataProvider.isOwner(event.getAuthor().getId()) || DataProvider.isAdmin(event.getGuild().getMember(event.getAuthor()).getRoles())))
            return "";
        return "<add|del|rename|color|colour>, <name>, <newname|#color> - Edits the role in the specified way.";
    }
}
