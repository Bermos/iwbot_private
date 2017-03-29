package commands.iw_commands;


import iw_bot.Listener;
import iw_bot.LogUtil;

import org.hamcrest.core.IsNull;
import provider.Connections;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

import static iw_bot.Constants.*;

class BGSStats {
    static List<String> getFullTick(String[] args) {
        List<String> outputs = new ArrayList<>();
        try {
            Date time = USER_SDF.parse(args[3]);
            List<String> lines = getCSVData(time, Integer.parseInt(args[2]));

            args[2] = args[2].equals("1") ? "1 tick" : args[2] + " ticks";

            String output = "Data for " + args[2] + " after " + args[3] + ":\n";
            output += "----------------------------------------------------------------------\n";
            output += lines.get(0) + "\n```";
            for (int i = 1; i < lines.size(); i++) {
                output += lines.get(i) + "\n";
                if (output.length() >= 1900) {
                    output += "```";
                    outputs.add(output);
                    output = "```";
                }
            }
            if (lines.size() <= 1)
                output += "No records found";
            output += "```";

            outputs.add(output);
        } catch (ParseException e) {
            outputs.add("Parsing error. Make sure the date follows the pattern 'dd/MM/yy HH:mm'");
        }

        return outputs;
    }

    static List<String> getTick(String[] args) {
        String system = "all";
        Boolean first = true;
        List<String> output = new ArrayList<>();
        String block;

        if (args.length == 5) {
            system = args[4];
        }
        try {
            Date time = USER_SDF.parse(args[3]);
            block = "Data for " + args[2] + " ticks after " + args[3] + " UTC System Filter: " + system + "\n";
            Map<String, String> entries = getTotalAmount(time, Integer.parseInt(args[2]), system);
            for (Entry<String, String> entry : entries.entrySet()) {
                if(Objects.equals(entry.getValue(), "systemtitle")){
                    if (!first){
                        block += "```";
                        output.add(block);
                        block = "";
                    }
                    block += "**" + entry.getKey() + "**\n```";
                    first = false;
                }
                else {
                    if(Objects.equals(entry.getValue(), "factionlist")){
                        block += "```" + entry.getKey();
                    }
                    else {
                        block += entry.getKey() + ": " + entry.getValue() + "\n";
                    }
                }
            }
            output.add(block);
            if (entries.isEmpty()) {
                output.add("No records for the specified period");
                return output;
            }
            else {
                return output;
            }
        } catch (ParseException e) {
            output.add("Parsing error. Make sure the date follows the pattern 'dd/MM/yy HH:mm'");
            return output;
        }
    }

    static String getUserStats(String userID) {
        String output = "**All Time Totals**\n```";
        for (Entry<BGS.Activity, Double> entry : getTotalAmount(userID).entrySet()) {
            output += entry.getKey().toString() + ": " + NumberFormat.getInstance(Locale.GERMANY).format(entry.getValue().intValue()).replace('.', '\'') + "\n";
        }
        output += "```\n";
        return output;
    }

    private static Map<BGS.Activity, Double> getTotalAmount(String userid) {
        Map<BGS.Activity, Double> totals = new LinkedHashMap<>();

        Connection connect = new Connections().getConnection();
        try {
            PreparedStatement ps = connect.prepareStatement("SELECT activity, SUM(amount) AS total FROM bgs_activity WHERE userid = ? GROUP BY activity ORDER BY activity ASC");
            ps.setString(1, userid);
            ResultSet rs = ps.executeQuery();

            while (rs.next())
                totals.put(BGS.Activity.from(rs.getString("activity")), rs.getDouble("total"));
        } catch (SQLException e) {
            LogUtil.logErr(e);
        }
        return totals;
    }

    static String getTotalAmount() {
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

    private static Map<String, String> getTotalAmount(Date start, int ticks, String system) {
        Map<String, String> totals = new LinkedHashMap<>();
        Date end = ticks == 0 ? new Date() : new Date(start.getTime() + (ticks * 24 * 60 * 60 * 1000L));
        String systemcheck = "";
        Set<String> factions = new TreeSet<>();

        Connection connect = new Connections().getConnection();
        try {
            if (system.equals("all")) {
                PreparedStatement ps = connect.prepareStatement("SELECT (SELECT bgs_system.s_fullname FROM bgs_system WHERE bgs_system.systemid = b.systemid) AS s_fullname, " +
                        "(SELECT bgs_faction.f_fullname FROM bgs_faction WHERE bgs_faction.factionid = b.factionid) AS f_fullname, " +
                        "(SELECT bgs_faction.f_shortname FROM bgs_faction WHERE bgs_faction.factionid = b.factionid) AS f_shortname, " +
                        "b.activity, " +
                        "SUM(b.amount) AS total," +
                        "COUNT(DISTINCT(b.userid)) AS numcmdrs " +
                        "FROM bgs_activity b " +
                        "WHERE timestamp > ? AND timestamp < ? " +
                        "GROUP BY s_fullname, f_fullname, activity " +
                        "ORDER BY s_fullname ASC, activity ASC, f_fullname ASC");
                ps.setString(1, SQL_SDF.format(start));
                ps.setString(2, SQL_SDF.format(end));
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    double cmdravg = (double) rs.getInt("total") / rs.getInt("numcmdrs");
                    if(!Objects.equals(systemcheck, rs.getString("s_fullname"))){
                        totals.put(rs.getString("s_fullname"),"systemtitle");
                        systemcheck = rs.getString("s_fullname");
                    }
                    totals.put((BGS.Activity.valueOf(rs.getString("activity").toUpperCase()).toString()) + " (" + rs.getString("f_shortname") + ")", NumberFormat.getInstance(Locale.GERMANY).format(rs.getInt("total")).replace('.', '\'') + " from " + rs.getInt("numcmdrs") + " CMDRs (" + BGS.int_format_short((int) cmdravg) + " avg.)");
                    factions.add(rs.getString("f_shortname") + " = " + rs.getString("f_fullname"));
                }
            } else {
                PreparedStatement ps = connect.prepareStatement("SELECT (SELECT bgs_system.s_fullname FROM bgs_system WHERE bgs_system.systemid = b.systemid) AS s_fullname," +
                        "(SELECT bgs_faction.f_fullname FROM bgs_faction WHERE bgs_faction.factionid = b.factionid) AS f_fullname, " +
                        "(SELECT bgs_faction.f_shortname FROM bgs_faction WHERE bgs_faction.factionid = b.factionid) AS f_shortname, " +
                        "b.activity, " +
                        "SUM(b.amount) AS total, " +
                        "COUNT(DISTINCT(b.userid)) AS numcmdrs " +
                        "FROM bgs_activity b " +
                        "WHERE b.timestamp > ? AND b.timestamp < ? AND b.systemid = (SELECT systemid FROM bgs_system WHERE (s_shortname = ? OR s_fullname = ?) AND s_hidden = '0' LIMIT 1) " +
                        "GROUP BY s_fullname, f_fullname, activity " +
                        "ORDER BY s_fullname ASC, activity ASC, f_fullname ASC");
                ps.setString(1, SQL_SDF.format(start));
                ps.setString(2, SQL_SDF.format(end));
                ps.setString(3, system);
                ps.setString(4, system);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    double cmdravg = (double) rs.getInt("total") / rs.getInt("numcmdrs");
                    if(!Objects.equals(systemcheck, rs.getString("s_fullname"))){
                        totals.put(rs.getString("s_fullname"),"systemtitle");
                        systemcheck = rs.getString("s_fullname");
                    }
                    totals.put((BGS.Activity.valueOf(rs.getString("activity").toUpperCase()).toString()) + " (" + rs.getString("f_shortname") + ")", NumberFormat.getInstance(Locale.GERMANY).format(rs.getInt("total")).replace('.', '\'') + " from " + rs.getInt("numcmdrs") + " CMDRs (" + BGS.int_format_short((int) cmdravg) + " avg.)");
                }
            }
            if(factions.size() > 0) {
                totals.put("\n" + String.join("\n", factions),"factionlist");
            }
        } catch (SQLException e) {
            LogUtil.logErr(e);
        }
        return totals;
    }

    private static List<String> getCSVData(Date start, int ticks) {
        List<String> lines = new ArrayList<>();
        Date end = (ticks == 0) ? new Date() : new Date(start.getTime() + (ticks * 24 * 60 * 60 * 1000L));

        int tickHour = Integer.parseInt(new SimpleDateFormat("HH").format(start));
        int tickMinute = Integer.parseInt(new SimpleDateFormat("mm").format(start));

        Connection connect = new Connections().getConnection();
        try {
            PreparedStatement ps = connect.prepareStatement("SELECT " +
                    "b.userid, " +
                    "(SELECT user.username FROM user WHERE user.iduser = b.userid) AS CMDR, " +
                    "from_unixtime(floor((unix_timestamp(timestamp) - ((?*60*60) + (?*60)))/(24*60*60)) * (24*60*60) + ((?*60*60) + (?*60) + (24*60*60)), '%e %b %Y') AS Tick, " +
                    "(SELECT bgs_system.s_fullname FROM bgs_system WHERE bgs_system.systemid = b.systemid) AS System, " +
                    "SUM( if( b.activity = 'Bond',      b.amount, 0 ) ) AS Bonds, " +
                    "SUM( if( b.activity = 'Bounty',    b.amount, 0 ) ) AS Bounties, " +
                    "SUM( if( b.activity = 'Failed',    b.amount, 0 ) ) AS Failed, " +
                    "SUM( if( b.activity = 'Fine',      b.amount, 0 ) ) AS Fine, " +
                    "SUM( if( b.activity = 'Intel',     b.amount, 0 ) ) AS Intel, " +
                    "SUM( if( b.activity = 'Mining',    b.amount, 0 ) ) AS Mining, " +
                    "SUM( if( b.activity = 'Mission',   b.amount, 0 ) ) AS Missions, " +
                    "SUM( if( b.activity = 'Murder',    b.amount, 0 ) ) AS Murder," +
                    "SUM( if( b.activity = 'Scan',      b.amount, 0 ) ) AS Scans, " +
                    "SUM( if( b.activity = 'Smuggling', b.amount, 0 ) ) AS Smuggling, " +
                    "SUM( if( b.activity = 'Trade',     b.amount, 0 ) ) AS Trading " +
                    "FROM " +
                    "bgs_activity b " +
                    "WHERE " +
                    "b.timestamp >= ? AND b.timestamp < ? " +
                    "GROUP BY " +
                    "System, b.userid, Tick " +
                    "ORDER BY Tick ASC, b.userid ASC");
            ps.setInt(1, tickHour);
            ps.setInt(2, tickMinute);
            ps.setInt(3, tickHour);
            ps.setInt(4, tickMinute);
            ps.setString(5, SQL_SDF.format(start));
            ps.setString(6, SQL_SDF.format(end));
            ResultSet rs = ps.executeQuery();

            // We already know those 3 columns for sure. Don't check for them, just append all the uncertain ones afterwards
            String columnNames = "CMDR, Tick";
            int columnCount = rs.getMetaData().getColumnCount();
            for (int i = 5; i <= columnCount; i++) {
                columnNames = String.join(", ", columnNames, rs.getMetaData().getColumnName(i));
            }
            columnNames += ", System";
            lines.add(columnNames);

            List<String> cmdrNamesList = new ArrayList<>();
            while (rs.next()) {
                String rowValues = Listener.jda.getUserById(rs.getString("userid")).getName() + ",";
                if (!cmdrNamesList.contains("@" + Listener.jda.getUserById(rs.getString("userid")).getName())) {
                    cmdrNamesList.add("@" + Listener.jda.getUserById(rs.getString("userid")).getName());
                }
                rowValues += rs.getString("Tick") + ",";
                for (int i = 4; i <= columnCount; i++) {
                    rowValues += (rs.getString(i).equals("0") ? "" : rs.getString(i)) + ",";
                }
                rowValues += rs.getString("System");
                lines.add(rowValues);
            }
            if (!cmdrNamesList.isEmpty()) {
                String cmdrNames = String.join(", ", cmdrNamesList);
                try {
                    cmdrNames = new StringBuilder(cmdrNames).replace(cmdrNames.lastIndexOf(","), cmdrNames.lastIndexOf(",") + 1, " and").toString();
                } catch (StringIndexOutOfBoundsException ignored) {
                }


                lines.add("");
                lines.add(cmdrNames);
            }

        } catch (SQLException e) {
            LogUtil.logErr(e);
        }
        return lines;
    }
}
