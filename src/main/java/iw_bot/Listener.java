package iw_bot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import iw_core.Users;
import misc.CMDRLookup;
import misc.DankMemes;
import misc.Reminder;
import misc.StatusGenerator;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.core.events.user.UserAvatarUpdateEvent;
import net.dv8tion.jda.core.events.user.UserNameUpdateEvent;
import net.dv8tion.jda.core.events.user.UserOnlineStatusUpdateEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import provider.Connections;
import provider.DiscordInfo;
import provider.Statistics;

class Listener extends ListenerAdapter {
	private Commands commands;
	static long startupTime;
	private static SimpleDateFormat sdf;
	static final String VERSION_NUMBER = "2.4.1_32";
	
	Listener() {
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
		new StatusGenerator(event.getJDA().getPresence());
		
		new Users();
		Users.sync(event);

		new Reminder().startChecks(event.getJDA());
		CMDRLookup.setup();
	}
	
	@Override
	public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
		System.out.printf("[" + sdf.format(new Date()) + "][PM][%s] %s: %s\n",
											event.getChannel().getUser().getName(),
											event.getAuthor().getName(),
											event.getMessage().getContent());
		
		//Check for command
				if (event.getMessage().getContent().startsWith("/") && !event.getAuthor().equals(event.getJDA().getSelfUser())) {
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
												event.getMember().getEffectiveName(),
												event.getMessage().getContent());
		
		//Check for command
		if (event.getMessage().getContent().startsWith("/") && !event.getAuthor().equals(event.getJDA().getSelfUser())) {
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
				Statistics.getInstance().logCommandReceived(commandName, event.getMember().getEffectiveName());
				commands.guildCommands.get(commandName).runCommand(event, args);
			}
		}
		//Check for dankness
		DankMemes.check(event);
		
		Statistics.getInstance().logMessage(event);
	}
	
	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		TextChannel channel = event.getGuild().getPublicChannel(); 
		channel.sendTyping();
		
		channel.sendMessage(DiscordInfo.getNewMemberInfo().replaceAll("<user>", event.getMember().getAsMention())).queue();
		event.getJDA().getTextChannelById(DiscordInfo.getAdminChanID())
			.sendMessage("New user, " + event.getMember().getEffectiveName() + ", just joined!").queue();
		
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
		System.out.printf("[" + sdf.format(new Date()) + "][Online Status] %s: %s\n", event.getUser().getName(), event.getGuild().getMember(event.getUser()).getOnlineStatus().name());
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
