package commands.iw_commands;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import commands.GuildCommand;
import commands.PMCommand;
import iw_bot.JDAUtil;
import iw_bot.LogUtil;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
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
    enum Activity {
        BOND, BOUNTY, MINING, MISSION, SCAN, SMUGGLING, TRADE, MURDER;

        public String toString() {
            return name().charAt(0) + name().substring(1).toLowerCase();
        }

        public static Activity from(String input) {
            input = input.toLowerCase();
            Activity output;
            switch (input) {
                case "bond"      :
                case "bonds" 	 : output = Activity.BOND;	    break;
                case "bounty"    :
                case "bounties"  : output = Activity.BOUNTY;	break;
                case "mining" 	 : output = Activity.MINING;	break;
                case "mission"   :
                case "missions"  : output = Activity.MISSION;   break;
                case "exploration" :
                case "scan"      :
                case "scans"	 : output = Activity.SCAN;      break;
                case "smuggling" : output = Activity.SMUGGLING; break;
                case "trading"   :
                case "trade"	 : output = Activity.TRADE;	    break;
                case "murder"	 : output = Activity.MURDER;	break;
                default          : output = null;               break;
            }

            return output;
        }
    }

    private static SimpleDateFormat sqlSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void runCommand(PrivateMessageReceivedEvent event, String[] args) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("mystats")) {
                String output = "```";
                for (Map.Entry<Activity, Double> entry : getTotalAmount(event.getAuthor().getId()).entrySet()) {
                    output += entry.getKey().toString() + ": " + NumberFormat.getInstance(Locale.GERMANY).format(entry.getValue().intValue()).replace('.', '\'') + "\n";
                }
                output += "```";
                event.getChannel().sendMessage(output).queue();
            }
            else if (args[0].equalsIgnoreCase("help")) {
                event.getChannel().sendMessage(bgsHelp).queue();
            }
        }
    }

    @Override
    public void runCommand(GuildMessageReceivedEvent event, String[] args) {
        if (args.length == 0) {
            toggleBgsRole(event);

        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("mystats")) {
                JDAUtil.getPrivateChannel(event.getAuthor()).sendMessage(getUserStats(event.getAuthor().getId())).queue();

            } else if (args[0].equalsIgnoreCase("total")) {
                if (DataProvider.isAdmin(event.getMember().getRoles()))
                    event.getChannel().sendMessage(getTotalAmount()).queue();
                else
                    JDAUtil.getPrivateChannel(event.getAuthor()).sendMessage(getTotalAmount()).queue();

            } else if (args[0].equalsIgnoreCase("help")) {
                JDAUtil.getPrivateChannel(event.getAuthor()).sendMessage(bgsHelp).queue();

            }
        } else if (args.length == 2) {
            event.getChannel().sendMessage("**WARNING ACTION NOT LOGGED**\nThe BGS logging commands require a system name. Please enter '/bgs help' for more info.").queue();

        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("gettick") && DataProvider.isAdmin(event)) {
                event.getChannel().sendMessage(getTick(args)).queue();

            } else if (args[0].equalsIgnoreCase("gettickfull") && DataProvider.isAdmin(event)) {
                event.getChannel().sendMessage(getFullTick(args)).queue();

            } else {
                event.getChannel().sendMessage(logActivity(args[0], event.getAuthor().getId(), event.getMember().getEffectiveName(), args[1], args[2])).queue();

            }
        } else if (args.length > 3) {
            // anything more than 3 is likely the user has tried to log data but used a number seperate (1,000,000)
            event.getChannel().sendMessage("**WARNING ACTION NOT LOGGED**\n" +
                    "Have you used a thousand/million seperator when entering the amount?\n" +
                    "e.g. Enter /bgs trade, 1500000\n" +
                    "Do not enter /bgs trade, 1,500,000").queue();

        }
    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        return "For help with BGS bot commands use '/bgs help'";
    }

    private String getFullTick(String[] args) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy HH:mm");
        Date time;
        try {
            time = sdf.parse(args[2]);
            List<String> lines = getCSVData(time, Integer.parseInt(args[1]));

            String output = "Data for " + args[1] + " ticks after " + args[2] + ":\n";
            output += "----------------------------------------------------------------------\n";
            output += lines.get(0) + "\n```";
            for (int i = 1; i < lines.size(); i++) {
                output += lines.get(i) + "\n";
            }
            if (lines.size() < 2)
                output += "No records found";
            output += "```";

            return output;
        } catch (ParseException e) {
            return "Parsing error. Make sure the date follows the pattern 'dd/MM/yy HH:mm'";
        }
    }

    private String getTick(String[] args) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy HH:mm");
        Date time;
        try {
            time = sdf.parse(args[2]);
            String output = "Data for " + args[1] + " ticks after " + args[2] + " UTC:\n```";
            Map<Activity, Double> entries = getTotalAmount(time, Integer.parseInt(args[1]));
            for (Map.Entry<Activity, Double> entry : entries.entrySet()) {
                output += entry.getKey().toString() + ": " + NumberFormat.getInstance(Locale.GERMANY).format(entry.getValue().intValue()).replace('.', '\'') + "\n";
            }
            output += "```";
            if (entries.isEmpty())
                return "No records for the specified period";
            else
                return output;
        } catch (ParseException e) {
            return "Parsing error. Make sure the date follows the pattern 'dd/MM/yy HH:mm'";
        }
    }

    private String getUserStats(String userID) {
        String output = "```";
        for (Map.Entry<Activity, Double> entry : getTotalAmount(userID).entrySet()) {
            output += entry.getKey().toString() + ": " + NumberFormat.getInstance(Locale.GERMANY).format(entry.getValue().intValue()).replace('.', '\'') + "\n";
        }
        output += "```";

        return output;
    }

    private void toggleBgsRole(GuildMessageReceivedEvent event) {
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
    }

    private static String getSystems() {
        String message = "```Shortname ¦ Fullname\n";
        try {
            PreparedStatement ps = new Connections().getConnection().prepareStatement("SELECT * FROM bgs_systems ORDER BY fullname ASC");
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                message += String.format("%-3s | %s\n", rs.getString("shortname"), rs.getString("fullname"));
        } catch (SQLException e) {
            LogUtil.logErr(e);
        }
        message +="```\n";
        return message;
    }

    private static Map<Activity, Double> getTotalAmount(String userid) {
        Map<Activity, Double> totals = new LinkedHashMap<>();

        Connection connect = new Connections().getConnection();
        try {
            PreparedStatement ps = connect.prepareStatement("SELECT activity, SUM(amount) AS total FROM bgs_activity WHERE userid = ? GROUP BY activity ORDER BY activity ASC");
            ps.setString(1, userid);
            ResultSet rs = ps.executeQuery();

            while (rs.next())
                totals.put(Activity.valueOf(rs.getString("activity").toUpperCase()), rs.getDouble("total"));
        } catch (SQLException e) {
            LogUtil.logErr(e);
        }
        return totals;
    }

    private static String getTotalAmount() {
        String output = "```";

        Connection connect = new Connections().getConnection();
        try {
            PreparedStatement ps = connect.prepareStatement("SELECT activity, SUM(amount) AS total FROM bgs_activity GROUP BY activity ORDER BY activity ASC");
            ResultSet rs = ps.executeQuery();

            while (rs.next())
                output += String.format(Locale.GERMANY, "%-9s: %.0f\n", rs.getString("activity"), rs.getDouble("total")).replace('.', '\'');
        } catch (SQLException e) {
            LogUtil.logErr(e);
        }
        output += "```";

        return output;
    }

    private static Map<Activity, Double> getTotalAmount(Date start, int ticks) {
        Map<Activity, Double> totals = new LinkedHashMap<>();
        Date end = ticks == 0 ? new Date() : new Date(start.getTime() + (ticks*24*60*60*1000));

        Connection connect = new Connections().getConnection();
        try {
            PreparedStatement ps = connect.prepareStatement("SELECT activity, SUM(amount) AS total FROM bgs_activity WHERE timestamp > ? AND timestamp < ? GROUP BY activity ORDER BY activity ASC");
            ps.setString(1, sqlSdf.format(start));
            ps.setString(2, sqlSdf.format(end));
            ResultSet rs = ps.executeQuery();

            while (rs.next())
                totals.put(Activity.valueOf(rs.getString("activity").toUpperCase()), rs.getDouble("total"));
        } catch (SQLException e) {
            LogUtil.logErr(e);
        }
        return totals;
    }

    private static String logActivity(String sActivity, String userid, String username, String sAmount, String system) {
        /*TODO Logging Improvements List
        *Split this off into separate function
        *Support for direct message
        *Confirmation message that does NOT tag them but is customised per the action logged
        *If goal is already met direct message once per activity with details on what still needs work.
        */
        int amount = Integer.parseInt(sAmount);
        Activity activity = Activity.from(sActivity);

        if (activity == null) {
            String output = "";
            for (Activity act : Activity.values())
                output += act.toString() + "\n";
            return "This activity does not exist. These do:\n" + output;

        }

        Connection connect = new Connections().getConnection();
        try {
            PreparedStatement ps = connect.prepareStatement("INSERT INTO bgs_activity (username, userid, amount, activity, systemid) " +
                    "VALUES (?, ?, ?, ?, " +
                    "(SELECT systemid FROM bgs_systems WHERE shortname = ? OR fullname = ? LIMIT 1))");
            ps.setString(1, username);
            ps.setString(2, userid);
            ps.setInt   (3, amount);
            ps.setString(4, activity.toString());
            ps.setString(5, system);
            ps.setString(6, system);
            ps.executeUpdate();
        } catch (MySQLIntegrityConstraintViolationException e) {
            //This happens when the system was not found.

            String message = "**WARNING ACTION NOT LOGGED**\nInvalid system entered. You can use either the shortname or the fullname. Please select from:\n";
            return message + getSystems();
        } catch (SQLException e) {
            LogUtil.logErr(e);
            return "**WARNING ACTION NOT LOGGED**\nSomething went wrong saving your contribution. Please retry later";
        }

        if (!DataProvider.isDev())
            Statistics.getInstance().logBGSActivity(System.currentTimeMillis(), userid, username, activity.toString(), amount, system.toUpperCase());

        //TODO nice output for commander
        return "Your engagement has been noticed. Thanks for your service o7";
    }

    private static List<String> getCSVData(Date start, int ticks) {
        List<String> lines = new ArrayList<>();
        Date end = ticks == 0 ? new Date() : new Date(start.getTime() + (ticks*24*60*60*1000));

        Connection connect = new Connections().getConnection();
        try {
            PreparedStatement ps = connect
                    .prepareStatement("SELECT " +
                            "username AS CMDR, " +
                            "from_unixtime(floor((unix_timestamp(timestamp) - (15*60*60))/(24*60*60)) * (24*60*60) + (15*60*60)) AS tick_start, " +
                            "SUM( if( activity = 'Bond', amount, 0 ) ) AS Bonds, " +
                            "SUM( if( activity = 'Bounty', amount, 0 ) ) AS Bounties, " +
                            "SUM( if( activity = 'Mining', amount, 0 ) ) AS Mining, " +
                            "SUM( if( activity = 'Mission', amount, 0 ) ) AS Missions, " +
                            "SUM( if( activity = 'Scan', amount, 0 ) ) AS Scans, " +
                            "SUM( if( activity = 'Smuggling', amount, 0 ) ) AS Smuggling, " +
                            "SUM( if( activity = 'Trade', amount, 0 ) ) AS Trading " +
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
            LogUtil.logErr(e);
        }
        return lines;
    }

    private final String bgsHelp = " **BGS Bot Commands:**\n" +
            "Enter '/bgs' to add or remove BGS role\n" +
            "\n" +
            "Format for entering bgs commands is:\n"+
            "/bgs <activity>, <amount with no seperators>, <system identifier>\n" +
            "\n" +
            "\n" +
            "**BGS Activities:**\n" +
            "*bonds*: To log the value of combat bonds (#) claimed in a war or civil war.\n" +
            "\n" +
            "*bounties*: To log the value of bounties (#) cashed in.\n" +
            "\n" +
            "*mining*: To log the profit (#) you've made from selling mined commodities.\n" +
            "\n" +
            "*missions*: To log the number of missions completed (#) successfully.\n" +
            "\n" +
            "*murder*: To log number amount (#) of murders on opposing factions.\n" +
            "\n" +
            "*mystats*: To Receive a direct message detailing your total logged actions.\n" +
            "\n" +
            "*scans*: To log the value of exploration data (#) made with Universal Cartographics.\n" +
            "\n" +
            "*smuggling*: To log the profit (#) you've made by smuggling into a black market.\n" +
            "\n" +
            "*trade*: To log the profit made (#) when selling at a commodity market.\n" +
            "\n" +
            "**Important Notes / Caveats:**\n" +
            "When entering numbers (#) do not use thousand / million seperators.\n" +
            "e.g. '/bgs trade, 1500000'\n";
}
