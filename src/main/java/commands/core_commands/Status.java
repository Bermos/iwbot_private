package commands.core_commands;

import commands.GuildCommand;
import commands.PMCommand;
import iw_bot.Listener;
import iw_bot.LogUtil;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import provider.Connections;
import provider.jda.Discord;
import provider.jda.events.PrivateMessageEvent;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Status implements PMCommand, GuildCommand {
    @Override
    public void runCommand(PrivateMessageEvent event, Discord discord) {
        event.replyAsync(status(discord.getListener()));
    }

    @Override
    public void runCommand(GuildMessageReceivedEvent event, String[] args) {
        event.getChannel().sendMessage(status()).queue();
    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        return "Shows information about the bot";
    }

    String status(Listener listener) {
        Long diff 			 = (new Date().getTime() - listener.getStarupTime());
        int days			 = (int) TimeUnit.MILLISECONDS.toDays(diff);
        int hours			 = (int) TimeUnit.MILLISECONDS.toHours(diff) % 24;
        int minutes			 = (int) TimeUnit.MILLISECONDS.toMinutes(diff) % 60;
        int seconds			 = (int) TimeUnit.MILLISECONDS.toSeconds(diff) % 60;
        NumberFormat nForm	 = NumberFormat.getInstance(Locale.GERMANY);
        int noThreads		 = Thread.getAllStackTraces().keySet().size();
        String uniqueSets    = "error";
        String totalSets     = "error";
        String totalMemory	 = String.format("%.2f",(double) Runtime.getRuntime().maxMemory() / 1024 / 1024);
        String usedMemory	 = String.format("%.2f",(double)(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024);

        try {
            PreparedStatement ps = new Connections().getConnection().prepareStatement("SELECT COUNT(idmarkov) AS unique_sets, sum(prob) AS total_sets FROM markov");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                uniqueSets = nForm.format(rs.getInt("unique_sets")).replace('.', '\'');
                totalSets = nForm.format(rs.getInt("total_sets")).replace('.', '\'');
                rs.close();
                ps.close();
            }
        } catch (SQLException e) {
            LogUtil.logErr(e);
        }

        String contOut = "```"
                + "Uptime              | " + String.format("%dd %02d:%02d:%02d\n", days, hours, minutes, seconds)
                + "# Threads           | " + noThreads						+ "\n"
                + "Memory usage        | " + usedMemory + "/" + totalMemory	+ " MB\n"
                + "Unique AI Datasets  | " + uniqueSets						+ "\n"
                + "Total AI Datasets   | " + totalSets						+ "\n"
                + "Version             | " + listener.getVersion()          + "```";

        return contOut;
    }
}
