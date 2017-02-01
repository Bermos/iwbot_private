package iw_bot;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import commands.GuildCommand;
import commands.PMCommand;
import commands.core_commands.*;
import commands.ed_commands.Distance;
import commands.iw_commands.Auth;
import commands.iw_commands.BGS;
import commands.misc_commands.*;
import net.dv8tion.jda.core.entities.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import iw_core.Missions;
import misc.Dance;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import provider.Connections;
import provider.DiscordInfo;

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

		guildCommands.put("role", new GuildCommand() {
			public void runCommand(GuildMessageReceivedEvent event, String[] args) {
				event.getChannel().sendTyping();
				//Permission check
				if (!(DiscordInfo.isOwner(event.getAuthor().getId()) || DiscordInfo.isAdmin(event.getGuild().getMember(event.getAuthor()).getRoles()))) {
					event.getChannel().sendMessage("[Error] You aren't authorized to do this").queue();
					return;
				}
				
				if (args.length == 0) {
					event.getChannel().sendMessage("[Error] No name stated").queue();
				} else if (args.length == 1) {
					event.getChannel().sendMessage("[Error] No action selected").queue();
				} else {
					if (args[0].equalsIgnoreCase("add")) {
						event.getGuild().getController().createRole().setName(args[1]).queue();
						event.getChannel().sendMessage("[Success] role '" + args[1] + "' created").queue();
					}
					if (args[0].equalsIgnoreCase("del")) {
						for (Role role : event.getGuild().getRolesByName(args[1], true)) {
							String oldName = role.getName();
							role.delete().queue();
							event.getChannel().sendMessage("[Success] role '" + oldName + "' deleted").queue();
						}
					}
					if (args[0].equalsIgnoreCase("color") || args[0].equalsIgnoreCase("colour")) {
						if (args.length < 5) {
							event.getChannel().sendMessage("[Error] you need to specify the RGB values for the new color. '0, 0, 0' for example").queue();
							return;
						}

						for (Role role : event.getGuild().getRolesByName(args[1], true)) {
							role.getManager().setColor(new Color(Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]))).queue();
							event.getChannel().sendMessage("[Success] colour of role '" + args[1] + "' changed").queue();
						}
					}
					if (args[0].equalsIgnoreCase("rename")) {
						if (args.length < 3) {
							event.getChannel().sendMessage("[Error] no new name set").queue();
							return;
						}
						for (Role role : event.getGuild().getRolesByName(args[1], true)) {
							String oldName = role.getName();
							role.getManager().setName(args[2]).queue();
							event.getChannel().sendMessage("[Success] role '" + oldName + "' renamed to '" + args[2] + "'").queue();
						}
					}
				}
			}
			
			public String getHelp(GuildMessageReceivedEvent event) {
				//Permission check
				if (!(DiscordInfo.isOwner(event.getAuthor().getId()) || DiscordInfo.isAdmin(event.getGuild().getMember(event.getAuthor()).getRoles())))
					return "";
				return "<add|del|rename|color|colour>, <name>, <newname|#color> - Edits the role in the specified way.";
			}
		});

		guildCommands.put("dist", new Distance()); //done

		guildCommands.put("new", new GuildCommand() {
			public void runCommand(GuildMessageReceivedEvent event, String[] args) {
				//Permission check
				if (!(DiscordInfo.isOwner(event.getAuthor().getId()) || DiscordInfo.isAdmin(event.getGuild().getMember(event.getAuthor()).getRoles()))) {
					event.getChannel().sendMessage("[Error] You aren't authorized to do this").queue();
					return;
				}
				
				if (args.length == 0) {
					event.getChannel().sendMessage(DiscordInfo.getNewMemberInfo().replaceAll("<user>", event.getMember().getEffectiveName())).queue();
				}
				else {
					
					DiscordInfo.setNewMemberInfo(event.getMessage().getRawContent().replaceFirst("/new", "").trim());
					event.getChannel().sendMessage("[Success] New member message changed").queue();
				}
			}
			
			public String getHelp(GuildMessageReceivedEvent event) {
				//Permission check
				if (!(DiscordInfo.isOwner(event.getAuthor().getId()) || DiscordInfo.isAdmin(event.getGuild().getMember(event.getAuthor()).getRoles())))
					return "";
				return "<information?> - sets information for new players or shows it";
			}
		});

		guildCommands.put("adminchannel", new GuildCommand() {
			public void runCommand(GuildMessageReceivedEvent event, String[] args) {
				//Permission check
				if (!(DiscordInfo.isOwner(event.getAuthor().getId()) || DiscordInfo.isAdmin(event.getGuild().getMember(event.getAuthor()).getRoles()))) {
					event.getChannel().sendMessage("[Error] You aren't authorized to do this").queue();
					return;
				}
				
				if (args.length == 0){
					event.getChannel().sendMessage("Admin channel is: <#" + DiscordInfo.getAdminChanID() + ">").queue();
				}
				else if (!event.getMessage().getMentionedChannels().isEmpty()) {
					DiscordInfo.setAdminChanID(event.getMessage().getMentionedChannels().get(0).getId());
					event.getChannel().sendMessage("[Success] Admin channel saved").queue();
				}
				else {
					TextChannel chan = event.getGuild().getTextChannels().stream().filter(vChan -> vChan.getName().equalsIgnoreCase(args[0].trim()))
							.findFirst().orElse(null);
					if (chan == null) {
						event.getChannel().sendMessage("Channel not found").queue();
						return;
					} else
						DiscordInfo.setAdminChanID(chan.getId());
					event.getChannel().sendMessage("[Success] Admin channel saved").queue();
				}
			}
			
			public String getHelp(GuildMessageReceivedEvent event) {
				//Permission check
				if (!(DiscordInfo.isOwner(event.getAuthor().getId()) || DiscordInfo.isAdmin(event.getGuild().getMember(event.getAuthor()).getRoles())))
					return "";
				return "<channel> - sets admin channel";
			}
		});

		guildCommands.put("admin", new GuildCommand() {
			public void runCommand(GuildMessageReceivedEvent event, String[] args) {
				//Permission check
				if (!(DiscordInfo.isOwner(event.getAuthor().getId()) || DiscordInfo.isAdmin(event.getGuild().getMember(event.getAuthor()).getRoles()))) {
					event.getChannel().sendMessage("[Error] You aren't authorized to do this").queue();
					return;
				}
				
				if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("view"))) {
					String message = "";
					for (String id : DiscordInfo.getAdminRoleIDs())
						message += ( "-" + event.getGuild().getRoleById(id).getName() + "\n" );
					
					if (!message.isEmpty())
						event.getChannel().sendMessage("Roles with admin privileges:\n" + message).queue();
					else
						event.getChannel().sendMessage("No admin roles defined").queue();
				}
				else if (args[0].equalsIgnoreCase("add")) {
					if (!event.getMessage().getMentionedRoles().isEmpty()) {
						DiscordInfo.addAdminRoleID(event.getMessage().getMentionedRoles().get(0).getId());
					} else if (args.length == 2) {
						Role role = event.getGuild().getRoles().stream().filter(vrole -> vrole.getName().equalsIgnoreCase(args[1].trim())).findFirst().orElse(null);
						if (role != null) {
							DiscordInfo.addAdminRoleID(role.getId());
						} else {
							event.getChannel().sendMessage("[Error] No role with this name found").queue();
							return;
						}
					} else {
						event.getChannel().sendMessage("[Error] No role specified").queue();
						return;
					}
					event.getChannel().sendMessage("[Success] Admin role saved").queue();
				}
				else if (args[0].equalsIgnoreCase("del")) {
					if (!event.getMessage().getMentionedRoles().isEmpty()) {
						DiscordInfo.removeAdminRoleID(event.getMessage().getMentionedRoles().get(0).getId());
					} else if (args.length == 2) {
						Role role = event.getGuild().getRoles().stream().filter(vrole -> vrole.getName().equalsIgnoreCase(args[1].trim())).findFirst().orElse(null);
						if (role != null) {
							DiscordInfo.removeAdminRoleID(role.getId());
						} else {
							event.getChannel().sendMessage("[Error] No role with this name found").queue();
							return;
						}
					} else {
						event.getChannel().sendMessage("[Error] No role specified").queue();
						return;
					}
					event.getChannel().sendMessage("[Success] Admin role removed").queue();
				}
			}
			
			public String getHelp(GuildMessageReceivedEvent event) {
				//Permission check
				if (!(DiscordInfo.isOwner(event.getAuthor().getId()) || DiscordInfo.isAdmin(event.getGuild().getMember(event.getAuthor()).getRoles())))
					return "";
				return "<add?>|<del?>, <role?> - shows, adds or delets a role in/to/from admins";
			}
		});

		guildCommands.put("dance", new GuildCommand() {
			public void runCommand(GuildMessageReceivedEvent event, String[] args) {
				Dance dance = new Dance(event);
				dance.setDance(Dance.ASCII.DANCE);
				dance.start();
			}
			
			public String getHelp(GuildMessageReceivedEvent event) {
				return "Makes the bot dance";
			}
		});

		guildCommands.put("topic", new GuildCommand() {
			public void runCommand(GuildMessageReceivedEvent event, String[] args) {
				event.getChannel().sendMessage(event.getChannel().getTopic()).queue();
			}
			
			public String getHelp(GuildMessageReceivedEvent event) {
				return "Shows the channel details";
			}
		});

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
