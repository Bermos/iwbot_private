package iw_core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Role;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.ReadyEvent;
import net.dv8tion.jda.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.events.user.UserAvatarUpdateEvent;
import net.dv8tion.jda.events.user.UserNameUpdateEvent;
import net.dv8tion.jda.events.user.UserOnlineStatusUpdateEvent;
import provider.Connections;

public class Users {
	private static Connection connect;
	
	public Users() {
		Users.connect = new Connections().getConnection();
	}
	
	public static void sync(ReadyEvent event) {
		Guild guild = event.getJDA().getGuildById("142749481530556416");
		System.out.println("[MYSQL] Sync started.");
		System.out.println("[MYSQL] # Users found: " + guild.getUsers().size());
		int delUsers = 0, iwUsers = 0, leftUsers = 0, newUsers = 0;
		
		List<User> userListDS = new ArrayList<User>(guild.getUsers());
		try {
			PreparedStatement ps = connect.prepareStatement("SELECT * FROM user");
			ResultSet rs = ps.executeQuery();
			PreparedStatement us = connect.prepareStatement("UPDATE user SET onlinestatus = ?, ppurl = ?, role = ? WHERE iduser = ?");
			PreparedStatement is = connect.prepareStatement("INSERT INTO user(iduser, username, role, onlinestatus, lastonline, added, ppurl) VALUES (?, ?, ?, ?, ?, ?, ?)");
			
			while (rs.next()) {
				User user = guild.getJDA().getUserById(Long.toString(rs.getLong("iduser")));
				if (user == null) {
					us.setInt	 (1, 3);
					us.setNull	 (2, Types.VARCHAR);
					us.setString (3, "No membership");
					us.setLong	 (4, rs.getLong("iduser"));
					us.addBatch();
					delUsers++;
				}
				else if (userListDS.contains(user)) {
					String role = "No membership"; if (!guild.getRolesForUser(user).isEmpty()) { role = guild.getRolesForUser(user).get(0).getName();}
					us.setInt	 (1, user.getOnlineStatus().ordinal());
					us.setString (2, user.getAvatarUrl());
					us.setString (3, role);
					us.setLong	 (4, rs.getLong("iduser"));
					us.addBatch();
					iwUsers++;
				}
				else {
					us.setInt	 (1, 3);
					us.setString (2, user.getAvatarUrl());
					us.setString (3, "No membership");
					us.setLong	 (4, rs.getLong("iduser"));
					us.addBatch();
					leftUsers++;
				}
				userListDS.remove(user);
			}
			for (User user : userListDS) {
				String role = "No membership"; if (!guild.getRolesForUser(user).isEmpty()) { role = guild.getRolesForUser(user).get(0).getName();}
				is.setLong		(1, Long.parseLong((user).getId().replaceAll("[^0-9]", "")));
				is.setString	(2, (user.getUsername()));
				is.setString	(3, role);
				is.setInt		(4, (user.getOnlineStatus().ordinal()));
				is.setTimestamp	(5, new Timestamp(System.currentTimeMillis()));
				is.setTimestamp	(6, new Timestamp(System.currentTimeMillis()));
				is.setString	(7, user.getAvatarUrl());
				is.addBatch();
				newUsers++;
			}
			
			us.executeBatch();
			is.executeBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		System.out.println("[MYSQL] Sync finished.\n"
						 + "[Revmoved] " + leftUsers + "\n"
						 + "[Added]    " + newUsers  + "\n"
						 + "[Stayed]   " + iwUsers   + "\n"
						 + "[Deleted]  " + delUsers);
	}

	public static void joined(GuildMemberJoinEvent event) {
		System.out.printf("[%s] %s has joined the guild.\n", event.getGuild().getName(), event.getUser().getUsername());
		User user = event.getUser();
		String rName = event.getGuild().getRolesForUser(user).isEmpty() ? "none" : event.getGuild().getRolesForUser(user).get(0).getName();
		
		try {
			PreparedStatement ps = connect.prepareStatement("INSERT INTO user (iduser, username, role, onlinestatus, added, ppurl) VALUES (?, ?, ?, ?, ?, ?)");
			ps.setLong		(1, Long.parseLong((user).getId().replaceAll("[^0-9]", "")));
			ps.setString	(2, user.getUsername());
			ps.setString	(3, rName);
			ps.setInt		(4, user.getOnlineStatus().ordinal());
			ps.setTimestamp	(5, new Timestamp(System.currentTimeMillis()));
			ps.setString	(6, user.getAvatarUrl());
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void left(GuildMemberLeaveEvent event) {
		System.out.printf("[%s] %s has left the guild.\n", event.getGuild().getName(), event.getUser().getUsername());
		
		try {
			PreparedStatement ps = connect.prepareStatement("UPDATE iwmembers.user SET onlinestatus = ?, role = ?, password = default, sessionkey = default, salt = default WHERE iduser = ?");
			ps.setInt	(1, 3);
			ps.setString (2, "none");
			ps.setLong	(3, Long.parseLong(event.getUser().getId().replaceAll("[^0-9]", "")));
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void roleUpdate(GuildMemberRoleAddEvent event) {
		System.out.printf("[Role Added] %s: %s\n", event.getUser().getUsername(), event.getRoles().get(0).getName());
		roleUpdate(event.getRoles(), event.getGuild(), event.getUser());
	}
	
	public static void roleUpdate(GuildMemberRoleRemoveEvent event) {
		System.out.printf("[Role Removed] %s: %s\n", event.getUser().getUsername(), event.getRoles().get(0).getName());
		roleUpdate(event.getRoles(), event.getGuild(), event.getUser());
		
	}
	
	private static void roleUpdate(List<Role> roles, Guild guild, User user) {
		String rName = guild.getRolesForUser(user).isEmpty() ? "none" : guild.getRolesForUser(user).get(0).getName();
		System.out.printf("[Role Display] %s: %s\n", user.getUsername(), rName);
		
		try {
			PreparedStatement ps = connect.prepareStatement("UPDATE iwmembers.user SET role = ? WHERE iduser = ?");
			ps.setString	(1, rName);
			ps.setLong		(2, Long.parseLong(user.getId().replaceAll("[^0-9]", "")));
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void setOnlineStatus(UserOnlineStatusUpdateEvent event) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		System.out.printf("[" + sdf.format(new Date()) + "][Online Status] %s: %s\n", event.getUser().getUsername(), event.getUser().getOnlineStatus().name());
		try {
			if(event.getUser().getOnlineStatus().name().equals("AWAY")) {
				PreparedStatement ps = connect.prepareStatement("UPDATE iwmembers.user SET onlinestatus = ? WHERE iduser = ?");
				ps.setInt	(1, event.getUser().getOnlineStatus().ordinal());
				ps.setLong	(2, Long.parseLong(event.getUser().getId().replaceAll("[^0-9]", "")));
				ps.executeUpdate();
			} else {
				PreparedStatement ps = connect.prepareStatement("UPDATE iwmembers.user SET onlinestatus = ?, lastonline = ? WHERE iduser = ?");
				ps.setInt		(1, event.getUser().getOnlineStatus().ordinal());
				ps.setTimestamp	(2, new Timestamp(System.currentTimeMillis()));
				ps.setLong		(3, Long.parseLong(event.getUser().getId().replaceAll("[^0-9]", "")));
				ps.executeUpdate	();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void avatarUpdate(UserAvatarUpdateEvent event) {
		try {
			PreparedStatement ps = connect.prepareStatement("UPDATE iwmembers.user SET ppurl = ? WHERE iduser = ?");
			ps.setString	(1, event.getUser().getAvatarUrl());
			ps.setLong	(2, Long.parseLong(event.getUser().getId().replaceAll("[^0-9]", "")));
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void nameUpdate(UserNameUpdateEvent event) {
		System.out.printf("[Name Update] %s changed to %s\n", event.getPreviousUsername(), event.getUser().getUsername());
		
		try {
			PreparedStatement ps = connect.prepareStatement("UPDATE iwmembers.user SET username = ? WHERE iduser = ?");
			ps.setString	(1, event.getUser().getUsername());
			ps.setLong	(2, Long.parseLong(event.getUser().getId().replaceAll("[^0-9]", "")));
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}