package commands.core_commands;

import commands.GuildCommand;
import provider.DataProvider;
import provider.jda.Discord;
import provider.jda.Role;
import provider.jda.events.GuildMessageEvent;

import java.awt.*;

public class RoleManipulator implements GuildCommand {
    @Override
    public void runCommand(GuildMessageEvent event, Discord discord) {
        event.getChannel().sendTyping();
        String[] args = event.getArgs();

        //Permission check
        if (!(DataProvider.isOwner(event.getAuthor().getId()) || DataProvider.isAdmin(event.getGuild().getMember(event.getAuthor()).getRoles()))) {
            event.replyAsync("[Error] You aren't authorized to do this");
            return;
        }

        if (args.length == 0) {
            event.replyAsync("[Error] No name stated");
        } else if (args.length == 1) {
            event.replyAsync("[Error] No action selected");
        } else {
            if (args[0].equalsIgnoreCase("add")) {
                event.getGuild().getController().createRole().setName(args[1]).queue(); //TODO
                event.replyAsync("[Success] role '" + args[1] + "' created");
            }
            if (args[0].equalsIgnoreCase("del")) {
                for (Role role : event.getGuild().getRolesByName(args[1], false)) {
                    String oldName = role.getName();
                    role.deleteAsync();
                    event.replyAsync("[Success] role '" + oldName + "' deleted");
                }
            }
            if (args[0].equalsIgnoreCase("color") || args[0].equalsIgnoreCase("colour")) {
                if (args.length < 5) {
                    event.replyAsync("[Error] you need to specify the RGB values for the new color. '0, 0, 0' for example");
                    return;
                }

                for (Role role : event.getGuild().getRolesByName(args[1], false)) {
                    role.getManager().setColor(new Color(Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]))).queue();
                    event.replyAsync("[Success] colour of role '" + args[1] + "' changed");
                }
            }
            if (args[0].equalsIgnoreCase("rename")) {
                if (args.length < 3) {
                    event.replyAsync("[Error] no new name set");
                    return;
                }
                for (Role role : event.getGuild().getRolesByName(args[1], false)) {
                    String oldName = role.getName();
                    role.getManager().setName(args[2]).queue();
                    event.replyAsync("[Success] role '" + oldName + "' renamed to '" + args[2] + "'");
                }
            }
        }
    }

    @Override
    public String getHelp(GuildMessageEvent event) {
        //Permission check
        if (!(DataProvider.isOwner(event.getAuthor().getId()) || DataProvider.isAdmin(event.getGuild().getMember(event.getAuthor()).getRoles())))
            return "";
        return "<add|del|rename|color|colour>, <name>, <newname|#color> - Edits the role in the specified way.";
    }
}
