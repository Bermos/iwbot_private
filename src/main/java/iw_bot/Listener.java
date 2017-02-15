package iw_bot;

import commands.misc_commands.Reminder;
import iw_core.Users;
import misc.DankMemes;
import misc.StatusGenerator;
import net.dv8tion.jda.core.JDA;
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
import provider.DataProvider;
import provider.Statistics;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Listener extends ListenerAdapter {
	private Commands commands;
	private static SimpleDateFormat sdf;


	public static final String prefix = DataProvider.getPrefix().isEmpty() ? "/" : DataProvider.getPrefix();
	public static boolean isDebug = DataProvider.isDev(); //Default setting but can be changed on runtime if need be
	public static long startupTime;
	public static final String VERSION_NUMBER = "3.1.8_73";
	public static JDA jda;
	
	Listener() {
		this.commands = new Commands();
        new AutoUpdate();
		Listener.startupTime = new Date().getTime();
		Listener.sdf = new SimpleDateFormat("HH:mm:ss");
		Listener.sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

		//Initial parsing of the memes.json file
		DankMemes.update();
	}
	
	@Override
	public void onReady(ReadyEvent event) {
		//Print out startup info
		System.out.println("[" + sdf.format(new Date()) + "][Info] Listener v" + VERSION_NUMBER + " ready!");
		System.out.println("[" + sdf.format(new Date()) + "][Info] Connected to:");
		for (Guild guild : event.getJDA().getGuilds()) {
			System.out.println("	" + guild.getName());
		}

		//I'm not sure this is actually needed but it's here so whatever
		new Connections().getConnection();

		jda = event.getJDA();

		if (!DataProvider.isDev()) {
			//Start metadata statistics logging
			Statistics stats = Statistics.getInstance();
			stats.connect(event.getJDA());

            //Start random Playing... generator
            new StatusGenerator(event.getJDA().getPresence());
		}

        //Setup and synchronise users and online status with MySQL db
        new Users();
        Users.sync(event);

        //Start checks for any set reminders from users
        new Reminder().startChecks(event.getJDA());
	}
	
	@Override
	public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
		//Print out message to console if debug
		if (isDebug) {
			System.out.printf("[" + sdf.format(new Date()) + "][PM][%s] %s: %s\n",
					event.getChannel().getUser().getName(),
					event.getAuthor().getName(),
					event.getMessage().getContent());
		}
		
		//Check for command
        if (event.getMessage().getContent().startsWith(prefix) && !event.getAuthor().isBot()) {
            String content = event.getMessage().getContent();
            String commandName = content.replaceFirst(prefix, "").split(" ")[0];
            String[] args = {};
            if (content.replaceFirst(prefix + commandName, "").trim().length() > 0) {
                args = content.replaceFirst(prefix + commandName, "").trim().split(",");
                for (int i = 0; i < args.length; i++)
                    args[i] = args[i].trim();
            }

            if (commands.pmCommands.containsKey(commandName)) {
                event.getChannel().sendTyping();
                commands.pmCommands.get(commandName).runCommand(event, args);
            }
        }
	}
	
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent  event) {
		if (isDebug) {
			System.out.printf("[" + sdf.format(new Date()) + "][%s][%s] %s: %s\n", event.getGuild().getName(),
					event.getChannel().getName(),
					event.getMember().getEffectiveName(),
					event.getMessage().getContent());
		}
		
		//Check for command
		if (event.getMessage().getContent().startsWith(prefix) && !event.getAuthor().isBot()) {
			String content = event.getMessage().getContent();
			String commandName = content.replaceFirst(prefix, "").split(" ")[0];
			String[] args = {};
			if (content.replaceFirst(prefix + commandName, "").trim().length() > 0) {
				args = content.replaceFirst(prefix + commandName, "").trim().split(",");
				for (int i = 0; i < args.length; i++)
					args[i] = args[i].trim();
			}
			
			if (commands.guildCommands.containsKey(commandName)) {
                if(!DataProvider.isDev())
                    Statistics.getInstance().logCommandReceived(commandName, event.getMember().getEffectiveName());

				event.getChannel().sendTyping();
				commands.guildCommands.get(commandName).runCommand(event, args);
			}
		}
		//Check for dankness
		 DankMemes.check(event);

        if (!DataProvider.isDev())
		    Statistics.getInstance().logMessage(event);
	}
	
	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
	    if (!DataProvider.isDev()) {
            TextChannel channel = event.getGuild().getPublicChannel();
            channel.sendMessage(DataProvider.getNewMemberInfo().replaceAll("<user>", event.getMember().getAsMention())).queue();

            event.getJDA().getTextChannelById(DataProvider.getAdminChanID())
                    .sendMessage("New user, " + event.getMember().getEffectiveName() + ", just joined!").queue();

        }

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
	    if (isDebug)
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
