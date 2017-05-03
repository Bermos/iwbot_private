package ed_core;

import core.Listener;
import net.dv8tion.jda.core.JDA;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

public class GuildHandler {

    static class Guild {
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

    public static void newGuild(String guildId, String guildName, String ownerId, String ownerName) {
        if (guilds.containsKey(guildId)) {
            guilds.remove(guildId);
        }

        guilds.put(guildId, new Guild(guildName, ownerId, ownerName));
    }

    public static void memberJoined(String userId, String guildId) {
        guilds.get(guildId).newUsers.add(userId);
    }

    public static void leaveGuild(String id) {
        guilds.remove(id);
    }

    public static boolean isAutoWelcome(String id) {
        return guilds.get(id).autoWelcome;
    }

    public static void welcome(String guildId, String userId) {
        Guild guild = guilds.get(guildId);
        JDA jda = Listener.jda;

        // If a welcome message is set
        if (guild.welcome) {
            String message;
            if (guild.welcomeMessage.contains("<user>")) {
                message = guild.welcomeMessage.replace("<user>", jda.getUserById(userId).getAsMention());
            } else {
                message = jda.getUserById(userId).getAsMention() + " " + guild.welcomeMessage;
            }
            jda.getGuildById(guildId).getTextChannelById(guild.welcomeChannel).sendMessage(message).queue();

        // Store user for when a welcome message is set
        } else {
            guilds.get(guildId).newUsers.add(userId);
        }

    }

    public static void updateGuildName(String id, String name) {
        guilds.get(id).name = name;
    }
}
