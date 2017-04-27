package commands.iw_commands;

import commands.GuildCommand;
import commands.PMCommand;
import iw_bot.JDAUtil;
import iw_bot.Listener;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import provider.DataProvider;

import java.sql.ResultSet;
import java.sql.SQLException;

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
                case "bon":
                case "bond":
                case "bonds":
                    output = Activity.BOND;
                    break;
                case "bou":
                case "bounty":
                case "bounties":
                    output = Activity.BOUNTY;
                    break;
                case "fai":
                case "fail":
                case "failed":
                    output = Activity.FAILED;
                    break;
                case "fin":
                case "fine":
                case "fines":
                    output = Activity.FINE;
                    break;
                case "int":
                case "intel":
                    output = Activity.INTEL;
                    break;
                case "min":
                case "mining":
                    output = Activity.MINING;
                    break;
                case "mis":
                case "mission":
                case "missions":
                    output = Activity.MISSION;
                    break;
                case "mur":
                case "murder":
                case "murders":
                    output = Activity.MURDER;
                    break;
                case "exploration":
                case "sca":
                case "scan":
                case "scans":
                    output = Activity.SCAN;
                    break;
                case "smu":
                case "smuggle":
                case "smuggling":
                    output = Activity.SMUGGLING;
                    break;
                case "tra":
                case "trading":
                case "trade":
                    output = Activity.TRADE;
                    break;
                default:
                    output = null;
                    break;
            }

            return output;
        }
    }

    @Override
    public void runCommand(PrivateMessageReceivedEvent event, String[] args) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("mystats")) {
                ArrayList<String> messages = BGSGoal.listGoal("0", event.getAuthor().getId(), true);
                messages.add(BGSStats.getUserStats(event.getAuthor().getId()));
                String messageToSend = "";
                for (String message : messages) { // check if we are going to exceed the 2000 character limit of a message
                    if (messageToSend.length() > 0 && (messageToSend.length() + message.length() > 2000)) {
                        event.getChannel().sendMessage(messageToSend + "\u0000").queue();
                        messageToSend = "";
                        messageToSend += message;
                    } else { // need the line break if not going to be seperate messages.
                        messageToSend += "\n" + message;
                    }
                }
                event.getChannel().sendMessage(messageToSend).queue();
            } else if (args[0].equalsIgnoreCase("mustard")) {  // for the fun of autocorrect added mustard command (mystats gone wrong)
                event.getChannel().sendMessage(":hotdog:").queue();
            } else if (args[0].equalsIgnoreCase("help")) {
                event.getChannel().sendMessage(BGS_LOG_HELP).queue();
            }
        }
    }

    @Override
    public void runCommand(GuildMessageReceivedEvent event, String[] args) {
        if (args.length == 0) {
            BGSRole.toggleBgsRole(event);
        }
        // admin calls for stats
        else if (args[0].equalsIgnoreCase("stats") && DataProvider.isAdmin(event)) {
            if (args.length == 1) { // send default help message for stats admin
                event.getChannel().sendMessage(BGS_STATS_HELP).queue();

            } else if (args[1].equalsIgnoreCase("summary") || args[1].equalsIgnoreCase("sum")) { // summary stats
                // Two options. No system filter and filtering for a system
                if (args.length == 4 || args.length == 5) {

                    List<String> messages = BGSStats.getTick(args);
                    String messageToSend = "";
                    for (String message : messages) { // check if we are going to exceed the 2000 character limit of a message
                        if (messageToSend.length() > 0 && (messageToSend.length() + message.length() > 2000)) {
                            event.getChannel().sendMessage(messageToSend + "\u0000").queue();
                            messageToSend = "";
                            messageToSend += message;
                        } else { // need the line break if not going to be seperate messages.
                            messageToSend += "\n" + message;
                        }
                    }
                    event.getChannel().sendMessage(messageToSend).queue();

                } else { // send help for summary stats
                    event.getChannel().sendMessage(BGS_STATS_HELP).queue();

                }
            }
            // CSV output
            else if (args[1].equalsIgnoreCase("csv") && args.length >= 4) {
                JDAUtil.sendMultipleMessages(event.getChannel(), BGSStats.getFullTick(args));

            } else { // If got this far something went wrong so show help for stats
                event.getChannel().sendMessage(BGS_STATS_HELP).queue();

            }
        }
        // admin commands for system factions
        else if ((args[0].equalsIgnoreCase("faction") || args[0].equalsIgnoreCase("factions") || args[0].equalsIgnoreCase("fac")) && DataProvider.isAdmin(event)) {
            if (args.length == 1) { // send default help message for system admin
                event.getChannel().sendMessage(BGS_FACTION_HELP).queue();
            } else if (args[1].equalsIgnoreCase("list")) { // Output a list of the factions available
                //bgs faction, list, <system>
                if (args.length == 2) {
                    event.getChannel().sendMessage("**BGS Factions**\n" + BGSFaction.getFactions(true, 0) + BGSFaction.getFactions(true, -1)).queue();
                } else if (args.length == 3) {
                    event.getChannel().sendMessage(BGSFaction.getFactions(DataProvider.isAdmin(event), BGSSystem.systemExists(args[2], args[2], 0, true))).queue();
                } else {
                    event.getChannel().sendMessage(BGS_FACTION_HELP).queue();
                }
            } else if (args[1].equalsIgnoreCase("hide") || args[1].equalsIgnoreCase("show")) { // show or hide a faction
                //bgs faction, <show | hide>, faction, system
                if (args.length == 4) {
                    event.getChannel().sendMessage(BGSFaction.setFactionVisibility(args[1].equalsIgnoreCase("show"), args[2], args[3])).queue();
                } else { // show system help if wrong number of arguments
                    event.getChannel().sendMessage(BGS_FACTION_HIDE_HELP + "\n" + BGSFaction.getFactions(DataProvider.isAdmin(event), -1)).queue();
                }

            } else if (args[1].equalsIgnoreCase("add")) { // add a new faction to the database
                //bgs faction, add, <shortname>, <fullname>
                event.getChannel().sendMessage(BGSFaction.addFaction(args)).queue();

            } else if (args[1].equalsIgnoreCase("edit")) { // edit a faction in the database
                //bgs faction, edit, <factionid>, <shortname>, <fullname>
                event.getChannel().sendMessage(BGSFaction.editFaction(args)).queue();

            } else if (args[1].equalsIgnoreCase("assign") || args[1].equalsIgnoreCase("ass")) { // assign a faction to a system
                //bgs faction, assign, <factionname>, <systemname>
                event.getChannel().sendMessage(BGSFaction.assignFaction(args)).queue();

            } else if (args[1].equalsIgnoreCase("remove") || args[1].equalsIgnoreCase("rem")) { // remove a faction from a system
                //bgs faction, remove, <factionname>, <systemname>
                event.getChannel().sendMessage(BGSFaction.removeFaction(args)).queue();

            } else { // if got this far something went wrong. Show system help
                event.getChannel().sendMessage(BGS_FACTION_HELP).queue();
            }
        }
        // admin functions for systems
        else if ((args[0].equalsIgnoreCase("system") || args[0].equalsIgnoreCase("systems") || args[0].equalsIgnoreCase("sys")) && DataProvider.isAdmin(event)) {
            if (args.length == 1) { // send default help message for system admin
                event.getChannel().sendMessage(BGS_SYSTEM_HELP).queue();

            } else if (args[1].equalsIgnoreCase("list")) { // Output a list of the systems available
                event.getChannel().sendMessage("**BGS Star Systems**\n" + BGSSystem.getSystems(DataProvider.isAdmin(event))).queue();

            } else if (args[1].equalsIgnoreCase("hide") || args[1].equalsIgnoreCase("show")) { // show or hide a system
                if (args.length == 3) {
                    event.getChannel().sendMessage(BGSSystem.setSystemVisibility(args[1].equalsIgnoreCase("show"), args[2])).queue();

                } else { // show system help if wrong number of arguments
                    event.getChannel().sendMessage(BGS_SYSTEM_HIDE_HELP + "\n" + BGSSystem.getSystems(DataProvider.isAdmin(event))).queue();
                }

            } else if (args[1].equalsIgnoreCase("add")) { // add a new system to the database
                //bgs system, add, <shortname>, <fullname>
                event.getChannel().sendMessage(BGSSystem.addSystem(args)).queue();

            } else if (args[1].equalsIgnoreCase("edit")) { // edit a system in the database
                //bgs system, edit, <systemid>, <shortname>, <fullname>
                event.getChannel().sendMessage(BGSSystem.editSystem(args)).queue();

            } else { // if got this far something went wrong. Show system help
                event.getChannel().sendMessage(BGS_SYSTEM_HELP).queue();
            }
        }
        // admin functions for goals
        else if ((args[0].equalsIgnoreCase("goal") || args[0].equalsIgnoreCase("goals")) && DataProvider.isAdmin(event)) {
            if (args.length == 1) { // Send help message for goals
                event.getChannel().sendMessage(BGS_GOAL_HELP).queue();

            } else if (args[1].equalsIgnoreCase("end")) { // end a goal now a new goal.
                //bgs goals,end,goalid
                if (args.length >= 3) { // 5 arguments specifies no goal items. More than 5 means at least one goal item is specified.
                    event.getChannel().sendMessage(BGSGoal.endGoal(args)).queue();
                } else { // show help message for adding a goal as less than 5 arguments
                    event.getChannel().sendMessage(BGS_GOAL_END_HELP).queue();
                }
            } else if (args[1].equalsIgnoreCase("add")) { // add a new goal.
                //bgs goals,add,system,Start Date:Time, Ticks, activity/faction/usergoal/globalgoal,activity/faction/usergoal/globalgoal,activity/faction/usergoal/globalgoal
                if (args.length >= 5) { // 5 arguments specifies no goal items. More than 5 means at least one goal item is specified.
                    event.getChannel().sendMessage(BGSGoal.addGoal(args)).queue();
                } else { // show help message for adding a goal as less than 5 arguments
                    event.getChannel().sendMessage(BGS_GOAL_ADD_HELP).queue();
                }
            } else if (args[1].equalsIgnoreCase("delete") || args[1].equalsIgnoreCase("del")) { // delete a goal
                //bgs goals,<del>,goalid
                if (args.length == 3) {
                    event.getChannel().sendMessage(BGSGoal.deleteGoal(args[2])).queue();
                } else {
                    event.getChannel().sendMessage(BGS_GOAL_DEL_HELP).queue();
                }
            } else if (args[1].equalsIgnoreCase("deleteact") || args[1].equalsIgnoreCase("delact")) { // delete an individual goal item from a goal
                //bgs goals,<delact>,goalid,Activity
                if (args.length == 5) {
                    event.getChannel().sendMessage(BGSGoal.deleteGoalItem(Activity.from(args[3]), args[2], args[4])).queue();
                } else {
                    event.getChannel().sendMessage(BGS_GOAL_DELACT_HELP).queue();
                }
            } else if (args[1].equalsIgnoreCase("edit")) { // edit a goal. Goal items are edited using /editact
                //bgs golas,edit,goalid,system,Start Date:Time, Ticks
                if (args.length == 6) {
                    event.getChannel().sendMessage(BGSGoal.editGoal(args)).queue();
                } else {
                    event.getChannel().sendMessage(BGS_GOAL_EDIT_HELP).queue();
                }
            } else if (args[1].equalsIgnoreCase("editactivity") || args[1].equalsIgnoreCase("addactivity") || args[1].equalsIgnoreCase("editact") || args[1].equalsIgnoreCase("addact")) {
                //bgs goals,<addact,editact>,goalid,Activity/usergoal/globalgoal,Activity/usergoal/globalgoal,Activity/usergoal/globalgoal
                String message = "";
                // need 4 or more arguments to add a goal item
                if (args.length >= 4) {
                    // loop through the goal items which are in the array from 3 onwards
                    for (int i = 3; i < args.length; i++) {
                        String[] goalitem = args[i].split("/");
                        message += BGSGoal.addGoalItem(goalitem, i - 2, args[2]); // add the goal item
                    }
                    event.getChannel().sendMessage(message).queue();
                } else {
                    event.getChannel().sendMessage(BGS_GOAL_EDITACT_HELP).queue();
                }
            } else if (args[1].equalsIgnoreCase("note")) { // add a note to a goal. Allows special instructions to be given.
                if (args.length >= 3) {
                    //bgs goals, goalid, note
                    event.getChannel().sendMessage(BGSGoal.addGoalNote(args)).queue();
                } else {
                    event.getChannel().sendMessage(BGS_GOAL_NOTE_HELP).queue();
                }
            } else if (args[1].equalsIgnoreCase("list")) {
                //bgs goals, list,#
                //Get # most recent goals
                //If # is not specified then get all active goals
                String recent = "0";
                if (args.length == 3) {
                    recent = args[2];
                }

                ArrayList<String> messages = BGSGoal.listGoal(recent, event.getAuthor().getId(), false);
                String messageToSend = "";
                for (String message : messages) { // check if we are going to exceed the 2000 character limit of a message
                    if (messageToSend.length() > 0 && (messageToSend.length() + message.length() > 2000)) {
                        event.getChannel().sendMessage(messageToSend + "\u0000").queue();
                        messageToSend = "";
                        messageToSend += message;
                    } else { // need the line break if not going to be seperate messages.
                        messageToSend += "\n" + message;
                    }
                }
                event.getChannel().sendMessage(messageToSend).queue();
            } else {
                event.getChannel().sendMessage(BGS_GOAL_HELP).queue();
            }
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("mystats")) {
                ArrayList<String> messages = BGSGoal.listGoal("0", event.getAuthor().getId(), true);
                messages.add(BGSStats.getUserStats(event.getAuthor().getId()));
                String messageToSend = "";
                for (String message : messages) { // check if we are going to exceed the 2000 character limit of a message
                    if (messageToSend.length() > 0 && (messageToSend.length() + message.length() > 2000)) {
                        JDAUtil.getPrivateChannel(event.getAuthor()).sendMessage(messageToSend + "\u0000").queue();
                        messageToSend = "";
                        messageToSend += message;
                    } else { // need the line break if not going to be seperate messages.
                        messageToSend += "\n" + message;
                    }
                }
                JDAUtil.getPrivateChannel(event.getAuthor()).sendMessage(messageToSend).queue();

            } else if (args[0].equalsIgnoreCase("mustard")) { // for the fun of autocorrect added mustard command (mystats gone wrong)
                JDAUtil.getPrivateChannel(event.getAuthor()).sendMessage(":hotdog:").queue();
            } else if (args[0].equalsIgnoreCase("total")) {
                if (DataProvider.isAdmin(event.getMember().getRoles()))
                    event.getChannel().sendMessage(BGSStats.getTotalAmount()).queue();
                else
                    JDAUtil.getPrivateChannel(event.getAuthor()).sendMessage(BGSStats.getTotalAmount()).queue();

            } else {
                JDAUtil.getPrivateChannel(event.getAuthor()).sendMessage(BGS_LOG_HELP).queue();
            }
        } else if (args.length == 2) {
            String message = "**WARNING ACTION NOT LOGGED**\nStar system not specified? Enter '" + Listener.prefix + "bgs help' or use one of the star system names below:\n";
            message += BGSSystem.getSystems(DataProvider.isAdmin(event));
            event.getChannel().sendMessage(message).queue();

        } else if (args.length == 3) {
            String message;
            int systemid = BGSSystem.systemExists(args[2], args[2], 0, false);
            if (systemid > 0) {
                message = "**WARNING ACTION NOT LOGGED**\nFaction not specified? Enter '" + Listener.prefix + "bgs help' or use one of the factions below:\n";
                message += BGSFaction.getFactions(DataProvider.isAdmin(event), systemid);
            } else {
                message = "**WARNING ACTION NOT LOGGED**\nYou specified an invalid system. Enter '" + Listener.prefix + "bgs help' or use one of the star system names below:\n";
                message += BGSSystem.getSystems(DataProvider.isAdmin(event));
            }

            event.getChannel().sendMessage(message).queue();

        } else if (args.length == 4) {
            //bgs activity, amount, system, faction
            event.getChannel().sendMessage(BGSLogging.logActivity(DataProvider.isAdmin(event), args[0], event.getAuthor().getId(), event.getMember().getEffectiveName(), args[1], args[2], args[3])).queue();

        } else if (args.length > 4) {
            // anything more than 4 is likely the user has tried to log data but used a number seperate (1,000,000)
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


    // other stuff starts
    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();

    static {
        suffixes.put(1_000L, "k");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "G");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "P");
        suffixes.put(1_000_000_000_000_000_000L, "E");
    }

    static String int_format_short(long value) {
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

    static String dateDiff(Date date1, Date date2) {
        //in milliseconds
        long diff = date2.getTime() - date1.getTime();
        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000) % 24;
        long diffDays = diff / (24 * 60 * 60 * 1000);
        List<String> output = new ArrayList<>();
        if (diffDays > 0)
            output.add(diffDays + " day" + ((diffDays > 1) ? "s" : ""));
        if (diffHours > 0)
            output.add(diffHours + " hr" + ((diffHours > 1) ? "s" : ""));
        if (diffMinutes > 0)
            output.add(diffMinutes + " min" + ((diffMinutes > 1) ? "s" : ""));
        if (diffSeconds > 0 && diffHours == 0)
            output.add(diffSeconds + " sec" + ((diffSeconds > 1) ? "s" : ""));
        if (diff < 0)
            return "Finished";
        else
            return String.join(", ", output) + " left";
    }

    static String getRandom(String[] array) {
        int rnd = new Random().nextInt(array.length);
        return array[rnd];
    }

    static int getRows(ResultSet rs) {
        try {
            rs.last();
            int rows = rs.getRow();
            rs.beforeFirst();
            return rows;
        } catch (SQLException e) {
            return 0;
        }
    }
    // other stuff ends
}
