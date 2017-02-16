package commands.misc_commands;

import commands.GuildCommand;
import commands.PMCommand;
import misc.DankMemes;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import provider.DataProvider;

public class Memes implements PMCommand, GuildCommand {
    @Override
    public void runCommand(PrivateMessageReceivedEvent event, String[] args) {
        event.getChannel().sendMessage(memes(event.getAuthor().getId(), args)).queue();
    }

    @Override
    public void runCommand(GuildMessageReceivedEvent event, String[] args) {
        event.getChannel().sendMessage(event.getAuthor().getId()).queue();
    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        //Permission check
        if (!(DataProvider.isOwner(event.getAuthor().getId()))) {
            return "";
        }
        return "Interacts with the maymays";
    }

    String memes(String authorId, String[] args) {
        //Permission check
        if (!(DataProvider.isOwner(authorId))) {
            //return "[Error] You aren't authorized to do this";
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("update")) {
            DankMemes.update();
            return "Memes updated from file.";
        }

        return "[Error] Wrong arguments";
    }
}
