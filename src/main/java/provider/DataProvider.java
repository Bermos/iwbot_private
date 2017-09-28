package provider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import core.LogUtil;
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

	static class Login {
	    public String username;
	    public String password;
    }

    static class ConData {
        String IP;
        String DB;
        String US;
        String PW;
    }

    public static class Bot {
        public String token;
        public String pmPrefix;
		public boolean dev;
        public Map<String, ConData> connections;
        public Map<String, String> pmCommands;
        public Map<String, String> guildCommands;
    }

    public class Info {
        List<String> botAdminIds;
		String JAVA_HOME;
		Map<String, Login> logins;
		Map<String, Bot> bots;
	}

	// Read info from data.json
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

	// Save info into data.json
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
     * Takes a string to set as discord token for the specified bot
     *
     * @param botName of the bot to set the token for
     * @param discordToken to be set
     */
    public static void setDiscordToken(String botName, String discordToken) {
        info.bots.get(botName).token = discordToken;

        setInfo();
    }
	
	/**
	 * Returns the token necessary to login to Discord
	 *
	 * @param botName from the bot you want the token for
	 * @return the token to login to Discord
	 */
	public static String getToken(String botName) {
		return info.bots.get(botName).token;
	}

	/**
	 * Returns a string containing the configured password for Inara
	 *
	 * @return password as string
	 */
	public static String getLoginUsername(String type) {
		return info.logins.get(type).username;
	}

	/**
	 * Returns a string containing the configured username for Inara
	 *
	 * @return username as string
	 */
	public static String getLoginPassword(String type) {
	    return info.logins.get(type).password;
    }

    /**
     * Returns true if the author is a bot admin
     *
     * @param event the message event
     * @return true if the author is a bot admin
     */
    public static boolean isBotAdmin(GuildMessageReceivedEvent event) {
        return info.botAdminIds.contains(event.getAuthor().getId());
    }

    /**
     * Returns true if the author is a bot admin
     *
     * @param event the message event
     * @return true if the author is a bot admin
     */
    public static boolean isBotAdmin(PrivateMessageReceivedEvent event) {
        return info.botAdminIds.contains(event.getAuthor().getId());
    }

	/**
	 * Returns if the bot runs in development
	 *
	 * @param botName name of the bot the dev status belongs to
	 * @return if the bot runs in development
	 */
	public static boolean isDev (String botName) {
		return info.bots.get(botName).dev;
	}

    /**
     * Returns a object of ConData containing connection data for a
     * database with the given name for the specified bot. Can be
     * null if there is no connection or bot.
     *
     * @param conName name of the connection
     * @param botName name of the bot the connection belongs to
     * @return conData object of requested connection
     */
	static ConData getConData(String conName, String botName) {
	    if (info.bots.get(botName) == null)
	        return null;

		return info.bots.get(botName).connections.get(conName);
	}

	/**
	 * Takes connection information and a name to save a new connection to the config file
	 *
     * @param botName of the bot the connection belongs to
	 * @param conName by which the connection can be retrieved
	 * @param ip where the database can be reached
	 * @param db name of the database
	 * @param us username for the connection
	 * @param pw password for the connection
	 */
    public static void addConnection(String botName, String conName, String ip, String db, String us, String pw) {
	    ConData con = new ConData();
	    con.IP = ip; con.DB = db;
	    con.US = us; con.PW = pw;
	    info.bots.get(botName).connections.put(conName, con);

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
