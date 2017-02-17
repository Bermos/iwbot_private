package commands.misc_commands;

import commands.GuildCommand;
import commands.PMCommand;
import iw_bot.LogUtil;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import provider.Connections;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class Reminder implements PMCommand, GuildCommand {
    @Override
    public void runCommand(PrivateMessageReceivedEvent event, String[] args) {
        event.getChannel().sendMessage(reminder(event.getAuthor().getId(), args));
    }

    @Override
    public void runCommand(GuildMessageReceivedEvent event, String[] args) {
        event.getChannel().sendMessage(reminder(event.getAuthor().getId(), args));
    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        return "Syntax is: '/reminder ##t, reason' - ## number, t time unit (s|m|h|d|w|y), reason is optional";
    }

    String reminder(String userid, String[] args) {
        if (args.length == 0)
            return "[Error] Please specify a time frame number(s|m|h|d|w|y)";

        String reason = "";
        long time = decode(args[0]);
        if (time == -1) {
            return "[Error] Please specify the time unit (s|m|h|d|w|y)";
        }
        if (time == -2) {
            return "[Error] Incompatible time format, please use: number(s|m|h|d|w|y)";
        }

        if (args.length >= 2) {
            for (int i = 1; i < args.length; i++)
                reason = reason + ", " + args[i];
        }
        reason = reason.replaceFirst(", ", "");

        if (add(userid, reason, time))
            return "Reminder set";

        return "Something went terribly wrong, reminder not set.";
    }

    private long decode(String argsTime) {
        long time = new Date().getTime();
        try {
            if (argsTime.contains("s")) {
                time += Integer.parseInt(argsTime.replace("s", "")) * 1000;
            } else if (argsTime.contains("m")) {
                time += Integer.parseInt(argsTime.replace("m", "")) * 1000 * 60;
            } else if (argsTime.contains("h")) {
                time += Integer.parseInt(argsTime.replace("h", "")) * 1000 * 60 * 60;
            } else if (argsTime.contains("d")) {
                time += Integer.parseInt(argsTime.replace("d", "")) * 1000 * 60 * 60 * 24;
            } else if (argsTime.contains("w")) {
                time += Integer.parseInt(argsTime.replace("w", "")) * 1000 * 60 * 60 * 24 * 7;
            } else if (argsTime.contains("y")) {
                time += Integer.parseInt(argsTime.replace("y", "")) * 1000 * 60 * 60 * 24 * 365;
            } else {
                return -1;
            }
        } catch (Exception e) {
            return -2;
        }
        return time;
    }

    private static boolean add (String userid, String reason, long time) {
        Connection connect = new Connections().getConnection();

        try {
            PreparedStatement ps = connect.prepareStatement("INSERT INTO reminders (userid, time, reason) VALUES (?, ?, ?)");
            ps.setString(1, userid);
            ps.setLong  (2, time);
            ps.setString(3, reason);

            if(ps.executeUpdate() == 1)
                return true;
        } catch (SQLException e) {
            LogUtil.logErr(e);
        }
        return false;
    }

    public void startChecks(JDA jda) {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new CheckTask(jda), new Date(), TimeUnit.MILLISECONDS.convert(10, TimeUnit.SECONDS));
    }

    private class CheckTask extends TimerTask {
        private JDA jda;
        private Connection connect;

        CheckTask(JDA jda) {
            this.jda = jda;
            this.connect = new Connections().getConnection();
            boolean initialised = jda != null && connect != null;
            if (initialised)
                System.out.println("[Info] reminder initialised");
            else
                System.out.println("[Error] could not initialise reminder check");
        }

        @Override
        public void run() {
            checkTimer();
        }

        private void checkTimer() {
            try {
                PreparedStatement ps = connect.prepareStatement("SELECT userid, reason FROM reminders WHERE reminded = 0 AND time < ?");
                ps.setLong(1, new Date().getTime());
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    jda.getUserById(rs.getString("userid")).getPrivateChannel().sendMessage("REMINDED!\n" + rs.getString("reason")).queue();
                }
                ps.close();
                ps = connect.prepareStatement("UPDATE reminders SET reminded = 1 WHERE reminded = 0 AND time < ?");
                ps.setLong(1, new Date().getTime());
                ps.executeUpdate();

            } catch (SQLException e) {
                LogUtil.logErr(e);
            }
        }
    }
}
