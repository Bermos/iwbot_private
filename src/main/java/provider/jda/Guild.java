package provider.jda;

public class Guild {
    private String id;
    private Discord discord;

    public Guild(String id, Discord discord) {
        this.id = id;
        this.discord = discord;
    }

    public Member getMember(User user) {
        return discord.getMember(id, user.getId());
    }
}
