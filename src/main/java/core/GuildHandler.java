package core;

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

public class GuildHandler {
    private String GUILDS_LOC;

    public GuildHandler(String guild_loc) {
        this.GUILDS_LOC = guild_loc;
    }

    public String getWelcomeMessage(String id) {
        return guilds.get(id).welcomeMessage;
    }

    public void setWelcomeMessage(String id, String message) {
        guilds.get(id).welcomeMessage = message;
    }

    public boolean autoWelcome(String guildId) {
        return guilds.get(guildId).welcome;
    }

    static class Guild {
        public String name;
        public String ownerId;
        public String ownerName;
        public String prefix;
        public String messageChannel;
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

    private LinkedHashMap<String, Guild> guilds = loadGuilds();

    public LinkedHashMap<String,Guild> loadGuilds() {
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

    private void saveGuilds() {
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

    public void newGuild(String guildId, String guildName, String ownerId, String ownerName) {
        if (guilds.containsKey(guildId)) {
            guilds.remove(guildId);
        }

        guilds.put(guildId, new Guild(guildName, ownerId, ownerName));
        saveGuilds();
    }

    public void memberJoined(String userId, String guildId) {
        guilds.get(guildId).newUsers.add(userId);
        saveGuilds();
    }

    public void leaveGuild(String id) {
        guilds.remove(id);
        saveGuilds();
    }

    public boolean isAutoWelcome(String id) {
        return guilds.get(id).autoWelcome;
    }

    public void welcome(String guildId, String userId) {
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

    public void updateGuildName(String id, String name) {
        guilds.get(id).name = name;

        saveGuilds();
    }

    public void setGuildPrefix(String id, String prefix) {
        if (id.equals("*")) {
            for (Guild guild : guilds.values())
                guild.prefix = prefix;
        }
        else
            guilds.get(id).prefix = prefix;

        saveGuilds();
    }

    /**
     *
     * @param id
     * @param channelId
     */
    public void setMessageChannel(String id, String channelId) {
        guilds.get(id).messageChannel = channelId;
    }

    /**
     * Returns the standard MessageChannel object the bot uses for the guild with the id
     * @param id of the Guild
     * @return the MessageChannel object
     */
    public String getMessageChannel(String id) {
        return guilds.get(id).messageChannel;
    }

    /**
     * Returns the prefix set for the guild
     * @param guildId of the guild
     * @return the prefix
     */
    public String getPrefix(String guildId) {
        return guilds.get(guildId).prefix;
    }
}
