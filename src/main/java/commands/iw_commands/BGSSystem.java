package commands.iw_commands;

import core.LogUtil;

import provider.Connections;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static core.Constants.*;

class BGSSystem {
    // system stuff start
    static String editSystem(String[] args) {
        //bgs system, edit, <systemid>, <shortname>, <fullname>
        if (args.length == 5) {
            if (systemExists(args[3], args[4], Integer.parseInt(args[2]), true) == 0 && checkSystemID(Integer.parseInt(args[2])) == Integer.parseInt(args[2])) {
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

    static String addSystem(String[] args) {
        //bgs system, add, <shortname>, <fullname>
        if (args.length == 4) {
            Connection connect = new Connections().getConnection();
            try {
                if (systemExists(args[2], args[3], 0, true) == 0) {
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
        } else {
            return BGS_SYSTEM_ADD_HELP;
        }
    }

    private static int checkSystemID(int systemid) {
        // make sure that system with the specified ID exists
        Connection connect = new Connections().getConnection();
        try {
            PreparedStatement ps;
            ps = connect.prepareStatement("SELECT systemid FROM bgs_system WHERE systemid = ?;");
            ps.setInt(1, systemid);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("systemid");
            } else {
                return 0;
            }
        } catch (SQLException e) {
            return -1;
        }
    }

    static int systemExists(String shortname, String fullname, int ignore, boolean showhidden) {
        // make sure that system with same short or fullname does not already exist
        Connection connect = new Connections().getConnection();
        try {
            PreparedStatement ps;
            if (ignore > 0) {
                ps = connect.prepareStatement("SELECT systemid FROM bgs_system WHERE (s_shortname = ? OR s_shortname = ? OR s_fullname = ? OR s_fullname = ?) AND systemid <> ? AND s_hidden <= ?;");
                ps.setString(1, shortname);
                ps.setString(2, fullname);
                ps.setString(3, shortname);
                ps.setString(4, fullname);
                ps.setInt(5, ignore);
                ps.setInt(6, (showhidden) ? 1 : 0);
            } else {
                ps = connect.prepareStatement("SELECT systemid FROM bgs_system WHERE (s_shortname = ? OR s_shortname = ? OR s_fullname = ? OR s_fullname = ?) AND s_hidden <= ?;");
                ps.setString(1, shortname);
                ps.setString(2, fullname);
                ps.setString(3, shortname);
                ps.setString(4, fullname);
                ps.setInt(5, (showhidden) ? 1 : 0);
            }
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("systemid");
            } else {
                return 0;
            }
        } catch (SQLException e) {
            return -1;
        }
    }

    static String getSystems(boolean admin) {
        String message = null;
        try {
            // show hidden systems in italics for admins
            if (admin) {
                message = "```ID   | Short | Full\n";
                PreparedStatement ps = new Connections().getConnection().prepareStatement("SELECT * FROM bgs_system ORDER BY s_fullname ASC");
                ResultSet rs = ps.executeQuery();
                if (!rs.isBeforeFirst()) {
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
                if (!rs.isBeforeFirst()) {
                    message += "No Systems";
                }
                while (rs.next()) {
                    message += String.format("%1$-6s| %2$s\n", rs.getString("s_shortname"), rs.getString("s_fullname"));
                }
            }

        } catch (SQLException e) {
            LogUtil.logErr(e);
        }
        message += "```\n";
        return message;
    }

    static String setSystemVisibility(boolean show, String system) {
        Connection connect = new Connections().getConnection();
        try {
            PreparedStatement ps = connect.prepareStatement("UPDATE bgs_system SET s_hidden = ? WHERE s_shortname = ? OR s_fullname = ? LIMIT 1");
            ps.setInt(1, show ? 0 : 1);
            ps.setString(2, system);
            ps.setString(3, system);

            // if one row was altered
            if (ps.executeUpdate() == 1) {
                if (show)
                    return "BGS star system '" + system + "' **VISIBLE**. Logging possible for this system.\n" + getSystems(true);
                else
                    return "BGS star system '" + system + "' **HIDDEN**. Logging no longer possible for this system.\n" + getSystems(true);
            } else { // if no row was altered the system wasn't found
                return "**WARNING SYSTEM VISIBILITY NOT CHANGED**\nSystem '" + system + "' not found.\n" + getSystems(true);
            }
        } catch (SQLException e) {
            //This only happens when there's a serious issue with mysql or the connection to it
            LogUtil.logErr(e);
            return "**WARNING SYSTEM VISIBILITY NOT CHANGED**";
        }
    }

    static String getSystemFullname(int systemid) {
        try {
            PreparedStatement ps = new Connections().getConnection().prepareStatement("SELECT s_fullname FROM bgs_system WHERE systemid = ?");
            ps.setInt(1, systemid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("s_fullname");
            } else {
                return "Missing!";
            }
        } catch (SQLException e) {
            return "SQL ERROR!";
        }
    }
    // system stuff ends
}
