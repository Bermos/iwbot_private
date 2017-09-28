package commands.core_commands;

import commands.GuildCommand;
import commands.PMCommand;
import core.JDAUtil;
import core.Listener;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import provider.DataProvider;

public class Setgame implements PMCommand, GuildCommand {
    @Override
    public void runCommand(Listener listener, PrivateMessageReceivedEvent event, String[] args) {
        //Permission check
        if (!DataProvider.isBotAdmin(event)) {
            event.getChannel().sendMessage("[Error] You aren't authorized to do this").queue();
            return;
        }

        event.getChannel().sendMessage(setgame(event.getJDA(), args));
    }

    @Override
    public void runCommand(Listener listener, GuildMessageReceivedEvent event, String[] args) {
        //Permission check
        if (!JDAUtil.isAuthorized(event)) {
            event.getChannel().sendMessage("[Error] You aren't authorized to do this").queue();
            return;
        }

        event.getChannel().sendMessage(setgame(event.getJDA(), args));
    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        //Permission check
        if (!JDAUtil.isAuthorized(event))
            return "";
        return "<game?> - To set the Playing: ...";
    }

    private String setgame(JDA jda, String[] args) {
        if (args.length == 0)
            jda.getPresence().setGame(null);
        else
            jda.getPresence().setGame(Game.of(args[0]));
        return "[Success] Game changed";
    }
}
