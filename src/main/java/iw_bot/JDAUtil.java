package iw_bot;

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

    public static void sendMultipleMessages(TextChannel chan, List<String> messages) {
        for (String message : messages) {
            chan.sendMessage(message).queue();
        }
    }
}
