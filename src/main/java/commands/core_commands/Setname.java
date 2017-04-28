package commands.core_commands;

import commands.GuildCommand;
import commands.PMCommand;
import provider.DataProvider;
import provider.jda.Discord;
import provider.jda.events.GuildMessageEvent;
import provider.jda.events.PrivateMessageEvent;

public class Setname implements PMCommand, GuildCommand {
    @Override
    public void runCommand(PrivateMessageEvent event, Discord discord) {
        //Permission check
        if (!(DataProvider.isOwner(event.getAuthor().getId()))) {
            event.replyAsync("[Error] You aren't authorized to do this");
            return;
        }

        event.getChannel().sendMessage(setname(event.getDiscord(), event.getArgs()));
    }

    @Override
    public void runCommand(GuildMessageEvent event, Discord discord) {
        //Permission check
        if (!(DataProvider.isOwner(event.getAuthor().getId()) || DataProvider.isAdmin(event.getGuild().getMember(event.getAuthor()).getRoles()))) {
            event.replyAsync("[Error] You aren't authorized to do this");
            return;
        }

        event.getChannel().sendMessage(setname(event.getDiscord(), event.getArgs()));
    }

    @Override
    public String getHelp(GuildMessageEvent event) {
        //Permission check
        if (!(DataProvider.isOwner(event.getAuthor().getId()) || DataProvider.isAdmin(event.getGuild().getMember(event.getAuthor()).getRoles())))
            return "";
        return "<name>";
    }

    private String setname(Discord discord, String[] args) {
        if (args.length == 0)
            return "[Error] No name stated";

        discord.setName(args[0]);
        return "[Success] Name changed";
    }
}
