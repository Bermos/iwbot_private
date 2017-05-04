package core;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.util.List;

public class JDAUtil {
    public static PrivateChannel getPrivateChannel(User user) {
        if (!user.hasPrivateChannel())
            user.openPrivateChannel().complete();

        return user.getPrivateChannel();
    }

    public static String[] getRoleIdStrings(Member member) {
        String[] roleIds = new String[member.getRoles().size()];

        for (int i = 0; i < roleIds.length; i++) {
            roleIds[i] = member.getRoles().get(i).getId();
        }

        return roleIds;
    }
      
    public static void sendMultipleMessages(TextChannel chan, List<String> messages) {
        for (String message : messages) {
            chan.sendMessage(message).queue();
        }
    }

    /**
     * Determines if the user is authorized to perform
     * actions that require higher clarance.
     *
     * @param event Guild message received
     * @return whether that user is authorized or not
     */
    public static boolean isAuthorized(GuildMessageReceivedEvent event) {
        if (event.getGuild().getOwner().equals(event.getAuthor())) {
            return true;
        }

        for (Role role : event.getMember().getRoles()) {
            if (role.hasPermission(Permission.ADMINISTRATOR) || role.hasPermission(Permission.MANAGE_SERVER))
                return true;
        }

        return false;
    }
}
