package iw_bot;

import java.text.SimpleDateFormat;

public class Constants {
    public final static String[] QUOTE = {"The true soldier fights not because he hates what is in front of him, but because he loves what is behind him. (G. K. Chesterton)",
            "I do not know with what weapons World War III will be fought, but I do know that World War IV will be fought with rocks. (Albert Einstein)",
            "War is like love, it always finds a way. (Bertolt Brecht)",
            "War makes rattling good history; but Peace is poor reading. (Thomas Hardy)",
            "War is a severe doctor; but it sometimes heals grievances. (Edward Counsel)",
            "They who can give up essential liberty to obtain a little temporary safety deserve neither liberty nor safety. (Benjamin Franklin)",
            "If you have built castles in the air, your work need not be lost; that is where they should be. Now put the foundations under them. (Henry David Thoreau)",
            "The good we secure for ourselves is precarious and uncertain until it is secured for all of us and incorporated into our common life. (Jane Addams)",
            "Those who surrender freedom for security will not have, nor do they deserve, either one. (Benjamin Franklin)",
            "Don't cry because it's over, smile because it happened. (Dr. Seuss)",
            "Insanity is doing the same thing, over and over again, but expecting different results. (Narcotics Anonymous)"
    };

    public final static SimpleDateFormat SQL_SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public final static SimpleDateFormat USER_SDF = new SimpleDateFormat("dd/MM/yy HH:mm");

    public final static String BGS_STATS_HELP = "**Help: Produce Statistics on BGS Data**\n" + Listener.prefix + "bgs stats, <type {summary,csv}, <tick count>, <start date time DD:MM:YY HH:MM>, [system filter {shortname,longname}]";

    public final static String BGS_SYSTEM_HELP = "**Help: Manage BGS Star Systems**\n" + Listener.prefix + "bgs system, <command modifier {add, edit, hide, show, list}>";
    public final static String BGS_SYSTEM_HIDE_HELP = "**Help: Show/Hide BGS Star Systems**\n" + Listener.prefix + "bgs system, hide|show <systemname>";
    public final static String BGS_SYSTEM_ADD_HELP = "**Help: Add BGS Star Systems**\n" + Listener.prefix + "bgs system, add, <shortname>, <fullname>";
    public final static String BGS_SYSTEM_EDIT_HELP = "**Help: Edit BGS Star Systems**\n" + Listener.prefix + "bgs system, edit, <systemid>, <shortname>, <fullname>";

    public final static String BGS_FACTION_HELP = "**Help: Manage BGS Factions**\n" + Listener.prefix + "bgs faction, <command modifier {add, edit, hide, show, list, assign}>";
    public final static String BGS_FACTION_HIDE_HELP = "**Help: Show/Hide BGS Faction in a System**\n" + Listener.prefix + "bgs faction, hide|show <factionname>, <systemname>";
    public final static String BGS_FACTION_ADD_HELP = "**Help: Add BGS  Faction**\n" + Listener.prefix + "bgs faction, add, <shortname>, <fullname>";
    public final static String BGS_FACTION_ASSIGN_HELP = "**Help: Assign BGS Faction to a System**\n" + Listener.prefix + "bgs faction, assign, <factionname>, <systemname>";
    public final static String BGS_FACTION_EDIT_HELP = "**Help: Edit BGS Faction**\n" + Listener.prefix + "bgs faction, edit, <factionid>, <shortname>, <fullname>";



    public final static String BGS_GOAL_HELP = "**Help: Manage BGS Goals**\n" + Listener.prefix + "bgs goal, <command modifier {add, delete, edit, addactivity, deleteactivity, editactivity, end, list}>\n";
    public final static String BGS_GOAL_ADD_HELP = "**Help: Add BGS Goal**\n" + Listener.prefix + "bgs goal, add, <system>, <Start dd/MM/yy HH:mm>, <ticks #>, <Activity/Faction/CMDRGoal #/GlobalGoal #>\n"+
            "You can append as many <Activity/Faction/CMDRGoal #/GlobalGoal #> to the command as you require. Just seperate them by commas.";
    public final static String BGS_GOAL_DEL_HELP = "**Help: Delete BGS Goal**\n" + Listener.prefix + "bgs goal, {delete,del}, <goalid #>\n"+
            "You can get a list of goals by using " + Listener.prefix + "bgs goals, list, <last # goals>";
    public final static String BGS_GOAL_DELACT_HELP = "**Help: Delete Specific BGS Goal Activity Target**\n" + Listener.prefix + "bgs goal, {deleteactivity, delact}, <goalid #>, <activity>, <faction>\n"+
            "You can get a list of goals by using " + Listener.prefix + "bgs goal, list, <last # goals>";
    public final static String BGS_GOAL_EDIT_HELP = "**Help: Edit BGS Goal**\n" + Listener.prefix + "bgs goal, edit, <goalid #>, <system>, <Start dd/MM/yy HH:mm>, <ticks #>\n"+
            "You can get a list of goals by using " + Listener.prefix + "bgs goal, list, <last # goals>\n"+
            "To edit the activity targets of a goal use " + Listener.prefix + "bgs goal, editactivity\n"+
            "To add an activity target to a goal use " + Listener.prefix + "bgs goal, addactivity";
    public final static String BGS_GOAL_EDITACT_HELP = "**Help: Add/Edit BGS Goal Activity Targets**\n" + Listener.prefix + "bgs goal, {addactivity, addact, editactivity, editact}, <goalid> <Activity/Faction/CMDRGoal #/GlobalGoal #>\n"+
            "You can append as many <Activity/Faction/CMDRGoal #/GlobalGoal #> to the command as you require. Just seperate them by commas.";
    public final static String BGS_GOAL_NOTE_HELP = "**Help: BGS Goal Notes**\n" + Listener.prefix + "bgs goal, note, <goalid>, <text>\n"+
            "Add a note to a goal with specific instructions for CMDRs";
    public final static String BGS_GOAL_END_HELP = "**Help: END BGS Goal**\n" + Listener.prefix + "bgs goal, end, <goalid>,[<End dd/MM/yy HH:mm>]\n"+
            "End a BGS goal early. Useful if tick time changes. If you do not specify the end data/time then it will end the goal straight away.";



    public final static String BGS_LOG_HELP = " **BGS Bot Commands:**\n" +
            "Enter '" + Listener.prefix + "bgs' to add or remove BGS role\n" +
            "\n" +
            "Format for logging BGS actions is:\n"+
            Listener.prefix + "bgs <activity>, <amount with no seperators>, <system identifier>, <faction identifier>\n" +
            "\n" +
            "\n" +
            "**BGS Activities:**\n" +
            "*bonds*: To log the value of combat bonds (#) claimed in a war or civil war.\n" +
            "\n" +
            "*bounties*: To log the value of bounties (#) cashed in.\n" +
            "\n" +
            "*failed*: To log the number of missions failed (#).\n" +
            "\n" +
            "*fine*: To log the amount (#) of fines gained (#).\n" +
            "\n" +
            "*intel*: To log the value of of intel packages (#) cashed in (#).\n" +
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
            "e.g. '" + Listener.prefix + "bgs trade, 1500000'\n";
}
