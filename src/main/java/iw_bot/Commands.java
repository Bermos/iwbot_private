package iw_bot;

import commands.GuildCommand;
import commands.PMCommand;
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

		//Guild message commands
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

		loadPMCommands();

		loadGuildCommands();
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
