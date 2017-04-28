package commands.core_commands;

import commands.GuildCommand;
import provider.DataProvider;
import provider.jda.Discord;
import provider.jda.events.GuildMessageEvent;

import java.util.Arrays;

public class AdminRole implements GuildCommand {
    @Override
    public void runCommand(GuildMessageEvent event, Discord discord) {
        //Permission check
        if (!(DataProvider.isOwner(event.getAuthor().getId()) || DataProvider.isAdmin(event.getGuild().getMember(event.getAuthor()).getRoles()))) {
            event.replyAsync("[Error] You aren't authorized to do this");
            return;
        }

        String[] args = event.getArgs();
        Arrays.sort(args);

        if ((Arrays.binarySearch(args, "view") > -1) || args.length == 0) {
            String message = "";
            for (String id : DataProvider.getAdminRoleIDs())
                message += ( "-" + event.getGuild().getRoleById(id).getName() + "\n" );

            if (!message.isEmpty())
                event.replyAsync("Roles with admin privileges:\n" + message);
            else
                event.getChannel().sendMessage("No admin roles defined");
        }
        else if (args[0].equalsIgnoreCase("add")) {
            if (!event.getMessage().getMentionedRoles().isEmpty()) {
                DataProvider.addAdminRoleID(event.getMessage().getMentionedRoles().get(0).getId());
            } else if (args.length == 2) {
                if (!event.getGuild().getRolesByName(args[1], false).isEmpty()) {
                    DataProvider.addAdminRoleID(event.getGuild().getRolesByName(args[1], false).get(0).getId());
                } else {
                    event.replyAsync("[Error] No role with this name found");
                    return;
                }
            } else {
                event.replyAsync("[Error] No role specified");
                return;
            }
            event.replyAsync("[Success] Admin role saved");
        }
        else if (args[0].equalsIgnoreCase("del")) {
            if (!event.getMessage().getMentionedRoles().isEmpty()) {
                DataProvider.removeAdminRoleID(event.getMessage().getMentionedRoles().get(0).getId());
            } else if (args.length == 2) {
                net.dv8tion.jda.core.entities.Role role = event.getGuild().getRoles().stream().filter(vrole -> vrole.getName().equalsIgnoreCase(args[1].trim())).findFirst().orElse(null);
                if (role != null) {
                    DataProvider.removeAdminRoleID(role.getId());
                } else {
                    event.replyAsync("[Error] No role with this name found");
                    return;
                }
            } else {
                event.replyAsync("[Error] No role specified");
                return;
            }
            event.replyAsync("[Success] Admin role removed");
        }
    }

    @Override
    public String getHelp(GuildMessageEvent event) {
        //Permission check
        if (!(DataProvider.isOwner(event.getAuthor().getId()) || DataProvider.isAdmin(event.getGuild().getMember(event.getAuthor()).getRoles())))
            return "";
        return "<add?>|<del?>, <role?> - shows, adds or delets a role in/to/from admins";
    }
}
