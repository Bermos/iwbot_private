package core;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

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
}
