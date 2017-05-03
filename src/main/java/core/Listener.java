package core;

import misc.DankMemes;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import provider.Connections;
import provider.DataProvider;
import provider.Statistics;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Listener extends ListenerAdapter {
	private Commands commands;
	static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	static final String BOT_NAME = DataProvider.getBotName();

	public static final String prefix = DataProvider.getPrefix().isEmpty() ? "/" : DataProvider.getPrefix();
	public static final long startupTime = new Date().getTime();
	public static final String VERSION_NUMBER = Main.class.getPackage().getImplementationVersion() == null ? "0.0.0_0" : Main.class.getPackage().getImplementationVersion();
    public static boolean isDebug = DataProvider.isDev(); //Default setting but can be changed at runtime if need be
    public static boolean isTest = false; // Will be changed by JUnit when running a test
	public static JDA jda;
	
	@Override
	public void onReady(ReadyEvent event) {
		commands = new Commands();
		new AutoUpdate();

		//Print out startup info
		System.out.println("[" + sdf.format(new Date()) + "][Info] " + BOT_NAME + " v" + VERSION_NUMBER + " ready!");
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
		}
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
			String[] args = getArgs(content, commandName);

            if (commands.pmCommands.containsKey(commandName)) {
                event.getChannel().sendTyping();
                commands.pmCommands.get(commandName).runCommand(event, args);
            }
        }
	}
	
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
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
			String[] args = getArgs(content, commandName);
			
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

	private static String[] getArgs(String content, String commandName) {
		String[] args = {};
		if (content.replaceFirst(prefix + commandName, "").trim().length() > 0) {
            args = content.replaceFirst(prefix + commandName, "").trim().split(",");
            for (int i = 0; i < args.length; i++)
                args[i] = args[i].trim();
        }
		return args;
	}

	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
	    if (!DataProvider.isDev()) {
            TextChannel channel = event.getGuild().getPublicChannel();
            channel.sendMessage(DataProvider.getNewMemberInfo().replaceAll("<user>", event.getMember().getAsMention())).queue();

            event.getJDA().getTextChannelById(DataProvider.getAdminChanID())
                    .sendMessage("New user, " + event.getMember().getEffectiveName() + ", just joined!").queue();

        }
	}
}
