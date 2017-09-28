package commands.ed_commands;

import commands.GuildCommand;
import commands.PMCommand;
import core.JDAUtil;
import core.Listener;
import core.LogUtil;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import provider.Connections;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This command class is for handling
 * the KOS list that is specific for
 * every guild.
 */
public class KOS implements GuildCommand, PMCommand {
    @Override
    public void runCommand(Listener listener, PrivateMessageReceivedEvent event, String[] args) {
        String message = "Your command seems invalid, maybe this helps:\n" + getHelp(null);
        if (args.length == 1) {
            message = getForAllGuilds(args[0], event.getAuthor());
        }

        event.getChannel().sendMessage(message).queue();
    }

    @Override
    public void runCommand(Listener listener, GuildMessageReceivedEvent event, String[] args) {
        String message = "Your command seems invalid, maybe this helps:\n" + getHelp(null);
        if (args.length == 1) {
            message = getForAllGuilds(args[0], event.getAuthor());

        } else if (!JDAUtil.isAuthorized(event)) {
            event.getChannel().sendMessage("Sorry, you aren't authorized to do this.").queue();
            return;

        } else if (args.length == 2 && (args[1].equalsIgnoreCase("remove") || args[1].equalsIgnoreCase("delete"))) {
            message = remove(args[1], event.getGuild().getId()) ?
                    "Successfully removed " + args[1] :
                    "Couldn't remove " + args[1] + ". Make sure he is spelled correctly.";

        } else if (args.length >= 3 && args[1].equals("add")) {
            String reason = "";
            for (int i = 2; i < args.length; i++)
                reason += ", " + args[i];

            message = add(args[1], reason.replaceFirst(", ", ""), event.getGuild().getId()) ?
            "Added " + args[1] + " to KOS list" :
            "Couldn't add " + args[1] + " to KOS list, is he already there?";

        }

        event.getChannel().sendMessage(message).queue();
    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        return "<name> -> Yes/No | <add>, <name>, <reason> - to add | <remove>, <name> - to delete";
    }

    private static Connections cons = new Connections();

    private String getForAllGuilds(String arg, User author) {
        String output = "";
        for (Guild guild : Listener.jda.getGuilds()) {
            if (guild.isMember(author)) {
                output += ", " + get(arg, guild.getId());
            }
        }

        return output.replaceFirst(", ", "");
    }

    public static boolean add(String username, String reason, String guildid) {
        if(!get(username, guildid).equals("No"))
            return false;
        try (PreparedStatement ps = cons.getConnection().prepareStatement("INSERT INTO kos (username, reason, guildid) VALUES (?, ?, ?)")) {
            ps.setString(1, username);
            ps.setString(2, reason == null ? "" : reason);
            ps.setString(3, guildid);

            if (ps.executeUpdate() > 0) {
                return true;
            }
        } catch (SQLException e) {
            LogUtil.logErr(e);
        }
        return false;
    }

    public static String get(String username, String guildid) {
        try (PreparedStatement ps = cons.getConnection().prepareStatement("SELECT * FROM kos WHERE username = ? AND guildid = ?")) {
            ps.setString(1, username);
            ps.setString(2, guildid);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("reason").equals("") ? "Yes" : "Yes, " + rs.getString("reason");
            }
        } catch (SQLException e) {
            LogUtil.logErr(e);
        }
        return "No";
    }

    public static boolean remove(String username, String guildid) {
        try (PreparedStatement ps = cons.getConnection().prepareStatement("DELETE FROM kos WHERE username = ? AND guildid = ?")) {
            ps.setString(1, username);
            ps.setString(2, guildid);

            if (ps.executeUpdate() > 0) {
                return true;
            }
        } catch (SQLException e) {
            LogUtil.logErr(e);
        }
        return false;
    }
}
