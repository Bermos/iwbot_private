package core;

import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.update.GuildUpdateNameEvent;

public class ListenerEDBot extends Listener {

    public ListenerEDBot(String botName) {
        super(botName);
    }

    @Override
    public void onGuildJoin (GuildJoinEvent event) {
        GuildHandler.newGuild(event.getGuild().getId(), event.getGuild().getName(),event.getGuild().getOwner().getUser().getId(), event.getGuild().getOwner().getEffectiveName());

        event.getGuild().getPublicChannel().sendMessage("Hello. Thanks for inviting me to this server.\n"
                + "My standard prefix is '!!' you can use '!!setprefix <symbol>' to change it to your likings.\n"
                + "However only people with the Admin permission can do so.\n"
                + "You can use '!!help' to get some help.").queue();
    }

    @Override
    public void onGuildLeave (GuildLeaveEvent event) {
        GuildHandler.leaveGuild(event.getGuild().getId());
    }

    @Override
    public void onGuildMemberJoin (GuildMemberJoinEvent event) {
        if (GuildHandler.isAutoWelcome(event.getGuild().getId())) {
            GuildHandler.welcome(event.getGuild().getId(), event.getMember().getUser().getId());
        }
        else {
            GuildHandler.memberJoined(event.getMember().getUser().getId(), event.getGuild().getId());
        }
    }

    @Override
    public void onGuildUpdateName (GuildUpdateNameEvent event) {
        GuildHandler.updateGuildName(event.getGuild().getId(), event.getGuild().getName());
    }
}
