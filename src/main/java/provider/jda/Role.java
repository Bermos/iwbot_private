package provider.jda;

public class Role {
    private String id;
    private String guildId;
    private Discord discord;

    public Role(String id,
                String guildId,
                Discord discord) {
        this.id = id;
        this.guildId = guildId;
        this.discord = discord;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return discord.getRoleName(id, guildId);
    }

    public void deleteAsync() {
        discord.deleteRoleAsync(id, guildId);
    }


}
