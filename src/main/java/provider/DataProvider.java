package provider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import iw_bot.LogUtil;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class DataProvider {
	private static Info info = getInfo();
	public static String lastMessageSent;

    static class ConData {
        String IP;
        String DB;
        String US;
        String PW;
    }

    public class Info {
		class Discord {
			String token;
			List<String> idOwner;
			List<String> idRoles;
			String newMember;
			String adminChanID;
			String prefix;
		}

		class Inara {
			String name;
			String password;
		}

		class Bot {
			String name;
			Map<String, String> pmCommands;
			Map<String, String> guildCommands;
		}

		Discord discord;
		Map<String, ConData> connections;
		Inara inara;
		String googleToken;
		String githubToken;
		String JAVA_HOME;
		boolean dev;
		Bot bot;
	}

	public static Info getInfoBackup() {
		return info;
	}

	public static void revertToBackup(Info backupInfo) {
		info = backupInfo;
	}

    public static void setDiscordToken(String discordToken) {
        info.discord.token = discordToken;

        setInfo();
    }

    public static void setPrefix(String prefix) {
        info.discord.prefix = prefix;

        setInfo();
    }

    public static void setPrefix(String guildId, String prefix) {
        //TODO
        info.discord.prefix = prefix;

        setInfo();
    }


    public static String getPrefix() {
        return info.discord.prefix;
    }

	private static Info getInfo() {
		try {
			Gson gson = new Gson();
			JsonReader jReader = new JsonReader(new FileReader("./data.json"));
			return gson.fromJson(jReader, Info.class);
		} catch (FileNotFoundException e) {
			LogUtil.logErr(e);
		}
		System.out.println("ERROR data.json file not found. Exiting...");
		System.exit(2);
		return null;
	}
	
	private static void setInfo() {
		try {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			JsonWriter jWriter = new JsonWriter(new FileWriter("./data.json"));
			jWriter.setHtmlSafe(false);
			jWriter.setIndent("  ");
			gson.toJson(info, Info.class, jWriter);
			jWriter.close();
		} catch (IOException e) {
			LogUtil.logErr(e);
		}
	}
	
	/**
	 * Returns the token necessary to login to Discord
	 * 
	 * @return the token to login to Discord
	 */
	public static String getToken() {
		return info.discord.token;
	}
	
	/**
	 * Returns a list of all owners that are allowed
	 * to perform critical changes
	 * 
	 * @return list of ID strings
	 */
	public static List<String> getOwnerIDs() {
		return info.discord.idOwner;
	}
	
	/**
	 * 
	 * @param id of the owner to add
	 */
	public static void addOwner(String id) {
		info.discord.idOwner.add(id);
		setInfo();
	}
	
	/**
	 * 
	 * @param id of the owner to remove
	 */
	public static boolean removeOwner(String id) {
		if (info.discord.idOwner.indexOf(id) == -1)
		    return false;
		info.discord.idOwner.remove(info.discord.idOwner.indexOf(id));
		setInfo();
		return true;
	}
	
	/**
	 * Get the saved message for new members.
	 * 
	 * @return message as string
	 */
	public static String getNewMemberInfo() {
		return info.discord.newMember;
	}
	
	/**
	 * Save a new message for the new members.
	 * 
	 * @param message as string
	 */
	public static void setNewMemberInfo(String message) {
		info.discord.newMember = message;
		setInfo();
	}

	/**
	 * 
	 * @return the admin channel id as string
	 */
	public static String getAdminChanID() {
		return info.discord.adminChanID;
	}
	
	/**
	 * 
	 * @param id of the channel used for admin
	 */
	public static void setAdminChanID(String id) {
		info.discord.adminChanID = id;
		setInfo();
	}
	
	/**
	 * 
	 * @return the ids of all admin roles
	 */
	public static List<String> getAdminRoleIDs() {
		return info.discord.idRoles;
	}
	
	/**
	 * 
	 * @param id of the admin role
	 */
	public static void addAdminRoleID(String id) {
		info.discord.idRoles.add(id);
		setInfo();
	}
	
	public static void removeAdminRoleID(String id) {
		info.discord.idRoles.remove(id);
		setInfo();
	}

	public static boolean isOwner(GuildMessageReceivedEvent event) {
		return getOwnerIDs().contains(event.getAuthor().getId());
	}

	public static boolean isOwner(PrivateMessageReceivedEvent event) {
		return getOwnerIDs().contains(event.getAuthor().getId());
	}

	public static boolean isOwner(String id) {
		return getOwnerIDs().contains(id);
	}

	public static boolean isAdmin(GuildMessageReceivedEvent event) {
		boolean isAdmin = false;
		for (Role role : event.getMember().getRoles()) {
			if (getAdminRoleIDs().contains(role.getId()))
				isAdmin = true;
		}
		return isAdmin;
	}

	public static boolean isAdmin(List<Role> roles) {
		boolean isAdmin = false;
		for (Role role : roles) {
			if (getAdminRoleIDs().contains(role.getId()))
				isAdmin = true;
		}
		return isAdmin;
	}

	public static boolean isAdmin(String[] roleIds) {
		boolean isAdmin = false;
		for (String roleId : roleIds) {
			if (getAdminRoleIDs().contains(roleId))
				isAdmin = true;
		}
		return isAdmin;
	}

	public static String getInaraPW() {
		return info.inara.password;
	}

	public static String getInaraName() {
	    return info.inara.name;
    }

	public static String getGoogleToken() {
		return info.googleToken;
	}

	/**
	 *
	 * @return if the bot runs in development
	 */
	public static boolean isDev () {
		return info.dev;
	}

	public static ConData getConData(String conName) {
		return info.connections.get(conName);
	}

	public static String getGithubToken() {
	    return info.githubToken;
    }

    public static void addConnection(String name, String ip, String db, String us, String pw) {
	    ConData con = new ConData();
	    con.IP = ip; con.DB = db;
	    con.US = us; con.PW = pw;
	    info.connections.put(name, con);

	    setInfo();
    }

	public static String getJavaHome() {
		return info.JAVA_HOME;
	}

	public static Map<String, String> getGuildCommands() {
		return info.bot.guildCommands;
	}

	public static Map<String, String> getPMCommands() {
		return info.bot.pmCommands;
	}

    public static String getBotName() {
        return info.bot.name;
    }

    public static boolean isGuildOwner(String id, String id1) {
        //TODO
	    return false;
	}
}
