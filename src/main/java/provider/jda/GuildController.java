package provider.jda;

public class GuildController {
    private String id;
    private Discord discord;
    
    public GuildController(String id, Discord discord) {
        this.id = id;
        this.discord = discord;
    }

    public void addRolesToMember(Member applicantMem, Role pc) {
        discord.addRolesToMember(id, applicantMem, pc);
    }

    public Discord createRole() {
        discord.createRole(id);
        return discord;
    }
}
