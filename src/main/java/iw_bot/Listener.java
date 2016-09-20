package iw_bot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import iw_bot.Commands;
import iw_core.Channels;
import iw_core.Users;
import misc.DankMemes;
import misc.Reminder;
import misc.StatusGenerator;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.events.ReadyEvent;
import net.dv8tion.jda.events.channel.text.TextChannelUpdatePositionEvent;
import net.dv8tion.jda.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.events.user.UserAvatarUpdateEvent;
import net.dv8tion.jda.events.user.UserNameUpdateEvent;
import net.dv8tion.jda.events.user.UserOnlineStatusUpdateEvent;
import net.dv8tion.jda.hooks.ListenerAdapter;
import provider.Connections;
import provider.DiscordInfo;
import provider.Statistics;

public class Listener extends ListenerAdapter {
	private Commands commands;
	public static long startupTime;
	public static SimpleDateFormat sdf;
	public static final String VERSION_NUMBER = "2.3.1_28";
	
	public Listener() {
		this.commands = new Commands();
		sdf = new SimpleDateFormat("HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		DankMemes.update();
	}
	
	@Override
	public void onReady(ReadyEvent event) {
		System.out.println("[" + sdf.format(new Date()) + "][Info] Listener v" + VERSION_NUMBER + " ready!");
		System.out.println("[" + sdf.format(new Date()) + "][Info] Connected to:");
		for (Guild guild : event.getJDA().getGuilds()) {
			System.out.println("	" + guild.getName());
		}

		new Connections().getConnection();

		Statistics stats = Statistics.getInstance();
		stats.connect(event.getJDA());
		
		Listener.startupTime = new Date().getTime();
		new StatusGenerator(event.getJDA().getAccountManager());
		
		new Users();
		Users.sync(event);

		new Reminder().startChecks(event.getJDA());
	}
	
	@Override
	public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
		System.out.printf("[" + sdf.format(new Date()) + "][PM][%s] %s: %s\n",
											event.getChannel().getUser().getUsername(),
											event.getAuthor().getUsername(),
											event.getMessage().getContent());
		
		//Check for command
				if (event.getMessage().getContent().startsWith("/") && !event.getAuthor().equals(event.getJDA().getSelfInfo())) {
					String content = event.getMessage().getContent();
					String commandName = content.replaceFirst("/", "").split(" ")[0];
					String[] args = {};
					if (content.replaceFirst("/" + commandName, "").trim().length() > 0) {
						args = content.replaceFirst("/" + commandName, "").trim().split(",");
						for (int i = 0; i < args.length; i++)
							args[i] = args[i].trim();
					}
					
					event.getChannel().sendTyping();
					if (commands.pmCommands.containsKey(commandName)) {
						commands.pmCommands.get(commandName).runCommand(event, args);
					}
				}
	}
	
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent  event) {
		System.out.printf("[" + sdf.format(new Date()) + "][%s][%s] %s: %s\n", 	event.getGuild().getName(),
												event.getChannel().getName(),
												event.getAuthor().getUsername(),
												event.getMessage().getContent());
		
		//Check for command
		if (event.getMessage().getContent().startsWith("/") && !event.getAuthor().equals(event.getJDA().getSelfInfo())) {
			String content = event.getMessage().getContent();
			String commandName = content.replaceFirst("/", "").split(" ")[0];
			String[] args = {};
			if (content.replaceFirst("/" + commandName, "").trim().length() > 0) {
				args = content.replaceFirst("/" + commandName, "").trim().split(",");
				for (int i = 0; i < args.length; i++)
					args[i] = args[i].trim();
			}
			
			if (commands.guildCommands.containsKey(commandName)) {
				event.getChannel().sendTyping();
				Statistics.getInstance().logCommandReceived(commandName, event.getAuthor().getUsername());
				commands.guildCommands.get(commandName).runCommand(event, args);
			}
		}
		//Check for dankness
		DankMemes.check(event);
		
		Statistics.getInstance().logMessage(event);
	}
	
	@Override
	public void onTextChannelUpdatePosition(TextChannelUpdatePositionEvent event) {
		Channels.changed(event);
	}
	
	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		TextChannel channel = event.getGuild().getPublicChannel(); 
		channel.sendTyping();
		
		channel.sendMessageAsync(DiscordInfo.getNewMemberInfo().replaceAll("<user>", event.getUser().getAsMention()), null);
		event.getJDA().getTextChannelById(DiscordInfo.getAdminChanID())
			.sendMessageAsync("New user, " + event.getUser().getUsername() + ", just joined!", null);
		
		Users.joined(event);
	}
	
	@Override
	public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
		Users.left(event);
	}
	
	@Override
	public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
		Users.roleUpdate(event);
	}
	
	@Override
	public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
		Users.roleUpdate(event);
	}
	
	@Override
	public void onUserOnlineStatusUpdate(UserOnlineStatusUpdateEvent event) {
		System.out.printf("[" + sdf.format(new Date()) + "][Online Status] %s: %s\n", event.getUser().getUsername(), event.getUser().getOnlineStatus().name());
		Users.setOnlineStatus(event);
	}
	
	@Override
	public void onUserNameUpdate(UserNameUpdateEvent event) {
		Users.nameUpdate(event);
	}
	
	@Override
	public void onUserAvatarUpdate(UserAvatarUpdateEvent event) {
		Users.avatarUpdate(event);
	}
}
