package iw_core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.dv8tion.jda.MessageHistory;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.Role;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.managers.ChannelManager;
import net.dv8tion.jda.managers.GuildManager;
import net.dv8tion.jda.managers.PermissionOverrideManager;
import provider.DiscordInfo;

public class Missions {
	private static List<MissionChannel> missionChannels = new ArrayList<MissionChannel>();

	private static MissionChannel getChannel(String textChanID) {
		for (MissionChannel chan : missionChannels) {
			if (chan.getId().equals(textChanID))
				return chan;
		}
		
		return null;
	}
	
	public static void newList(TextChannel channel, String list) {
		MissionChannel missionChannel = getChannel(channel.getId());
		if (missionChannel == null) {
			missionChannel = new MissionChannel(channel.getId(), channel.getGuild());
			missionChannels.add(missionChannel);
		}
		
		missionChannel.add(list);
	}
	
	public static void nextListEntry(String textChanID) {
		getChannel(textChanID).next();
	}
	
	public static void getList(String textChanID) {
		getChannel(textChanID).print(true);
	}
	
	public static void create(String name, GuildManager guildManager, User explorer) {
		ChannelManager missionChannelManager = null;
		Role iwRole = guildManager.getGuild().getRoleById("143171790225670145");
		Role explorerRole = guildManager.getGuild().getRoleById("143403360081543168");
		Role everyoneRole = guildManager.getGuild().getPublicRole();
		Role moderatorRole = guildManager.getGuild().getRoleById(DiscordInfo.getAdminRoleIDs().get(0));
		
		String channelName = "mission_" + name;
		String explName = "*edit*";
		
		missionChannelManager = guildManager.getGuild().createTextChannel(channelName);
		PermissionOverrideManager permissionManager = missionChannelManager.getChannel().createPermissionOverride(moderatorRole);
		permissionManager.grant(Permission.MESSAGE_READ);
		permissionManager.grant(Permission.MESSAGE_WRITE);
		permissionManager.grant(Permission.MESSAGE_MENTION_EVERYONE);
		permissionManager.grant(Permission.MESSAGE_HISTORY);
		permissionManager.grant(Permission.MANAGE_PERMISSIONS);
		permissionManager.grant(Permission.MANAGE_CHANNEL);
		permissionManager.update();
		
		permissionManager = missionChannelManager.getChannel().createPermissionOverride(iwRole);
		permissionManager.grant(Permission.MESSAGE_READ);
		permissionManager.grant(Permission.MESSAGE_WRITE);
		permissionManager.grant(Permission.MESSAGE_MENTION_EVERYONE);
		permissionManager.grant(Permission.MESSAGE_HISTORY);
		permissionManager.update();
		
		permissionManager = missionChannelManager.getChannel().createPermissionOverride(everyoneRole);
		permissionManager.deny (Permission.MESSAGE_READ);
		permissionManager.deny (Permission.MESSAGE_WRITE);
		permissionManager.update();
		
		if (explorer != null) {
			permissionManager = missionChannelManager.getChannel().createPermissionOverride(explorer);
			permissionManager.grant(Permission.MESSAGE_READ);
			permissionManager.grant(Permission.MESSAGE_WRITE);
			permissionManager.grant(Permission.MESSAGE_MENTION_EVERYONE);
			permissionManager.grant(Permission.MESSAGE_HISTORY);
			permissionManager.update();
			
			guildManager.addRoleToUser(explorer, explorerRole);
			guildManager.update();
			
			if (guildManager.getGuild().getNicknameForUser(explorer) != null)
				explName = guildManager.getGuild().getNicknameForUser(explorer);
			else
				explName = explorer.getUsername();
		}
		
		String topic = "__**Explorer:**__\n"
							+ "CMDR " + explName + "\n"
							+ "Status: *edit*\n"
							+ "PP affiliation: *edit*\n"
							+ "\n"
							+ "__**Systems:**__\n"
							+ "RV: *edit*\n"
							+ "Dest: *edit*\n"
							+ "Dest Station: *edit*\n"
							+ "\n"
							+ "Prep time: *edit*\n"
							+ "T-0: *edit* UTC\n"
							+ "Mission duration: *edit*\n"
							+ "\n"
							+ "Alpha: *TBA*\n"
							+ "Bravo: *TBA*\n";
		
		missionChannelManager.setTopic(topic).update();
	}

	public static void archive(TextChannel channel, String id) {
		MissionChannel mChannel = getChannel(channel.getId());
		if (mChannel == null) {
			mChannel = new MissionChannel(channel.getId(), channel.getGuild());
			missionChannels.add(mChannel);
		}
		if (!mChannel.isPrimed(id))
			return;
		
		Role assoRole = null;
		for (Role role : channel.getGuild().getRoles()) {
			if (role.getName().equalsIgnoreCase(channel.getName()))
				assoRole = role;
		}
		
		List<String> lines = new ArrayList<String>();
		List<Message> history = new MessageHistory(channel).retrieveAll();
		Collections.reverse(history);
		lines.add("*****************START OF CHANNEL '" + channel.getName() + "' LOG*****************");
		for (Message message : history) {
			String timestamp = message.getTime() 	== null ? "[?]" : "[" + message.getTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "]";
			String author	 = message.getAuthor() 	== null ? "?" 	: message.getAuthor().getUsername();
			String content 	 = message.getContent() == null ? "?" 	: message.getContent();
			lines.add(timestamp + " " + author + ": " + content);
		}
		lines.add("*******************END OF CHANNEL '" + channel.getName() + "' LOG*****************");
		
		try {
			FileWriter writer = new FileWriter("./ChannelLogs/" + channel.getName() + ".txt"); 
			for(String line: lines) {
			  writer.write(line + "\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		File file = new File ("./ChannelLogs/" + channel.getName() + ".txt");
		channel.getJDA().getTextChannelById(DiscordInfo.getAdminChanID()).sendMessage(channel.getName() + " archived.");
		channel.getJDA().getTextChannelById(DiscordInfo.getAdminChanID()).sendFile(file, null);
		channel.getManager().delete();
		
		if (assoRole != null)
			assoRole.getManager().delete();
	}

	public static void archiveRequest(TextChannel channel, String id) {
		MissionChannel mChannel = getChannel(channel.getId());
		if (mChannel == null) {
			mChannel = new MissionChannel(channel.getId(), channel.getGuild());
			missionChannels.add(mChannel);
		}
		mChannel.primeForDelete(id);
	}
	
}
