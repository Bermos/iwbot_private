package commands.iw_commands;


import iw_bot.Listener;
import iw_bot.LogUtil;

import provider.Connections;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.ParseException;

import java.util.*;


import static commands.iw_commands.BGS.int_format_short;
import static iw_bot.Constants.*;

class BGSGoal {
    // goal stuff start
    static String endGoal(String[] args) {
        //bgs goal,end,goalid,endts (optional)
        Connection connect = new Connections().getConnection();
        try {
            Date endtime = new Date();
            if (args.length == 4) {
                endtime = USER_SDF.parse(args[3]);
            }
            PreparedStatement ps = connect.prepareStatement("UPDATE bgs_goal SET endts = ? WHERE goalid = ?");
            ps.setString(1, SQL_SDF.format(endtime));
            ps.setInt(2, Integer.parseInt(args[2]));
            ps.executeUpdate();
            return "**End time updated**\nSet to: " + USER_SDF.format(endtime);
        } catch (SQLException e) {
            return "**SQL Failed!**";
        } catch (ParseException e) {
            return "**SQL Date Failed!**";
        } catch (NumberFormatException e) {
            return "Parsing error. Make sure 'goalid' is a whole number";
        }

    }

    static ArrayList<String> listGoal(String startid, String recent, String userid, boolean showUserP) {
        Connection connect = new Connections().getConnection();
        ArrayList<String> messages = new ArrayList<>();
        try {
            PreparedStatement ps;
            String message;
            if (Integer.parseInt(recent) == 0) { // get active goals
                ps = connect.prepareStatement("SELECT goalid, (SELECT bgs_system.s_fullname FROM bgs_system WHERE bgs_system.systemid = g.systemid) AS s_fullname, " +
                        "startts, endts, ticks, note " +
                        "FROM bgs_goal g " +
                        "WHERE startts <= CURRENT_TIMESTAMP AND endts >= CURRENT_TIMESTAMP ORDER BY startts");
                message = "**Active Goals**\n" +
                        "Please ensure you carry out actions for the correct faction. If you are not sure ask on the #bgs_ops channel.\n\n";
            } else if(Integer.parseInt(startid) == 0 && Integer.parseInt(recent) <= 10) { // get X most recent goals (no more than 10)
                ps = connect.prepareStatement("SELECT *, (SELECT bgs_system.s_fullname FROM bgs_system WHERE bgs_system.systemid = g.systemid) AS s_fullname " +
                        "FROM bgs_goal g ORDER BY startts DESC LIMIT ?");
                ps.setInt(1, Integer.parseInt(recent));
                message = "**" + recent + " Most Recent Goals**\n" +
                        "Please ensure you carry out actions for the correct faction. If you are not sure ask on the #bgs_ops channel.\n\n";
            }
            else if(Integer.parseInt(startid) > 0 && Integer.parseInt(recent) <= 10) { // get X most recent goals starting from goalID Y (no more than 10)
                ps = connect.prepareStatement("SELECT *, (SELECT bgs_system.s_fullname FROM bgs_system WHERE bgs_system.systemid = g.systemid) AS s_fullname " +
                        "FROM bgs_goal g " +
                        "WHERE startts <= (SELECT startts FROM bgs_goal WHERE goalid = ?) " +
                        "ORDER BY startts DESC LIMIT ?");
                ps.setInt(1, Integer.parseInt(startid));
                ps.setInt(2, Integer.parseInt(recent));
                message = "**" + recent + " most recent goals starting from goal #" + startid + "**\n" +
                        "Please ensure you carry out actions for the correct faction. If you are not sure ask on the #bgs_ops channel.\n\n";
            }
            else {
                messages.add("You can not return more than 10 goals");
                return messages;
            }
            ResultSet rs = ps.executeQuery();
            int rows = 0;

            while (rs.next()) {
                rows = rows + 1;
                ps = connect.prepareStatement("SELECT i.activity, i.factionid, i.usergoal, i.globalgoal, g.systemid, g.endts, g.startts, " +
                        "(SELECT f_shortname FROM bgs_faction f WHERE f.factionid = i.factionid) AS f_shortname, " +
                        "(SELECT f_fullname FROM bgs_faction f WHERE f.factionid = i.factionid) AS f_fullname, " +
                        "(SELECT SUM(a.amount) FROM bgs_activity a WHERE a.activity = i.activity AND a.timestamp >= g.startts AND a.timestamp <= g.endts AND a.systemid = g.systemid AND a.factionid = i.factionid) AS globaldone, " +
                        "(SELECT SUM(a.amount) FROM bgs_activity a WHERE a.activity = i.activity AND a.timestamp >= g.startts AND a.timestamp <= g.endts AND a.systemid = g.systemid AND a.factionid = i.factionid AND a.userid = ?) AS userdone " +
                        "FROM bgs_goal_item i " +
                        "LEFT JOIN bgs_goal g ON i.goalid = g.goalid " +
                        "WHERE i.goalid = ? ORDER BY activity ASC;");
                ps.setString(1, userid);
                ps.setInt(2, rs.getInt("goalid"));
                ResultSet rs1 = ps.executeQuery();
                rs1.last();
                int numrows = rs1.getRow();
                rs1.beforeFirst();
                message += "**" + ((showUserP) ? "" : "(#" + rs.getString("goalid") + ") ") + rs.getString("s_fullname") + "**\nFrom " + USER_SDF.format(SQL_SDF.parse(rs.getString("startts"))) + " UTC to " + USER_SDF.format(SQL_SDF.parse(rs.getString("endts"))) + " UTC (" + BGS.dateDiff(new Date(), SQL_SDF.parse(rs.getString("endts"))) + ")";
                if (numrows > 0) {
                    if (showUserP) {
                        message += String.format("```%1$-17s | %2$-16s | %3$s\n", "", "Your Goal", "System Goal");
                    } else {
                        message += String.format("```%1$-17s | %2$-15s | %3$s\n", "", "CMDR (Num Met)", "System Goal");
                    }
                    List<String> factionNamesList = new ArrayList<>();
                    while (rs1.next()) {
                        double userP = ((double) rs1.getInt("userdone") / rs1.getInt("usergoal")) * 100;
                        double globalP = ((double) rs1.getInt("globaldone") / rs1.getInt("globalgoal")) * 100;
                        if (!factionNamesList.contains(rs1.getString("f_shortname") + " = " + rs1.getString("f_fullname"))) {
                            factionNamesList.add(rs1.getString("f_shortname") + " = " + rs1.getString("f_fullname"));
                        }
                        if (showUserP) {
                            message += String.format("%1$-17s | %2$-16s | %3$s\n", rs1.getString("activity") + " (" + rs1.getString("f_shortname") + ")", int_format_short(Integer.parseInt(rs1.getString("usergoal"))) + " (" + int_format_short(rs1.getInt("userdone")) + "/" + (int) userP + "%)", int_format_short(Integer.parseInt(rs1.getString("globalgoal"))) + " (" + int_format_short(rs1.getInt("globaldone")) + "/" + (int) globalP + "%)");
                        } else {
                            ps = connect.prepareStatement("SELECT IFNULL(COUNT(*),0) AS userfinished FROM (SELECT SUM(a.amount) AS total FROM bgs_activity a WHERE a.activity = ? AND a.timestamp >= ? AND a.timestamp <= ? AND a.systemid = ? AND a.factionid = ? GROUP BY userid HAVING total >= ?) AS tmp");
                            ps.setString(1, rs1.getString("activity"));
                            ps.setString(2, SQL_SDF.format(SQL_SDF.parse(rs1.getString("startts"))));
                            ps.setString(3, SQL_SDF.format(SQL_SDF.parse(rs1.getString("endts"))));
                            ps.setInt(4, rs1.getInt("systemid"));
                            ps.setInt(5, rs1.getInt("factionid"));
                            ps.setInt(6, rs1.getInt("usergoal"));
                            ResultSet rs2 = ps.executeQuery();
                            rs2.first();
                            message += String.format("%1$-17s | %2$-15s | %3$s\n", rs1.getString("activity") + " (" + rs1.getString("f_shortname") + ")", int_format_short(Integer.parseInt(rs1.getString("usergoal"))) + " (" + rs2.getString("userfinished") + ")", int_format_short(Integer.parseInt(rs1.getString("globalgoal"))) + " (" + int_format_short(rs1.getInt("globaldone")) + "/" + (int) globalP + "%)");
                        }
                    }
                    if (rs.getString("note").length() > 0) {
                        message += "\nSPECIAL ORDERS\n--------------\n" + rs.getString("note");
                    }
                    if (!factionNamesList.isEmpty()) {
                        String factionNames = String.join("\n", factionNamesList);
                        message += "\n" + factionNames;
                    }
                    message += "```";
                } else {
                    message += "```There are currently no goals set```";
                }
                messages.add(message);
                message = "";
            }
            if (rows == 0) {
                message += "No " + (Integer.parseInt(recent) == 0 ? "Active " : "") + "Goals found\n";
                messages.add(message);
            }
            return messages;

        } catch (SQLException e) {
            //This happens when the system was not found.
            messages.add("**SQL Failed!**");
            LogUtil.logErr(e);
            return messages;
        } catch (ParseException e) {
            messages.add("**SQL Date Failed!**");
            return messages;
        }
    }

    static String deleteGoal(String goalid) {
        Connection connect = new Connections().getConnection();
        try {
            PreparedStatement ps = connect.prepareStatement("DELETE FROM bgs_goal WHERE goalid = ?");
            ps.setInt(1, Integer.parseInt(goalid));
            if (ps.executeUpdate() == 0) {
                return "**Failed to delete goal!**\nDid you specify a correct goalid?";
            }

        } catch (SQLException e) {
            return "**Failed to delete goal!**";
        }
        return "**Goal and all Items Deleted**";
    }

    static String editGoal(String[] args) {
        //bgs goals,edit,goalid,System,Start Date:Time, Ticks
        Connection connect = new Connections().getConnection();
        try {
            Date starttime = USER_SDF.parse(args[4]);
            Date endtime = new Date(starttime.getTime() + (Integer.parseInt(args[5]) * 24 * 60 * 60 * 1000L));
            PreparedStatement ps = connect.prepareStatement("UPDATE bgs_goal SET systemid = (SELECT systemid FROM bgs_system WHERE (s_shortname = ? OR s_fullname = ?) LIMIT 1), " +
                    "startts = ?, " +
                    "endts = ?, " +
                    "ticks = ? " +
                    "WHERE goalid = ?");
            ps.setString(1, args[3]);
            ps.setString(2, args[3]);
            ps.setString(3, SQL_SDF.format(starttime));
            ps.setString(4, SQL_SDF.format(endtime));
            ps.setInt(5, Integer.parseInt(args[5]));
            ps.setInt(6, Integer.parseInt(args[2]));
            ps.executeUpdate();
        } catch (SQLException e) {
            //This only happens when there's a serious issue with mysql or the connection to it
            return "**Warning goal not updated**";
        } catch (ParseException e) {
            return "Parsing error. Make sure the start date follows the pattern 'dd/MM/yy HH:mm'";
        } catch (NumberFormatException e) {
            return "Parsing error. Make sure 'goalid' and 'ticks' are whole numbers";
        }
        return "**Goal Updated**";
    }

    static String addGoal(String[] args) {
        //bgs goals,add,System,Start Date:Time, Ticks, activity/faction/usergoal/globalgoal,activity/faction/usergoal/globalgoal,activity/faction/usergoal/globalgoal
        if (args.length >= 5) {
            Connection connect = new Connections().getConnection();
            try {
                Date starttime = USER_SDF.parse(args[3]);
                Date endtime = new Date(starttime.getTime() + (Integer.parseInt(args[4]) * 24 * 60 * 60 * 1000L));
                PreparedStatement ps = connect.prepareStatement("INSERT INTO bgs_goal (systemid, startts, endts, ticks, note) VALUES (" +
                        "(SELECT systemid FROM bgs_system WHERE (s_shortname = ? OR s_fullname = ?) AND s_hidden = '0' LIMIT 1), ?, ?, ?, '')", PreparedStatement.RETURN_GENERATED_KEYS);
                ps.setString(1, args[2]);
                ps.setString(2, args[2]);
                ps.setString(3, SQL_SDF.format(starttime));
                ps.setString(4, SQL_SDF.format(endtime));
                ps.setInt(5, Integer.parseInt(args[4]));
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                int goalid = 0;
                if (rs.next()) {
                    goalid = rs.getInt(1);
                }

                String message = "**New goal added to BGS logging #" + goalid + "**\n" +
                        "*Start:* " + starttime + "\n" +
                        "*End:* " + endtime + " (" + args[4] + " ticks)\n";
                if (args.length >= 6) {
                    for (int i = 5; i < args.length; i++) {
                        String[] goalitem = args[i].split("/");
                        message += addGoalItem(goalitem, i - 4, Integer.toString(goalid));
                    }
                    return message;
                }
            } catch (SQLException e) {
                //This only happens when there's a serious issue with mysql or the connection to it
                return "**Warning goal not created**";
            } catch (ParseException e) {
                return "Parsing error. Make sure the start date follows the pattern 'dd/MM/yy HH:mm'";
            } catch (NumberFormatException e) {
                return "Parsing error. Make sure the 'ticks', and any 'UserGoals' or 'GlobalGoals' are whole numbers";
            }

            return "New goal added to BGS logging.";
        } else {
            return BGS_GOAL_ADD_HELP;
        }
    }

    static String addGoalNote(String[] args) {
        //bgs goals, goalid, note
        try {
            int goalid = Integer.parseInt(args[2]);
            String note = String.join(", ", Arrays.copyOfRange(args, 3, args.length)); // join notes back together in case the note string contained a comma

            Connection connect = new Connections().getConnection();

            PreparedStatement ps = connect.prepareStatement("UPDATE bgs_goal SET note = ? WHERE goalid = ?");
            ps.setString(1, note);
            ps.setInt(2, goalid);
            int updated = ps.executeUpdate();
            if (updated == 0) {
                return "**Failed to add Note**\nCheck you have specified a valid goalid";
            }
        } catch (SQLException e) {
            return "**SQL ERROR NOTE NOT ADDED**\n";
        }
        return "**Note added to goal #" + args[2] + "**";
    }

    static String deleteGoalItem(BGS.Activity activity, String goalid, String factionid) {
        Connection connect = new Connections().getConnection();
        try {
            PreparedStatement ps = connect.prepareStatement("DELETE FROM bgs_goal_item WHERE goalid = (SELECT goalid FROM bgs_goal WHERE goalid = ?) AND activity = ? AND factionid = (SELECT factionid FROM bgs_faction WHERE factionid = ?) LIMIT 1");
            ps.setInt(1, Integer.parseInt(goalid));
            ps.setString(2, activity.toString());
            ps.setString(3, factionid);
            ps.executeUpdate();
        } catch (SQLException e) {
            return(BGS_GOAL_DELACT_HELP);
        }
        return "**Goal Item Deleted**";
    }

    private static int getGoalSystemid(int goalid) {
        int systemid = 0;
        Connection connect = new Connections().getConnection();
        try {
            PreparedStatement ps = connect.prepareStatement("SELECT systemid FROM bgs_goal WHERE goalid = ?");
            ps.setInt(1, goalid);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                systemid = rs.getInt("systemid");
            }
            return systemid;
        } catch (SQLException e) {
            return -1;
        }
    }

    static String addGoalItem(String[] goalitem, Integer i, String goalid) {
        String message;
        if (goalitem.length == 4) {
            BGS.Activity activity = BGS.Activity.from(goalitem[0]);
            int systemid = getGoalSystemid(Integer.parseInt(goalid));
            if (systemid < 0) {
                return "Invalid goal!";
            }
            int factionid = BGSFaction.checkFactionInSystem(goalitem[1], BGSSystem.getSystemFullname(systemid), false);
            if (factionid <= 0) {
                factionid = BGSFaction.checkFactionInSystem(goalitem[1], BGSSystem.getSystemFullname(systemid), true);
                if (factionid > 0) {
                    return "Faction (" + BGSFaction.getFactionFullname(factionid) + ") is currently hidden in " + BGSSystem.getSystemFullname(systemid) + ". Make the faction visible or try use of these:\n" + BGSFaction.getFactions(true, systemid);
                } else {
                    return "Incorrect faction (" + goalitem[1] + ") for goal item #" + Integer.toString(i) + ". Try one of these:\n" + BGSFaction.getFactions(true, systemid);
                }
            } else if (activity == null) {
                String output = "";
                for (BGS.Activity act : BGS.Activity.values())
                    output += act.toString() + "\n";
                return "Incorrect activity (" + goalitem[0] + ") for goal item #" + Integer.toString(i) + ". Try one of these:\n" + output;
            } else {
                Connection connect = new Connections().getConnection();
                try {
                    PreparedStatement ps = connect.prepareStatement("INSERT INTO bgs_goal_item (goalid, factionid, activity, usergoal, globalgoal) \n" +
                            "VALUES ((SELECT goalid FROM bgs_goal WHERE goalid = ?), ?, ?, ?, ?) ON DUPLICATE KEY UPDATE usergoal=?,globalgoal=?");
                    ps.setInt(1, Integer.parseInt(goalid));
                    ps.setInt(2, factionid);
                    ps.setString(3, activity.toString());
                    ps.setInt(4, Integer.parseInt(goalitem[2]));
                    ps.setInt(5, Integer.parseInt(goalitem[3]));
                    ps.setInt(6, Integer.parseInt(goalitem[2]));
                    ps.setInt(7, Integer.parseInt(goalitem[3]));

                    ps.executeUpdate();
                } catch (NumberFormatException e) {
                    return "Parsing error. Make sure 'UserGoal' and 'GlobalGoal' are whole numbers for goal item #" + Integer.toString(i) + ".\n" + String.join("/", goalitem);
                } catch (SQLException e) {
                    //This only happens when there's a serious issue with mysql or the connection to it
                    return "**Failed adding Goal Item #" + Integer.toString(i) + "**\n" +
                            "Check goalid (" + goalid + ") is valid. Check format of goal item (" + String.join("/", goalitem) + ")\n";
                }
            }

            message = "**Goal Item #" + Integer.toString(i) + " Added/Updated to Goal " + goalid + "**\n" +
                    "*Activity:* " + activity + "\n" +
                    "*Faction:* " + goalitem[1] + "\n" +
                    "*CMDR Goal:* " + NumberFormat.getInstance(Locale.GERMANY).format(Integer.parseInt(goalitem[2])).replace('.', '\'') + "\n" +
                    "*Global Goal:* " + NumberFormat.getInstance(Locale.GERMANY).format(Integer.parseInt(goalitem[3])).replace('.', '\'') + "\n";
        } else {
            message = "**Failed adding goal item #" + Integer.toString(i) + ":**\n" +
                    "Each goal item requires 4 arguments: <activity>/<faction>/<UserGoal>/<GlobalGoal>.\n" +
                    "You supplied the following: " + String.join("/", goalitem) + "\n\n";
        }
        return message;
    }

    // goal stuff end
}
