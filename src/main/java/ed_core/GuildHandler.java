package ed_core;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import core.Listener;
import net.dv8tion.jda.core.JDA;

import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import static core.Main.GUILDS_LOC;

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

    private static LinkedHashMap<String, Guild> guilds = loadGuilds();

    public static LinkedHashMap<String,Guild> loadGuilds() {
        LinkedHashMap<String, Guild> guilds;
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Guild>>() {}.getType();
        try {
            guilds = gson.fromJson(new JsonReader(new FileReader(GUILDS_LOC)), listType);
            return guilds;
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.exit(2);
        return null;
    }

    private static void saveGuilds() {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Guild>>() {}.getType();
        try {
            JsonWriter jw = new JsonWriter(new FileWriter(GUILDS_LOC));
            jw.setIndent("  ");
            gson.toJson(guilds, listType, jw);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void newGuild(String guildId, String guildName, String ownerId, String ownerName) {
        if (guilds.containsKey(guildId)) {
            guilds.remove(guildId);
        }

        guilds.put(guildId, new Guild(guildName, ownerId, ownerName));
        saveGuilds();
    }

    public static void memberJoined(String userId, String guildId) {
        guilds.get(guildId).newUsers.add(userId);
        saveGuilds();
    }

    public static void leaveGuild(String id) {
        guilds.remove(id);
        saveGuilds();
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

        saveGuilds();
    }

    public static void updateGuildName(String id, String name) {
        guilds.get(id).name = name;

        saveGuilds();
    }
}
