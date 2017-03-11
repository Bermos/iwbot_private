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

    public String getId() {
        return this.id;
    }

    public Iterable<? extends Role> getRolesByName(String name, boolean exact) {
        return discord.getRolesByName(id, name, exact);
    }
}
