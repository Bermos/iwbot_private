package commands.core_commands;

import commands.GuildCommand;
import commands.PMCommand;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import provider.DataProvider;
import provider.jda.Discord;
import provider.jda.Member;
import provider.jda.events.GuildMessageEvent;
import provider.jda.events.PrivateMessageEvent;

public class Restart implements PMCommand, GuildCommand {
    @Override
    public void runCommand(PrivateMessageEvent event, Discord discord) {
        //Permission check
        Member author = event.getDiscord().getMember("142749481530556416", event.getAuthor().getId());
        if ( !( DataProvider.isOwner(event.getAuthor().getId()) || (author != null && DataProvider.isAdmin(author.getRoles())) ) ) {
            event.replyAsync("[Error] You aren't authorized to do this");
            return;
        }

        event.replyAsync("Restarting...");
        System.exit(1);
    }

    @Override
    public void runCommand(GuildMessageEvent event, Discord discord) {
        //Permission check
        if (!(DataProvider.isOwner(event.getAuthor().getId()) || DataProvider.isAdmin(event.getMember().getRoles()))) {
            event.replyAsync("[Error] You aren't authorized to do this");
            return;
        }

        event.replyAsync("Restarting...");
        System.exit(1);
    }

    @Override
    public String getHelp(GuildMessageEvent event) {
        if (!(DataProvider.isOwner(event.getAuthor().getId()) || DataProvider.isAdmin(event.getMember().getRoles()))) {
            return "";
        }
        return "Restarts the bot. Use when he behaves strangely or throws a fit";
    }
}
