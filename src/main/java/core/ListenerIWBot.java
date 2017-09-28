package core;

import commands.misc_commands.Reminder;
import iw_core.Users;
import misc.DankMemes;
import misc.StatusGenerator;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.core.events.user.UserAvatarUpdateEvent;
import net.dv8tion.jda.core.events.user.UserNameUpdateEvent;
import net.dv8tion.jda.core.events.user.UserOnlineStatusUpdateEvent;
import provider.DataProvider;

import java.util.Date;

import static core.Main.SDF_TIME;

public class ListenerIWBot extends Listener{

    public ListenerIWBot(String botName) {
        super(botName);
    }

    @Override
    public void onReady(ReadyEvent event) {
        super.onReady(event);

        //Initial parsing of the memes.json file
        DankMemes.update();

        if (!DataProvider.isDev(BOT_NAME)) {
            //Start random Playing... generator
            new StatusGenerator(event.getJDA().getPresence());
        }

        //Setup and synchronise users and online status with MySQL db
        new Users();
        Users.sync(event);

        //Start checks for any set reminders from users
        new Reminder().startChecks(event.getJDA());
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        super.onGuildMemberJoin(event);
        Users.joined(event);
    }

    @Override
    public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
        Users.left(event);
    }

    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
        Users.roleUpdate(event);
    }

    @Override
    public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
        Users.roleUpdate(event);
    }

    @Override
    public void onUserOnlineStatusUpdate(UserOnlineStatusUpdateEvent event) {
        if (isDebug)
            System.out.printf("[" + SDF_TIME.format(new Date()) + "][Online Status] %s: %s\n", event.getUser().getName(), event.getGuild().getMember(event.getUser()).getOnlineStatus().name());

        Users.setOnlineStatus(event);
    }

    @Override
    public void onUserNameUpdate(UserNameUpdateEvent event) {
        Users.nameUpdate(event);
    }

    @Override
    public void onUserAvatarUpdate(UserAvatarUpdateEvent event) {
        Users.avatarUpdate(event);
    }
}
