package provider.jda;

public class Member extends User {
    private String guildId;

    public Member(String id, Discord discord, String guildId) {
        super(id, discord);
        this.guildId = guildId;
    }

    public String[] getRoles() {
        return super.getDiscord().getRoleIdsForMember(super.getId(), guildId);
    }

    public String getEffectiveName() {
        return super.getDiscord().getEffectiveName(super.getId(), guildId);
    }
}
