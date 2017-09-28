package core;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import provider.DataProvider;

import java.util.List;

public class JDAUtil {
    public static PrivateChannel getPrivateChannel(User user) {
        if (!user.hasPrivateChannel())
            user.openPrivateChannel().complete();

        return user.getPrivateChannel();
    }

    /**
     * Returns all role ids of a specified member in a string
     * array.
     *
     * @param member The member you want the roles for
     * @return array of id in string form
     */
    public static String[] getRoleIdStrings(Member member) {
        String[] roleIds = new String[member.getRoles().size()];

        for (int i = 0; i < roleIds.length; i++) {
            roleIds[i] = member.getRoles().get(i).getId();
        }

        return roleIds;
    }

    /**
     * Simple method to send multiple messages in one channel
     * for example if a single one would exceed the max char count.
     *
     * @param chan TextChannel in which the messages should be send
     * @param messages to be sent
     */
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
        // Checks if the author is the owner of the guild
        if (event.getGuild().getOwner().equals(event.getAuthor()))
            return true;

        // Checks if the author is a admin or server manager in the guild
        for (Role role : event.getMember().getRoles()) {
            if (role.hasPermission(Permission.ADMINISTRATOR) || role.hasPermission(Permission.MANAGE_SERVER))
                return true;
        }

        // Check if the author is a bot admin
        return DataProvider.isBotAdmin(event);
    }

    /*@anot.ToDo
    public static boolean isAdmin(GuildMessageReceivedEvent event) {
        //TODO
        return false;
    }*/
}
