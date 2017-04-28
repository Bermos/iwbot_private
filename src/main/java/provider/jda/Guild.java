package provider.jda;

import java.util.List;

public class Guild {
    private String id;
    private Discord discord;
    private GuildController controller;

    public Guild(String id, Discord discord) {
        this.id = id;
        this.discord = discord;
        this.controller = new GuildController(id, discord);
    }

    public Member getMember(User user) {
        return discord.getMember(id, user.getId());
    }

    public String getId() {
        return this.id;
    }

    public List<Role> getRolesByName(String name, boolean exact) {
        return discord.getRolesByName(id, name, exact);
    }

    public Role getRoleById(String roleId) {
        return new Role(roleId, id, discord);
    }

    public String getEmoteById(String emoteId) {
        return discord.getEmoteByGuild(id, emoteId);
    }

    public GuildController getController() {
        return controller;
    }
}
