package iw_bot;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import provider.DataProvider;

import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;

public class LogUtil {
    public static void logErr(Exception e) {
        try {
            String message = e.getMessage();
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);

            String stacktrace = "";
            for (String line : sw.toString().split("\\n")) {
                if (line.contains("commands")
                        || line.contains("iw_bot")
                        || line.contains("iw_core")
                        || line.contains("misc")
                        || line.contains("provider")) {
                    stacktrace += line + "\n";
                }
            }

            if (stacktrace.length() > 2000 || DataProvider.getOwnerIDs().get(0).equals("1")) {
                e.printStackTrace();
                return;
            }

            //TODO fix url
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle(message)
                    //.setUrl("https://google.com/#q=" + e.getMessage())
                    .setDescription(stacktrace)
                    .setColor(new Color(179, 58, 58));
            MessageEmbed embed = eb.build();

            for (String userId : DataProvider.getOwnerIDs()) {
                JDAUtil.getPrivateChannel(Listener.jda.getUserById(userId)).sendMessage(embed).queue();
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
}