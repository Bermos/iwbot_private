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

		pmCommands.put("owner", new EditOwner());

		pmCommands.put("send", new SendMessage());

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
		});

		guildCommands.put("setavatar", new Setavatar());

		guildCommands.put("setname", new Setname());

		guildCommands.put("setgame", new Setgame());

		guildCommands.put("role", new commands.core_commands.Role());

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

		guildCommands.put("applicant-test", new Applicant());

		guildCommands.put("restart", new Restart());

		//end of commands
	}
}
