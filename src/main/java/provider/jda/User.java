package provider.jda;

public class User {
    private String id;
    private Discord discord;

    public User(String id, Discord discord) {
        this.id = id;
        this.discord = discord;
    }

    public String getId() {
        return id;
    }

    public Discord getDiscord() {
        return discord;
    }

    public String getName() {
        return discord.getName(id);
    }
}
