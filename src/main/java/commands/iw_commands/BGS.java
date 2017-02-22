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
                case "mur"	     :
                case "murder"	 : output = Activity.MURDER;	break;
                default          : output = null;               break;
            }

            return output;
        }
    }

    @Override
    public void runCommand(PrivateMessageReceivedEvent event, String[] args) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("mystats")) {
                String output = getUserStats(event.getAuthor().getId());
                event.getChannel().sendMessage(output).queue();
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
            if (args.length == 1) {
                event.getChannel().sendMessage(BGS_STATS_HELP).queue();

            } else if (args[1].equalsIgnoreCase("summary") || args[1].equalsIgnoreCase("sum")){
                // no system filter
                if(args.length == 4 || args.length == 5 ) {
                    event.getChannel().sendMessage(getTick(args)).queue();

                } else {
                    event.getChannel().sendMessage(BGS_STATS_HELP).queue();

                }
            }
            // CSV output
            else if (args[1].equalsIgnoreCase("csv") && args.length >= 4){
                JDAUtil.sendMultipleMessages(event.getChannel(), getFullTick(args));

            } else {
                event.getChannel().sendMessage(BGS_STATS_HELP).queue();

            }
        }
        // admin functions for systems
        else if ((args[0].equalsIgnoreCase("system") || args[0].equalsIgnoreCase("systems") || args[0].equalsIgnoreCase("sys")) && DataProvider.isAdmin(event)){
            if (args.length == 1) {
                event.getChannel().sendMessage(BGS_SYSTEM_HELP).queue();

            } else if (args[1].equalsIgnoreCase("list")) {
                event.getChannel().sendMessage(getSystems(DataProvider.isAdmin(event))).queue();

            } else if (args[1].equalsIgnoreCase("hide") || args[1].equalsIgnoreCase("show")) {
                if (args.length == 3) {
                    event.getChannel().sendMessage(setSystemVisibility(args[1].equalsIgnoreCase("show"), args[2])).queue();

                } else {
                    event.getChannel().sendMessage("Help: " + Listener.prefix + "bgs system, hide|show, <systemid>\n" + getSystems(DataProvider.isAdmin(event))).queue();
                }

            } else if (args[1].equalsIgnoreCase("add")) {
                event.getChannel().sendMessage(addSystem(args)).queue();

            } else if (args[1].equalsIgnoreCase("edit")) {
                event.getChannel().sendMessage(editSystem(args)).queue();

            } else{
                event.getChannel().sendMessage(BGS_SYSTEM_HELP).queue();
            }
        }
        // admin functions for goals
        else if ((args[0].equalsIgnoreCase("goal") || args[0].equalsIgnoreCase("goals")) && DataProvider.isAdmin(event)) {
            if (args.length == 1) {
                event.getChannel().sendMessage(BGS_GOAL_HELP).queue();

            } else if (args[1].equalsIgnoreCase("add")) {
                //ToDo Add a goal
                //bgs goals,add,system,Start Date:Time, Ticks, Activity/usergoal/globalgoal,Activity/usergoal/globalgoal,Activity/usergoal/globalgoal
                if (args.length >= 5) {
                    event.getChannel().sendMessage(addGoal(args)).queue();
                } else{
                    event.getChannel().sendMessage(BGS_GOAL_ADD_HELP).queue();
                }
            }
            else if (args[1].equalsIgnoreCase("delete") || args[1].equalsIgnoreCase("del")) {
                //ToDo Delete a goal
                //bgs goals,<del>,goalid
                if (args.length == 3) {

                } else {
                    event.getChannel().sendMessage(BGS_GOAL_DEL_HELP).queue();
                }
            }
            else if (args[1].equalsIgnoreCase("deleteact") || args[1].equalsIgnoreCase("delact")) {
                //ToDo Delete activities of a goal
                //bgs goals,<delact>,goalid,Activity
                if (args.length == 4) {

                } else {
                    event.getChannel().sendMessage(BGS_GOAL_DELACT_HELP).queue();
                }
            }
            else if (args[1].equalsIgnoreCase("edit")) {
                //ToDo edit a goal
                //bgs goals,edit,goalid,Start Date:Time, Ticks
                if (args.length == 5) {

                } else {
                    event.getChannel().sendMessage(BGS_GOAL_EDIT_HELP).queue();
                }
            }
            else if (args[1].equalsIgnoreCase("editactivity") || args[1].equalsIgnoreCase("addactivity") || args[1].equalsIgnoreCase("editact") || args[1].equalsIgnoreCase("addact")) {
                //ToDo Add/edit activities of a goal
                //bgs goals,<addact,editact>,goalid,Activity/usergoal/globalgoal,Activity/usergoal/globalgoal,Activity/usergoal/globalgoal
                if (args.length >= 4) {

                } else {
                    event.getChannel().sendMessage(BGS_GOAL_EDITACT_HELP).queue();
                }
            }

            else if (args[1].equalsIgnoreCase("list")) {
                //ToDo get X most recent goals grouped by startts so that goals with multiple activity targets are grouped together
                //bgs goals, list,#
                //Get # most recent goals
                //If # is not specified then get all active goals
                String recent = "0";
                if(args.length == 3) {
                    recent = args[2];
                }
                event.getChannel().sendMessage(listGoal(recent,event.getAuthor().getId(), false)).queue();
                /*Output
                Goals for the last # time periods
                **ID | Start Date:Time | End Date:Time | Currently Active**
                * Activity, User Goal (% complete), Global Goal (% complete)
                * Activity, User Goal (% complete), Global Goal (% complete)
                 */
            }
            else{
                event.getChannel().sendMessage(BGS_GOAL_HELP).queue();
            }
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("mystats")) {
                JDAUtil.getPrivateChannel(event.getAuthor()).sendMessage(getUserStats(event.getAuthor().getId())).queue();

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

    private static String listGoal(String recent, String userid, boolean showUserP) {
        Connection connect = new Connections().getConnection();

        try {
            PreparedStatement ps;
            PreparedStatement ps1;
            String message;
            if(Integer.parseInt(recent) == 0) { // get active goals
                ps = connect.prepareStatement("SELECT goalid, (SELECT bgs_systems.fullname FROM bgs_systems WHERE bgs_systems.systemid = g.systemid) AS fullname, "+
                        "startts, endts, ticks "+
                        "FROM bgs_goal g "+
                        "WHERE startts <= CURRENT_TIMESTAMP AND endts >= CURRENT_TIMESTAMP ORDER BY startts");
                message = "**Active Goals**\n";
            } else{
                ps = connect.prepareStatement("SELECT *, (SELECT bgs_systems.fullname FROM bgs_systems WHERE bgs_systems.systemid = g.systemid) AS fullname "+
                                "FROM bgs_goal g ORDER BY startts DESC LIMIT ?");
                ps.setInt(1, Integer.parseInt(recent));
                message = "**" + recent + " Most Recent Goals**\n";
            }
            ResultSet rs = ps.executeQuery();
            int rows = 0;

            while (rs.next()) {
                rows = rows + 1;
                message += "**" + rs.getString("fullname") + "** from " + USER_SDF.format(SQL_SDF.parse(rs.getString("startts"))) + " to " + USER_SDF.format(SQL_SDF.parse(rs.getString("endts"))) + " (" + rs.getString("ticks") + " ticks)\n";

                ps1 = connect.prepareStatement("SELECT i.activity, i.usergoal, i.globalgoal, " +
                        "(SELECT SUM(a.amount) FROM bgs_activity a WHERE a.activity = i.activity AND a.timestamp >= g.startts AND a.timestamp <= g.endts AND a.systemid = g.systemid) AS globaldone, " +
                        "(SELECT SUM(a.amount) FROM bgs_activity a WHERE a.activity = i.activity AND a.timestamp >= g.startts AND a.timestamp <= g.endts AND a.systemid = g.systemid AND a.userid = ?) AS userdone " +
                        "FROM bgs_goal_item i " +
                        "LEFT JOIN bgs_goal g ON i.goalid = g.goalid WHERE i.goalid = ?");
                ps1.setString(1, userid);
                ps1.setInt(2, rs.getInt("goalid"));
                ResultSet rs1 = ps1.executeQuery();

                message += String.format("```%1$-9s | %2$-17s | %3$s\n", "Activity",  (showUserP) ? "Your Goal" : "Per CMDR Goal", "Iridium Wing Goal");
                while (rs1.next()) {
                    double userP = ((double) rs1.getInt("userdone") / rs1.getInt("usergoal")) * 100;
                    double globalP = ((double) rs1.getInt("globaldone") / rs1.getInt("globalgoal")) * 100;
                    if (showUserP) {
                        message += String.format("%1$-9s | %2$-17s | %3$s\n", rs1.getString("activity"), NumberFormat.getInstance(Locale.GERMANY).format(Integer.parseInt(rs1.getString("usergoal"))).replace('.', '\'') + " (" + (int) userP + "%)", NumberFormat.getInstance(Locale.GERMANY).format(Integer.parseInt(rs1.getString("globalgoal"))).replace('.', '\'') + " (" + (int) globalP + "%)");
                    }else{
                        message += String.format("%1$-9s | %2$-17s | %3$s\n", rs1.getString("activity"), NumberFormat.getInstance(Locale.GERMANY).format(Integer.parseInt(rs1.getString("usergoal"))).replace('.', '\''), NumberFormat.getInstance(Locale.GERMANY).format(Integer.parseInt(rs1.getString("globalgoal"))).replace('.', '\'') + " (" + (int) globalP + "%)");
                    }
                }
                message += "```\n";

            }
            if(rows == 0) {
                message += "No goals found\n";
            }
            return message;

        } catch (SQLException e) {
            //This happens when the system was not found.
            return "**SQL Failed!**";
        } catch (ParseException e) {
            return "**SQL Date Failed!**";
        }
    }

    private String addGoal(String[] args) {
        //bgs goals,add,System,Start Date:Time, Ticks, Activity/usergoal/globalgoal,Activity/usergoal/globalgoal,Activity/usergoal/globalgoal
        if (args.length >= 5) {
            Connection connect = new Connections().getConnection();
            try {
                Date starttime = USER_SDF.parse(args[3]);
                Date endtime = new Date(starttime.getTime() + (Integer.parseInt(args[4])*24*60*60*1000L));
                PreparedStatement ps = connect.prepareStatement("INSERT INTO bgs_goal (systemid, startts, endts, ticks) VALUES ("+
                        "(SELECT systemid FROM bgs_systems WHERE (shortname = ? OR fullname = ?) AND hidden = '0' LIMIT 1), ?, ?, ?)",PreparedStatement.RETURN_GENERATED_KEYS);
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
                        "*System:* " + getSystemFullname(args[2]) + "\n"+
                        "*Start:* " + starttime + "\n" +
                        "*End:* " + endtime + " (" + args[4] + " ticks)\n";
                if (args.length >= 6) {
                    for (int i = 5; i < args.length; i++) {
                        String[] goalitem = args[i].split("/");
                        message += addGoalItem(goalitem, i-4, goalid);
                    }
                    return message;
                }
            } catch (SQLException e) {
                //This only happens when there's a serious issue with mysql or the connection to it
                return "**WARNING ACTION NOT LOGGED**\nDid you specify a correct system? Try using one of the star system names below:\n" + getSystems(true);
            } catch (ParseException e) {
                return "Parsing error. Make sure the start date follows the pattern 'dd/MM/yy HH:mm'";
            } catch (NumberFormatException e) {
                return "Parsing error. Make sure the ticks, and any UserGoals or GlobalGoals are whole numbers'";
            }

            return "New goal added to BGS logging.";
        } else{
            return BGS_GOAL_ADD_HELP;
        }
    }
    private String addGoalItem(String[] goalitem, Integer i, Integer goalid) {
        String message;
        if (goalitem.length == 3) {
            Activity activity = Activity.from(goalitem[0]);

            if (activity == null) {
                String output = "";
                for (Activity act : Activity.values())
                    output += act.toString() + "\n";
                return "Incorrect activity (" + goalitem[0] + ") for goal item #" + Integer.toString(i) + ". Try one of these:\n" + output;
            } else {
                Connection connect = new Connections().getConnection();
                try {
                    PreparedStatement ps = connect.prepareStatement("INSERT INTO bgs_goal_item (goalid, activity, usergoal, globalgoal) VALUES (?, ?, ?, ?);");
                    ps.setInt(1, goalid);
                    ps.setString(2, activity.toString());
                    ps.setInt(3, Integer.parseInt(goalitem[1]));
                    ps.setInt(4, Integer.parseInt(goalitem[2]));
                    ps.executeUpdate();
                } catch (NumberFormatException e) {
                    return "Parsing error. Make sure 'UserGoal' and 'GlobalGoal' are whole numbers for goal item #" + Integer.toString(i) + ".\n" + String.join("/",goalitem);
                } catch (SQLException e) {
                    //This only happens when there's a serious issue with mysql or the connection to it
                    return "Incorrect format for goal item #" + Integer.toString(i) + ":\n" + String.join("/",goalitem);
                }
            }

            message = "**Goal Item #" + Integer.toString(i) + " Added**\n" +
                    "*Activity:* " + activity + "\n" +
                    "*CMDR Goal:* " + NumberFormat.getInstance(Locale.GERMANY).format(Integer.parseInt(goalitem[1])).replace('.', '\'') + "\n" +
                    "*Global Goal:* " + NumberFormat.getInstance(Locale.GERMANY).format(Integer.parseInt(goalitem[2])).replace('.', '\'') + "\n";
        } else {
            message = "**Failed adding goal item #" + Integer.toString(i) + ":**\n" +
                    "Each goal item requires 3 arguments: <activity>/<UserGoal>/<GlobalGoal>.\n" +
                    "You supplied the following: " + String.join("/",goalitem) + "\n\n";
        }
        return message;
    }

    private String editSystem(String[] args) {
        if (args.length == 5) {

            Connection connect = new Connections().getConnection();
            try {
                PreparedStatement ps = connect.prepareStatement("UPDATE bgs_systems SET shortname = ?, fullname = ? WHERE systemid = ?");
                ps.setString(1, args[3]);
                ps.setString(2, args[4]);
                ps.setInt   (3, Integer.parseInt(args[2]));
                ps.executeUpdate();
            } catch (SQLException e) {
                //This happens when the system was not found.
                return "**WARNING STAR SYSTEM NOT UPDATED**";
            }
            return "Star system updated\n" + getSystems(true);
        } else {
            return "Help: " + Listener.prefix + "bgs system,edit,<systemid>, <shortname>, <fullname>\n" + getSystems(true);
        }
    }

    private String getSystemFullname(String system) {
        String fullname = null;
        try {
            PreparedStatement ps = new Connections().getConnection().prepareStatement("SELECT fullname FROM bgs_systems WHERE shortname = ? OR fullname = ? LIMIT 1");
            ps.setString(1, system);
            ps.setString(2, system);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                fullname = rs.getString("fullname");
            return fullname;
        } catch (SQLException e) {
            //This happens when the system was not found.
            return "**WARNING STAR SYSTEM DOES NOT EXIST**" + system;
        }
    }

    private String addSystem(String[] args) {
        if (args.length == 4) {
            Connection connect = new Connections().getConnection();
            try {
                PreparedStatement ps = connect.prepareStatement("INSERT INTO bgs_systems (shortname, fullname) VALUES (?, ?)");
                ps.setString(1, args[1]);
                ps.setString(2, args[2]);
                ps.executeUpdate();
            } catch (SQLException e) {
                //This only happens when there's a serious issue with mysql or the connection to it
                return "**WARNING STAR SYSTEM NOT ADDED**";
            }
            return "New star system added to BGS logging.\n" + getSystems(true);
        } else{
            return "Help: " + Listener.prefix + "bgs system, add, <shortname>, <fullname>";
        }
    }

    private String setSystemVisibility(boolean show, String system) {
        int systemid = Integer.parseInt(system);
        Connection connect = new Connections().getConnection();
        try {
            PreparedStatement ps = connect.prepareStatement("UPDATE bgs_systems SET hidden = ? WHERE systemid = ?");
            ps.setInt(1, show ? 0 : 1);
            ps.setInt(2, systemid);

            // if one row was altered
            if (ps.executeUpdate() == 1) {
                if (show)
                    return "BGS star system **VISIBLE**. Logging possible for this system.\n"  + getSystems(true);
                else
                    return "BGS star system **HIDDEN**. Logging no longer possible for this system.\n"  + getSystems(true);
            } else { // if no row was altered the system wasn't found
                return "**WARNING SYSTEM VISIBILITY NOT CHANGED**\nSystem not found.\n" + getSystems(true);
            }
        } catch (SQLException e) {
            //This only happens when there's a serious issue with mysql or the connection to it
            return "**WARNING SYSTEM VISIBILITY NOT CHANGED**";
        }
    }

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
            Map<String, Double> entries = getTotalAmount(time, Integer.parseInt(args[2]), system);
            for (Entry<String, Double> entry : entries.entrySet()) {
                output += entry.getKey() + ": " + NumberFormat.getInstance(Locale.GERMANY).format(entry.getValue().intValue()).replace('.', '\'') + "\n";
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
        String output = listGoal("0", userID, true);
        output += "**All Time Totals**\n```";
        for (Entry<Activity, Double> entry : getTotalAmount(userID).entrySet()) {
            output += entry.getKey().toString() + ": " + NumberFormat.getInstance(Locale.GERMANY).format(entry.getValue().intValue()).replace('.', '\'') + "\n";
        }
        output += "```\n";
        return output;
    }

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

    private static String getSystems(boolean admin) {
        String message = null;
        try {
            // show hidden systems in italics for admins
            if (admin) {
                message = "```ID   | Short | Full\n";
                PreparedStatement ps = new Connections().getConnection().prepareStatement("SELECT * FROM bgs_systems ORDER BY fullname ASC");
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    if (rs.getString("hidden").equals("1")) {
                        message += String.format("%1$-4s | %2$-5s | %3$s (hidden system)\n", rs.getString("systemid"), rs.getString("shortname"), rs.getString("fullname"));
                    } else {
                        message += String.format("%1$-4s | %2$-5s | %3$s\n", rs.getString("systemid"), rs.getString("shortname"), rs.getString("fullname"));
                    }

                }
            } else { // just show live systems
                message = "```Short | Full\n";
                PreparedStatement ps = new Connections().getConnection().prepareStatement("SELECT * FROM bgs_systems WHERE hidden = 0 ORDER BY fullname ASC");
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    message += String.format("%1$-6s| %2$s\n", rs.getString("shortname"), rs.getString("fullname"));
                }
            }
        }
        catch (SQLException e) {
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

    private static Map<String, Double> getTotalAmount(Date start, int ticks, String system) {
        Map<String, Double> totals = new LinkedHashMap<>();
        Date end = ticks == 0 ? new Date() : new Date(start.getTime() + (ticks*24*60*60*1000L));

        Connection connect = new Connections().getConnection();
        try {
            if (system.equals("all")) {
                PreparedStatement ps = connect.prepareStatement("SELECT (SELECT bgs_systems.fullname FROM bgs_systems WHERE bgs_systems.systemid = b.systemid) AS fullname, " +
                        "b.activity, " +
                        "SUM(b.amount) AS total " +
                        "FROM bgs_activity b " +
                        "WHERE timestamp > ? AND timestamp < ? " +
                        "GROUP BY fullname, activity " +
                        "ORDER BY fullname ASC, activity ASC");
                ps.setString(1, SQL_SDF.format(start));
                ps.setString(2, SQL_SDF.format(end));
                ResultSet rs = ps.executeQuery();
                while (rs.next())
                    totals.put((Activity.valueOf(rs.getString("activity" ).toUpperCase()).toString())+ " (" + rs.getString("fullname" ) + ")", rs.getDouble("total"));
            } else {
                PreparedStatement ps = connect.prepareStatement("SELECT (SELECT bgs_systems.fullname FROM bgs_systems WHERE bgs_systems.systemid = b.systemid) AS fullname," +
                        "b.activity, " +
                        "SUM(b.amount) AS total " +
                        "FROM bgs_activity b " +
                        "WHERE b.timestamp > ? AND b.timestamp < ? AND b.systemid = (SELECT systemid FROM bgs_systems WHERE (shortname = ? OR fullname = ?) AND hidden = '0' LIMIT 1) " +
                        "GROUP BY fullname, activity ORDER BY fullname ASC, activity ASC");
                ps.setString(1, SQL_SDF.format(start));
                ps.setString(2, SQL_SDF.format(end));
                ps.setString(3, system);
                ps.setString(4, system);
                ResultSet rs = ps.executeQuery();
                while (rs.next())
                    totals.put((Activity.valueOf(rs.getString("activity" ).toUpperCase()).toString())+ " (" + rs.getString("fullname" ) + ")", rs.getDouble("total"));
            }
        } catch (SQLException e) {
            LogUtil.logErr(e);
        }
        return totals;
    }

    private static String logActivity(boolean admin, String sActivity, String userid, String username, String sAmount, String system) {
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
                    "(SELECT systemid FROM bgs_systems WHERE (shortname = ? OR fullname = ?) AND hidden = '0' LIMIT 1))");
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
            return message + getSystems(admin);
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
        Date end = (ticks == 0) ? new Date() : new Date(start.getTime() + (ticks*24*60*60*1000L));

        int tickHour = Integer.parseInt(new SimpleDateFormat("HH").format(start));
        int tickMinute = Integer.parseInt(new SimpleDateFormat("mm").format(start));

        Connection connect = new Connections().getConnection();
        try {
            PreparedStatement ps = connect.prepareStatement("SELECT " +
                "(SELECT user.username FROM user WHERE user.iduser = b.userid) AS CMDR, " +
                "from_unixtime(floor((unix_timestamp(timestamp) - ((?*60*60) + (?*60)))/(24*60*60)) * (24*60*60) + ((?*60*60) + (?*60) + (24*60*60)), '%D %b %Y') AS Tick, " +
                "(SELECT bgs_systems.fullname FROM bgs_systems WHERE bgs_systems.systemid = b.systemid) AS System, " +
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
            for (int i = 4; i <= columnCount; i++) {
                columnNames = String.join(", ", columnNames, rs.getMetaData().getColumnName(i));
            }
            columnNames += ", System";
            lines.add(columnNames);

            while (rs.next()) {
                String rowValues = rs.getString("CMDR") + ",";
                rowValues += rs.getString("Tick") + ",";
                for (int i = 4; i <= columnCount; i++) {
                    rowValues += (rs.getString(i).equals("0") ? "" : rs.getString(i)) + ",";
                }
                rowValues += rs.getString("System");
                lines.add(rowValues);
            }
        } catch (SQLException e) {
            LogUtil.logErr(e);
        }
        return lines;
    }
}
