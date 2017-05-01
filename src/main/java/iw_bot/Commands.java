package iw_bot;

import commands.GuildCommand;
import commands.PMCommand;
import commands.core_commands.*;
import commands.ed_commands.*;
import commands.iw_commands.*;
import commands.misc_commands.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import provider.DataProvider;

import java.util.LinkedHashMap;
import java.util.Map;

class Commands {
	Map<String, PMCommand> pmCommands = new LinkedHashMap<>();
	Map<String, GuildCommand> guildCommands = new LinkedHashMap<>();
	
	Commands() {
		//Private message commands
		pmCommands.put("ping", (event, args) -> event.getChannel().sendMessage("pong").queue());

		pmCommands.put("version", (event, args) -> event.getChannel().sendMessage(Listener.VERSION_NUMBER).queue());

		loadPMCommands();

		loadGuildCommands();

        /* pmCommands.put("update", new Update());
		
		pmCommands.put("bgs", new BGS());
		
		pmCommands.put("time", new UTCTime());
		
		pmCommands.put("dist", new Distance());

		pmCommands.put("roll", new RollDice());
		
		pmCommands.put("status", new Status());
		
		pmCommands.put("restart", new Restart());

		pmCommands.put("shutdown", new Shutdown());

		pmCommands.put("account", new Auth());

		pmCommands.put("owner", new EditOwner());

		pmCommands.put("send", new SendMessage());

		//Guild message commands

		guildCommands.put("debug", new DebugMode());

		guildCommands.put("update", new Update());

		//TODO Unify all 3 setters
		guildCommands.put("setavatar", new Setavatar());

		guildCommands.put("setname", new Setname());

		guildCommands.put("setgame", new Setgame());

		guildCommands.put("role", new Role());

		guildCommands.put("dist", new Distance());

		guildCommands.put("welcome", new Welcome());

		guildCommands.put("adminchannel", new AdminChannel());

		guildCommands.put("adminrole", new AdminRole());

		guildCommands.put("dance", new Dance());

		guildCommands.put("topic", new Topic());

		guildCommands.put("time", new UTCTime());

		guildCommands.put("status", new Status());

		guildCommands.put("xkcd", new XKCD());
		
		guildCommands.put("stripme", new Stripme());

		guildCommands.put("note", new Notes());
		
		guildCommands.put("list", new MissionsList());
		
		guildCommands.put("next", new MissionsNext());
		
		guildCommands.put("mission", new Missions());
		
		guildCommands.put("bgs", new BGS());

		guildCommands.put("roll", new RollDice());
		
		guildCommands.put("memes", new Memes());

		guildCommands.put("reminder", new Reminder());

		guildCommands.put("whois", new CMDRLookup());

		guildCommands.put("clear", new BulkDelete());

		guildCommands.put("applicant", new Applicant());

		guildCommands.put("restart", new Restart());

		guildCommands.put("shutdown", new Shutdown()); */

		guildCommands.put("shitdown", new GuildCommand() {
			@Override
			public void runCommand(GuildMessageReceivedEvent event, String[] args) {
				event.getChannel().sendMessage(":poop:").queue();
			}

			@Override
			public String getHelp(GuildMessageReceivedEvent event) {
				return "";
			}
		});

		guildCommands.put("help", new GuildCommand() {
			public void runCommand(GuildMessageReceivedEvent event, String[] args) {
				String message = "Commands available:\n```html\n";
				for (Map.Entry<String, GuildCommand> entry : guildCommands.entrySet()) {
					if (!entry.getValue().getHelp(event).isEmpty())
						message += String.format(Listener.prefix + "%-12s | " + entry.getValue().getHelp(event) + "\n", entry.getKey());
				}
				message += "```\n";
				message += "For a detailed help please use this guide: https://drive.google.com/file/d/0B1EHAnlL83qgbnRLV2ktQmVlOXM/view?usp=sharing";
				event.getChannel().sendMessage(message).queue();
			}

			public String getHelp(GuildMessageReceivedEvent event) {
				return "< ?> variables are optional, <a>|<b> either var a OR b";
			}
		});

		guildCommands.put("version", new GuildCommand() {
			@Override
			public void runCommand(GuildMessageReceivedEvent event, String[] args) {
				event.getChannel().sendMessage(Listener.VERSION_NUMBER).queue();
			}

			@Override
			public String getHelp(GuildMessageReceivedEvent event) {
				return "Returns the current version";
			}
		});

		//end of commands
	}

	private void loadGuildCommands() {
		for (Map.Entry<String, String> entry: DataProvider.getGuildCommands().entrySet()) {
			try {
			    Class t = Class.forName("commands." + entry.getValue());

				guildCommands.put(entry.getKey(), (GuildCommand) t.newInstance());
			} catch (Exception e) {
				System.out.println("I failed getting class: \"" + entry.getValue() + "\"");
				LogUtil.logErr(e);
			}
		}
	}

	private void loadPMCommands() {

        for (Map.Entry<String, String> entry: DataProvider.getPMCommands().entrySet()) {
            try {
                Class t = Class.forName("commands." + entry.getValue());

                pmCommands.put(entry.getKey(), (PMCommand) t.newInstance());
            } catch (Exception e) {
                System.out.println("I failed getting class: \"" + entry.getValue() + "\"");
                LogUtil.logErr(e);
            }
        }
	}
}
