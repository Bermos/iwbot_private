package commands.iw_commands;

import commands.GuildCommand;
import commands.PMCommand;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import provider.Connections;
import provider.DataProvider;
import provider.Statistics;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class BGS implements PMCommand, GuildCommand {
    @Override
    public void runCommand(PrivateMessageReceivedEvent event, String[] args) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("mystats")) {
                String output = "```";
                for (Map.Entry<BGS.Activity, Double> entry : BGS.getTotalAmount(event.getAuthor().getId()).entrySet()) {
                    output += entry.getKey().toString() + ": " + NumberFormat.getInstance(Locale.GERMANY).format(entry.getValue().intValue()).replace('.', '\'') + "\n";
                }
                output += "```";
                event.getChannel().sendMessage(output).queue();
            }
        }
    }

    @Override
    public void runCommand(GuildMessageReceivedEvent event, String[] args) {
        if (args.length == 0) {
            Role rBGS = null;
            Role rIW = null;
            for (Role role : event.getGuild().getRoles()) {
                if (role.getName().equals("BGS"))
                    rBGS = role;
                if (role.getName().equals("Iridium Wing"))
                    rIW  = role;
            }

            if (event.getMember().getRoles().contains(rBGS)) {
                event.getGuild().getController().removeRolesFromMember(event.getMember(), rBGS).queue();
                event.getChannel().sendMessage("BGS role removed").queue();
            }
            else if (event.getMember().getRoles().contains(rIW)) {
                event.getGuild().getController().addRolesToMember(event.getMember(), rBGS).queue();
                event.getChannel().sendMessage("BGS role added").queue();
            }
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("mystats")) {
                String output = "```";
                for (Map.Entry<BGS.Activity, Double> entry : BGS.getTotalAmount(event.getAuthor().getId()).entrySet()) {
                    output += entry.getKey().toString() + ": " + NumberFormat.getInstance(Locale.GERMANY).format(entry.getValue().intValue()).replace('.', '\'') + "\n";
                }
                output += "```";
                event.getAuthor().getPrivateChannel().sendMessage(output).queue();
            } else if (args[0].equalsIgnoreCase("total")) {
                String output = "```";
                for (Map.Entry<BGS.Activity, Double> entry : BGS.getTotalAmount().entrySet()) {
                    output += entry.getKey().toString() + ": " + NumberFormat.getInstance(Locale.GERMANY).format(entry.getValue().intValue()).replace('.', '\'') + "\n";
                }
                output += "```";
                if (DataProvider.isAdmin(event.getMember().getRoles()))
                    event.getChannel().sendMessage(output).queue();
                else
                    event.getAuthor().getPrivateChannel().sendMessage(output).queue();
            }
        } else if (args.length == 2) {
            String activity = null;
            String username = event.getMember().getEffectiveName();
            String userid = event.getAuthor().getId();
            int ammount = Integer.parseInt(args[1]);

            args[0] = args[0].toLowerCase();
            switch (args[0]) {
                case "bonds" 	 : activity = "BOND";	   break;
                case "bounties"  : activity = "BOUNTY";	   break;
                case "mining" 	 : activity = "MINING";	   break;
                case "missions"  : activity = "MISSION";   break;
                case "scans"	 : activity = "SCAN";	   break;
                case "smuggling" : activity = "SMUGGLING"; break;
                case "trade"	 : activity = "TRADE";	   break;
            }
            if (activity != null) {
                BGS.logActivity(BGS.Activity.valueOf(activity), userid, username, ammount);
                event.getChannel().sendMessage("Your engagement has been noticed. Thanks for your service o7").queue();
            }

        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("gettick") && DataProvider.isAdmin(event.getMember().getRoles())) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy HH:mm");
                Date time;
                try {
                    time = sdf.parse(args[2]);
                    String output = "Data for " + args[1] + " ticks after " + args[2] + " UTC:\n```";
                    Map<BGS.Activity, Double> entries = BGS.getTotalAmount(time, Integer.parseInt(args[1]));
                    for (Map.Entry<BGS.Activity, Double> entry : entries.entrySet()) {
                        output += entry.getKey().toString() + ": " + NumberFormat.getInstance(Locale.GERMANY).format(entry.getValue().intValue()).replace('.', '\'') + "\n";
                    }
                    output += "```";
                    if (entries.isEmpty())
                        event.getChannel().sendMessage("No records for the specified period").queue();
                    else
                        event.getChannel().sendMessage(output).queue();
                } catch (ParseException e) {
                    event.getChannel().sendMessage("Parsing error. Make sure the date follows the pattern 'dd/MM/yy HH:mm'").queue();
                }

            } else if (args[0].equalsIgnoreCase("gettickfull") && DataProvider.isAdmin(event.getMember().getRoles())) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy HH:mm");
                Date time;
                try {
                    time = sdf.parse(args[2]);
                    List<String> lines = BGS.getCSVData(time, Integer.parseInt(args[1]));

                    String output = "Data for " + args[1] + " ticks after " + args[2] + ":\n";
                    output += "----------------------------------------------------------------------\n";
                    output += lines.get(0) + "\n```";
                    for (int i = 1; i < lines.size(); i++) {
                        output += lines.get(i) + "\n";
                    }
                    if (lines.size() < 2)
                        output += "No records found";
                    output += "```";

                    event.getChannel().sendMessage(output).queue();
                } catch (ParseException e) {
                    event.getChannel().sendMessage("Parsing error. Make sure the date follows the pattern 'dd/MM/yy HH:mm'").queue();
                }

            }
        }
    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        return "Add or remove the bgs role to you";
    }

    enum Activity {
        BOND, BOUNTY, MINING, MISSION, SCAN, SMUGGLING, TRADE;

        public String toString() {
            return name().charAt(0) + name().substring(1).toLowerCase();
        }
    }
    private static SimpleDateFormat sqlSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static int getTotalAmount(BGS.Activity activity) {
        int total = 0;

        Connection connect = new Connections().getConnection();
        try {
            PreparedStatement ps = connect.prepareStatement("SELECT SUM(ammount) AS total FROM bgs_activity WHERE activity = ?");
            ps.setString(1, activity.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next())
                total = rs.getInt("total");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return total;
    }

    public static int getTotalAmount(BGS.Activity activity, String userid) {
        int total = 0;

        Connection connect = new Connections().getConnection();
        try {
            PreparedStatement ps = connect.prepareStatement("SELECT SUM(ammount) AS total FROM bgs_activity WHERE activity = ? AND userid = ?");
            ps.setString(1, activity.toString());
            ps.setString(2, userid);
            ResultSet rs = ps.executeQuery();

            if (rs.next())
                total = rs.getInt("total");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return total;
    }

    public static Map<BGS.Activity, Double> getTotalAmount(String userid) {
        Map<BGS.Activity, Double> totals = new LinkedHashMap<>();

        Connection connect = new Connections().getConnection();
        try {
            PreparedStatement ps = connect.prepareStatement("SELECT activity, SUM(ammount) AS total FROM bgs_activity WHERE userid = ? GROUP BY activity ORDER BY activity ASC");
            ps.setString(1, userid);
            ResultSet rs = ps.executeQuery();

            while (rs.next())
                totals.put(BGS.Activity.valueOf(rs.getString("activity").toUpperCase()), rs.getDouble("total"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return totals;
    }

    public static Map<BGS.Activity, Double> getTotalAmount() {
        Map<BGS.Activity, Double> totals = new LinkedHashMap<>();

        Connection connect = new Connections().getConnection();
        try {
            PreparedStatement ps = connect.prepareStatement("SELECT activity, SUM(ammount) AS total FROM bgs_activity GROUP BY activity ORDER BY activity ASC");
            ResultSet rs = ps.executeQuery();

            while (rs.next())
                totals.put(BGS.Activity.valueOf(rs.getString("activity").toUpperCase()), rs.getDouble("total"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return totals;
    }

    public static Map<BGS.Activity, Double> getTotalAmount(Date start, int ticks) {
        Map<BGS.Activity, Double> totals = new LinkedHashMap<>();
        Date end = ticks == 0 ? new Date() : new Date(start.getTime() + (ticks*24*60*60*1000));

        Connection connect = new Connections().getConnection();
        try {
            PreparedStatement ps = connect.prepareStatement("SELECT activity, SUM(ammount) AS total FROM bgs_activity WHERE timestamp > ? AND timestamp < ? GROUP BY activity ORDER BY activity ASC");
            ps.setString(1, sqlSdf.format(start));
            ps.setString(2, sqlSdf.format(end));
            ResultSet rs = ps.executeQuery();

            while (rs.next())
                totals.put(BGS.Activity.valueOf(rs.getString("activity").toUpperCase()), rs.getDouble("total"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return totals;
    }

    public static int getTotalParticipants() {
        int total = 0;

        Connection connect = new Connections().getConnection();
        try {
            PreparedStatement ps = connect.prepareStatement("SELECT COUNT(DISTINCT userid) AS total FROM bgs_activity");
            ResultSet rs = ps.executeQuery();

            if (rs.next())
                total = rs.getInt("total");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return total;
    }

    public static void logActivity(BGS.Activity activity, String userid, String username, int amount) {
        Connection connect = new Connections().getConnection();
        try {
            PreparedStatement ps = connect.prepareStatement("INSERT INTO bgs_activity (username, userid, ammount, activity) VALUES (?, ?, ?, ?)");
            ps.setString(1, username);
            ps.setString(2, userid);
            ps.setInt	(3, amount);
            ps.setString(4, activity.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Statistics.getInstance().logBGSActivity(System.currentTimeMillis(), userid, username, activity.toString(), amount);
    }

    public static List<String> getCSVData(Date start, int ticks) {
        List<String> lines = new ArrayList<>();
        Date end = ticks == 0 ? new Date() : new Date(start.getTime() + (ticks*24*60*60*1000));

        Connection connect = new Connections().getConnection();
        try {
            PreparedStatement ps = connect
                    .prepareStatement("SELECT " +
                            "username AS CMDR, " +
                            "from_unixtime(floor((unix_timestamp(timestamp) - (15*60*60))/(24*60*60)) * (24*60*60) + (15*60*60)) AS tick_start, " +
                            "SUM( if( activity = 'Bond', ammount, 0 ) ) AS Bonds, " +
                            "SUM( if( activity = 'Bounty', ammount, 0 ) ) AS Bounties, " +
                            "SUM( if( activity = 'Mining', ammount, 0 ) ) AS Mining, " +
                            "SUM( if( activity = 'Mission', ammount, 0 ) ) AS Missions, " +
                            "SUM( if( activity = 'Scan', ammount, 0 ) ) AS Scans, " +
                            "SUM( if( activity = 'Smuggling', ammount, 0 ) ) AS Smuggling, " +
                            "SUM( if( activity = 'Trade', ammount, 0 ) ) AS Trading " +
                            "FROM " +
                            "bgs_activity " +
                            "WHERE " +
                            "timestamp >= ? AND timestamp < ? " +
                            "GROUP BY " +
                            "userid, tick_start");
            ps.setString(1, sqlSdf.format(start));
            ps.setString(2, sqlSdf.format(end));
            ResultSet rs = ps.executeQuery();

            String columnNames = "";
            int columnCount = rs.getMetaData().getColumnCount();
            int columnDateTime = -1;
            int columnCMDRName = -1;
            for (int i = 0; i < columnCount; i++) {
                if (rs.getMetaData().getColumnName(i+1).equalsIgnoreCase("username")) {
                    columnCMDRName = i;
                } else if (rs.getMetaData().getColumnName(i+1).equalsIgnoreCase("tick_start")) {
                    columnDateTime = i;
                }
                columnNames = String.join(", ", columnNames, rs.getMetaData().getColumnName(i+1));
            }
            lines.add(columnNames.replaceFirst(",", "").replace("username", "CMDR"));
            while (rs.next()) {
                String rowValues = "";
                for (int i = 0; i < columnCount; i++) {
                    String rowValue;
                    if (i == columnCMDRName)
                        rowValue = rs.getString(i+1);
                    else if (i == columnDateTime)
                        rowValue = rs.getString(i+1).replaceAll("-", "/").replace(".0", "");
                    else
                        rowValue = rs.getString(i+1).equals("0") ? "" : rs.getString(i+1);
                    rowValues = String.join(",", rowValues, rowValue);
                }
                lines.add(rowValues.replaceFirst(",", ""));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lines;
    }
}
