package commands.misc_commands;

import commands.GuildCommand;
import commands.PMCommand;
import misc.DankMemes;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import provider.DiscordInfo;

public class Memes implements PMCommand, GuildCommand {
    @Override
    public void runCommand(PrivateMessageReceivedEvent event, String[] args) {
        //Permission check
        if (!(DiscordInfo.isOwner(event.getAuthor().getId()))) {
            event.getChannel().sendMessage("[Error] You aren't authorized to do this").queue();
            return;
        }

        event.getChannel().sendMessage(memes(args)).queue();
    }

    @Override
    public void runCommand(GuildMessageReceivedEvent event, String[] args) {
        //Permission check
        if (!(DiscordInfo.isOwner(event.getAuthor().getId()))) {
            event.getChannel().sendMessage("[Error] You aren't authorized to do this").queue();
            return;
        }

        event.getChannel().sendMessage(memes(args)).queue();
    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        //Permission check
        if (!(DiscordInfo.isOwner(event.getAuthor().getId()))) {
            return "";
        }
        return "Interacts with the maymays";
    }

    private String memes(String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("update")) {
            DankMemes.update();
            return "Memes updated from file.";
        }

        return "[Error] Wrong arguments";
    }
}
