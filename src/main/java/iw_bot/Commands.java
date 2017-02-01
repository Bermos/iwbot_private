package iw_bot;

import commands.GuildCommand;
import commands.PMCommand;
import commands.core_commands.Welcome;
import commands.core_commands.*;
import commands.ed_commands.CMDRLookup;
import commands.ed_commands.Distance;
import commands.iw_commands.*;
import commands.misc_commands.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.util.LinkedHashMap;
import java.util.Map;

class Commands {
	Map<String, PMCommand> pmCommands = new LinkedHashMap<>();
	Map<String, GuildCommand> guildCommands = new LinkedHashMap<>();
	
	Commands() {
		//Private message commands
		pmCommands.put("ping", (event, args) -> event.getChannel().sendMessage("pong").queue());

		pmCommands.put("version", (event, args) -> event.getChannel().sendMessage(Listener.VERSION_NUMBER).queue());
		
		pmCommands.put("bgs", new commands.iw_commands.BGS());
		
		pmCommands.put("time", new UTCTime());
		
		pmCommands.put("dist", new Distance());

		pmCommands.put("roll", new RollDice());
		
		pmCommands.put("status", new Status());
		
		pmCommands.put("restart", new Restart());

		pmCommands.put("account", new Auth());
		
		//Guild message commands
		guildCommands.put("help", new GuildCommand() {
			public void runCommand(GuildMessageReceivedEvent event, String[] args) {
				String message = "Commands available:\n```html\n";
				for (Map.Entry<String, GuildCommand> entry : guildCommands.entrySet()) {
					if (!entry.getValue().getHelp(event).isEmpty())
						message += String.format("/%1$-12s | " + entry.getValue().getHelp(event) + "\n", entry.getKey());
				}
				message += "```\n";
				message += "For a detailed help please use this guide: https://drive.google.com/file/d/0B1EHAnlL83qgbnRLV2ktQmVlOXM/view?usp=sharing";
				event.getChannel().sendMessage(message).queue();
			}
			
			public String getHelp(GuildMessageReceivedEvent event) {
				return "< ?> variables are optional, <a>|<b> either var a OR b";
			}
		}); //done

		guildCommands.put("setavatar", new Setavatar()); //done

		guildCommands.put("setname", new Setname()); //done

		guildCommands.put("setgame", new Setgame()); //done

		guildCommands.put("role", new commands.core_commands.Role()); //done

		guildCommands.put("dist", new Distance()); //done

		guildCommands.put("welcome", new Welcome()); //done

		guildCommands.put("adminchannel", new AdminChannel()); //done

		guildCommands.put("adminrole", new AdminRole()); //done

		guildCommands.put("dance", new Dance()); //done

		guildCommands.put("topic", new Topic()); //done

		guildCommands.put("time", new UTCTime()); //done

		guildCommands.put("status", new Status()); //done

		guildCommands.put("xkcd", new XKCD()); //done
		
		guildCommands.put("stripme", new Stripme()); //done

		guildCommands.put("note", new Notes()); //done
		
		guildCommands.put("list", new MissionsList()); //done
		
		guildCommands.put("next", new MissionsNext()); //done
		
		guildCommands.put("mission", new Missions()); //done
		
		guildCommands.put("bgs", new BGS()); //done

		guildCommands.put("roll", new RollDice()); //done
		
		guildCommands.put("memes", new Memes()); //done

		guildCommands.put("reminder", new Reminder()); //done

		guildCommands.put("whois", new CMDRLookup()); //done

		guildCommands.put("clear", new BulkDelete()); //done

		//end of commands
	}
}
