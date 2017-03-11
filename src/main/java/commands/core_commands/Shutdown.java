package commands.core_commands;

import commands.GuildCommand;
import commands.PMCommand;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import provider.DataProvider;
import provider.jda.Discord;
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
    public void runCommand(GuildMessageReceivedEvent event, String[] args) {
        //Permission check
        if (!(DataProvider.isOwner(event))) {
            event.getChannel().sendMessage("[Error] You aren't authorized to do this").queue();
            return;
        }

        event.getChannel().sendMessage(event.getGuild().getEmoteById("281065961325461504").getAsMention() + " I don't want to go...").complete();
        event.getJDA().shutdown();
        System.exit(0);
    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        return "";
    }
}