package iw_core;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.core.events.user.UserAvatarUpdateEvent;
import net.dv8tion.jda.core.events.user.UserNameUpdateEvent;
import net.dv8tion.jda.core.events.user.UserOnlineStatusUpdateEvent;
import provider.Connections;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Users {
	private static Connection connect;
	
	public Users() {
		Users.connect = new Connections().getConnection();
	}
	
	public static void sync(ReadyEvent event) {
		Guild guild = event.getJDA().getGuildById("142749481530556416");
		System.out.println("[MYSQL] Sync started.");
		System.out.println("[MYSQL] # Users found: " + guild.getMembers().size());
		int delUsers = 0, iwUsers = 0, leftUsers = 0, newUsers = 0;
		
		List<Member> memberListDS = new ArrayList<>(guild.getMembers());
		try {
			PreparedStatement ps = connect.prepareStatement("SELECT * FROM user");
			ResultSet rs = ps.executeQuery();
			PreparedStatement us = connect.prepareStatement("UPDATE user SET onlinestatus = ?, ppurl = ?, role = ? WHERE iduser = ?");
			PreparedStatement is = connect.prepareStatement("INSERT INTO user(iduser, username, role, onlinestatus, lastonline, added, ppurl) VALUES (?, ?, ?, ?, ?, ?, ?)");
			
			while (rs.next()) {
				User user = guild.getJDA().getUserById(Long.toString(rs.getLong("iduser")));
				if (user == null) {
					us.setInt	 (1, 4);
					us.setNull	 (2, Types.VARCHAR);
					us.setString (3, "No membership");
					us.setLong	 (4, rs.getLong("iduser"));
					us.addBatch();
					delUsers++;
					continue;
				}
				else if (memberListDS.contains(guild.getMember(user))) {
					String role = "No membership"; if (!guild.getMember(user).getRoles().isEmpty()) { role = guild.getMember(user).getRoles().get(0).getName();}
					us.setInt	 (1, guild.getMember(user).getOnlineStatus().ordinal());
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
				memberListDS.remove(guild.getMember(user));
			}
			for (Member member : memberListDS) {
				User user = member.getUser();
				String role = "No membership"; if (!member.getRoles().isEmpty()) { role = member.getRoles().get(0).getName();}
				is.setLong		(1, Long.parseLong(member.getUser().getId().replaceAll("[^0-9]", "")));
				is.setString	(2, user.getName());
				is.setString	(3, role);
				is.setInt		(4, member.getOnlineStatus().ordinal());
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
						 + "[Removed] " + leftUsers + "\n"
						 + "[Added]    " + newUsers  + "\n"
						 + "[Stayed]   " + iwUsers   + "\n"
						 + "[Deleted]  " + delUsers);
	}

	public static void joined(GuildMemberJoinEvent event) {
		System.out.printf("[%s] %s has joined the guild.\n", event.getGuild().getName(), event.getMember().getNickname());
		Member member = event.getMember();
		String rName = member.getRoles().isEmpty() ? "none" : member.getRoles().get(0).getName();
		
		try {
			PreparedStatement ps = connect.prepareStatement("INSERT INTO user (iduser, username, role, onlinestatus, added, ppurl) VALUES (?, ?, ?, ?, ?, ?)");
			ps.setLong		(1, Long.parseLong(member.getUser().getId().replaceAll("[^0-9]", "")));
			ps.setString	(2, member.getEffectiveName());
			ps.setString	(3, rName);
			ps.setInt		(4, member.getOnlineStatus().ordinal());
			ps.setTimestamp	(5, new Timestamp(System.currentTimeMillis()));
			ps.setString	(6, member.getUser().getAvatarUrl());
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void left(GuildMemberLeaveEvent event) {
		System.out.printf("[%s] %s has left the guild.\n", event.getGuild().getName(), event.getMember().getNickname());
		
		try {
			PreparedStatement ps = connect.prepareStatement("UPDATE iwmembers.user SET onlinestatus = ?, role = ?, password = default, sessionkey = default, salt = default WHERE iduser = ?");
			ps.setInt	(1, 3);
			ps.setString (2, "none");
			ps.setLong	(3, Long.parseLong(event.getMember().getUser().getId().replaceAll("[^0-9]", "")));
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void roleUpdate(GuildMemberRoleAddEvent event) {
		System.out.printf("[Role Added] %s: %s\n", event.getMember().getNickname(), event.getRoles().get(0).getName());
		roleUpdate(event.getMember());
	}
	
	public static void roleUpdate(GuildMemberRoleRemoveEvent event) {
		System.out.printf("[Role Removed] %s: %s\n", event.getMember().getNickname(), event.getRoles().get(0).getName());
		roleUpdate(event.getMember());
		
	}
	
	private static void roleUpdate(Member member) {
		String rName = member.getRoles().isEmpty() ? "none" : member.getRoles().get(0).getName();
		System.out.printf("[Role Display] %s: %s\n", member.getNickname(), rName);
		
		try {
			PreparedStatement ps = connect.prepareStatement("UPDATE iwmembers.user SET role = ? WHERE iduser = ?");
			ps.setString	(1, rName);
			ps.setLong		(2, Long.parseLong(member.getUser().getId().replaceAll("[^0-9]", "")));
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void setOnlineStatus(UserOnlineStatusUpdateEvent event) {
		try {
			if(event.getGuild().getMember(event.getUser()).getOnlineStatus().name().equals("AWAY")) {
				PreparedStatement ps = connect.prepareStatement("UPDATE iwmembers.user SET onlinestatus = ? WHERE iduser = ?");
				ps.setInt	(1, event.getGuild().getMember(event.getUser()).getOnlineStatus().ordinal());
				ps.setLong	(2, Long.parseLong(event.getUser().getId().replaceAll("[^0-9]", "")));
				ps.executeUpdate();
			} else {
				PreparedStatement ps = connect.prepareStatement("UPDATE iwmembers.user SET onlinestatus = ?, lastonline = ? WHERE iduser = ?");
				ps.setInt		(1, event.getGuild().getMember(event.getUser()).getOnlineStatus().ordinal());
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
		System.out.printf("[Name Update] %s changed to %s\n", event.getOldName(), event.getUser().getName());
		
		try {
			PreparedStatement ps = connect.prepareStatement("UPDATE iwmembers.user SET username = ? WHERE iduser = ?");
			ps.setString	(1, event.getUser().getName());
			ps.setLong	(2, Long.parseLong(event.getUser().getId().replaceAll("[^0-9]", "")));
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}