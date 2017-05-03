package commands.misc_commands;


import commands.GuildCommand;
import commands.PMCommand;
import core.JDAUtil;
import core.Listener;
import core.LogUtil;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import provider.Connections;
import provider.DataProvider;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Notes implements PMCommand, GuildCommand{
	private static Connections connections;

	@Override
	public void runCommand(PrivateMessageReceivedEvent event, String[] args) {
		String userId = event.getAuthor().getId();
		String effName = event.getAuthor().getName();
		String[] roleIds = {};

		event.getChannel().sendMessage(notes(userId, effName, roleIds, args)).queue();
	}

	@Override
	public void runCommand(GuildMessageReceivedEvent event, String[] args) {
		String userId = event.getAuthor().getId();
		String effName = event.getMember().getEffectiveName();
		String[] roleIds = JDAUtil.getRoleIdStrings(event.getMember());

		event.getChannel().sendMessage(notes(userId, effName, roleIds, args)).queue();
	}

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        return "Relatively complicated. Refer to the guide linked below";
    }

	String notes(String authorId, String effectiveName, String[] roleIds, String[] args) {

		if (args.length == 1) {
			String response = Notes.get(args[0], authorId);
			if (response == null)
				return "Sorry, couldn't find that note for you";
			else {
				String name = effectiveName.lastIndexOf("s") == effectiveName.length() ? effectiveName : effectiveName + "'s";
				return name + " note:\n" + response;
			}
		}

		else if (args.length > 1) {
			boolean hasRights = DataProvider.isAdmin(roleIds);

			if (args[0].equalsIgnoreCase("add")) {
				boolean isPublic = false;
                if (args[2].equals("1") || args[2].equalsIgnoreCase("public")) {
					isPublic = true;
					args[2] = args[3];
					if (args.length > 4) {
						for (int i = 4; i < args.length; i++)
							args[2] = String.join(", ", args[2], args[i]);
					}
				} else {
					if (args.length > 3) {
						for (int i = 3; i < args.length; i++)
							args[2] = String.join(", ", args[2], args[i]);
					}
				}

				if (isPublic && !hasRights)
                    return "[Error] You are not authorised to make public notes.";
				if (Notes.add(args[1], authorId, args[2], (isPublic)))
					return "Saved";

				return "Error, something went wrong. Maybe there's already a note with that name?";
			} else if (args[0].equalsIgnoreCase("edit")) {
				if (args.length < 3) {
					return "Seems like you forgot to put the name or the new content in your message.";
				}

				if (Notes.edit(args[1], authorId, args[2], hasRights))
					return "Edited";
				else
					return "No note with that name found or you aren't allowed to edit the one I did find.";
			} else if (args[0].equalsIgnoreCase("del")) {
				if (Notes.delete(args[1], authorId, hasRights))
					return "Deleted";
				else
					return "No note with that name found or you aren't allowed to delete the one I did find.";
			}
		}

		return "'/note' help : [add|edit|del], [public], notes name, notes content";
	}
	
	private static void connect() {
		if (connections == null)
			connections = new Connections();
	}

	private static String get (String name, String id) {
		connect();
		
		try {
			PreparedStatement ps = connections.getConnection().prepareStatement("SELECT content FROM notes WHERE (is_public = 1 OR authorid = ?) AND name = ?");
			ps.setString(1, id);
			ps.setString(2, name);
			ResultSet rs = ps.executeQuery();
			
			if (rs.next())
				return rs.getString("content");
		} catch (SQLException e) {
			LogUtil.logErr(e);
			return "$Error";
		}
		
		return null;
	}
	
	private static boolean add(String name, String id, String content, boolean isPublic) {
		connect();
		
		try {
			if ((get (name, id) != null) && !Listener.isTest)
				return false;
			
			PreparedStatement ps = connections.getConnection().prepareStatement("INSERT INTO notes (authorid, name, is_public, content) VALUES (?, ?, ?, ?)");
			ps.setString (1, id);
			ps.setString (2, name);
			ps.setBoolean(3, isPublic);
			ps.setString (4, content);
			
			if (ps.executeUpdate() == 1)
				return true;
						
		} catch (SQLException e) {
			LogUtil.logErr(e);
		}
		
		return false;
	}
	
	private static boolean edit(String name, String id, String content, boolean canEditPublic) {
		connect();
		
		try {
			if (get (name, id) == null)
				return false;
			
			PreparedStatement ps = connections.getConnection().prepareStatement("UPDATE notes SET content = ? WHERE (is_public <= ? AND authorid = ?) AND name = ?");
			ps.setString (1, content);
			ps.setBoolean(2, canEditPublic);
			ps.setString (3, id);
			ps.setString (4, name);
			
			if (ps.executeUpdate() == 1)
				return true;
			
		} catch (SQLException e) {
			LogUtil.logErr(e);
		}
		
		return false;
	}
	
	private static boolean delete(String name, String id, boolean canDelPublic) {
		connect();
		
		try {
			if (get (name, id) == null)
				return false;
			
			PreparedStatement ps = connections.getConnection().prepareStatement("DELETE FROM notes WHERE (is_public <= ? AND authorid = ?) AND name = ?");
			ps.setBoolean(1, canDelPublic);
			ps.setString (2, id);
			ps.setString (3, name);
			
			if (ps.executeUpdate() == 1)
				return true;
			
		} catch (SQLException e) {
			LogUtil.logErr(e);
		}
		
		return false;
	}
}
