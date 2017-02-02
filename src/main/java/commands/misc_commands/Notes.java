package commands.misc_commands;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import commands.GuildCommand;
import commands.PMCommand;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import provider.Connections;
import provider.DiscordInfo;

public class Notes implements PMCommand, GuildCommand{
	private static Connection connect;
	
	private static void connect() {
		if (connect == null)
			connect = new Connections().getConnection();
	}

	public static String get (String name, String id) {
		connect();
		
		try {
			PreparedStatement ps = connect.prepareStatement("SELECT content FROM notes WHERE (is_public = 1 OR authorid = ?) AND name = ?");
			ps.setString(1, id);
			ps.setString(2, name);
			ResultSet rs = ps.executeQuery();
			
			if (rs.next())
				return rs.getString("content");
		} catch (SQLException e) {
			e.printStackTrace();
			return "$Error";
		}
		
		return null;
	}
	
	private static boolean add(String name, String id, String content, boolean isPublic) {
		connect();
		
		try {
			if (get (name, id) != null)
				return false;
			
			PreparedStatement ps = connect.prepareStatement("INSERT INTO notes (authorid, name, is_public, content) VALUES (?, ?, ?, ?)");
			ps.setString (1, id);
			ps.setString (2, name);
			ps.setBoolean(3, isPublic);
			ps.setString (4, content);
			
			if (ps.executeUpdate() == 1)
				return true;
						
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	private static boolean edit(String name, String id, String content, boolean canEditPublic) {
		connect();
		
		try {
			if (get (name, id) == null)
				return false;
			
			PreparedStatement ps = connect.prepareStatement("UPDATE notes SET content = ? WHERE (is_public <= ? AND authorid = ?) AND name = ?");
			ps.setString (1, content);
			ps.setBoolean(2, canEditPublic);
			ps.setString (3, id);
			ps.setString (4, name);
			
			if (ps.executeUpdate() == 1)
				return true;
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	private static boolean delete(String name, String id, boolean canDelPublic) {
		connect();
		
		try {
			if (get (name, id) == null)
				return false;
			
			PreparedStatement ps = connect.prepareStatement("DELETE FROM notes WHERE (is_public <= ? AND authorid = ?) AND name = ?");
			ps.setBoolean(1, canDelPublic);
			ps.setString (2, id);
			ps.setString (3, name);
			
			if (ps.executeUpdate() == 1)
				return true;
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return false;
	}

	@Override
	public void runCommand(PrivateMessageReceivedEvent event, String[] args) {
		//TODO add in pm functionality
	}

	@Override
	public void runCommand(GuildMessageReceivedEvent event, String[] args) {
		if (args.length == 1) {
			String response = Notes.get(args[0], event.getAuthor().getId());
			if (response == null)
				event.getChannel().sendMessage("Sorry, couldn't find that note for you").queue();
			else
				event.getChannel().sendMessage(response).queue();
		}
		else if (args.length > 1) {
			boolean hasRights = (DiscordInfo.isOwner(event) || DiscordInfo.isAdmin(event));

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

				if (Notes.add(args[1], event.getAuthor().getId(), args[2], (isPublic && hasRights)))
					event.getChannel().sendMessage("Saved").queue();
				else
					event.getChannel().sendMessage("Error, something went wrong. Maybe there's already a note with that name?").queue();
			} else if (args[0].equalsIgnoreCase("edit")) {
				if (args.length < 3) {
					event.getChannel().sendMessage("Seems like you forgot to put the name or the new content in your message").queue();
					return;
				}

				if (Notes.edit(args[1], event.getAuthor().getId(), args[2], hasRights))
					event.getChannel().sendMessage("Edited").queue();
				else
					event.getChannel().sendMessage("No note with that name found or you aren't allowed to edit the ones I did find").queue();
			} else if (args[0].equalsIgnoreCase("del")) {
				if (args.length < 2) {
					event.getChannel().sendMessage("Seems like you forgot to put the name or the new content in your message").queue();
					return;
				}

				if (Notes.delete(args[1], event.getAuthor().getId(), hasRights))
					event.getChannel().sendMessage("Deleted").queue();
				else
					event.getChannel().sendMessage("No note with that name found or you aren't allowed to edit the ones I did find").queue();
			}
		}
	}

	@Override
	public String getHelp(GuildMessageReceivedEvent event) {
		return "Relatively complicated. Refer to the guide linked below";
	}
}
