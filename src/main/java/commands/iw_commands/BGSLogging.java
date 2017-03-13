package commands.iw_commands;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

import iw_bot.LogUtil;

import provider.Connections;
import provider.DataProvider;
import provider.Statistics;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static iw_bot.Constants.*;

class BGSLogging {
    static String logActivity(boolean admin, String sActivity, String userid, String username, String sAmount, String system, String faction) {

        //ToDo Logging: If goal is already met direct message once per activity with details on what still needs work.
        //ToDo Logging: Consider automatically choose the faction unless more than one faction has a goal for that activity
        //ToDo Logging: Consider default to IW if no goals for that activity and no faction specified.
        int amount = 0;
        int systemid = 0;
        int factionid = 0;
        BGS.Activity activity =  BGS.Activity.from(sActivity);
        try {
            amount = Integer.parseInt(sAmount);
            // check we have a valid system specified and that system is not hidden (which prevents logging)
            systemid = BGSSystem.systemExists(system, system, 0, false);
            factionid = BGSFaction.checkFactionInSystem(faction,system,false);

            if (activity == null) {
                String output = "";
                for (BGS.Activity act : BGS.Activity.values())
                    output += act.toString() + "\n";
                return "This activity does not exist. These do:\n" + output;

            }

            Connection connect = new Connections().getConnection();

            if (systemid > 0) {
                if(factionid > 0) {
                    PreparedStatement ps = connect.prepareStatement("INSERT INTO bgs_activity (username, userid, amount, activity, systemid, factionid) " +
                            "VALUES (?, ?, ?, ?, ?, ?);");
                    ps.setString(1, username);
                    ps.setString(2, userid);
                    ps.setInt(3, amount);
                    ps.setString(4, activity.toString());
                    ps.setInt(5, systemid);
                    ps.setInt(6, factionid);
                    ps.executeUpdate();
                } else {
                    String message = "**WARNING ACTION NOT LOGGED**\nInvalid faction entered. You can use either the shortname or the fullname. Please select from:\n";
                    return message + BGSFaction.getFactions(admin,systemid);
                }

            } else {
                String message = "**WARNING ACTION NOT LOGGED**\nInvalid system entered. You can use either the shortname or the fullname. Please select from:\n";
                return message + BGSSystem.getSystems(admin);
            }

        } catch (NumberFormatException e) {

            return "[Error] " + sAmount + " is not a valid number to log. Make sure you have the format '/bgs *activity*, *number*, *system*, *faction*";
        } catch (MySQLIntegrityConstraintViolationException e) {
            //This happens when the system was not found.


        } catch (SQLException e) {
            LogUtil.logErr(e);
            return "**WARNING ACTION NOT LOGGED**\nSomething went wrong saving your contribution. Please retry later";
        }

        if (!DataProvider.isDev())
            Statistics.getInstance().logBGSActivity(System.currentTimeMillis(), userid, username, activity.toString(), amount, system.toUpperCase());

        return "**Your engagement with " + BGSFaction.getFactionFullname(factionid) + " in " + BGSSystem.getSystemFullname(systemid)+ " has been noticed. o7.**\n*" + BGS.getRandom(QUOTE) + "*";
    }
}
