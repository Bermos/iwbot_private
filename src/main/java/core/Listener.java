package core;

import misc.DankMemes;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import provider.Connections;
import provider.DataProvider;
import provider.Statistics;

import java.util.Date;

import static core.Main.SDF_TIME;

public class Listener extends ListenerAdapter {
	private static Commands commands;

	String BOT_NAME;
	String PM_PREFIX = DataProvider.getBotByName(BOT_NAME).pmPrefix;

    public boolean isDebug = DataProvider.isDev(BOT_NAME); //Default setting but can be changed at runtime if need be
    public boolean isTest = false; // Will be changed by JUnit when running a test

	public static final String VERSION_NUMBER = Main.class.getPackage().getImplementationVersion() == null ? "0.0.0_0" : Main.class.getPackage().getImplementationVersion();
	public static JDA jda;

	public final long startupTime = System.currentTimeMillis();
	public final GuildHandler gh = new GuildHandler(BOT_NAME);

	public Listener(String botName) {
		BOT_NAME = botName;
	}
	
	@Override
	public void onReady(ReadyEvent event) {
		commands = new Commands(this);
		new AutoUpdate();

		//Print out startup info
		System.out.println("[" + SDF_TIME.format(new Date()) + "][Info] " + BOT_NAME + " v" + VERSION_NUMBER + " ready!");
		System.out.println("[" + SDF_TIME.format(new Date()) + "][Info] Connected to:");
		for (Guild guild : event.getJDA().getGuilds()) {
			System.out.println("	" + guild.getName());
		}

		//I'm not sure this is actually needed but it's here so whatever
		new Connections().getConnection();

		jda = event.getJDA();

		if (!DataProvider.isDev(BOT_NAME)) {
			//Start metadata statistics logging
			Statistics stats = Statistics.getInstance();
			stats.connect(event.getJDA());
		}
	}
	
	@Override
	public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
		//Print out message to console if debug
		if (isDebug) {
			System.out.printf("[" + SDF_TIME.format(new Date()) + "][PM][%s] %s: %s\n",
					event.getChannel().getUser().getName(),
					event.getAuthor().getName(),
					event.getMessage().getContent());
		}
		
		//Check for command
        if (event.getMessage().getContent().startsWith(PM_PREFIX) && !event.getAuthor().isBot()) {
            String content = event.getMessage().getContent();
            String commandName = content.replaceFirst(PM_PREFIX, "").split(" ")[0];
			String[] args = getArgs(content, PM_PREFIX,commandName);

            if (commands.pmCommands.containsKey(commandName)) {
                event.getChannel().sendTyping();
                commands.pmCommands.get(commandName).runCommand(this, event, args);
            }
        }
	}
	
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		String prefix = gh.getPrefix(event.getGuild().getId());
		if (isDebug) {
			System.out.printf("[" + SDF_TIME.format(new Date()) + "][%s][%s] %s: %s\n", event.getGuild().getName(),
					event.getChannel().getName(),
					event.getMember().getEffectiveName(),
					event.getMessage().getContent());
		}
		
		//Check for command
		if (event.getMessage().getContent().startsWith(prefix) && !event.getAuthor().isBot()) {
			String content = event.getMessage().getContent();
			String commandName = content.replaceFirst(prefix, "").split(" ")[0];
			String[] args = getArgs(content, prefix, commandName);
			
			if (commands.guildCommands.containsKey(commandName)) {
                if(!DataProvider.isDev(BOT_NAME))
                    Statistics.getInstance().logCommandReceived(commandName, event.getMember().getEffectiveName());

				event.getChannel().sendTyping();
				commands.guildCommands.get(commandName).runCommand(this, event, args);
			}
		}
		//Check for dankness
		 DankMemes.check(event);

        if (!DataProvider.isDev(BOT_NAME))
		    Statistics.getInstance().logMessage(event);
	}

	private static String[] getArgs(String content, String prefix, String commandName) {
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
	    if (!DataProvider.isDev(BOT_NAME)) {
            TextChannel channel = event.getGuild().getPublicChannel();
            channel.sendMessage(gh.getWelcomeMessage(event.getGuild().getId()).replaceAll("<user>", event.getMember().getAsMention())).queue();

            event.getJDA().getTextChannelById(gh.getMessageChannel(event.getGuild().getId()))
                    .sendMessage("New user, " + event.getMember().getEffectiveName() + ", just memberJoined!").queue();

        }
	}

	void reloadCmds(GuildMessageReceivedEvent event) {
		commands = null;
		commands = new Commands(this);

		event.getChannel().sendMessage("(Re-)loaded " + commands.guildCommands.size() + " guild and " + commands.pmCommands.size() + " pm commands.").queue();
	}
}
