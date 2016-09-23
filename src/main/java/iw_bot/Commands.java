package iw_bot;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.ParseException;
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

import misc.CMDRLookup;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import iw_core.BGS;
import iw_core.BGS.Activity;
import iw_core.Channels;
import iw_core.Missions;
import iw_core.Notes;
import misc.Dance;
import misc.Reminder;
import misc.DankMemes;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Message.Attachment;
import net.dv8tion.jda.entities.Role;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.utils.AvatarUtil;
import net.dv8tion.jda.utils.AvatarUtil.Avatar;
import provider.Connections;
import provider.DiscordInfo;
import structs.EDSystem;

interface PMCommand {
	void runCommand(PrivateMessageReceivedEvent event, String[] args);
}

interface GuildCommand {
	void runCommand(GuildMessageReceivedEvent event, String[] args);
	String getHelp(GuildMessageReceivedEvent event);
}

public class Commands {
	public Map<String, PMCommand> pmCommands = new LinkedHashMap<String, PMCommand>();
	public Map<String, GuildCommand> guildCommands = new LinkedHashMap<String, GuildCommand>();
	
	public Commands() {
		//Private message commands
		pmCommands.put("ping", new PMCommand() {
			public void runCommand(PrivateMessageReceivedEvent event, String[] args) {
				event.getChannel().sendMessageAsync("pong", null);
			}
		});

		pmCommands.put("version", new PMCommand() {
			public void runCommand(PrivateMessageReceivedEvent event, String[] args) {
				event.getChannel().sendMessageAsync(Listener.VERSION_NUMBER, null);
			}
		});
		
		pmCommands.put("bgs", new PMCommand() {
			public void runCommand(PrivateMessageReceivedEvent event, String[] args) {
				if (args.length == 1) {
					if (args[0].equalsIgnoreCase("mystats")) {
						String output = "```";
						for (Map.Entry<Activity, Double> entry : BGS.getTotalAmmount(event.getAuthor().getId()).entrySet()) {
							output += entry.getKey().toString() + ": " + NumberFormat.getInstance(Locale.GERMANY).format(entry.getValue().intValue()).replace('.', '\'') + "\n";
						}
						output += "```";
						event.getChannel().sendMessageAsync(output, null);
					}
				}
			}
		});
		
		pmCommands.put("time", new PMCommand() {
			public void runCommand(PrivateMessageReceivedEvent event, String[] args) {
				Date date = new Date();
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
				sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
				event.getChannel().sendMessageAsync("UTC time:\n" + sdf.format(date), null);
			}
		});
		
		pmCommands.put("dist", new PMCommand() {
			public void runCommand(PrivateMessageReceivedEvent event, String[] args) {
				if (args.length < 2) {
					event.getChannel().sendMessageAsync("Sorry, I need 2 systems, separated by a ',' to give you what you want.", null);
					return;
				}
				boolean failed = false;
				Gson gson = new Gson();
				EDSystem sys1;
				EDSystem sys2;
				String jsonSys1 = "";
				String jsonSys2 = "";
				String urlSys1 = "http://www.edsm.net/api-v1/system?sysname=" + args[0].trim().replaceAll(" ", "+") + "&coords=1";
				String urlSys2 = "http://www.edsm.net/api-v1/system?sysname=" + args[1].trim().replaceAll(" ", "+") + "&coords=1";
				
				try {
					Document docSys1 = Jsoup.connect(urlSys1).ignoreContentType(true).get();
					Document docSys2 = Jsoup.connect(urlSys2).ignoreContentType(true).get();
					
					jsonSys1 = docSys1.body().text();
					jsonSys2 = docSys2.body().text();
					
					if (jsonSys1.contains("[]")) {
						event.getChannel().sendMessageAsync(args[0].trim().toUpperCase() + " not found.", null);
						failed = true;
					}
					if (jsonSys2.contains("[]")) {
						event.getChannel().sendMessageAsync(args[1].trim().toUpperCase() + " not found.", null);
						failed = true;
					}
					if (failed)
						return;
					
					sys1 = gson.fromJson(jsonSys1, EDSystem.class);
					sys2 = gson.fromJson(jsonSys2, EDSystem.class);
					
					if (sys1.coords == null) {
						event.getChannel().sendMessageAsync(args[0].trim().toUpperCase() + " found but coordinates not in db.", null);
						failed = true;
					}
					if (sys2.coords == null) {
						event.getChannel().sendMessageAsync(args[1].trim().toUpperCase() + " found but coordinates not in db.", null);
						failed = true;
					}
					if (failed)
						return;
					
					float x = sys2.coords.x - sys1.coords.x;
					float y = sys2.coords.y - sys1.coords.y;
					float z = sys2.coords.z - sys1.coords.z;
					
					double dist = Math.sqrt(x*x + y*y + z*z);

					event.getChannel().sendMessageAsync(String.format("Distance: %.1f ly", dist), null);
				} catch (JsonSyntaxException e) {
					event.getChannel().sendMessageAsync("[Error] Processing edsm result failed. Please contact Bermos.", null);
				} catch (SocketException e) {
					event.getChannel().sendMessageAsync("[Error] Failed connecting to edsm. You might want to retry in a few", null);
				} catch (IOException e) {
					event.getChannel().sendMessageAsync("[Error] Processing data failed", null);
				}
			}
		});
		
		pmCommands.put("status", new PMCommand() {
			public void runCommand(PrivateMessageReceivedEvent event, String[] args) {
				Long diff 			 = (new Date().getTime() - Listener.startupTime);
				int days			 = (int) TimeUnit.MILLISECONDS.toDays(diff);
				int hours			 = (int) TimeUnit.MILLISECONDS.toHours(diff) % 24;
				int minutes			 = (int) TimeUnit.MILLISECONDS.toMinutes(diff) % 60;
				int seconds			 = (int) TimeUnit.MILLISECONDS.toSeconds(diff) % 60;
				NumberFormat nForm	 = NumberFormat.getInstance(Locale.GERMANY);
				int noThreads		 = Thread.getAllStackTraces().keySet().size();
				String uniqueSets	 = "";
				String totalSets	 = "";
				String totalMemory	 = String.format("%.2f",(double) Runtime.getRuntime().maxMemory() / 1024 / 1024);
				String usedMemory	 = String.format("%.2f",(double)(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024);
				
				try {
					PreparedStatement ps = new Connections().getConnection().prepareStatement("SELECT COUNT(idmarkov) AS unique_sets, sum(prob) AS total_sets FROM iwmembers.markov");
					ResultSet rs = ps.executeQuery();
					rs.next();
					uniqueSets = nForm.format(rs.getInt("unique_sets")).replace('.', '\'');
					totalSets = nForm.format(rs.getInt("total_sets")).replace('.', '\'');
					rs.close();
					ps.close();
				} catch (SQLException e) {
					event.getChannel().sendMessageAsync("Hmm, looks like I fucked up the number of datasets. Here's the rest of the result:", null);
					e.printStackTrace();
				}
				
				String contOut = "```"
						+ "Uptime              | " + String.format("%dd %02d:%02d:%02d\n", days, hours, minutes, seconds)
						+ "# Threads           | " + noThreads						+ "\n"
						+ "Memory usage        | " + usedMemory + "/" + totalMemory	+ " MB\n"
						+ "Unique AI Datasets  | " + uniqueSets						+ "\n"
						+ "Total AI Datasets   | " + totalSets						+ "\n"
						+ "Version             | " + Listener.VERSION_NUMBER		+ "```";
				
				event.getChannel().sendMessageAsync(contOut, null);
			}
		});
		
		pmCommands.put("restart", new PMCommand() {
			public void runCommand(PrivateMessageReceivedEvent event, String[] args) {
				//Permission check
				if (!DiscordInfo.isOwner(event.getAuthor().getId())) {
					event.getChannel().sendMessageAsync("[Error] You aren't authorized to do this", null);
					return;
				}
				
				event.getChannel().sendMessage("Trying to restart...");
				System.exit(1);
			}
		});
		
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
				event.getChannel().sendMessageAsync(message, null);
			}
			
			public String getHelp(GuildMessageReceivedEvent event) {
				return "< ?> variables are optional, <a>|<b> either var a OR b";
			}
		});

		guildCommands.put("setavatar", new GuildCommand() {
			public void runCommand(GuildMessageReceivedEvent event, String[] args) {
				event.getChannel().sendTyping();
				//Permission check
				if (!(DiscordInfo.isOwner(event.getAuthor().getId()) || DiscordInfo.isAdmin(event.getGuild().getRolesForUser(event.getAuthor())))) {
					event.getChannel().sendMessageAsync("[Error] You aren't authorized to do this", null);
					return;
				}
				
				if (!event.getMessage().getAttachments().isEmpty()) {
					File avatarFile;
					Attachment attachment = event.getMessage().getAttachments().get(0);
					attachment.download(avatarFile = new File("./temp/newavatar.jpg"));
					try {
						Avatar avatar = AvatarUtil.getAvatar(avatarFile);
						event.getJDA().getAccountManager().setAvatar(avatar).update();
					} catch (UnsupportedEncodingException e) {
						event.getChannel().sendMessageAsync("[Error] Filetype", null);
					}
					event.getChannel().sendMessageAsync("[Success] Avatar changed.", null);
					avatarFile.delete();
				}
				else {
					event.getChannel().sendMessageAsync("[Error] No image attached", null);
				}
			}
			
			public String getHelp(GuildMessageReceivedEvent event) {
				//Permission check
				if (!(DiscordInfo.isOwner(event.getAuthor().getId()) || DiscordInfo.isAdmin(event.getGuild().getRolesForUser(event.getAuthor()))))
					return "";
				return "Upload desired pic to discord and enter command in the description prompt";
			}
		});

		guildCommands.put("setname", new GuildCommand() {
			public void runCommand(GuildMessageReceivedEvent event, String[] args) {
				event.getChannel().sendTyping();
				//Permission check
				if (!(DiscordInfo.isOwner(event.getAuthor().getId()) || DiscordInfo.isAdmin(event.getGuild().getRolesForUser(event.getAuthor())))) {
					event.getChannel().sendMessageAsync("[Error] You aren't authorized to do this", null);
					return;
				}
				
				if (args.length == 0) {
					event.getChannel().sendMessageAsync("[Error] No name stated", null);
				} else {
					event.getJDA().getAccountManager().setUsername(args[0]).update();
					event.getChannel().sendMessageAsync("[Success] Name changed", null);
				}
			}
			
			public String getHelp(GuildMessageReceivedEvent event) {
				//Permission check
				if (!(DiscordInfo.isOwner(event.getAuthor().getId()) || DiscordInfo.isAdmin(event.getGuild().getRolesForUser(event.getAuthor()))))
					return "";
				return "<name>";
			}
		});

		guildCommands.put("setgame", new GuildCommand() {
			public void runCommand(GuildMessageReceivedEvent event, String[] args) {
				event.getChannel().sendTyping();
				//Permission check
				if (!(DiscordInfo.isOwner(event.getAuthor().getId()) || DiscordInfo.isAdmin(event.getGuild().getRolesForUser(event.getAuthor())))) {
					event.getChannel().sendMessageAsync("[Error] You aren't authorized to do this", null);
					return;
				}
				
				if (args.length == 0)
					event.getJDA().getAccountManager().setGame(null);
				else
					event.getJDA().getAccountManager().setGame(args[0]);
				event.getChannel().sendMessageAsync("[Success] Game changed", null);
			}
			
			public String getHelp(GuildMessageReceivedEvent event) {
				//Permission check
				if (!(DiscordInfo.isOwner(event.getAuthor().getId()) || DiscordInfo.isAdmin(event.getGuild().getRolesForUser(event.getAuthor()))))
					return "";
				return "<game?> - To set the Playing: ...";
			}
		});

		guildCommands.put("role", new GuildCommand() {
			public void runCommand(GuildMessageReceivedEvent event, String[] args) {
				event.getChannel().sendTyping();
				//Permission check
				if (!(DiscordInfo.isOwner(event.getAuthor().getId()) || DiscordInfo.isAdmin(event.getGuild().getRolesForUser(event.getAuthor())))) {
					event.getChannel().sendMessageAsync("[Error] You aren't authorized to do this", null);
					return;
				}
				
				if (args.length == 0) {
					event.getChannel().sendMessageAsync("[Error] No name stated", null);
				} else if (args.length == 1) {
					event.getChannel().sendMessageAsync("[Error] No action selected", null);
				} else {
					if (args[0].equalsIgnoreCase("add")) {
						event.getGuild().createRole().setName(args[1]).update();
					}
					if (args[0].equalsIgnoreCase("del")) {
						for (Role role : event.getGuild().getRoles()) {
							if (role.getName().equalsIgnoreCase(args[1]))
								role.getManager().delete();
						}
					}
					if (args[0].equalsIgnoreCase("color")) {
						if (args.length < 5) {
							event.getChannel().sendMessageAsync("[Error] you need to specify the RGB values for the new color. '0, 0, 0' for example", null);
							return;
						}
						for (Role role : event.getGuild().getRoles()) {
							if (role.getName().equalsIgnoreCase(args[1]))
								role.getManager().setColor(new Color(Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]))).update();
						}
					}
					if (args[0].equalsIgnoreCase("rename")) {
						if (args.length < 3) {
							event.getChannel().sendMessageAsync("[Error] no new name set", null);
							return;
						}
						for (Role role : event.getGuild().getRoles()) {
							if (role.getName().equalsIgnoreCase(args[1]))
								role.getManager().setName(args[2]).update();
						}
					}
				}
			}
			
			public String getHelp(GuildMessageReceivedEvent event) {
				//Permission check
				if (!(DiscordInfo.isOwner(event.getAuthor().getId()) || DiscordInfo.isAdmin(event.getGuild().getRolesForUser(event.getAuthor()))))
					return "";
				return "<name>, <add|del|rename|color>, <newname|#color> - Edits the role in the specified way.";
			}
		});

		guildCommands.put("dist", new GuildCommand() {
			public void runCommand(GuildMessageReceivedEvent event, String[] args) {
				if (args.length < 2) {
					event.getChannel().sendMessageAsync("[Error] Not enough systems specified", null);
					return;
				}
				boolean failed = false;
				Gson gson = new Gson();
				EDSystem sys1;
				EDSystem sys2;
				String jsonSys1 = "";
				String jsonSys2 = "";
				String urlSys1 = "http://www.edsm.net/api-v1/system?sysname=" + args[0].trim().replaceAll(" ", "+") + "&coords=1";
				String urlSys2 = "http://www.edsm.net/api-v1/system?sysname=" + args[1].trim().replaceAll(" ", "+") + "&coords=1";
				
				try {
					Document docSys1 = Jsoup.connect(urlSys1).ignoreContentType(true).get();
					Document docSys2 = Jsoup.connect(urlSys2).ignoreContentType(true).get();
					
					jsonSys1 = docSys1.body().text();
					jsonSys2 = docSys2.body().text();
					
					if (jsonSys1.contains("[]")) {
						event.getChannel().sendMessageAsync(args[0].trim().toUpperCase() + " not found.", null);
						failed = true;
					}
					if (jsonSys2.contains("[]")) {
						event.getChannel().sendMessageAsync(args[1].trim().toUpperCase() + " not found.", null);
						failed = true;
					}
					if (failed)
						return;
					
					sys1 = gson.fromJson(jsonSys1, EDSystem.class);
					sys2 = gson.fromJson(jsonSys2, EDSystem.class);
					
					if (sys1.coords == null) {
						event.getChannel().sendMessageAsync(args[0].trim().toUpperCase() + " found but coordinates not in db.", null);
						failed = true;
					}
					if (sys2.coords == null) {
						event.getChannel().sendMessageAsync(args[1].trim().toUpperCase() + " found but coordinates not in db.", null);
						failed = true;
					}
					if (failed)
						return;
					
					float x = sys2.coords.x - sys1.coords.x;
					float y = sys2.coords.y - sys1.coords.y;
					float z = sys2.coords.z - sys1.coords.z;
					
					double dist = Math.sqrt(x*x + y*y + z*z);

					event.getChannel().sendMessageAsync(String.format("Distance: %.1f ly", dist), null);
				} catch (JsonSyntaxException e) {
					event.getChannel().sendMessageAsync("[Error] Processing edsm result failed", null);
				} catch (SocketException e) {
					event.getChannel().sendMessageAsync("[Error] Failed connecting to edsm. You might want to retry in a few", null);
				} catch (IOException e) {
					event.getChannel().sendMessageAsync("[Error] Processing data failed", null);
				}
			}
			
			public String getHelp(GuildMessageReceivedEvent event) {
				return "<system1>, <system2> - Gives the distance between those systems.";
			}
		});

		guildCommands.put("new", new GuildCommand() {
			public void runCommand(GuildMessageReceivedEvent event, String[] args) {
				//Permission check
				if (!(DiscordInfo.isOwner(event.getAuthor().getId()) || DiscordInfo.isAdmin(event.getGuild().getRolesForUser(event.getAuthor())))) {
					event.getChannel().sendMessageAsync("[Error] You aren't authorized to do this", null);
					return;
				}
				
				if (args.length == 0) {
					event.getChannel().sendMessageAsync(DiscordInfo.getNewMemberInfo().replaceAll("<user>", event.getAuthorName()), null);
				}
				else {
					
					DiscordInfo.setNewMemberInfo(event.getMessage().getRawContent().replaceFirst("/new", "").trim());
					event.getChannel().sendMessageAsync("[Success] New member message changed", null);
				}
			}
			
			public String getHelp(GuildMessageReceivedEvent event) {
				//Permission check
				if (!(DiscordInfo.isOwner(event.getAuthor().getId()) || DiscordInfo.isAdmin(event.getGuild().getRolesForUser(event.getAuthor()))))
					return "";
				return "<information?> - sets information for new players or shows it";
			}
		});

		guildCommands.put("adminchannel", new GuildCommand() {
			public void runCommand(GuildMessageReceivedEvent event, String[] args) {
				//Permission check
				if (!(DiscordInfo.isOwner(event.getAuthor().getId()) || DiscordInfo.isAdmin(event.getGuild().getRolesForUser(event.getAuthor())))) {
					event.getChannel().sendMessageAsync("[Error] You aren't authorized to do this", null);
					return;
				}
				
				if (args.length == 0){
					event.getChannel().sendMessageAsync("Admin channel is: <#" + DiscordInfo.getAdminChanID() + ">", null);
				}
				else if (!event.getMessage().getMentionedChannels().isEmpty()) {
					DiscordInfo.setAdminChanID(event.getMessage().getMentionedChannels().get(0).getId());
					event.getChannel().sendMessageAsync("[Success] Admin channel saved", null);
				}
				else {
					TextChannel chan = event.getGuild().getTextChannels().stream().filter(vChan -> vChan.getName().equalsIgnoreCase(args[0].trim()))
							.findFirst().orElse(null);
					if (chan == null) {
						event.getChannel().sendMessageAsync("Channel not found", null);
						return;
					} else
						DiscordInfo.setAdminChanID(chan.getId());
					event.getChannel().sendMessageAsync("[Success] Admin channel saved", null);
				}
			}
			
			public String getHelp(GuildMessageReceivedEvent event) {
				//Permission check
				if (!(DiscordInfo.isOwner(event.getAuthor().getId()) || DiscordInfo.isAdmin(event.getGuild().getRolesForUser(event.getAuthor()))))
					return "";
				return "<channel> - sets admin channel";
			}
		});

		guildCommands.put("admin", new GuildCommand() {
			public void runCommand(GuildMessageReceivedEvent event, String[] args) {
				//Permission check
				if (!(DiscordInfo.isOwner(event.getAuthor().getId()) || DiscordInfo.isAdmin(event.getGuild().getRolesForUser(event.getAuthor())))) {
					event.getChannel().sendMessageAsync("[Error] You aren't authorized to do this", null);
					return;
				}
				
				if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("view"))) {
					String message = "";
					for (String id : DiscordInfo.getAdminRoleIDs())
						message += ( "-" + event.getGuild().getRoleById(id).getName() + "\n" );
					
					if (!message.isEmpty())
						event.getChannel().sendMessageAsync("Roles with admin privileges:\n" + message, null);
					else
						event.getChannel().sendMessageAsync("No admin roles defined", null);
				}
				else if (args[0].equalsIgnoreCase("add")) {
					if (!event.getMessage().getMentionedRoles().isEmpty()) {
						DiscordInfo.addAdminRoleID(event.getMessage().getMentionedRoles().get(0).getId());
					} else if (args.length == 2) {
						Role role = event.getGuild().getRoles().stream().filter(vrole -> vrole.getName().equalsIgnoreCase(args[1].trim())).findFirst().orElse(null);
						if (role != null) {
							DiscordInfo.addAdminRoleID(role.getId());
						} else {
							event.getChannel().sendMessageAsync("[Error] No role with this name found", null);
							return;
						}
					} else {
						event.getChannel().sendMessageAsync("[Error] No role specified", null);
						return;
					}
					event.getChannel().sendMessageAsync("[Success] Admin role saved", null);
				}
				else if (args[0].equalsIgnoreCase("del")) {
					if (!event.getMessage().getMentionedRoles().isEmpty()) {
						DiscordInfo.removeAdminRoleID(event.getMessage().getMentionedRoles().get(0).getId());
					} else if (args.length == 2) {
						Role role = event.getGuild().getRoles().stream().filter(vrole -> vrole.getName().equalsIgnoreCase(args[1].trim())).findFirst().orElse(null);
						if (role != null) {
							DiscordInfo.removeAdminRoleID(role.getId());
						} else {
							event.getChannel().sendMessageAsync("[Error] No role with this name found", null);
							return;
						}
					} else {
						event.getChannel().sendMessageAsync("[Error] No role specified", null);
						return;
					}
					event.getChannel().sendMessageAsync("[Success] Admin role removed", null);
				}
			}
			
			public String getHelp(GuildMessageReceivedEvent event) {
				//Permission check
				if (!(DiscordInfo.isOwner(event.getAuthor().getId()) || DiscordInfo.isAdmin(event.getGuild().getRolesForUser(event.getAuthor()))))
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
				event.getChannel().sendMessageAsync(event.getChannel().getTopic(), null);
			}
			
			public String getHelp(GuildMessageReceivedEvent event) {
				return "Shows the channel details";
			}
		});

		guildCommands.put("time", new GuildCommand() {
			public void runCommand(GuildMessageReceivedEvent event, String[] args) {
				Date date = new Date();
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
				sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
				event.getMessage().deleteMessage();
				event.getChannel().sendMessageAsync("UTC time:\n" + sdf.format(date), null);
			}
			
			public String getHelp(GuildMessageReceivedEvent event) {
				return "UTC date & time now";
			}
		});

		guildCommands.put("status", new GuildCommand() {
			public void runCommand(GuildMessageReceivedEvent event, String[] args) {
				Long diff 			 = (new Date().getTime() - Listener.startupTime);
				int days			 = (int) TimeUnit.MILLISECONDS.toDays(diff);
				int hours			 = (int) TimeUnit.MILLISECONDS.toHours(diff) % 24;
				int minutes			 = (int) TimeUnit.MILLISECONDS.toMinutes(diff) % 60;
				int seconds			 = (int) TimeUnit.MILLISECONDS.toSeconds(diff) % 60;
				NumberFormat nForm	 = NumberFormat.getInstance(Locale.GERMANY);
				int noThreads		 = Thread.getAllStackTraces().keySet().size();
				String uniqueSets	 = "";
				String totalSets	 = "";
				String totalMemory	 = String.format("%.2f",(double) Runtime.getRuntime().maxMemory() / 1024 / 1024);
				String usedMemory	 = String.format("%.2f",(double)(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024);
				
				try {
					PreparedStatement ps = new Connections().getConnection().prepareStatement("SELECT COUNT(idmarkov) AS unique_sets, sum(prob) AS total_sets FROM iwmembers.markov");
					ResultSet rs = ps.executeQuery();
					rs.next();
					uniqueSets = nForm.format(rs.getInt("unique_sets")).replace('.', '\'');
					totalSets = nForm.format(rs.getInt("total_sets")).replace('.', '\'');
					rs.close();
					ps.close();
				} catch (SQLException e) { e.printStackTrace(); }
				
				String contOut = "```"
						+ "Uptime              | " + String.format("%dd %02d:%02d:%02d\n", days, hours, minutes, seconds)
						+ "# Threads           | " + noThreads						+ "\n"
						+ "Memory usage        | " + usedMemory + "/" + totalMemory	+ " MB\n"
						+ "Unique AI Datasets  | " + uniqueSets						+ "\n"
						+ "Total AI Datasets   | " + totalSets						+ "\n"
						+ "Version             | " + Listener.VERSION_NUMBER		+ "```";
				
				event.getChannel().sendMessageAsync(contOut, null);
			}
			
			public String getHelp(GuildMessageReceivedEvent event) {
				return "Shows information about the bot";
			}
		});

		guildCommands.put("xkcd", new GuildCommand() {
			public void runCommand(GuildMessageReceivedEvent event, String[] args) {
				Image image = null;
				File file = null;
				Random RNJesus = new Random();
				int iRandom = RNJesus.nextInt(1662);
				String url = "http://xkcd.com/" + iRandom + "/";
				
				try {
					Document doc = Jsoup.connect(url).get();
					Elements images = doc.select("img[src$=.png]");
					
					for(Element eImage : images) {
						if(eImage.attr("src").contains("comic")) {
							URL uRl = new URL("http:" + eImage.attr("src"));
							image = ImageIO.read(uRl);
							ImageIO.write((RenderedImage) image, "png", file = new File("./temp/" + iRandom + ".png"));
							event.getChannel().sendFile(file, null);
							file.delete();
						}
					}
				} catch (SocketException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			public String getHelp(GuildMessageReceivedEvent event) {
				return "Post a random XKCD comic";
			}
		});
		
		guildCommands.put("stripme", new GuildCommand() {
			public void runCommand(GuildMessageReceivedEvent event, String[] args) {
				User author = event.getAuthor();
				Guild guild = event.getGuild();
				List<Role> rolesToStrip = new ArrayList<>();
				
				for (Role role : guild.getRoles()) {
					for (String roleName : args) {
						if (role.getName().equalsIgnoreCase(roleName.trim()))
							rolesToStrip.add(role);
					}
				}
				
				String output = "```Removed roles: ";
				for (Role role : rolesToStrip) {
					guild.getManager().removeRoleFromUser(author, role).update();
					output += "\n" + role.getName();
				}
				output += "```";
				
				event.getChannel().sendMessageAsync(output, null);
			}
			
			public String getHelp(GuildMessageReceivedEvent event) {
				return "Removes the specified roles";
			}
		});

		guildCommands.put("note", new GuildCommand() {
			public void runCommand(GuildMessageReceivedEvent event, String[] args) {
				if (args.length == 1) {
					String response = Notes.get(args[0], event.getAuthor().getId());
					if (response == null)
						event.getChannel().sendMessageAsync("Sorry, couldn't find that note for you", null);
					else
						event.getChannel().sendMessageAsync(response, null);
				}
				else if (args.length > 1) {
					boolean hasRights = (!(DiscordInfo.isOwner(event.getAuthor().getId()) || DiscordInfo.isAdmin(event.getGuild().getRolesForUser(event.getAuthor()))));
					
					if (args[0].equalsIgnoreCase("add")) {
						boolean isPublic = false;
						if (args[2].equals("1") || args[2].equalsIgnoreCase("public")) {
							isPublic = true;
							args[2] = args[3];
							if (args.length > 4) {
								for (int i = 4; i < args.length; i++)
									args[2] = String.join(", ", args[2], args[i]);
							}
						} else {
							if (args.length > 3) {
								for (int i = 3; i < args.length; i++)
									args[2] = String.join(", ", args[2], args[i]);
							}
						}
						
						if (Notes.add(args[1], event.getAuthor().getId(), args[2], (isPublic && hasRights)))
							event.getChannel().sendMessageAsync("Saved", null);
						else
							event.getChannel().sendMessageAsync("Error, something went wrong. Maybe there's already a note with that name?", null);
					} else if (args[0].equalsIgnoreCase("edit")) {
						if (args.length < 3) {
							event.getChannel().sendMessageAsync("Seems like you forgot to put the name or the new content in your message", null);
							return;
						}
						
						if (Notes.edit(args[1], event.getAuthor().getId(), args[2], hasRights))
							event.getChannel().sendMessageAsync("Saved", null);
						else
							event.getChannel().sendMessageAsync("No note with that name found or you aren't allowed to edit the ones I did find", null);
					} else if (args[0].equalsIgnoreCase("del")) {
						if (args.length < 2) {
							event.getChannel().sendMessageAsync("Seems like you forgot to put the name or the new content in your message", null);
							return;
						}
						
						if (Notes.delete(args[1], event.getAuthor().getId(), hasRights))
							event.getChannel().sendMessageAsync("Saved", null);
						else
							event.getChannel().sendMessageAsync("No note with that name found or you aren't allowed to edit the ones I did find", null);
					}
				}
			}
			
			public String getHelp(GuildMessageReceivedEvent event) {
				return "Relatively complicated. Refere to the guide linked below";
			}
		});
		
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
					Missions.create(args[1], event.getGuild().getManager(), explorer);
					event.getChannel().sendMessageAsync("Mission channel created and permissions set. Good luck!", null);
				}
				else if (args.length == 1 && args[0].equalsIgnoreCase("close")) {
					Missions.archiveRequest(event.getChannel(), event.getAuthor().getId());
					event.getChannel().sendMessageAsync("Please confirm with '/mission yes' that you actually want to delete this channel. You cannot undo this!", null);
				}
				else if (args.length == 1 && args[0].equalsIgnoreCase("yes")) {
					Missions.archive(event.getChannel(), event.getAuthor().getId());
					event.getJDA().getTextChannelById(DiscordInfo.getAdminChanID()).sendMessageAsync(event.getChannel().getName() + " channel and role deleted." ,null);
				}
			}
			
			public String getHelp(GuildMessageReceivedEvent event) {
				return "";
			}
		});
		
		guildCommands.put("bgs", new GuildCommand() {
			public void runCommand(GuildMessageReceivedEvent event, String[] args) {
				if (args.length == 0) {
					Role rBGS = null; //event.getGuild().getRoleById("");
					Role rIW = null;
					for (Role role : event.getGuild().getRoles()) {
						if (role.getName().equals("BGS"))
							rBGS = role;
						if (role.getName().equals("Iridium Wing"))
							rIW  = role;
					}
					
					if (event.getGuild().getRolesForUser(event.getAuthor()).contains(rBGS)) {
						event.getGuild().getManager().removeRoleFromUser(event.getAuthor(), rBGS).update();
						event.getChannel().sendMessageAsync("BGS role removed", null);
					}
					else if (event.getGuild().getRolesForUser(event.getAuthor()).contains(rIW)) {
						event.getGuild().getManager().addRoleToUser(event.getAuthor(), rBGS).update();
						event.getChannel().sendMessageAsync("BGS role added", null);
					}
				} else if (args.length == 1) {
					if (args[0].equalsIgnoreCase("mystats")) {
						String output = "```";
						for (Map.Entry<Activity, Double> entry : BGS.getTotalAmmount(event.getAuthor().getId()).entrySet()) {
							output += entry.getKey().toString() + ": " + NumberFormat.getInstance(Locale.GERMANY).format(entry.getValue().intValue()).replace('.', '\'') + "\n";
						}
						output += "```";
						event.getAuthor().getPrivateChannel().sendMessageAsync(output, null);
					} else if (args[0].equalsIgnoreCase("total")) {
						String output = "```";
						for (Map.Entry<Activity, Double> entry : BGS.getTotalAmmount().entrySet()) {
							output += entry.getKey().toString() + ": " + NumberFormat.getInstance(Locale.GERMANY).format(entry.getValue().intValue()).replace('.', '\'') + "\n";
						}
						output += "```";
						if (DiscordInfo.isAdmin(event.getGuild().getRolesForUser(event.getAuthor())))
							event.getChannel().sendMessageAsync(output, null);
						else
							event.getAuthor().getPrivateChannel().sendMessageAsync(output, null);
					}
				} else if (args.length == 2) {
					String activity = null;
					String username = event.getAuthorName();
					String userid = event.getAuthor().getId();
					int ammount = Integer.parseInt(args[1]);
					
					args[0] = args[0].toLowerCase();
					switch (args[0]) {
						case "bonds" 	 : activity = "BOND";	   break;
						case "bounties"  : activity = "BOUNTY";	   break;
						case "mining" 	 : activity = "MINING";	   break;
						case "missions"  : activity = "MISSION";   break;
						case "scans"	 : activity = "SCAN";	   break;
						case "smuggling" : activity = "SMUGGLING"; break;
						case "trade"	 : activity = "TRADE";	   break;
					}
					if (activity != null) {
						BGS.logActivity(Activity.valueOf(activity), userid, username, ammount);
						event.getChannel().sendMessageAsync("Your engagement has been noticed. Thanks for your service o7", null);
					}
					
				} else if (args.length == 3) {
					if (args[0].equalsIgnoreCase("gettick") && DiscordInfo.isAdmin(event.getGuild().getRolesForUser(event.getAuthor()))) {
						SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy HH:mm");
						Date time;
						try {
							time = sdf.parse(args[2]);
							String output = "Data for " + args[1] + " ticks after " + args[2] + " UTC:\n```";
							Map<Activity, Double> entries = BGS.getTotalAmmount(time, Integer.parseInt(args[1]));
							for (Map.Entry<Activity, Double> entry : entries.entrySet()) {
								output += entry.getKey().toString() + ": " + NumberFormat.getInstance(Locale.GERMANY).format(entry.getValue().intValue()).replace('.', '\'') + "\n";
							}
							output += "```";
							if (entries.isEmpty())
								event.getChannel().sendMessageAsync("No records for the specified period", null);
							else
								event.getChannel().sendMessageAsync(output, null);
						} catch (ParseException e) {
							event.getChannel().sendMessageAsync("Parsing error. Make sure the date follows the pattern 'dd/MM/yy HH:mm'", null);
						}
						
					} else if (args[0].equalsIgnoreCase("gettickfull") && DiscordInfo.isAdmin(event.getGuild().getRolesForUser(event.getAuthor()))) {
						SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy HH:mm");
						Date time;
						try {
							time = sdf.parse(args[2]);
							List<String> lines = BGS.getCSVData(time, Integer.parseInt(args[1]));
							
							String output = "Data for " + args[1] + " ticks after " + args[2] + ":\n";
							output += "----------------------------------------------------------------------\n";
							output += lines.get(0) + "\n```";
							for (int i = 1; i < lines.size(); i++) {
								output += lines.get(i) + "\n";
							}
							if (lines.size() < 2)
								output += "No records found";
							output += "```";
							
							event.getChannel().sendMessageAsync(output, null);
						} catch (ParseException e) {
							event.getChannel().sendMessageAsync("Parsing error. Make sure the date follows the pattern 'dd/MM/yy HH:mm'", null);
						}
						
					}
				}
			}
			
			public String getHelp(GuildMessageReceivedEvent event) {
				return "Add or remove the bgs role to you";
			}
		});
		
		guildCommands.put("completed", new GuildCommand() {
			public void runCommand(GuildMessageReceivedEvent event, String[] args) {
				event.getChannel().sendMessageAsync("Please use '/bgs *activity*, #' from now on, thanks", null);
			}
			
			public String getHelp(GuildMessageReceivedEvent event) {
				return "";
			}
		});

		guildCommands.put("roll", new GuildCommand() {
			public void runCommand(GuildMessageReceivedEvent event, String[] args) {
				if (args.length == 0) {
					event.getChannel().sendMessageAsync("You rolled a " + (new Random().nextInt(6) + 1), null);
				}
				else if (args.length == 1) {
					event.getChannel().sendMessageAsync("You rolled a " + (new Random().nextInt(Integer.parseInt(args[0])) + 1), null);
				}
			}

			public String getHelp(GuildMessageReceivedEvent event) {
				return "";
			}
		});
		
		guildCommands.put("channels", new GuildCommand() {
			public void runCommand(GuildMessageReceivedEvent event, String[] args) {
				//Permission check
				if (!(DiscordInfo.isOwner(event.getAuthor().getId()) || DiscordInfo.isAdmin(event.getGuild().getRolesForUser(event.getAuthor())))) {
					event.getChannel().sendMessageAsync("[Error] You aren't authorized to do this", null);
					return;
				}
				
				if (args.length == 1 && args[0].equalsIgnoreCase("lock")) {
					Channels.lock(event.getGuild().getTextChannels());
					event.getChannel().sendMessageAsync("Locked", null);
				}
				else if (args.length == 1 && args[0].equalsIgnoreCase("unlock")) {
					Channels.unlock();
					event.getChannel().sendMessageAsync("Unlocked", null);
				}
				else if (args.length == 2) {
					event.getJDA().getTextChannelById(args[0]).getManager().setPosition(Integer.parseInt(args[1])).update();
				}
			}
			
			public String getHelp(GuildMessageReceivedEvent event) {
				//Permission check
				if (!(DiscordInfo.isOwner(event.getAuthor().getId()) || DiscordInfo.isAdmin(event.getGuild().getRolesForUser(event.getAuthor())))) {
					return "";
				}
				return "Shows the channel details";
			}
		});
		
		guildCommands.put("memes", new GuildCommand() {
			public void runCommand(GuildMessageReceivedEvent event, String[] args) {
				//Permission check
				if (!(DiscordInfo.isOwner(event.getAuthor().getId()))) {
					event.getChannel().sendMessageAsync("[Error] You aren't authorized to do this", null);
					return;
				}
				
				if (args.length == 1 && args[0].equalsIgnoreCase("update")) {
					DankMemes.update();
					event.getChannel().sendMessageAsync("Memes updated from file.", null);
				}
			}
			
			public String getHelp(GuildMessageReceivedEvent event) {
				//Permission check
				if (!(DiscordInfo.isOwner(event.getAuthor().getId()))) {
					return "";
				}
				return "Interacts with the maymays";
			}
		});

		guildCommands.put("reminder", new GuildCommand() {
			public void runCommand(GuildMessageReceivedEvent event, String[] args) {
				String userid = event.getAuthor().getId();
				String reason;
				long time;

				if (args.length == 1) {
					reason = "";
					time = new Date().getTime();
					if (args[0].contains("s")) {
						time += Integer.parseInt(args[0].replace("s", "")) * 1000;
					}
					else if (args[0].contains("m")) {
						time += Integer.parseInt(args[0].replace("m", "")) * 1000 * 60;
					}
					else if (args[0].contains("h")) {
						time += Integer.parseInt(args[0].replace("h", "")) * 1000 * 60 * 60;
					}
					else if (args[0].contains("d")) {
						time += Integer.parseInt(args[0].replace("d", "")) * 1000 * 60 * 60 * 24;
					}
					else if (args[0].contains("w")) {
						time += Integer.parseInt(args[0].replace("w", "")) * 1000 * 60 * 60 * 24 * 7;
					}
					else if (args[0].contains("y")) {
						time += Integer.parseInt(args[0].replace("y", "")) * 1000 * 60 * 60 * 24 * 365;
					}
					else {
						event.getChannel().sendMessageAsync("Please specify the time unit (s, m, h, d, w, y)", null);
						return;
					}
				}
				else if (args.length == 2) {
					time = new Date().getTime();
					if (args[0].contains("s")) {
						time += Integer.parseInt(args[0].replace("s", "")) * 1000;
					}
					else if (args[0].contains("m")) {
						time += Integer.parseInt(args[0].replace("m", "")) * 1000 * 60;
					}
					else if (args[0].contains("h")) {
						time += Integer.parseInt(args[0].replace("h", "")) * 1000 * 60 * 60;
					}
					else if (args[0].contains("d")) {
						time += Integer.parseInt(args[0].replace("d", "")) * 1000 * 60 * 60 * 24;
					}
					else if (args[0].contains("w")) {
						time += Integer.parseInt(args[0].replace("w", "")) * 1000 * 60 * 60 * 24 * 7;
					}
					else if (args[0].contains("y")) {
						time += Integer.parseInt(args[0].replace("y", "")) * 1000 * 60 * 60 * 24 * 365;
					}
					else {
						event.getChannel().sendMessageAsync("Please specify the time unit (s, m, h, d, w, y)", null);
						return;
					}
					reason = args[1];
				}
				else {
					event.getChannel().sendMessageAsync("I need at least a delay after which to remind you.", null);
					return;
				}

				Reminder.add(userid, reason, time);
				event.getChannel().sendMessageAsync("Reminder set", null);
			}

			public String getHelp(GuildMessageReceivedEvent event) {
				return "Syntax is: '/reminder ##t, reason' - ## number, t time unit (s, m, h, d, w, y), reason is optional";
			}
		});

		guildCommands.put("whois", new GuildCommand() {
			public void runCommand(GuildMessageReceivedEvent event, String[] args) {
				if (args.length == 1) {
					String info = CMDRLookup.whois(args[0], false);
					event.getChannel().sendMessageAsync(info, null);
				}
				else if (args.length == 2) {
					String info = CMDRLookup.whois(args[0], args[1].equalsIgnoreCase("update"));
					event.getChannel().sendMessageAsync(info, null);
				}
			}

			public String getHelp(GuildMessageReceivedEvent event) {
				return "Finds all the information available on inara.cz and r/EliteCombatLoggers";
			}
		});
		
		//end of commands
	}
}
