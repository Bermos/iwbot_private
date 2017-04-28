package commands.core_commands;

import commands.GuildCommand;
import commands.PMCommand;
import net.dv8tion.jda.core.entities.Game;
import provider.DataProvider;
import provider.jda.Discord;
import provider.jda.events.GuildMessageEvent;
import provider.jda.events.PrivateMessageEvent;

public class Setgame implements PMCommand, GuildCommand {
    @Override
    public void runCommand(PrivateMessageEvent event, Discord discord) {
        //Permission check
        if (!(DataProvider.isOwner(event.getAuthor().getId()))) {
            event.replyAsync("[Error] You aren't authorized to do this");
            return;
        }

        event.getChannel().sendMessage(setgame(event.getDiscord(), event.getArgs()));
    }

    @Override
    public void runCommand(GuildMessageEvent event, Discord discord) {
        //Permission check
        if (!(DataProvider.isOwner(event.getAuthor().getId()) || DataProvider.isAdmin(event.getGuild().getMember(event.getAuthor()).getRoles()))) {
            event.replyAsync("[Error] You aren't authorized to do this");
            return;
        }

        event.getChannel().sendMessage(setgame(event.getDiscord(), event.getArgs()));
    }

    @Override
    public String getHelp(GuildMessageEvent event) {
        //Permission check
        if (!(DataProvider.isOwner(event.getAuthor().getId()) || DataProvider.isAdmin(event.getGuild().getMember(event.getAuthor()).getRoles())))
            return "";
        return "<game?> - To set the Playing: ...";
    }

    private String setgame(Discord discord, String[] args) {
        if (args.length == 0)
            discord.setGame(null);
        else
            discord.setGame(Game.of(args[0]));
        return "[Success] Game changed";
    }
}
