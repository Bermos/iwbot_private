package ed_core;

import net.dv8tion.jda.core.JDA;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

public class GuildHandler {
    public static void leaveGuild(String id) {
    }

    public static boolean isAutoWelcome(String id) {
        return false;
    }

    public static void welcome(JDA jda, String id, String id1) {
    }

    class Guild {
        public String name;
        public String ownerId;
        public String ownerName;
        public String prefix;
        public String welcomeMessage;
        public String welcomeChannel;
        public boolean welcome;
        public boolean autoWelcome;
        public List<String> newUsers;

        public Guild(String name, String ownerId, String ownerName) {
            this.name = name;
            this.ownerId = ownerId;
            this.ownerName = ownerName;
            
            this.prefix = "!!";
            this.welcomeMessage = "";
            this.welcomeChannel = "";
            this.welcome = false;
            this.autoWelcome = false;
            this.newUsers = new LinkedList<>();
        }
    }

    private static LinkedHashMap<String, Guild> guilds = getGuilds();

    private static LinkedHashMap<String,Guild> getGuilds() {
        return guilds;
    }

    public static void newGuild(String guildId, String ownerId, String ownerName) {
        //TODO
    }

    public static void joined(String userID, String guildId) {
        //TODO
    }
}
