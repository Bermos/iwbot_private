package commands.core_commands;

import commands.GuildCommand;
import commands.PMCommand;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import provider.DiscordInfo;

public class Restart implements PMCommand, GuildCommand {
    @Override
    public void runCommand(PrivateMessageReceivedEvent event, String[] args) {
        //Permission check
        Member author = event.getJDA().getGuildById("142749481530556416").getMember(event.getAuthor());
        if ( !( DiscordInfo.isOwner(event) || (author != null && DiscordInfo.isAdmin(author.getRoles())) ) ) {
            event.getChannel().sendMessage("[Error] You aren't authorized to do this").queue();
        }

        event.getChannel().sendMessage("Restarting...").queue();
        System.exit(1);
    }

    @Override
    public void runCommand(GuildMessageReceivedEvent event, String[] args) {
        //Permission check
        if (!(DiscordInfo.isOwner(event) || DiscordInfo.isAdmin(event))) {
            event.getChannel().sendMessage("[Error] You aren't authorized to do this").queue();
            return;
        }

        event.getChannel().sendMessage("Restarting...").queue();
        System.exit(1);
    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        return null;
    }
}
