package iw_bot;

import commands.GuildCommand;
import commands.PMCommand;
import commands.Welcome;
import commands.core_commands.*;
import commands.ed_commands.Distance;
import commands.iw_commands.Auth;
import commands.iw_commands.BGS;
import commands.misc_commands.*;
import iw_core.Missions;
import commands.misc_commands.Dance;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import provider.DiscordInfo;

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
		
		guildCommands.put("list", new GuildCommand() {
			public void runCommand(GuildMessageReceivedEvent event, String[] args) {
				if (args.length < 1) {
					Missions.getList(event.getChannel().getId());
				}
				else if ((args[0].equalsIgnoreCase("new") || args[0].equalsIgnoreCase("add")) && args.length > 1) {
					String list = "";
					for (int i = 1; i < args.length; i++) {
						list = String.join(", ", list, args[i]);
					}
					list = list.replaceFirst(", ", "");
					Missions.newList(event.getChannel(), list);
				}
				else if (args[0].equalsIgnoreCase("next")) {
					Missions.nextListEntry(event.getChannel().getId());
					event.getMessage().deleteMessage();
				}
			}
			
			public String getHelp(GuildMessageReceivedEvent event) {
				return "Nav list for missions";
			}
		});
		
		guildCommands.put("next", new GuildCommand() {
			public void runCommand(GuildMessageReceivedEvent event, String[] args) {
				if (args.length < 1) {
					Missions.nextListEntry(event.getChannel().getId());
					event.getMessage().deleteMessage();
				}
			}
			
			public String getHelp(GuildMessageReceivedEvent event) {
				return "Nav list for missions";
			}
		});
		
		guildCommands.put("mission", new GuildCommand() {
			public void runCommand(GuildMessageReceivedEvent event, String[] args) {
				if (args.length > 1 && args[0].equalsIgnoreCase("new")) {
					User explorer = event.getMessage().getMentionedUsers().isEmpty() ? null : event.getMessage().getMentionedUsers().get(0);
					Missions.create(args[1], event.getGuild().getManager(), event.getGuild().getMember(explorer));
					event.getChannel().sendMessage("Mission channel created and permissions set. Good luck!").queue();
				}
				else if (args.length == 1 && args[0].equalsIgnoreCase("close")) {
					Missions.archiveRequest(event.getChannel(), event.getAuthor().getId());
					event.getChannel().sendMessage("Please confirm with '/mission yes' that you actually want to delete this channel. You cannot undo this!").queue();
				}
				else if (args.length == 1 && args[0].equalsIgnoreCase("yes")) {
					Missions.archive(event.getChannel(), event.getAuthor().getId());
					event.getJDA().getTextChannelById(DiscordInfo.getAdminChanID()).sendMessage(event.getChannel().getName() + " channel and role deleted.").queue();
				}
			}
			
			public String getHelp(GuildMessageReceivedEvent event) {
				return "";
			}
		});
		
		guildCommands.put("bgs", new BGS()); //done

		guildCommands.put("roll", new RollDice()); //done
		
		guildCommands.put("memes", new Memes()); //done

		guildCommands.put("reminder", new Reminder()); //done

		guildCommands.put("whois", new commands.ed_commands.CMDRLookup()); //partly done

		guildCommands.put("clear", new BulkDelete()); //done

		//end of commands
	}
}
