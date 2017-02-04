package commands.core_commands;

import commands.GuildCommand;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import provider.DiscordInfo;

import java.util.Arrays;

public class AdminRole implements GuildCommand {
    @Override
    public void runCommand(GuildMessageReceivedEvent event, String[] args) {
        //Permission check
        if (!(DiscordInfo.isOwner(event.getAuthor().getId()) || DiscordInfo.isAdmin(event.getGuild().getMember(event.getAuthor()).getRoles()))) {
            event.getChannel().sendMessage("[Error] You aren't authorized to do this").queue();
            return;
        }

        Arrays.sort(args);

        if ((Arrays.binarySearch(args, "view") > -1) || args.length == 0) {
            String message = "";
            for (String id : DiscordInfo.getAdminRoleIDs())
                message += ( "-" + event.getGuild().getRoleById(id).getName() + "\n" );

            if (!message.isEmpty())
                event.getChannel().sendMessage("Roles with admin privileges:\n" + message).queue();
            else
                event.getChannel().sendMessage("No admin roles defined").queue();
        }
        else if (args[0].equalsIgnoreCase("add")) {
            if (!event.getMessage().getMentionedRoles().isEmpty()) {
                DiscordInfo.addAdminRoleID(event.getMessage().getMentionedRoles().get(0).getId());
            } else if (args.length == 2) {
                net.dv8tion.jda.core.entities.Role role = event.getGuild().getRoles().stream().filter(vrole -> vrole.getName().equalsIgnoreCase(args[1].trim())).findFirst().orElse(null);
                if (role != null) {
                    DiscordInfo.addAdminRoleID(role.getId());
                } else {
                    event.getChannel().sendMessage("[Error] No role with this name found").queue();
                    return;
                }
            } else {
                event.getChannel().sendMessage("[Error] No role specified").queue();
                return;
            }
            event.getChannel().sendMessage("[Success] Admin role saved").queue();
        }
        else if (args[0].equalsIgnoreCase("del")) {
            if (!event.getMessage().getMentionedRoles().isEmpty()) {
                DiscordInfo.removeAdminRoleID(event.getMessage().getMentionedRoles().get(0).getId());
            } else if (args.length == 2) {
                net.dv8tion.jda.core.entities.Role role = event.getGuild().getRoles().stream().filter(vrole -> vrole.getName().equalsIgnoreCase(args[1].trim())).findFirst().orElse(null);
                if (role != null) {
                    DiscordInfo.removeAdminRoleID(role.getId());
                } else {
                    event.getChannel().sendMessage("[Error] No role with this name found").queue();
                    return;
                }
            } else {
                event.getChannel().sendMessage("[Error] No role specified").queue();
                return;
            }
            event.getChannel().sendMessage("[Success] Admin role removed").queue();
        }
    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        //Permission check
        if (!(DiscordInfo.isOwner(event.getAuthor().getId()) || DiscordInfo.isAdmin(event.getGuild().getMember(event.getAuthor()).getRoles())))
            return "";
        return "<add?>|<del?>, <role?> - shows, adds or delets a role in/to/from admins";
    }
}
