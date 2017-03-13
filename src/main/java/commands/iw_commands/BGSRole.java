package commands.iw_commands;

import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.PermissionException;

class BGSRole {
    static void toggleBgsRole(GuildMessageReceivedEvent event) {
        Role rBGS = null;
        Role rIW = null;
        for (Role role : event.getGuild().getRoles()) {
            if (role.getName().equals("BGS"))
                rBGS = role;
            if (role.getName().equals("Iridium Wing"))
                rIW  = role;
        }

        if (event.getMember().getRoles().contains(rBGS)) {
            try {
                event.getGuild().getController().removeRolesFromMember(event.getMember(), rBGS).queue();
                event.getChannel().sendMessage("BGS role removed").queue();
            } catch (PermissionException e) {
                event.getChannel().sendMessage("**BGS ROLE CAN NOT BE MANAGED!**\nThe bot does not have the required permissions.").queue();
            }
        }
        else if (event.getMember().getRoles().contains(rIW)) {
            try {
                event.getGuild().getController().addRolesToMember(event.getMember(), rBGS).queue();
                event.getChannel().sendMessage("BGS role added").queue();
            } catch (PermissionException e) {
                event.getChannel().sendMessage("**BGS ROLE CAN NOT BE MANAGED!**\nThe bot does not have the required permissions.").queue();
            }
        }
    }
}
