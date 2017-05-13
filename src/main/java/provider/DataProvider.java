package provider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import core.LogUtil;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static core.Main.CONFIG_LOC;

public class DataProvider {
	private static Info info = getInfo();
	public static String lastMessageSent;

    static class ConData {
        String IP;
        String DB;
        String US;
        String PW;
    }

    public static class Bot {
        String token;
        Map<String, String> pmCommands;
        Map<String, String> guildCommands;

        public String getToken() {
            return token;
        }
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

		Discord discord;
		Map<String, ConData> connections;
		Inara inara;
		String googleToken;
		String githubToken;
		String JAVA_HOME;
		boolean dev;
		Map<String, Bot> bots;
	}

	private static Info getInfo() {
		try {
			Gson gson = new Gson();
			JsonReader jReader = new JsonReader(new FileReader(CONFIG_LOC));
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
			JsonWriter jWriter = new JsonWriter(new FileWriter(CONFIG_LOC));
			jWriter.setHtmlSafe(false);
			jWriter.setIndent("  ");
			gson.toJson(info, Info.class, jWriter);
			jWriter.close();
		} catch (IOException e) {
			LogUtil.logErr(e);
		}
	}

	/**
	 * Gets the info object for backup purposes
	 *
	 * @return info object
	 */
	public static Info getInfoBackup() {
		return info;
	}

	/**
	 * Give a info object to restore a previous state.
	 *
	 * @param backupInfo
	 */
	public static void revertToBackup(Info backupInfo) {
		info = backupInfo;
	}

	/**
	 * Takes a string to set as a discord token
	 *
	 * @param discordToken
	 */
    public static void setDiscordToken(String discordToken) {
        info.discord.token = discordToken;

        setInfo();
    }

	/**
	 * Takes a string to set as a prefix for the bot
	 *
	 * @param prefix
	 */
	public static void setPrefix(String prefix) {
        info.discord.prefix = prefix;

        setInfo();
    }

	/**
	 * Sets the prefix for a specific guild.
	 *
	 * @param guildId Id of the guild to set the prefix for.
	 * @param prefix The prefix to set
	 */
	public static void setPrefix(String guildId, String prefix) {
        //TODO
        info.discord.prefix = prefix;

        setInfo();
    }

	/**
	 * Gets the standard prefix
	 *
	 * @return string of the prefix
	 */
    public static String getPrefix() {
        return info.discord.prefix;
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
	 * Takes a string of a discord user id to add to the list of owners
	 *
	 * @param id of the owner to add
	 */
	public static void addOwner(String id) {
		info.discord.idOwner.add(id);
		setInfo();
	}
	
	/**
	 * Takes a string of an id of a discord user to remove
	 * from the list of owners
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
	 * Returns the string of a discord channel id of the admin channel
	 * 
	 * @return the admin channel id as string
	 */
	public static String getAdminChanID() {
		return info.discord.adminChanID;
	}
	
	/**
	 * Takes a string of a discord channel id to set as the new admin channel
     *
	 * @param id of the channel used for admin
	 */
	public static void setAdminChanID(String id) {
		info.discord.adminChanID = id;
		setInfo();
	}
	
	/**
     * Returns a list of discord role ids of roles that are allowed to
	 * administer the bot
	 * 
	 * @return the ids of all admin roles
	 */
	public static List<String> getAdminRoleIDs() {
		return info.discord.idRoles;
	}
	
	/**
	 * Takes a string of a discord role id to add to the list of roles
	 * with admin privileges for the bot
	 *
	 * @param id of the admin role
	 */
	public static void addAdminRoleID(String id) {
		info.discord.idRoles.add(id);
		setInfo();
	}

	/**
	 * Takes a string of a discord role id to remove from the list of
	 * roles with admin privileges for the bot
	 *
	 * @param id of the admin role
	 */
	public static void removeAdminRoleID(String id) {
		info.discord.idRoles.remove(id);
		setInfo();
	}

	/**
	 * Takes a GuildMessageReceivedEvent to check if the author of
	 * the message is an owner of the bot
	 *
	 * @param event
	 * @return true if the author is a owner
	 */
	public static boolean isOwner(GuildMessageReceivedEvent event) {
		return getOwnerIDs().contains(event.getAuthor().getId());
	}

	/**
	 * Takes a PrivateMessageReceivedEvent to check if the author of
	 * the message is an owner of the bot
	 *
	 * @param event
	 * @return true if the author is a owner
	 */
	public static boolean isOwner(PrivateMessageReceivedEvent event) {
		return getOwnerIDs().contains(event.getAuthor().getId());
	}

	/**
	 * Takes a string of a discord user id to check if he is a owner
	 * of the bot
	 *
	 * @param id of the user in question
	 * @return true if the user is a owner
	 */
	public static boolean isOwner(String id) {
		return getOwnerIDs().contains(id);
	}

	/**
	 * Takes a GuildMessageEvent to check if the author is a member
	 * of the roles that have admin privileges for the bot
	 *
	 * @param event
	 * @return true if the author has admin privileges
	 */
	public static boolean isAdmin(GuildMessageReceivedEvent event) {
		boolean isAdmin = false;
		for (Role role : event.getMember().getRoles()) {
			if (getAdminRoleIDs().contains(role.getId()))
				isAdmin = true;
		}
		return isAdmin;
	}

	/**
	 * Takes a list of Roles to check if any of them is given
	 * admin privileges for the bot
	 *
	 * @param roles
	 * @return true if any of the roles has admin privileges
	 */
	public static boolean isAdmin(List<Role> roles) {
		boolean isAdmin = false;
		for (Role role : roles) {
			if (getAdminRoleIDs().contains(role.getId()))
				isAdmin = true;
		}
		return isAdmin;
	}

	/**
	 * Takes a list of strings of discord role ids to check if any
	 * of the associated roles has admin privileges for the bot
	 *
	 * @param roleIds
	 * @return true if any of the roles has admin privileges
	 */
	public static boolean isAdmin(String[] roleIds) {
		boolean isAdmin = false;
		for (String roleId : roleIds) {
			if (getAdminRoleIDs().contains(roleId))
				isAdmin = true;
		}
		return isAdmin;
	}

	/**
	 * Returns a string containing the configured password for Inara
	 *
	 * @return password as string
	 */
	public static String getInaraPW() {
		return info.inara.password;
	}

	/**
	 * Returns a string containing the configured username for Inara
	 *
	 * @return username as string
	 */
	public static String getInaraName() {
	    return info.inara.name;
    }

	/**
	 * Returns a string containing the configured token for googles API
	 *
	 * @return token as string
	 */
	public static String getGoogleToken() {
		return info.googleToken;
	}

	/**
	 * Returns if the bot runs in development
	 *
	 * @return if the bot runs in development
	 */
	public static boolean isDev () {
		return info.dev;
	}

	/**
	 * Returns a object of ConData containing connection data for a
	 * database with the given name
	 *
	 * @param conName the name of the connection as in the config file
	 * @return ConData object containing connection info
	 */
	static ConData getConData(String conName) {
		return info.connections.get(conName);
	}

	/**
	 * Returns a string containing the configured token for github
	 *
	 * @return token as string
	 */
	public static String getGithubToken() {
	    return info.githubToken;
    }

	/**
	 * Takes connection information and a name to save a new connection to the config file
	 *
	 * @param name by which the connection can be retrieved
	 * @param ip where the database can be reached
	 * @param db name of the database
	 * @param us username for the connection
	 * @param pw password for the connection
	 */
    public static void addConnection(String name, String ip, String db, String us, String pw) {
	    ConData con = new ConData();
	    con.IP = ip; con.DB = db;
	    con.US = us; con.PW = pw;
	    info.connections.put(name, con);

	    setInfo();
    }

	/**
	 * Returns a string containing the path to the java binaries
	 *
	 * @return string containing java path
	 */
	public static String getJavaHome() {
		return info.JAVA_HOME;
	}

	/**
	 * Returns a map of guild commands by name with their corresponding location in the .jar
	 *
     * @param name of the bot you want the commands for
	 * @return map of guild commands
	 */
	public static Map<String, String> getGuildCommands(String name) {
		return info.bots.get(name).guildCommands;
	}

    /**
     * Returns a map of pm commands by name with their corresponding location in the .jar
     *
     * @param name of the bot you want the commands for
     * @return map of pm commands
     */
	public static Map<String, String> getPMCommands(String name) {
		return info.bots.get(name).pmCommands;
	}

    /**
     * Returns a bot object that corresponds to the given name
     *
     * @param name of the bot
     * @return the bot object
     */
    public static Bot getBotByName(String name) {
        return info.bots.get(name);
    }

    /**
     * Returns a list of strings of all bot names
     *
     * @return list of bot names
     */
    public static String[] getBotNames() {
        return (String[]) info.bots.keySet().toArray();
    }

    /**
     * Returns a map of all bots
     *
     * @return map of all bots
     */
    public static Map<String, Bot> getBots() {
        return info.bots;
    }
}
