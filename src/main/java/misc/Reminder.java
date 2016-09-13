package misc;

import net.dv8tion.jda.JDA;
import provider.Connections;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Created by Bermos on 29.08.2016.
 */
public class Reminder {

    public static void add (String userid, String reason, long time) {
        Connection connect = new Connections().getConnection();

        try {
            PreparedStatement ps = connect.prepareStatement("INSERT INTO reminders (userid, time, reason) VALUES (?, ?, ?)");
            ps.setString(1, userid);
            ps.setLong  (2, time);
            ps.setString(3, reason);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void startChecks(JDA jda) {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new CheckTask(jda), new Date(), TimeUnit.MILLISECONDS.convert(10, TimeUnit.SECONDS));
    }

    class CheckTask extends TimerTask {
        private JDA jda;
        private Connection connect;

        public CheckTask(JDA jda) {
            this.jda = jda;
            this.connect = new Connections().getConnection();
            boolean initialised = jda != null && connect != null;
            if (initialised)
                System.out.print("reminder initialised");
            else
                System.out.print("Error while initialising reminder check");
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
                    jda.getUserById(rs.getString("userid")).getPrivateChannel().sendMessageAsync("REMINDED!\n" + rs.getString("reason"), null);
                }
                ps.close();
                ps = connect.prepareStatement("UPDATE reminders SET reminded = 1 WHERE reminded = 0 AND time < ?");
                ps.setLong(1, new Date().getTime());
                ps.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
