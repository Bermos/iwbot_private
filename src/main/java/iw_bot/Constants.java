package iw_bot;

import java.text.SimpleDateFormat;

public class Constants {
    public final static SimpleDateFormat SQL_SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public final static SimpleDateFormat USER_SDF = new SimpleDateFormat("dd/MM/yy HH:mm");
    public final static String BGS_STATS_HELP = "Help: " + Listener.prefix + "bgs stats, <type {summary,csv}, <tick count>, <start date time DD:MM:YY HH:MM>, [system filter {shortname,longname}]";
    public final static String BGS_SYSTEM_HELP = "Help: " + Listener.prefix + "bgs system, <command modifier {add, edit, hide, show}>";
    public final static String BGS_LOG_HELP = " **BGS Bot Commands:**\n" +
            "Enter '" + Listener.prefix + "bgs' to add or remove BGS role\n" +
            "\n" +
            "Format for entering bgs commands is:\n"+
            Listener.prefix + "bgs <activity>, <amount with no seperators>, <system identifier>\n" +
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
