package commands.iw_commands;

import iw_bot.LogUtil;

import provider.Connections;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static iw_bot.Constants.*;

class BGSFaction {
    static String getFactionFullname(int factionid) {
        try {
            PreparedStatement ps = new Connections().getConnection().prepareStatement("SELECT f_fullname FROM bgs_faction WHERE factionid = ?");
            ps.setInt(1, factionid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("f_fullname");
            } else {
                return "Missing!";
            }
        } catch (SQLException e) {
            return "SQL ERROR!";
        }
    }

    private static int checkFactionID(int factionid) {
        // make sure that system with the specified ID exists
        Connection connect = new Connections().getConnection();
        try {
            PreparedStatement ps;
            ps = connect.prepareStatement("SELECT factionid FROM bgs_faction WHERE factionid = ?;");
            ps.setInt(1, factionid);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("factionid");
            } else {
                return 0;
            }
        } catch (SQLException e) {
            return -1;
        }
    }

    static int checkFactionInSystem(String faction, String system, boolean showhidden) {
        // make sure that faction is in a system
        Connection connect = new Connections().getConnection();
        try {
            // get faction id
            int factionid = factionExists(faction, faction, 0);
            int systemid = BGSSystem.systemExists(system, system, 0, true);

            PreparedStatement ps = connect.prepareStatement("SELECT systemid FROM bgs_system_faction WHERE systemid = ? AND factionid = ? AND f_hidden <= ?;");
            ps.setInt(1, systemid);
            ps.setInt(2, factionid);
            ps.setInt(3, (showhidden ? 1 : 0));
            ResultSet rs = ps.executeQuery();
            if (!rs.isBeforeFirst()) {
                return 0;
            }
            return factionid;
        } catch (SQLException e) {
            return -1;
        }
    }

    private static int factionExists(String shortname, String fullname, int ignore) {
        // make sure that faction with same short or fullname does not already exist
        Connection connect = new Connections().getConnection();
        try {
            PreparedStatement ps;
            if (ignore > 0) {
                ps = connect.prepareStatement("SELECT factionid FROM bgs_faction WHERE (f_shortname = ? OR f_shortname = ? OR f_fullname = ? OR f_fullname = ?) AND factionid <> ?;");
                ps.setString(1, shortname);
                ps.setString(2, fullname);
                ps.setString(3, shortname);
                ps.setString(4, fullname);
                ps.setInt(5, ignore);
            } else {
                ps = connect.prepareStatement("SELECT factionid FROM bgs_faction WHERE f_shortname = ? OR f_shortname = ? OR f_fullname = ? OR f_fullname = ?");
                ps.setString(1, shortname);
                ps.setString(2, fullname);
                ps.setString(3, shortname);
                ps.setString(4, fullname);
            }
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("factionid");
            } else {
                return 0;
            }
        } catch (SQLException e) {
            return -1;
        }
    }

    static ArrayList<String> editFaction(String[] args) {
        //bgs faction, edit, <factionid>, <shortname>, <fullname>
        ArrayList<String> messages = new ArrayList<>();
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
                    messages.add("**WARNING FACTION NOT UPDATED**");
                    return messages;
                }
                messages.add("Faction updated\n");
                messages.addAll(getFactions(true, 0));
                return messages;
            } else {
                messages.add("**WARNING FACTION NOT UPDATED**\nCheck that a faction with  ID '" + args[2] + "' exists and that no other faction with a shortname/fullname of '" + args[3] + "' or '" + args[4] + "' already exists!\n");
                messages.addAll(getFactions(true, 0));
                return messages;
            }
        } else {
            messages.add(BGS_FACTION_EDIT_HELP + "\n");
            messages.addAll(getFactions(true, 0));
            return messages;
        }
    }

    static ArrayList<String> assignFaction(String[] args) {
        //bgs faction, assign, <factionname>, <systemname>
        ArrayList<String> messages = new ArrayList<>();
        if (args.length == 4) {
            int factionid = factionExists(args[2], args[2], 0);
            int systemid = BGSSystem.systemExists(args[3], args[3], 0, true);
            if (factionid > 0) {
                if (systemid > 0) {
                    if (checkFactionInSystem(args[2], args[3], true) > 0) {
                        messages.add("**WARNING FACTION NOT ASSIGNED**\n" + getFactionFullname(factionid) + " is already assigned to " + BGSSystem.getSystemFullname(systemid) + "!");
                        return messages;
                    } else {
                        Connection connect = new Connections().getConnection();
                        try {
                            PreparedStatement ps = connect.prepareStatement("INSERT INTO bgs_system_faction SET factionid = ?, systemid = ?;");
                            ps.setInt(1, factionid);
                            ps.setInt(2, systemid);
                            ps.executeUpdate();
                        } catch (SQLException e) {
                            messages.add("**WARNING FACTION NOT ASSIGNED**\nSQL Error");
                            return messages;
                        }
                        messages.add("Faction Assigned\n" + String.join("",getFactions(true, systemid)));
                        return messages;
                    }
                } else {
                    messages.add("**WARNING SYSTEM DOES NOT EXIST**\nPlease select from the following\n" + BGSSystem.getSystems(true));
                    return messages;
                }
            } else {
                messages.add("**WARNING FACTION DOES NOT EXIST**\nPlease select from the following\n");
                messages.addAll(getFactions(true, 0));
                return messages;
            }
        } else {
            messages.add(BGS_FACTION_ASSIGN_HELP + "\n");
            messages.add("**Factions**\n");
            messages.addAll(getFactions(true, 0));
            messages.add("**Systems**\n");
            messages.add(BGSSystem.getSystems(true));
            return messages;
        }
    }

    static ArrayList<String> removeFaction(String[] args) {
        //bgs faction, remove, <factionname>, <systemname>
        ArrayList<String> messages = new ArrayList<>();
        if (args.length == 4) {
            int factionid = factionExists(args[2], args[2], 0);
            int systemid = BGSSystem.systemExists(args[3], args[3], 0, true);
            if (factionid > 0) {
                if (systemid > 0) {
                    if (checkFactionInSystem(args[2], args[3], true) <= 0) {
                        messages.add("**WARNING FACTION NOT REMOVED**\n" + getFactionFullname(factionid) + " is not assigned to " + BGSSystem.getSystemFullname(systemid) + "!");
                        return messages;
                    } else {
                        Connection connect = new Connections().getConnection();
                        try {
                            PreparedStatement ps = connect.prepareStatement("DELETE FROM bgs_system_faction WHERE factionid = ? AND systemid = ?;");
                            ps.setInt(1, factionid);
                            ps.setInt(2, systemid);
                            ps.executeUpdate();
                        } catch (SQLException e) {
                            messages.add("**WARNING FACTION NOT REMOVED**\nSQL Error");
                            return messages;
                        }
                        messages.add("Faction Removed\n");
                        messages.addAll(getFactions(true, systemid));
                        return messages;
                    }
                } else {
                    messages.add("**WARNING SYSTEM DOES NOT EXIST**\nPlease select from the following\n" + BGSSystem.getSystems(true));
                    return messages;
                }
            } else {
                messages.add("**WARNING FACTION DOES NOT EXIST**\nPlease select from the following\n");
                messages.addAll(getFactions(true, 0));
                return messages;
            }
        } else {
            messages.add(BGS_FACTION_REMOVE_HELP + "\n");
            messages.add("**Factions**\n");
            messages.addAll(getFactions(true, 0));
            messages.add("**Systems**\n");
            messages.add(BGSSystem.getSystems(true));
            return messages;
        }
    }


    static ArrayList<String> addFaction(String[] args) {
        //bgs faction, add, <shortname>, <fullname>
        ArrayList<String> messages = new ArrayList<>();
        if (args.length == 4) {
            Connection connect = new Connections().getConnection();
            try {
                // make sure that faction with same short or fullname does not already exist
                if (factionExists(args[2], args[3], 0) == 0) {
                    PreparedStatement ps = connect.prepareStatement("INSERT INTO bgs_faction (f_shortname, f_fullname) VALUES (?, ?)");
                    ps.setString(1, args[2]);
                    ps.setString(2, args[3]);
                    ps.executeUpdate();
                } else {
                    messages.add("**WARNING FACTION NOT ADDED**\nFaction with shortname/fullname set to either: '" + args[2] + "' or '" + args[3] + "' already exists!\n");
                    messages.addAll(getFactions(true, 0));
                    return messages;
                }
            } catch (SQLException e) {
                //This only happens when there's a serious issue with mysql or the connection to it
                messages.add("**WARNING FACTION NOT ADDED**");
                return messages;
            }
            messages.add("New faction added to BGS logging.\n");
            messages.addAll(getFactions(true, 0));
            return messages;
        } else {
            messages.add(BGS_FACTION_ADD_HELP);
            return messages;
        }
    }

    static ArrayList<String> setFactionVisibility(boolean show, String faction, String system) {
        //bgs faction, show|hide, faction, system
        ArrayList<String> messages = new ArrayList<>();
        int factionid = factionExists(faction, faction, 0);
        int systemid = BGSSystem.systemExists(system, system, 0, true);
        if (factionid > 0) {
            if (systemid > 0) {
                if (checkFactionInSystem(faction, system, true) > 0) {
                    Connection connect = new Connections().getConnection();
                    try {
                        PreparedStatement ps = connect.prepareStatement("UPDATE bgs_system_faction SET f_hidden = ? WHERE factionid = ? AND systemid = ?");
                        ps.setInt(1, show ? 0 : 1);
                        ps.setInt(2, factionid);
                        ps.setInt(3, systemid);

                        // if one row was altered
                        if (ps.executeUpdate() == 1) {
                            if (show) {
                                messages.add("BGS faction **VISIBLE**. Logging possible for this '" + getFactionFullname(factionid) + "' in '" + BGSSystem.getSystemFullname(systemid) + "'.\n");
                                messages.addAll(getFactions(true, systemid));
                                return messages;
                            }
                            else {
                                messages.add("BGS faction **HIDDEN** Logging no longer possible for '" + getFactionFullname(factionid) + "' in '" + BGSSystem.getSystemFullname(systemid) + "'.\n");
                                messages.addAll(getFactions(true, systemid));
                                return messages;
                            }
                        } else { // if no row was altered the system wasn't found
                            messages.add("**WARNING FACTION VISIBILITY NOT CHANGED**\nSystem '" + system + "' not found.\n");
                            messages.add(BGSSystem.getSystems(true));
                            return messages;
                        }
                    } catch (SQLException e) {
                        //This only happens when there's a serious issue with mysql or the connection to it
                        messages.add("**WARNING SYSTEM VISIBILITY NOT CHANGED**");
                        return messages;
                    }
                } else {
                    messages.add("**WARNING FACTION '" + getFactionFullname(factionid) + "' IS NOT ASSIGNED TO '" + BGSSystem.getSystemFullname(systemid) + "'**\nPlease select from the following\n");
                    messages.addAll(getFactions(true, systemid));
                    return messages;
                }

            } else {
                messages.add("**WARNING SYSTEM '" + system + "' DOES NOT EXIST**\nPlease select from the following\n" + BGSSystem.getSystems(true));
                return messages;
            }
        } else {
            messages.add("**WARNING FACTION '" + faction + "' DOES NOT EXIST**\nPlease select from the following\n");
            messages.addAll(getFactions(true, 0));
            return messages;
        }
    }

    static ArrayList<String> getFactions(boolean admin, int systemid) {
        String message = "";
        ArrayList<String> messages = new ArrayList<>();
        try {
            // show hidden factions in italics for admins
            Connection connect = new Connections().getConnection();
            if (admin) {
                PreparedStatement ps;
                if (systemid == -1) { // list all factions in each system
                    ps = connect.prepareStatement("SELECT systemid FROM bgs_system ORDER BY s_fullname ASC");
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        ps = connect.prepareStatement("SELECT * FROM bgs_faction f LEFT JOIN bgs_system_faction sf ON f.factionid = sf.factionid WHERE sf.systemid = ? ORDER BY f_fullname ASC");
                        ps.setInt(1, rs.getInt("systemid"));
                        ResultSet rs1 = ps.executeQuery();
                        message = "**" + BGS.getRows(rs1) + " Factions assigned to " + BGSSystem.getSystemFullname(rs.getInt("systemid")) + "**\n";
                        message += "```ID   | Short | Full\n";
                        if (!rs1.isBeforeFirst()) {
                            message += "No Factions";
                        }
                        while (rs1.next()) {
                            if (rs1.getString("f_hidden").equals("1")) {
                                message += String.format("%1$-4s | %2$-5s | %3$s (hidden faction)\n", rs1.getString("factionid"), rs1.getString("f_shortname"), rs1.getString("f_fullname"));
                            } else {
                                message += String.format("%1$-4s | %2$-5s | %3$s\n", rs1.getString("factionid"), rs1.getString("f_shortname"), rs1.getString("f_fullname"));
                            }
                        }
                        message += "```\n";
                        messages.add(message);
                    }
                } else {
                    if (systemid > 0) { // list factions in one system
                        ps = connect.prepareStatement("SELECT * FROM bgs_faction f LEFT JOIN bgs_system_faction sf ON f.factionid = sf.factionid WHERE sf.systemid = ? ORDER BY f_fullname ASC");
                        ps.setInt(1, systemid);
                    } else { // list factions
                        ps = connect.prepareStatement("SELECT *, 0 AS f_hidden FROM bgs_faction ORDER BY f_fullname ASC");
                    }
                    ResultSet rs = ps.executeQuery();
                    if (systemid > 0) {
                        message += "**" + BGS.getRows(rs) + " Factions assigned to " + BGSSystem.getSystemFullname(systemid) + "**\n";
                    }
                    message += "```ID   | Short | Full\n";
                    if (!rs.isBeforeFirst()) {
                        message += "No Factions";
                    }
                    while (rs.next()) {
                        if (message.length()> 1800) {
                            message += "```";
                            messages.add(message);
                            message = "```";
                        }
                        if (rs.getString("f_hidden").equals("1")) {
                            message += String.format("%1$-4s | %2$-5s | %3$s (hidden faction)\n", rs.getString("factionid"), rs.getString("f_shortname"), rs.getString("f_fullname"));
                        } else {
                            message += String.format("%1$-4s | %2$-5s | %3$s\n", rs.getString("factionid"), rs.getString("f_shortname"), rs.getString("f_fullname"));
                        }
                    }
                    message += "```\n";
                    messages.add(message);
                }

            } else { // just show live systems
                message = "```Short | Full\n";
                PreparedStatement ps = connect.prepareStatement("SELECT * FROM bgs_faction WHERE f_hidden = 0 ORDER BY f_fullname ASC");
                ResultSet rs = ps.executeQuery();
                if (!rs.isBeforeFirst()) {
                    message += "No Factions";
                }
                while (rs.next()) {
                    message += String.format("%1$-6s| %2$s\n", rs.getString("f_shortname"), rs.getString("f_fullname"));
                }
                message += "```\n";
                messages.add(message);
            }
        } catch (SQLException e) {
            LogUtil.logErr(e);
        }
        return messages;
    }
}
