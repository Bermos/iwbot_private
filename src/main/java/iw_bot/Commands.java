package iw_bot;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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

import commands.GuildCommand;
import commands.PMCommand;
import commands.core_commands.*;
import commands.ed_commands.Distance;
import commands.iw_commands.Auth;
import misc.CMDRLookup;
import net.dv8tion.jda.core.entities.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import iw_core.BGS;
import iw_core.BGS.Activity;
import iw_core.Missions;
import iw_core.Notes;
import misc.Dance;
import misc.Reminder;
import misc.DankMemes;
import net.dv8tion.jda.core.entities.Message.Attachment;
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
		
		pmCommands.put("bgs", (event, args) -> {
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("mystats")) {
                    String output = "```";
                    for (Map.Entry<Activity, Double> entry : BGS.getTotalAmount(event.getAuthor().getId()).entrySet()) {
                        output += entry.getKey().toString() + ": " + NumberFormat.getInstance(Locale.GERMANY).format(entry.getValue().intValue()).replace('.', '\'') + "\n";
                    }
                    output += "```";
                    event.getChannel().sendMessage(output).queue();
                }
            }
        });
		
		pmCommands.put("time", new UTCTime());
		
		pmCommands.put("dist", new Distance());
		
		pmCommands.put("status", new Status());
		
		pmCommands.put("restart", (event, args) -> {
            //Permission check
			Member author = event.getJDA().getGuildById("142749481530556416").getMember(event.getAuthor());
			if ( !( DiscordInfo.isOwner(event.getAuthor().getId()) || (author != null && DiscordInfo.isAdmin(author.getRoles())) ) ) {
				event.getChannel().sendMessage("[Error] You aren't authorized to do this").queue();
				return;
			}

            event.getChannel().sendMessage("Trying to restart...").complete();
            System.exit(1);
        });

		//TODO That's how it should be done from now on...
		pmCommands.put("account", new Auth()); //done
		
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

		guildCommands.put("time", new GuildCommand() {
			public void runCommand(GuildMessageReceivedEvent event, String[] args) {
				Date date = new Date();
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
				sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
				event.getMessage().deleteMessage();
				event.getChannel().sendMessage("UTC time:\n" + sdf.format(date)).queue();
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
				
				event.getChannel().sendMessage(contOut).queue();
			}
			
			public String getHelp(GuildMessageReceivedEvent event) {
				return "Shows information about the bot";
			}
		});

		guildCommands.put("xkcd", new GuildCommand() {
			public void runCommand(GuildMessageReceivedEvent event, String[] args) {
				Image image;
				File file;
				Random RNJesus = new Random();
				int iRandom = RNJesus.nextInt(1791);
				String url = "http://xkcd.com/" + iRandom + "/";
				
				try {
					Document doc = Jsoup.connect(url).get();
					Elements images = doc.select("img[src$=.png]");
					
					for(Element eImage : images) {
						if(eImage.attr("src").contains("comic")) {
							URL uRl = new URL("http:" + eImage.attr("src"));
							image = ImageIO.read(uRl);
							ImageIO.write((RenderedImage) image, "png", file = new File("./temp/" + iRandom + ".png"));
							event.getChannel().sendFile(file, null).queue();
							//noinspection ResultOfMethodCallIgnored
							file.delete();
						}
					}
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
					guild.getController().removeRolesFromMember(guild.getMember(author), role).queue();
					output += "\n" + role.getName();
				}
				output += "```";
				
				event.getChannel().sendMessage(output).queue();
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
						event.getChannel().sendMessage("Sorry, couldn't find that note for you").queue();
					else
						event.getChannel().sendMessage(response).queue();
				}
				else if (args.length > 1) {
					boolean hasRights = (!(DiscordInfo.isOwner(event.getAuthor().getId()) || DiscordInfo.isAdmin(event.getGuild().getMember(event.getAuthor()).getRoles())));
					
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
							event.getChannel().sendMessage("Saved").queue();
						else
							event.getChannel().sendMessage("Error, something went wrong. Maybe there's already a note with that name?").queue();
					} else if (args[0].equalsIgnoreCase("edit")) {
						if (args.length < 3) {
							event.getChannel().sendMessage("Seems like you forgot to put the name or the new content in your message").queue();
							return;
						}
						
						if (Notes.edit(args[1], event.getAuthor().getId(), args[2], hasRights))
							event.getChannel().sendMessage("Saved").queue();
						else
							event.getChannel().sendMessage("No note with that name found or you aren't allowed to edit the ones I did find").queue();
					} else if (args[0].equalsIgnoreCase("del")) {
						if (args.length < 2) {
							event.getChannel().sendMessage("Seems like you forgot to put the name or the new content in your message").queue();
							return;
						}
						
						if (Notes.delete(args[1], event.getAuthor().getId(), hasRights))
							event.getChannel().sendMessage("Saved").queue();
						else
							event.getChannel().sendMessage("No note with that name found or you aren't allowed to edit the ones I did find").queue();
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
		
		guildCommands.put("bgs", new GuildCommand() {
			public void runCommand(GuildMessageReceivedEvent event, String[] args) {
				if (args.length == 0) {
					Role rBGS = null;
					Role rIW = null;
					for (Role role : event.getGuild().getRoles()) {
						if (role.getName().equals("BGS"))
							rBGS = role;
						if (role.getName().equals("Iridium Wing"))
							rIW  = role;
					}
					
					if (event.getMember().getRoles().contains(rBGS)) {
						event.getGuild().getController().removeRolesFromMember(event.getMember(), rBGS).queue();
						event.getChannel().sendMessage("BGS role removed").queue();
					}
					else if (event.getMember().getRoles().contains(rIW)) {
						event.getGuild().getController().addRolesToMember(event.getMember(), rBGS).queue();
						event.getChannel().sendMessage("BGS role added").queue();
					}
				} else if (args.length == 1) {
					if (args[0].equalsIgnoreCase("mystats")) {
						String output = "```";
						for (Map.Entry<Activity, Double> entry : BGS.getTotalAmount(event.getAuthor().getId()).entrySet()) {
							output += entry.getKey().toString() + ": " + NumberFormat.getInstance(Locale.GERMANY).format(entry.getValue().intValue()).replace('.', '\'') + "\n";
						}
						output += "```";
						event.getAuthor().getPrivateChannel().sendMessage(output).queue();
					} else if (args[0].equalsIgnoreCase("total")) {
						String output = "```";
						for (Map.Entry<Activity, Double> entry : BGS.getTotalAmount().entrySet()) {
							output += entry.getKey().toString() + ": " + NumberFormat.getInstance(Locale.GERMANY).format(entry.getValue().intValue()).replace('.', '\'') + "\n";
						}
						output += "```";
						if (DiscordInfo.isAdmin(event.getMember().getRoles()))
							event.getChannel().sendMessage(output).queue();
						else
							event.getAuthor().getPrivateChannel().sendMessage(output).queue();
					}
				} else if (args.length == 2) {
					String activity = null;
					String username = event.getMember().getEffectiveName();
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
						event.getChannel().sendMessage("Your engagement has been noticed. Thanks for your service o7").queue();
					}
					
				} else if (args.length == 3) {
					if (args[0].equalsIgnoreCase("gettick") && DiscordInfo.isAdmin(event.getMember().getRoles())) {
						SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy HH:mm");
						Date time;
						try {
							time = sdf.parse(args[2]);
							String output = "Data for " + args[1] + " ticks after " + args[2] + " UTC:\n```";
							Map<Activity, Double> entries = BGS.getTotalAmount(time, Integer.parseInt(args[1]));
							for (Map.Entry<Activity, Double> entry : entries.entrySet()) {
								output += entry.getKey().toString() + ": " + NumberFormat.getInstance(Locale.GERMANY).format(entry.getValue().intValue()).replace('.', '\'') + "\n";
							}
							output += "```";
							if (entries.isEmpty())
								event.getChannel().sendMessage("No records for the specified period").queue();
							else
								event.getChannel().sendMessage(output).queue();
						} catch (ParseException e) {
							event.getChannel().sendMessage("Parsing error. Make sure the date follows the pattern 'dd/MM/yy HH:mm'").queue();
						}
						
					} else if (args[0].equalsIgnoreCase("gettickfull") && DiscordInfo.isAdmin(event.getMember().getRoles())) {
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
							
							event.getChannel().sendMessage(output).queue();
						} catch (ParseException e) {
							event.getChannel().sendMessage("Parsing error. Make sure the date follows the pattern 'dd/MM/yy HH:mm'").queue();
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
				event.getChannel().sendMessage("Please use '/bgs *activity*, #' from now on, thanks").queue();
			}
			
			public String getHelp(GuildMessageReceivedEvent event) {
				return "";
			}
		});

		guildCommands.put("roll", new GuildCommand() {
			public void runCommand(GuildMessageReceivedEvent event, String[] args) {
				if (args.length == 0) {
					event.getChannel().sendMessage("You rolled a " + (new Random().nextInt(6) + 1)).queue();
				}
				else if (args.length == 1) {
					event.getChannel().sendMessage("You rolled a " + (new Random().nextInt(Integer.parseInt(args[0])) + 1)).queue();
				}
			}

			public String getHelp(GuildMessageReceivedEvent event) {
				return "";
			}
		});
		
		guildCommands.put("memes", new GuildCommand() {
			public void runCommand(GuildMessageReceivedEvent event, String[] args) {
				//Permission check
				if (!(DiscordInfo.isOwner(event.getAuthor().getId()))) {
					event.getChannel().sendMessage("[Error] You aren't authorized to do this").queue();
					return;
				}
				
				if (args.length == 1 && args[0].equalsIgnoreCase("update")) {
					DankMemes.update();
					event.getChannel().sendMessage("Memes updated from file.").queue();
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
						event.getChannel().sendMessage("Please specify the time unit (s, m, h, d, w, y)").queue();
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
						event.getChannel().sendMessage("Please specify the time unit (s, m, h, d, w, y)").queue();
						return;
					}
					reason = args[1];
				}
				else {
					event.getChannel().sendMessage("I need at least a delay after which to remind you.").queue();
					return;
				}

				Reminder.add(userid, reason, time);
				event.getChannel().sendMessage("Reminder set").queue();
			}

			public String getHelp(GuildMessageReceivedEvent event) {
				return "Syntax is: '/reminder ##t, reason' - ## number, t time unit (s, m, h, d, w, y), reason is optional";
			}
		});

		guildCommands.put("whois", new commands.ed_commands.CMDRLookup()); //

		guildCommands.put("clear", new BulkDelete()); //done

		//end of commands
	}
}
