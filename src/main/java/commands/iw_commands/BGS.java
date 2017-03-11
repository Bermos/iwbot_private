package commands.iw_commands;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import commands.GuildCommand;
import commands.PMCommand;
import iw_bot.JDAUtil;
import iw_bot.Listener;
import iw_bot.LogUtil;
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
import java.util.Map.Entry;

import static iw_bot.Constants.*;


public class BGS implements PMCommand, GuildCommand {
    enum Activity {
        BOND, BOUNTY, FAILED, FINE, INTEL, MINING, MISSION, SCAN, SMUGGLING, TRADE, MURDER;

        public String toString() {
            return name().charAt(0) + name().substring(1).toLowerCase();
        }

        public static Activity from(String input) {
            input = input.toLowerCase();
            Activity output;
            switch (input) {
                case "bon"       :
                case "bond"      :
                case "bonds" 	 : output = Activity.BOND;	    break;
                case "bou"       :
                case "bounty"    :
                case "bounties"  : output = Activity.BOUNTY;	break;
                case "fai"       :
                case "fail"       :
                case "failed"    : output = Activity.FAILED;	break;
                case "fin"       :
                case "fine"      :
                case "fines"     : output = Activity.FINE;	    break;
                case "int"       :
                case "intel"     : output = Activity.INTEL;	    break;
                case "min"       :
                case "mining" 	 : output = Activity.MINING;	break;
                case "mis"       :
                case "mission"   :
                case "missions"  : output = Activity.MISSION;   break;
                case "mur"	     :
                case "murder"	 : output = Activity.MURDER;	break;
                case "exploration" :
                case "sca"       :
                case "scan"      :
                case "scans"	 : output = Activity.SCAN;      break;
                case "smu"       :
                case "smuggle"   :
                case "smuggling" : output = Activity.SMUGGLING; break;
                case "tra"       :
                case "trading"   :
                case "trade"	 : output = Activity.TRADE;	    break;
                default          : output = null;               break;
            }

            return output;
        }
    }

    @Override
    public void runCommand(PrivateMessageReceivedEvent event, String[] args) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("mystats")) {
                ArrayList<String> messages = listGoal("0", event.getAuthor().getId(), true);
                messages.add(getUserStats(event.getAuthor().getId()));
                String messageToSend = "";
                for (String message : messages){ // check if we are going to exceed the 2000 character limit of a message
                    if (messageToSend.length() > 0 && (messageToSend.length() + message.length() > 2000)){
                        event.getChannel().sendMessage(messageToSend + "\u0000").queue();
                        messageToSend = "";
                        messageToSend += message;
                    }
                    else { // need the line break if not going to be seperate messages.
                        messageToSend += "\n" + message;
                    }
                }
                event.getChannel().sendMessage(messageToSend).queue();
            } else if (args[0].equalsIgnoreCase("mustard")) {  // for the fun of autocorrect added mustard command (mystats gone wrong)
                event.getChannel().sendMessage(":hotdog:").queue();
            }
            else if (args[0].equalsIgnoreCase("help")) {
                event.getChannel().sendMessage(BGS_LOG_HELP).queue();
            }
        }
    }

    @Override
    public void runCommand(GuildMessageReceivedEvent event, String[] args) {
        if (args.length == 0) {
            toggleBgsRole(event);
        }
        // admin calls for stats
        else if (args[0].equalsIgnoreCase("stats") && DataProvider.isAdmin(event)) {
            if (args.length == 1) { // send default help message for stats admin
                event.getChannel().sendMessage(BGS_STATS_HELP).queue();

            } else if (args[1].equalsIgnoreCase("summary") || args[1].equalsIgnoreCase("sum")){ // summary stats
                // Two options. No system filter and filtering for a system
                if(args.length == 4 || args.length == 5 ) {
                    event.getChannel().sendMessage(getTick(args)).queue();

                } else { // send help for summary stats
                    event.getChannel().sendMessage(BGS_STATS_HELP).queue();

                }
            }
            // CSV output
            else if (args[1].equalsIgnoreCase("csv") && args.length >= 4){
                JDAUtil.sendMultipleMessages(event.getChannel(), getFullTick(args));

            } else { // If got this far something went wrong so show help for stats
                event.getChannel().sendMessage(BGS_STATS_HELP).queue();

            }
        }
        // admin functions for system factions
        else if ((args[0].equalsIgnoreCase("faction") || args[0].equalsIgnoreCase("factions") || args[0].equalsIgnoreCase("fac")) && DataProvider.isAdmin(event)){
            if (args.length == 1) { // send default help message for system admin
                event.getChannel().sendMessage(BGS_FACTION_HELP).queue();
            } else if (args[1].equalsIgnoreCase("list")) { // Output a list of the factions available
                //bgs faction, list, <system>
                if (args.length == 2) {
                    event.getChannel().sendMessage("**BGS Factions**\n" + getFactions(true, 0) + getFactions(true, -1)).queue();
                } else if (args.length == 3) {
                    event.getChannel().sendMessage(getFactions(DataProvider.isAdmin(event), systemExists(args[2], args[2], 0))).queue();
                } else {
                    event.getChannel().sendMessage(BGS_FACTION_HELP).queue();
                }
            } else if (args[1].equalsIgnoreCase("hide") || args[1].equalsIgnoreCase("show")) { // show or hide a faction
                //bgs faction, <show | hide>, faction, system
                if (args.length == 4) {
                    event.getChannel().sendMessage(setFactionVisibility(args[1].equalsIgnoreCase("show"), args[2], args[3])).queue();
                } else { // show system help if wrong number of arguments
                    event.getChannel().sendMessage(BGS_FACTION_HIDE_HELP + "\n" + getFactions(DataProvider.isAdmin(event),-1)).queue();
                }

            } else if (args[1].equalsIgnoreCase("add")) { // add a new faction to the database
                //bgs faction, add, <shortname>, <fullname>
                event.getChannel().sendMessage(addFaction(args)).queue();

            } else if (args[1].equalsIgnoreCase("edit")) { // edit a faction in the database
                //bgs faction, edit, <factionid>, <shortname>, <fullname>
                event.getChannel().sendMessage(editFaction(args)).queue();

            } else if (args[1].equalsIgnoreCase("assign") || args[1].equalsIgnoreCase("ass")) { // assign a faction to a system
                //bgs faction, assign, <factionname>, <systemname>
                event.getChannel().sendMessage(assignFaction(args)).queue();

            } else{ // if got this far something went wrong. Show system help
                event.getChannel().sendMessage(BGS_FACTION_HELP).queue();
            }
        }
        // admin functions for systems
        else if ((args[0].equalsIgnoreCase("system") || args[0].equalsIgnoreCase("systems") || args[0].equalsIgnoreCase("sys")) && DataProvider.isAdmin(event)){
            if (args.length == 1) { // send default help message for system admin
                event.getChannel().sendMessage(BGS_SYSTEM_HELP).queue();

            } else if (args[1].equalsIgnoreCase("list")) { // Output a list of the systems available
                event.getChannel().sendMessage("**BGS Star Systems**\n" + getSystems(DataProvider.isAdmin(event))).queue();

            } else if (args[1].equalsIgnoreCase("hide") || args[1].equalsIgnoreCase("show")) { // show or hide a system
                if (args.length == 3) {
                    event.getChannel().sendMessage(setSystemVisibility(args[1].equalsIgnoreCase("show"), args[2])).queue();

                } else { // show system help if wrong number of arguments
                    event.getChannel().sendMessage(BGS_SYSTEM_HIDE_HELP + "\n" + getSystems(DataProvider.isAdmin(event))).queue();
                }

            } else if (args[1].equalsIgnoreCase("add")) { // add a new system to the database
                //bgs system, add, <shortname>, <fullname>
                event.getChannel().sendMessage(addSystem(args)).queue();

            } else if (args[1].equalsIgnoreCase("edit")) { // edit a system in the database
                //bgs system, edit, <systemid>, <shortname>, <fullname>
                event.getChannel().sendMessage(editSystem(args)).queue();

            } else{ // if got this far something went wrong. Show system help
                event.getChannel().sendMessage(BGS_SYSTEM_HELP).queue();
            }
        }
        // admin functions for goals
        else if ((args[0].equalsIgnoreCase("goal") || args[0].equalsIgnoreCase("goals")) && DataProvider.isAdmin(event)) {
            if (args.length == 1) { // Send help message for goals
                event.getChannel().sendMessage(BGS_GOAL_HELP).queue();

            }else if (args[1].equalsIgnoreCase("end")) { // end a goal now a new goal.
                //bgs goals,end,goalid
                if (args.length >= 3) { // 5 arguments specifies no goal items. More than 5 means at least one goal item is specified.
                    event.getChannel().sendMessage(endGoal(args)).queue();
                } else{ // show help message for adding a goal as less than 5 arguments
                    event.getChannel().sendMessage(BGS_GOAL_END_HELP).queue();
                }
            }
            else if (args[1].equalsIgnoreCase("add")) { // add a new goal.
                //bgs goals,add,system,Start Date:Time, Ticks, activity/faction/usergoal/globalgoal,activity/faction/usergoal/globalgoal,activity/faction/usergoal/globalgoal
                if (args.length >= 5) { // 5 arguments specifies no goal items. More than 5 means at least one goal item is specified.
                    event.getChannel().sendMessage(addGoal(args)).queue();
                } else{ // show help message for adding a goal as less than 5 arguments
                    event.getChannel().sendMessage(BGS_GOAL_ADD_HELP).queue();
                }
            }
            else if (args[1].equalsIgnoreCase("delete") || args[1].equalsIgnoreCase("del")) { // delete a goal
                //bgs goals,<del>,goalid
                if (args.length == 3) {
                    event.getChannel().sendMessage(deleteGoal(args[2])).queue();
                } else {
                    event.getChannel().sendMessage(BGS_GOAL_DEL_HELP).queue();
                }
            }
            else if (args[1].equalsIgnoreCase("deleteact") || args[1].equalsIgnoreCase("delact")) { // delete an individual goal item from a goal
                //bgs goals,<delact>,goalid,Activity
                if (args.length == 4) {
                    event.getChannel().sendMessage(deleteGoalItem(Activity.from(args[3]),args[2])).queue();
                } else {
                    event.getChannel().sendMessage(BGS_GOAL_DELACT_HELP).queue();
                }
            }
            else if (args[1].equalsIgnoreCase("edit")) { // edit a goal. Goal items are edited using /editact
                //bgs golas,edit,goalid,system,Start Date:Time, Ticks
                if (args.length == 6) {
                    event.getChannel().sendMessage(editGoal(args)).queue();
                } else {
                    event.getChannel().sendMessage(BGS_GOAL_EDIT_HELP).queue();
                }
            }
            else if (args[1].equalsIgnoreCase("editactivity") || args[1].equalsIgnoreCase("addactivity") || args[1].equalsIgnoreCase("editact") || args[1].equalsIgnoreCase("addact")) {
                //bgs goals,<addact,editact>,goalid,Activity/usergoal/globalgoal,Activity/usergoal/globalgoal,Activity/usergoal/globalgoal
                String message = "";
                // need 4 or more arguments to add a goal item
                if (args.length >= 4) {
                    // loop through the goal items which are in the array from 3 onwards
                    for (int i = 3; i < args.length; i++) {
                        String[] goalitem = args[i].split("/");
                        message += addGoalItem(goalitem, i-2, args[2]); // add the goal item
                    }
                    event.getChannel().sendMessage(message).queue();
                } else {
                    event.getChannel().sendMessage(BGS_GOAL_EDITACT_HELP).queue();
                }
            }

            else if (args[1].equalsIgnoreCase("note")) { // add a note to a goal. Allows special instructions to be given.
                if (args.length >= 3) {
                    //bgs goals, goalid, note
                    event.getChannel().sendMessage(addGoalNote(args)).queue();
                } else {
                    event.getChannel().sendMessage(BGS_GOAL_NOTE_HELP).queue();
                }
            }

            else if (args[1].equalsIgnoreCase("list")) {
                //bgs goals, list,#
                //Get # most recent goals
                //If # is not specified then get all active goals
                String recent = "0";
                if(args.length == 3) {
                    recent = args[2];
                }

                ArrayList<String> messages = listGoal(recent,event.getAuthor().getId(), false);
                String messageToSend = "";
                for (String message : messages){ // check if we are going to exceed the 2000 character limit of a message
                    if (messageToSend.length() > 0 && (messageToSend.length() + message.length() > 2000)){
                        event.getChannel().sendMessage(messageToSend + "\u0000").queue();
                        messageToSend = "";
                        messageToSend += message;
                    }
                    else { // need the line break if not going to be seperate messages.
                        messageToSend += "\n" + message;
                    }
                }
                event.getChannel().sendMessage(messageToSend).queue();
            }
            else{
                event.getChannel().sendMessage(BGS_GOAL_HELP).queue();
            }
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("mystats")) {
                ArrayList<String> messages = listGoal("0", event.getAuthor().getId(), true);
                messages.add(getUserStats(event.getAuthor().getId()));
                String messageToSend = "";
                for (String message : messages){ // check if we are going to exceed the 2000 character limit of a message
                    if (messageToSend.length() > 0 && (messageToSend.length() + message.length() > 2000)){
                        JDAUtil.getPrivateChannel(event.getAuthor()).sendMessage(messageToSend + "\u0000").queue();
                        messageToSend = "";
                        messageToSend += message;
                    }
                    else { // need the line break if not going to be seperate messages.
                        messageToSend += "\n" + message;
                    }
                }
                JDAUtil.getPrivateChannel(event.getAuthor()).sendMessage(messageToSend).queue();

            } else if (args[0].equalsIgnoreCase("mustard")) { // for the fun of autocorrect added mustard command (mystats gone wrong)
                JDAUtil.getPrivateChannel(event.getAuthor()).sendMessage(":hotdog:").queue();
            } else if (args[0].equalsIgnoreCase("total")) {
                if (DataProvider.isAdmin(event.getMember().getRoles()))
                    event.getChannel().sendMessage(getTotalAmount()).queue();
                else
                    JDAUtil.getPrivateChannel(event.getAuthor()).sendMessage(getTotalAmount()).queue();

            } else if (args[0].equalsIgnoreCase("help")) {
                JDAUtil.getPrivateChannel(event.getAuthor()).sendMessage(BGS_LOG_HELP).queue();
            }
        } else if (args.length == 2) {
            String message = "**WARNING ACTION NOT LOGGED**\nStar system not specified? Enter '" + Listener.prefix + "bgs help' or use one of the star system names below:\n";
            message += getSystems(DataProvider.isAdmin(event));
            event.getChannel().sendMessage(message).queue();

        }
        else if (args.length == 3) {
            event.getChannel().sendMessage(logActivity(DataProvider.isAdmin(event), args[0], event.getAuthor().getId(), event.getMember().getEffectiveName(), args[1], args[2])).queue();

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

    // goal stuff start
    private String endGoal(String[] args) {
        //bgs goal,end,goalid,endts (optional)
        Connection connect = new Connections().getConnection();
        try {
            Date endtime = new Date();
            if(args.length == 4) {
                endtime = USER_SDF.parse(args[3]);
            }
            PreparedStatement ps = connect.prepareStatement("UPDATE bgs_goal SET endts = ? WHERE goalid = ?");
            ps.setString(1, SQL_SDF.format(endtime));
            ps.setInt(2, Integer.parseInt(args[2]));
            ps.executeUpdate();
            return "**End time updated**\nSet to: " + USER_SDF.format(endtime);
        } catch (SQLException e) {
            //This happens when the system was not found.
            return "**SQL Failed!**";
        } catch (ParseException e) {
            return "**SQL Date Failed!**";
        } catch (NumberFormatException e) {
            return "Parsing error. Make sure 'goalid' is a whole number";
        }

    }

    private static ArrayList<String> listGoal(String recent, String userid, boolean showUserP) {
        Connection connect = new Connections().getConnection();
        ArrayList<String> messages = new ArrayList<>();
        try {
            PreparedStatement ps;
            String message = "";
            if(Integer.parseInt(recent) == 0) { // get active goals
                ps = connect.prepareStatement("SELECT goalid, (SELECT bgs_system.s_fullname FROM bgs_system WHERE bgs_system.systemid = g.systemid) AS s_fullname, "+
                        "startts, endts, ticks, note "+
                        "FROM bgs_goal g "+
                        "WHERE startts <= CURRENT_TIMESTAMP AND endts >= CURRENT_TIMESTAMP ORDER BY startts");
            } else{
                ps = connect.prepareStatement("SELECT *, (SELECT bgs_system.s_fullname FROM bgs_system WHERE bgs_system.systemid = g.systemid) AS s_fullname "+
                                "FROM bgs_goal g ORDER BY startts DESC LIMIT ?");
                ps.setInt(1, Integer.parseInt(recent));
                message = "**" + recent + " Most Recent Goals**\n";
            }
            ResultSet rs = ps.executeQuery();
            int rows = 0;

            while (rs.next()) {
                rows = rows + 1;
                if (rows == 1) {
                    message = "**Active Goals**\n"+
                            "Please ensure you carry out actions for the correct faction. If you are not sure ask on the #bgs_ops channel.\n\n";
                }
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
                message += "**" + ((showUserP) ? "" : "(#" + rs.getString("goalid") + ") ") + rs.getString("s_fullname") + "**\nFrom " + USER_SDF.format(SQL_SDF.parse(rs.getString("startts"))) + " to " + USER_SDF.format(SQL_SDF.parse(rs.getString("endts"))) + " (" + dateDiff(new Date(),SQL_SDF.parse(rs.getString("endts")), " left") + ")";
                if(numrows> 0) {
                    if (showUserP) {
                        message += String.format("```%1$-17s | %2$-15s | %3$s\n","", "Your Goal", "System Goal");
                    } else{
                        message += String.format("```%1$-17s | %2$-15s | %3$s\n","", "CMDR (Num Met)", "System Goal");
                    }
                    List<String> factionNamesList = new ArrayList<>();
                    while (rs1.next()) {
                        double userP = ((double) rs1.getInt("userdone") / rs1.getInt("usergoal")) * 100;
                        double globalP = ((double) rs1.getInt("globaldone") / rs1.getInt("globalgoal")) * 100;
                        factionNamesList.add(rs1.getString("f_shortname") + " = " + rs1.getString("f_fullname"));
                        if (showUserP) {
                            message += String.format("%1$-17s | %2$-15s | %3$s\n", rs1.getString("activity") + " (" + rs1.getString("f_shortname") + ")", int_format_short(Integer.parseInt(rs1.getString("usergoal"))) + " (" + int_format_short(rs1.getInt("userdone")) + "/" + (int) userP + "%)", int_format_short(Integer.parseInt(rs1.getString("globalgoal"))) + " (" + int_format_short(rs1.getInt("globaldone")) + "/" + (int) globalP + "%)");
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
            if(rows == 0) {
                message += "No " + (Integer.parseInt(recent) == 0 ? "Active ":"") + "Goals found\n";
                messages.add(message);
            }
            return messages;

        } catch (SQLException e) {
            //This happens when the system was not found.
            messages.add("**SQL Failed!**");
            return messages;
        } catch (ParseException e) {
            messages.add("**SQL Date Failed!**");
            return messages;
        }
    }

    private String deleteGoal(String goalid) {
        Connection connect = new Connections().getConnection();
        try {
            PreparedStatement ps = connect.prepareStatement("DELETE FROM bgs_goal WHERE goalid = ?");
            ps.setInt(1, Integer.parseInt(goalid));
            if(ps.executeUpdate() == 0) {
                return "**Failed to delete goal!**\nDid you specify a correct goalid?";
            }

        } catch (SQLException e) {
            return "**Failed to delete goal!**";
        }
        return "**Goal and all Items Deleted**";
    }

    private String editGoal(String[] args) {
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

    private String addGoal(String[] args) {
        //bgs goals,add,System,Start Date:Time, Ticks, activity/faction/usergoal/globalgoal,activity/faction/usergoal/globalgoal,activity/faction/usergoal/globalgoal
        if (args.length >= 5) {
            Connection connect = new Connections().getConnection();
            try {
                Date starttime = USER_SDF.parse(args[3]);
                Date endtime = new Date(starttime.getTime() + (Integer.parseInt(args[4])*24*60*60*1000L));
                PreparedStatement ps = connect.prepareStatement("INSERT INTO bgs_goal (systemid, startts, endts, ticks, note) VALUES ("+
                        "(SELECT systemid FROM bgs_system WHERE (s_shortname = ? OR s_fullname = ?) AND s_hidden = '0' LIMIT 1), ?, ?, ?, '')",PreparedStatement.RETURN_GENERATED_KEYS);
                ps.setString(1, args[2]);
                ps.setString(2, args[2]);
                ps.setString(3, SQL_SDF.format(starttime));
                ps.setString(4, SQL_SDF.format(endtime));
                ps.setInt(5,  Integer.parseInt(args[4]));
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                int goalid = 0;
                if (rs.next()) {
                    goalid = rs.getInt(1);
                }

                String message = "**New goal added to BGS logging**\n"+
                        "*Start:* " + starttime + "\n" +
                        "*End:* " + endtime + " (" + args[4] + " ticks)\n";
                if (args.length >= 6) {
                    for (int i = 5; i < args.length; i++) {
                        String[] goalitem = args[i].split("/");
                        message += addGoalItem(goalitem, i-4, Integer.toString(goalid));
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
        } else{
            return BGS_GOAL_ADD_HELP;
        }
    }
    private String addGoalNote(String[] args) {
        //bgs goals, goalid, note
        try{
            int goalid = Integer.parseInt(args[2]);
            String note = String.join(", ", Arrays.copyOfRange(args, 3,args.length)); // join notes back together in case the note string contained a comma

            Connection connect = new Connections().getConnection();

            PreparedStatement ps = connect.prepareStatement("UPDATE bgs_goal SET note = ? WHERE goalid = ?");
            ps.setString(1, note);
            ps.setInt(2, goalid);
            int updated = ps.executeUpdate();
            if (updated==0) {
                return "**Failed to add Note**\nCheck you have specified a valid goalid";
            }
        } catch (SQLException e){
            return "**SQL ERROR NOTE NOT ADDED**\n";
        }
        return "**Note added to goal**";
    }
    private String deleteGoalItem(Activity activity, String goalid) {
        Connection connect = new Connections().getConnection();
        try {
            PreparedStatement ps = connect.prepareStatement("DELETE FROM bgs_goal_item WHERE goalid = (SELECT goalid FROM bgs_goal WHERE goalid = ?) AND activity = ? LIMIT 1");
            ps.setInt(1, Integer.parseInt(goalid));
            ps.setString(2, activity.toString());
            ps.executeUpdate();
        }
        catch(SQLException e) {
            return "**Failed deleting Goal Item**\n" + Listener.prefix + "bgs goal,{deleteitem,delitem},<goalid>";
        }
        return "**Goal Item Deleted**";
    }

    private int getGoalSystemid(int goalid) {
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

    private String addGoalItem(String[] goalitem, Integer i, String goalid) {
        String message;
        if (goalitem.length == 4) {
            Activity activity = Activity.from(goalitem[0]);
            int systemid = getGoalSystemid(Integer.parseInt(goalid));
            int factionid = checkFactionInSystem(goalitem[1],getSystemFullname(systemid));
            if (factionid <= 0) {
                return "Incorrect faction (" + goalitem[1] + ") for goal item #" + Integer.toString(i) + ". Try one of these:\n" + getFactions(true ,systemid);
            } else if (activity == null) {
                String output = "";
                for (Activity act : Activity.values())
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
                    return "Parsing error. Make sure 'UserGoal' and 'GlobalGoal' are whole numbers for goal item #" + Integer.toString(i) + ".\n" + String.join("/",goalitem);
                } catch (SQLException e) {
                    //This only happens when there's a serious issue with mysql or the connection to it
                    return "**Failed adding Goal Item #" + Integer.toString(i) + "**\n"+
                            "Check goalid (" + goalid + ") is valid. Check format of goal item (" + String.join("/",goalitem) + ")\n";
                }
            }

            message = "**Goal Item #" + Integer.toString(i) + " Added/Updated**\n" +
                    "*Activity:* " + activity + "\n" +
                    "*Faction:* " + goalitem[1] + "\n" +
                    "*CMDR Goal:* " + NumberFormat.getInstance(Locale.GERMANY).format(Integer.parseInt(goalitem[2])).replace('.', '\'') + "\n" +
                    "*Global Goal:* " + NumberFormat.getInstance(Locale.GERMANY).format(Integer.parseInt(goalitem[3])).replace('.', '\'') + "\n";
        } else {
            message = "**Failed adding goal item #" + Integer.toString(i) + ":**\n" +
                    "Each goal item requires 4 arguments: <activity>/<faction>/<UserGoal>/<GlobalGoal>.\n" +
                    "You supplied the following: " + String.join("/",goalitem) + "\n\n";
        }
        return message;
    }
    // goal stuff end

    // system stuff start
    private String editSystem(String[] args) {
        //bgs system, edit, <systemid>, <shortname>, <fullname>
        if (args.length == 5) {
            if (systemExists(args[3], args[4], Integer.parseInt(args[2])) == 0 && checkSystemID(Integer.parseInt(args[2])) == Integer.parseInt(args[2])) {
                Connection connect = new Connections().getConnection();
                try {
                    PreparedStatement ps = connect.prepareStatement("UPDATE bgs_system SET s_shortname = ?, s_fullname = ? WHERE systemid = ?");
                    ps.setString(1, args[3]);
                    ps.setString(2, args[4]);
                    ps.setInt(3, Integer.parseInt(args[2]));
                    ps.executeUpdate();
                } catch (SQLException e) {
                    //This happens when the system was not found.
                    return "**WARNING STAR SYSTEM NOT UPDATED**";
                }
                return "Star system updated\n" + getSystems(true);
            } else {
                return "**WARNING STAR SYSTEM NOT UPDATED**\nCheck that a system with ID '" + args[2] + "' exists and that no other system with a shortname/fullname of '" + args[3] + "' or '" + args[4] + "' already exists!\n" + getSystems(true);
            }
        } else {
            return BGS_SYSTEM_EDIT_HELP + "\n" + getSystems(true);
        }
    }

    private String addSystem(String[] args) {
        //bgs system, add, <shortname>, <fullname>
        if (args.length == 4) {
            Connection connect = new Connections().getConnection();
            try {
                if (systemExists(args[2], args[3], 0) == 0) {
                    PreparedStatement ps = connect.prepareStatement("INSERT INTO bgs_system (s_shortname, s_fullname) VALUES (?, ?)");
                    ps.setString(1, args[2]);
                    ps.setString(2, args[3]);
                    ps.executeUpdate();
                } else {
                    return "**WARNING STAR SYSTEM NOT ADDED**\nA system with a shortname/fullname of '" + args[2] + "' or '" + args[3] + "' already exists!\n" + getSystems(true);
                }
            } catch (SQLException e) {
                //This only happens when there's a serious issue with mysql or the connection to it
                return "**WARNING STAR SYSTEM NOT ADDED**";
            }
            return "New star system added to BGS logging.\n" + getSystems(true);
        } else{
            return BGS_SYSTEM_ADD_HELP;
        }
    }

    private int checkSystemID(int systemid) {
        // make sure that system with the specified ID exists
        Connection connect = new Connections().getConnection();
        try {
            PreparedStatement ps;
            ps = connect.prepareStatement("SELECT systemid FROM bgs_system WHERE systemid = ?;");
            ps.setInt(1, systemid);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("systemid");
            }
            else {
                return 0;
            }
        } catch (SQLException e){
            return -1;
        }
    }

    private int systemExists(String shortname, String fullname, int ignore) {
        // make sure that system with same short or fullname does not already exist
        Connection connect = new Connections().getConnection();
        try {
            PreparedStatement ps;
            if(ignore > 0) {
                ps = connect.prepareStatement("SELECT systemid FROM bgs_system WHERE (s_shortname = ? OR s_shortname = ? OR s_fullname = ? OR s_fullname = ?) AND systemid <> ?;");
                ps.setString(1, shortname);
                ps.setString(2, fullname);
                ps.setString(3, shortname);
                ps.setString(4, fullname);
                ps.setInt(5, ignore);
            }
            else {
                ps = connect.prepareStatement("SELECT systemid FROM bgs_system WHERE s_shortname = ? OR s_shortname = ? OR s_fullname = ? OR s_fullname = ?;");
                ps.setString(1, shortname);
                ps.setString(2, fullname);
                ps.setString(3, shortname);
                ps.setString(4, fullname);
            }
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("systemid");
            }
            else {
                return 0;
            }
        } catch (SQLException e){
            return -1;
        }
    }

    private static String getSystems(boolean admin) {
        String message = null;
        try {
            // show hidden systems in italics for admins
            if (admin) {
                message = "```ID   | Short | Full\n";
                PreparedStatement ps = new Connections().getConnection().prepareStatement("SELECT * FROM bgs_system ORDER BY s_fullname ASC");
                ResultSet rs = ps.executeQuery();
                if(!rs.isBeforeFirst()) {
                    message += "No Systems";
                }
                while (rs.next()) {
                    if (rs.getString("s_hidden").equals("1")) {
                        message += String.format("%1$-4s | %2$-5s | %3$s (hidden system)\n", rs.getString("systemid"), rs.getString("s_shortname"), rs.getString("s_fullname"));
                    } else {
                        message += String.format("%1$-4s | %2$-5s | %3$s\n", rs.getString("systemid"), rs.getString("s_shortname"), rs.getString("s_fullname"));
                    }
                }
            } else { // just show live systems
                message = "```Short | Full\n";
                PreparedStatement ps = new Connections().getConnection().prepareStatement("SELECT * FROM bgs_system WHERE s_hidden = 0 ORDER BY s_fullname ASC");
                ResultSet rs = ps.executeQuery();
                if(!rs.isBeforeFirst()) {
                    message += "No Systems";
                }
                while (rs.next()) {
                    message += String.format("%1$-6s| %2$s\n", rs.getString("s_shortname"), rs.getString("s_fullname"));
                }
            }

        }
        catch (SQLException e) {
            LogUtil.logErr(e);
        }
        message +="```\n";
        return message;
    }

    private String setSystemVisibility(boolean show, String system) {
        Connection connect = new Connections().getConnection();
        try {
            PreparedStatement ps = connect.prepareStatement("UPDATE bgs_system SET s_hidden = ? WHERE s_shortname = ? OR s_fullname = ? LIMIT 1");
            ps.setInt(1, show ? 0 : 1);
            ps.setString(2, system);
            ps.setString(3, system);

            // if one row was altered
            if (ps.executeUpdate() == 1) {
                if (show)
                    return "BGS star system '" + system + "' **VISIBLE**. Logging possible for this system.\n"  + getSystems(true);
                else
                    return "BGS star system '" + system + "' **HIDDEN**. Logging no longer possible for this system.\n"  + getSystems(true);
            } else { // if no row was altered the system wasn't found
                return "**WARNING SYSTEM VISIBILITY NOT CHANGED**\nSystem '" + system + "' not found.\n" + getSystems(true);
            }
        } catch (SQLException e) {
            //This only happens when there's a serious issue with mysql or the connection to it
            return "**WARNING SYSTEM VISIBILITY NOT CHANGED**";
        }
    }

    private static String getSystemFullname(int systemid) {
        try {
            PreparedStatement ps = new Connections().getConnection().prepareStatement("SELECT s_fullname FROM bgs_system WHERE systemid = ?");
            ps.setInt(1,systemid);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                return rs.getString("s_fullname");
            } else {
                return "Missing!";
            }
        } catch (SQLException e) {
            return "SQL ERROR!";
        }
    }
    // system stuff ends

    // faction stuff starts
    private int checkFactionID(int factionid) {
        // make sure that system with the specified ID exists
        Connection connect = new Connections().getConnection();
        try {
            PreparedStatement ps;
            ps = connect.prepareStatement("SELECT factionid FROM bgs_faction WHERE factionid = ?;");
            ps.setInt(1, factionid);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("factionid");
            }
            else {
                return 0;
            }
        } catch (SQLException e){
            return -1;
        }
    }

    private int checkFactionInSystem(String faction, String system) {
        // make sure that faction is in a system
        Connection connect = new Connections().getConnection();
        try {
            // get faction id
            int factionid = factionExists(faction,faction,0);
            int systemid = systemExists(system,system,0);

            PreparedStatement ps = connect.prepareStatement("SELECT systemid FROM bgs_system_faction WHERE systemid = ? AND factionid = ? AND f_hidden = 0;");
            ps.setInt(1, systemid);
            ps.setInt(2, factionid);
            ResultSet rs = ps.executeQuery();
            if(!rs.isBeforeFirst()) {
                return 0;
            }
            return factionid;
        } catch (SQLException e){
            return -1;
        }
    }

    private int factionExists(String shortname, String fullname, int ignore) {
        // make sure that faction with same short or fullname does not already exist
        Connection connect = new Connections().getConnection();
        try {
            PreparedStatement ps;
            if(ignore > 0) {
                ps = connect.prepareStatement("SELECT factionid FROM bgs_faction WHERE (f_shortname = ? OR f_shortname = ? OR f_fullname = ? OR f_fullname = ?) AND factionid <> ?;");
                ps.setString(1, shortname);
                ps.setString(2, fullname);
                ps.setString(3, shortname);
                ps.setString(4, fullname);
                ps.setInt(5, ignore);
            }
            else {
                ps = connect.prepareStatement("SELECT factionid FROM bgs_faction WHERE f_shortname = ? OR f_shortname = ? OR f_fullname = ? OR f_fullname = ?");
                ps.setString(1, shortname);
                ps.setString(2, fullname);
                ps.setString(3, shortname);
                ps.setString(4, fullname);
            }
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("factionid");
            }
            else {
                return 0;
            }
        } catch (SQLException e){
            return -1;
        }
    }

    private String editFaction(String[] args) {
        //bgs faction, edit, <factionid>, <shortname>, <fullname>
        if (args.length == 5) {
            if (factionExists(args[3], args[4], Integer.parseInt(args[2])) == 0 && checkFactionID(Integer.parseInt(args[2])) == Integer.parseInt(args[2])) {
                Connection connect = new Connections().getConnection();
                try {
                    PreparedStatement ps = connect.prepareStatement("UPDATE bgs_faction SET f_shortname = ?, f_fullname = ? WHERE factionid = ?");
                    ps.setString(1, args[3]);
                    ps.setString(2, args[4]);
                    ps.setInt(3, Integer.parseInt(args[2]));
                    ps.executeUpdate();
                } catch (SQLException e) {
                    //This happens when the system was not found.
                    return "**WARNING FACTION NOT UPDATED**";
                }
                return "Faction updated\n" + getFactions(true,0);
            } else {
                return "**WARNING FACTION NOT UPDATED**\nCheck that a faction with  ID '" + args[2] + "' exists and that no other faction with a shortname/fullname of '" + args[3] + "' or '" + args[4] + "' already exists!\n" + getFactions(true,0);
            }
        } else {
            return BGS_FACTION_EDIT_HELP + "\n" + getFactions(true,0);
        }
    }

    private String assignFaction(String[] args) {
        //bgs faction, edit, <factionname>, <systemname>
        if (args.length == 4) {
            int factionid = factionExists(args[2], args[2],0);
            int systemid = systemExists(args[3], args[3], 0);
            if (factionid > 0) {
                if (systemid > 0) {
                    Connection connect = new Connections().getConnection();
                    try {
                        PreparedStatement ps = connect.prepareStatement("INSERT INTO bgs_system_faction SET factionid = ?, systemid = ?;");
                        ps.setInt(1, factionid);
                        ps.setInt(2, systemid);
                        ps.executeUpdate();
                    } catch (SQLException e) {
                        //This happens when the system was not found.
                        return "**WARNING FACTION NOT ASSIGNED**\nIs it already assigned to that system?\n" + getFactions(true,systemid);
                    }
                    return "Faction Assigned\n" + getFactions(true,systemid);
                }
                else {
                    return "**WARNING SYSTEM DOES NOT EXIST**\nPlease select from the following\n" + getSystems(true);
                }
            } else {
                return "**WARNING FACTION DOES NOT EXIST**\nPlease select from the following\n" + getFactions(true,0);
            }
        } else {
            return BGS_FACTION_ASSIGN_HELP + "\n" + getFactions(true,0) + getSystems(true);
        }
    }

    private String addFaction(String[] args) {
        //bgs faction, add, <shortname>, <fullname>
        if (args.length == 4) {
            Connection connect = new Connections().getConnection();
            try {
                // make sure that faction with same short or fullname does not already exist
                if (factionExists(args[2], args[3], 0) == 0) {
                    PreparedStatement ps = connect.prepareStatement("INSERT INTO bgs_faction (f_shortname, f_fullname) VALUES (?, ?)");
                    ps.setString(1, args[2]);
                    ps.setString(2, args[3]);
                    ps.executeUpdate();
                }
                else
                    return "**WARNING FACTION NOT ADDED**\nFaction with shortname/fullname set to either: '" + args[2] + "' or '" + args[3] + "' already exists!\n" + getFactions(true,0);
            } catch (SQLException e) {
                //This only happens when there's a serious issue with mysql or the connection to it
                return "**WARNING FACTION NOT ADDED**";
            }
            return "New faction added to BGS logging.\n" + getFactions(true,0);
        } else{
            return BGS_FACTION_ADD_HELP;
        }
    }

    private String setFactionVisibility(boolean show, String faction, String system) {
        //bgs faction, show|hide, faction, system
        int factionid = factionExists(faction, faction,0);
        int systemid = systemExists(system, system, 0);
        if (factionid > 0) {
            if (systemid > 0) {
                if (checkFactionInSystem(faction,system) > 0) {
                    Connection connect = new Connections().getConnection();
                    try {
                        PreparedStatement ps = connect.prepareStatement("UPDATE bgs_system_faction SET f_hidden = ? WHERE factionid = ? AND systemid = ?");
                        ps.setInt(1, show ? 0 : 1);
                        ps.setInt(2, factionid);
                        ps.setInt(3, systemid);

                        // if one row was altered
                        if (ps.executeUpdate() == 1) {
                            if (show)
                                return "BGS faction **VISIBLE** in " + system + ". Logging possible for this '" + faction + "' in '" + system + "'.\n" + getFactions(true, systemid);
                            else
                                return "BGS faction **HIDDEN** in " + system + ". Logging no longer possible for '" + faction + "' in '" + system + "'.\n" + getFactions(true, systemid);
                        } else { // if no row was altered the system wasn't found
                            return "**WARNING FACTION VISIBILITY NOT CHANGED**\nSystem '" + system + "' not found.\n" + getSystems(true);
                        }
                    } catch (SQLException e) {
                        //This only happens when there's a serious issue with mysql or the connection to it
                        return "**WARNING SYSTEM VISIBILITY NOT CHANGED**";
                    }
                } else{
                    return "**WARNING FACTION '" + faction + "' IS NOT ASSIGNED TO '" + system + "'**\nPlease select from the following\n" + getFactions(true,systemid);
                }

            } else {
                return "**WARNING SYSTEM '" + system + "' DOES NOT EXIST**\nPlease select from the following\n" + getSystems(true);
            }
        } else {
            return "**WARNING FACTION '" + faction + "' DOES NOT EXIST**\nPlease select from the following\n" + getFactions(true,0);
        }
    }

    private static String getFactions(boolean admin, int systemid) {
        String message = "";
        try {
            // show hidden factions in italics for admins
            Connection connect = new Connections().getConnection();
            if (admin) {
                PreparedStatement ps;
                if (systemid == -1) { // list all factions in each system
                    ps = connect.prepareStatement("SELECT systemid FROM bgs_system ORDER BY s_fullname ASC");
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        message += "**Factions assigned to " + getSystemFullname(rs.getInt("systemid")) + "**\n";
                        ps = connect.prepareStatement("SELECT * FROM bgs_faction f LEFT JOIN bgs_system_faction sf ON f.factionid = sf.factionid WHERE sf.systemid = ? ORDER BY f_fullname ASC");
                        ps.setInt(1,rs.getInt("systemid"));
                        ResultSet rs1 = ps.executeQuery();
                        message += "```ID   | Short | Full\n";
                        if(!rs1.isBeforeFirst()) {
                            message += "No Factions";
                        }
                        while (rs1.next()) {
                            if (rs1.getString("f_hidden").equals("1")) {
                                message += String.format("%1$-4s | %2$-5s | %3$s (hidden faction)\n", rs1.getString("factionid"), rs1.getString("f_shortname"), rs1.getString("f_fullname"));
                            } else {
                                message += String.format("%1$-4s | %2$-5s | %3$s\n", rs1.getString("factionid"), rs1.getString("f_shortname"), rs1.getString("f_fullname"));
                            }
                        }
                        message +="```\n";
                    }
                } else {
                    if (systemid > 0) { // list factions in one system
                        message += "**Factions assigned to " + getSystemFullname(systemid) + "**\n";
                        ps = connect.prepareStatement("SELECT * FROM bgs_faction f LEFT JOIN bgs_system_faction sf ON f.factionid = sf.factionid WHERE sf.systemid = ? ORDER BY f_fullname ASC");
                        ps.setInt(1, systemid);
                    } else { // list factions
                        ps = connect.prepareStatement("SELECT *, 0 AS f_hidden FROM bgs_faction ORDER BY f_fullname ASC");
                    }
                    message += "```ID   | Short | Full\n";
                    ResultSet rs = ps.executeQuery();
                    if(!rs.isBeforeFirst()) {
                        message += "No Factions";
                    }
                    while (rs.next()) {
                        if (rs.getString("f_hidden").equals("1")) {
                            message += String.format("%1$-4s | %2$-5s | %3$s (hidden faction)\n", rs.getString("factionid"), rs.getString("f_shortname"), rs.getString("f_fullname"));
                        } else {
                            message += String.format("%1$-4s | %2$-5s | %3$s\n", rs.getString("factionid"), rs.getString("f_shortname"), rs.getString("f_fullname"));
                        }
                    }
                    message +="```\n";
                }

            } else { // just show live systems
                message = "```Short | Full\n";
                PreparedStatement ps = connect.prepareStatement("SELECT * FROM bgs_faction WHERE f_hidden = 0 ORDER BY f_fullname ASC");
                ResultSet rs = ps.executeQuery();
                if(!rs.isBeforeFirst()) {
                    message += "No Factions";
                }
                while (rs.next()) {
                    message += String.format("%1$-6s| %2$s\n", rs.getString("f_shortname"), rs.getString("f_fullname"));
                }
                message +="```\n";
            }
        }
        catch (SQLException e) {
            LogUtil.logErr(e);
        }
        return message;
    }
    // faction stuff ends

    // stats stuff start
    private static List<String> getFullTick(String[] args) {
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

    private static String getTick(String[] args) {
        String system = "all";

        if (args.length == 5){
            system = args[4];
        }
        try {
            Date time = USER_SDF.parse(args[3]);
            String output = "Data for " + args[2] + " ticks after " + args[3] + " UTC System Filter: " + system + "\n```";
            Map<String, String> entries = getTotalAmount(time, Integer.parseInt(args[2]), system);
            for (Entry<String, String> entry : entries.entrySet()) {
                output += entry.getKey() + ": " + entry.getValue() + "\n";
            }
            output += "```";
            if (entries.isEmpty())
                return "No records for the specified period";
            else {
                return output;
            }
        } catch (ParseException e) {
            return "Parsing error. Make sure the date follows the pattern 'dd/MM/yy HH:mm'";
        }
    }

    private static String getUserStats(String userID) {
        String output = "**All Time Totals**\n```";
        for (Entry<Activity, Double> entry : getTotalAmount(userID).entrySet()) {
            output += entry.getKey().toString() + ": " + NumberFormat.getInstance(Locale.GERMANY).format(entry.getValue().intValue()).replace('.', '\'') + "\n";
        }
        output += "```\n";
        return output;
    }

    private static Map<Activity, Double> getTotalAmount(String userid) {
        Map<Activity, Double> totals = new LinkedHashMap<>();

        Connection connect = new Connections().getConnection();
        try {
            PreparedStatement ps = connect.prepareStatement("SELECT activity, SUM(amount) AS total FROM bgs_activity WHERE userid = ? GROUP BY activity ORDER BY activity ASC");
            ps.setString(1, userid);
            ResultSet rs = ps.executeQuery();

            while (rs.next())
                totals.put(Activity.from(rs.getString("activity")), rs.getDouble("total"));
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

    private static Map<String, String> getTotalAmount(Date start, int ticks, String system) {
        Map<String, String> totals = new LinkedHashMap<>();
        Date end = ticks == 0 ? new Date() : new Date(start.getTime() + (ticks*24*60*60*1000L));

        Connection connect = new Connections().getConnection();
        try {
            if (system.equals("all")) {
                PreparedStatement ps = connect.prepareStatement("SELECT (SELECT bgs_system.s_fullname FROM bgs_system WHERE bgs_system.systemid = b.systemid) AS s_fullname, " +
                        "b.activity, " +
                        "SUM(b.amount) AS total," +
                        "COUNT(DISTINCT(b.userid)) AS numcmdrs " +
                        "FROM bgs_activity b " +
                        "WHERE timestamp > ? AND timestamp < ? " +
                        "GROUP BY s_fullname, activity " +
                        "ORDER BY s_fullname ASC, activity ASC");
                ps.setString(1, SQL_SDF.format(start));
                ps.setString(2, SQL_SDF.format(end));
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    double cmdravg = (double) rs.getInt("total") / rs.getInt("numcmdrs");
                    totals.put((Activity.valueOf(rs.getString("activity").toUpperCase()).toString()) + " (" + rs.getString("s_fullname") + ")", NumberFormat.getInstance(Locale.GERMANY).format(rs.getInt("total")).replace('.', '\'') + " from " + rs.getInt("numcmdrs") + " CMDRs (" + int_format_short((int) cmdravg) + " avg.)");
                }
            } else {
                PreparedStatement ps = connect.prepareStatement("SELECT (SELECT bgs_system.s_fullname FROM bgs_system WHERE bgs_system.systemid = b.systemid) AS s_fullname," +
                        "b.activity, " +
                        "SUM(b.amount) AS total, " +
                        "COUNT(DISTINCT(b.userid)) AS numcmdrs " +
                        "FROM bgs_activity b " +
                        "WHERE b.timestamp > ? AND b.timestamp < ? AND b.systemid = (SELECT systemid FROM bgs_system WHERE (s_shortname = ? OR s_fullname = ?) AND s_hidden = '0' LIMIT 1) " +
                        "GROUP BY s_fullname, activity ORDER BY s_fullname ASC, activity ASC");
                ps.setString(1, SQL_SDF.format(start));
                ps.setString(2, SQL_SDF.format(end));
                ps.setString(3, system);
                ps.setString(4, system);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    double cmdravg = (double) rs.getInt("total") / rs.getInt("numcmdrs");
                    totals.put((Activity.valueOf(rs.getString("activity").toUpperCase()).toString()) + " (" + rs.getString("s_fullname") + ")", NumberFormat.getInstance(Locale.GERMANY).format(rs.getInt("total")).replace('.', '\'') + " from " + rs.getInt("numcmdrs") + " CMDRs (" + int_format_short((int)cmdravg) + " avg.)");
                }
            }
        } catch (SQLException e) {
            LogUtil.logErr(e);
        }
        return totals;
    }

    private static List<String> getCSVData(Date start, int ticks) {
        List<String> lines = new ArrayList<>();
        Date end = (ticks == 0) ? new Date() : new Date(start.getTime() + (ticks*24*60*60*1000L));

        int tickHour = Integer.parseInt(new SimpleDateFormat("HH").format(start));
        int tickMinute = Integer.parseInt(new SimpleDateFormat("mm").format(start));

        Connection connect = new Connections().getConnection();
        try {
            PreparedStatement ps = connect.prepareStatement("SELECT " +
                    "b.userid, "+
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
                if(!cmdrNamesList.contains("@" + Listener.jda.getUserById(rs.getString("userid")).getName())) {
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
                } catch(StringIndexOutOfBoundsException ignored){}


                lines.add("");
                lines.add(cmdrNames);
            }

        } catch (SQLException e) {
            LogUtil.logErr(e);
        }
        return lines;
    }
    // stats stuff ends

    // logging stuff starts
    private static String logActivity(boolean admin, String sActivity, String userid, String username, String sAmount, String system) {

        //ToDo Logging: Confirmation message that does NOT tag them but is customised per the action logged
        //ToDo Logging: If goal is already met direct message once per activity with details on what still needs work.

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
                    "(SELECT systemid FROM bgs_system WHERE (s_shortname = ? OR s_fullname = ?) AND s_hidden = '0' LIMIT 1))");
            ps.setString(1, username);
            ps.setString(2, userid);
            ps.setInt(3, amount);
            ps.setString(4, activity.toString());
            ps.setString(5, system);
            ps.setString(6, system);
            ps.executeUpdate();

        } catch (NumberFormatException e) {

            return "[Error] " + sAmount + " is not a valid number to log. Make sure you have the format '/bgs *activity*, *number*, *system*";
        } catch (MySQLIntegrityConstraintViolationException e) {
            //This happens when the system was not found.

            String message = "**WARNING ACTION NOT LOGGED**\nInvalid system entered. You can use either the shortname or the fullname. Please select from:\n";
            return message + getSystems(admin);
        } catch (SQLException e) {
            LogUtil.logErr(e);
            return "**WARNING ACTION NOT LOGGED**\nSomething went wrong saving your contribution. Please retry later";
        }

        if (!DataProvider.isDev())
            Statistics.getInstance().logBGSActivity(System.currentTimeMillis(), userid, username, activity.toString(), amount, system.toUpperCase());

        return "Your engagement has been noticed. Thanks for your service o7";
    }
    // logging stuff ends

    // other stuff starts
    private static void toggleBgsRole(GuildMessageReceivedEvent event) {
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

    private static final NavigableMap<Long, String> suffixes = new TreeMap<> ();
    static {
        suffixes.put(1_000L, "k");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "G");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "P");
        suffixes.put(1_000_000_000_000_000_000L, "E");
    }

    private static String int_format_short(long value) {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return int_format_short(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + int_format_short(-value);
        if (value < 1000) return Long.toString(value); //deal with easy case

        Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }

    private static String dateDiff(Date date1,Date date2, String append) {
        //in milliseconds
        long diff = date2.getTime() - date1.getTime();
        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000) % 24;
        long diffDays = diff / (24 * 60 * 60 * 1000);
        List<String> output = new ArrayList<>();
        if(diffDays>0)
            output.add(diffDays + " day" + ((diffDays > 1) ? "s":""));
        if(diffHours>0)
            output.add(diffHours + " hr" + ((diffHours > 1) ? "s":""));
        if(diffMinutes>0)
            output.add(diffMinutes + " min" + ((diffMinutes > 1) ? "s":""));
        if(diffSeconds>0 && diffHours==0)
            output.add(diffSeconds + " sec" + ((diffSeconds > 1) ? "s":""));
        if(diff < 0)
            return "Finished";
        else
            return String.join(", ", output) + append;
    }
    // other stuff ends
}
