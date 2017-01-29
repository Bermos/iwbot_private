package iw_core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.dv8tion.jda.core.MessageHistory;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.managers.GuildManager;
import net.dv8tion.jda.core.managers.PermOverrideManagerUpdatable;
import provider.DiscordInfo;

public class Missions {
	private static List<MissionChannel> missionChannels = new ArrayList<>();

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
		//noinspection ConstantConditions
		getChannel(textChanID).next();
	}
	
	public static void getList(String textChanID) {
		//noinspection ConstantConditions
		getChannel(textChanID).print(true);
	}
	
	public static void create(String name, GuildManager guildManager, Member explorer) {
		Channel missionChannel;
		Guild guild = guildManager.getGuild();
		Role iwRole = guild.getRoleById("143171790225670145");
		Role explorerRole = guild.getRoleById("143403360081543168");
		Role everyoneRole = guild.getPublicRole();
		Role moderatorRole = guild.getRoleById(DiscordInfo.getAdminRoleIDs().get(0));
		
		String channelName = "mission_" + name;
		String explorerName = "*edit*";

		missionChannel = guild.getController().createTextChannel(channelName).complete();

		// Set permissions for moderators
		PermOverrideManagerUpdatable permManager = missionChannel.createPermissionOverride(moderatorRole).complete().getManagerUpdatable();
		permManager.grant(Permission.MESSAGE_READ)
			.grant(Permission.MESSAGE_WRITE)
			.grant(Permission.MESSAGE_MENTION_EVERYONE)
			.grant(Permission.MESSAGE_HISTORY)
			.grant(Permission.MANAGE_PERMISSIONS)
			.grant(Permission.MANAGE_CHANNEL)
			.update().queue();

		// Set permissions for iwmembers
		permManager = missionChannel.createPermissionOverride(iwRole).complete().getManagerUpdatable();
		permManager.grant(Permission.MESSAGE_READ)
			.grant(Permission.MESSAGE_WRITE)
			.grant(Permission.MESSAGE_MENTION_EVERYONE)
			.grant(Permission.MESSAGE_HISTORY)
			.update().queue();

		// Set permissions for @everyone
		permManager = missionChannel.createPermissionOverride(everyoneRole).complete().getManagerUpdatable();
		permManager.deny (Permission.MESSAGE_READ)
			.deny (Permission.MESSAGE_WRITE)
			.update().queue();

		// In case the explorer is mentioned in the message...
		if (explorer != null) {
			// Set permissions for the explorer
			permManager = missionChannel.createPermissionOverride(explorer).complete().getManagerUpdatable();
			permManager.grant(Permission.MESSAGE_READ)
				.grant(Permission.MESSAGE_WRITE)
				.grant(Permission.MESSAGE_MENTION_EVERYONE)
				.grant(Permission.MESSAGE_HISTORY)
				.update().queue();

			// Give the explorer role to the explorer
			guildManager.getGuild().getController().addRolesToMember(explorer, explorerRole).queue();

			explorerName = explorer.getEffectiveName();
		}
		
		String topic = "__**Explorer:**__\n"
							+ "CMDR " + explorerName + "\n"
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
		
		missionChannel.getManager().setTopic(topic).queue();
	}

	public static void archive(TextChannel channel, String id) {
		MissionChannel mChannel = getChannel(channel.getId());
		if (mChannel == null) {
			mChannel = new MissionChannel(channel.getId(), channel.getGuild());
			missionChannels.add(mChannel);
		}
		if (!mChannel.isPrimed(id))
			return;
		
		Role associatedRole = null;
		for (Role role : channel.getGuild().getRoles()) {
			if (role.getName().equalsIgnoreCase(channel.getName()))
				associatedRole = role;
		}
		
		List<String> lines = new ArrayList<>();
		List<Message> history = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			List<Message> tempHist = new MessageHistory(channel).retrievePast(100).complete();
			history.addAll(tempHist);
			channel.deleteMessages(tempHist);
			if (tempHist.size() < 100)
				break;
		}
		Collections.reverse(history);
		lines.add("*****************START OF CHANNEL '" + channel.getName() + "' LOG*****************");
		for (Message message : history) {
			String timestamp = message.getCreationTime() == null ? "[?]" : "[" + message.getCreationTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "]";
			String author	 = message.getAuthor() 	== null ? "?" 	: message.getAuthor().getName();
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
		try {
			channel.getJDA().getTextChannelById(DiscordInfo.getAdminChanID()).sendFile(file, null);
		} catch (IOException e) {
			e.printStackTrace();
		}

		channel.delete().queue();
		
		if (associatedRole != null)
			associatedRole.delete().queue();
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
