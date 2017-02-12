package commands.misc_commands;

import commands.GuildCommand;
import commands.PMCommand;
import iw_bot.LogUtil;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Message;
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
        reminder(event.getMessage(), args);
    }

    @Override
    public void runCommand(GuildMessageReceivedEvent event, String[] args) {
        reminder(event.getMessage(), args);
    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        return "Syntax is: '/reminder ##t, reason' - ## number, t time unit (s, m, h, d, w, y), reason is optional";
    }

    private void reminder(Message message, String[] args) {
        String userid = message.getAuthor().getId();
        String reason = "";
        long time = decode(args);
        if (time == -1) {
            message.getChannel().sendMessage("Please specify the time unit (s, m, h, d, w, y)").queue();
            return;
        }

        if (args.length >= 2) {
            for (int i = 1; i < args.length; i++)
                reason = reason + ", " + args[i];
        }
        reason = reason.replace(", ", "");

        add(userid, reason, time);
    }

    private long decode(String[] args) {
        long time = new Date().getTime();
        if (args[0].contains("s")) {
            time += Integer.parseInt(args[0].replace("s", "")) * 1000;
        }
        else if (args[0].contains("m")) {
            time += Integer.parseInt(args[0].replace("m", "")) * 1000 * 60;
        }
        else if (args[0].contains("h")) {
            time += Integer.parseInt(args[0].replace("h", "")) * 1000 * 60 * 60;
        }
        else if (args[0].contains("d")) {
            time += Integer.parseInt(args[0].replace("d", "")) * 1000 * 60 * 60 * 24;
        }
        else if (args[0].contains("w")) {
            time += Integer.parseInt(args[0].replace("w", "")) * 1000 * 60 * 60 * 24 * 7;
        }
        else if (args[0].contains("y")) {
            time += Integer.parseInt(args[0].replace("y", "")) * 1000 * 60 * 60 * 24 * 365;
        }
        else {
            return -1;
        }
        return time;
    }

    private static void add (String userid, String reason, long time) {
        Connection connect = new Connections().getConnection();

        try {
            PreparedStatement ps = connect.prepareStatement("INSERT INTO reminders (userid, time, reason) VALUES (?, ?, ?)");
            ps.setString(1, userid);
            ps.setLong  (2, time);
            ps.setString(3, reason);
            ps.executeUpdate();
        } catch (SQLException e) {
            LogUtil.logErr(e);
        }
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
