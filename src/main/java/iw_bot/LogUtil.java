package iw_bot;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageEmbed;

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

            //TODO fix url
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle(message)
                    //.setUrl("https://google.com/#q=" + e.getMessage())
                    .setDescription(stacktrace)
                    .setColor(new Color(179, 58, 58));
            MessageEmbed embed = eb.build();

            Guild iw = Listener.jda.getGuildById("142749481530556416");
            for (Member member : iw.getMembersWithRoles(iw.getRolesByName("Bot Wizard", true).get(0))) {
                JDAUtil.getPrivateChannel(member.getUser()).sendMessage(embed).queue();
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
}
