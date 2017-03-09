package iw_bot;

import provider.DataProvider;

import java.text.SimpleDateFormat;

public class Constants {
    private final static String pre = DataProvider.getPrefix();
    public final static SimpleDateFormat TIME_SDF = new SimpleDateFormat("HH:mm:ss");
    public final static SimpleDateFormat SQL_SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public final static SimpleDateFormat USER_SDF = new SimpleDateFormat("dd/MM/yy HH:mm");

    public final static String BGS_STATS_HELP = "**Help: Produce Statistics on BGS Data**\n" + pre + "bgs stats, <type {summary,csv}, <tick count>, <start date time DD:MM:YY HH:MM>, [system filter {shortname,longname}]";

    public final static String BGS_SYSTEM_HELP = "**Help: Manage BGS Star Systems**\n" + pre + "bgs system, <command modifier {add, edit, hide, show}>";

    public final static String BGS_GOAL_HELP = "**Help: Manage BGS Goals**\n" + pre + "bgs goal, <command modifier {add, delete, edit, addactivity, deleteactivity, editactivity, list}>\n";
    public final static String BGS_GOAL_ADD_HELP = "**Help: Add BGS Goal**\n" + pre + "bgs goal, add, <system>, <Start dd/MM/yy HH:mm>, <ticks #>, <Activity/CMDRGoal #/GlobalGoal #>\n"+
            "You can append as many <Activity/CMDRGoal #/GlobalGoal #> to the command as you require. Just seperate them by commas.";
    public final static String BGS_GOAL_DEL_HELP = "**Help: Delete BGS Goal**\n" + pre + "bgs goal, {delete,del}, <goalid #>\n"+
            "You can get a list of goals by using " + pre + "bgs goals, list, <last # goals>";
    public final static String BGS_GOAL_DELACT_HELP = "**Help: Delete Specific BGS Goal Activity Target**\n" + pre + "bgs goal, {deleteactivity, delact}, <goalid #>, <activity>\n"+
            "You can get a list of goals by using " + pre + "bgs goal, list, <last # goals>";
    public final static String BGS_GOAL_EDIT_HELP = "**Help: Edit BGS Goal**\n" + pre + "bgs goal, edit, <goalid #>, <system>, <Start dd/MM/yy HH:mm>, <ticks #>\n"+
            "You can get a list of goals by using " + pre + "bgs goal, list, <last # goals>\n"+
            "To edit the activity targets of a goal use " + pre + "bgs goal, editactivity\n"+
            "To add an activity target to a goal use " + pre + "bgs goal, addactivity";
    public final static String BGS_GOAL_EDITACT_HELP = "**Help: Add/Edit BGS Goal Activity Targets**\n" + pre + "bgs goal, {addactivity, addact, editactivity, editact}, <goalid> <Activity/CMDRGoal #/GlobalGoal #>\n"+
            "You can append as many <Activity/CMDRGoal #/GlobalGoal #> to the command as you require. Just seperate them by commas.";
    public final static String BGS_GOAL_NOTE_HELP = "**Help: List BGS Goals**\n" + pre + "bgs goal, note, <goalid>, <text>\n"+
            "Add a note to a goal with specific instructions for CMDRs";
    public final static String BGS_GOAL_END_HELP = "**Help: END BGS Goal**\n" + pre + "bgs goal, end, <goalid>,[<End dd/MM/yy HH:mm>]\n"+
            "End a BGS goal early. Useful if tick time changes. If you do not specify the end data/time then it will end the goal straight away.";



    public final static String BGS_LOG_HELP = " **BGS Bot Commands:**\n" +
            "Enter '" + pre + "bgs' to add or remove BGS role\n" +
            "\n" +
            "Format for entering bgs commands is:\n"+
            pre + "bgs <activity>, <amount with no seperators>, <system identifier>\n" +
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
            "e.g. '" + pre + "bgs trade, 1500000'\n";
}
