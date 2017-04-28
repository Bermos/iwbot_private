package commands.core_commands;

import commands.GuildCommand;
import commands.PMCommand;
import provider.DataProvider;
import provider.jda.Discord;
import provider.jda.events.GuildMessageEvent;
import provider.jda.events.PrivateMessageEvent;

public class Shutdown implements GuildCommand, PMCommand {
    @Override
    public void runCommand(PrivateMessageEvent event, Discord discord) {
        //Permission check
        if (!(DataProvider.isOwner(event.getAuthor().getId()))) {
            event.replyAsync("[Error] You aren't authorized to do this");
            return;
        }

        event.replyNow("Goodbye, master.");
        discord.shutdown();
        System.exit(0);
    }

    @Override
    public void runCommand(GuildMessageEvent event, Discord discord) {
        //Permission check
        if (!(DataProvider.isOwner(event.getAuthor().getId()))) {
            event.replyAsync("[Error] You aren't authorized to do this");
            return;
        }

        event.replyNow(event.getGuild().getEmoteById("281065961325461504") + " I don't want to go...");
        discord.shutdown();
        System.exit(0);
    }

    @Override
    public String getHelp(GuildMessageEvent event) {
        return "";
    }
}