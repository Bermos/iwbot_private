package commands.core_commands;

import commands.GuildCommand;
import commands.PMCommand;
import core.Listener;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import provider.DataProvider;

public class Shutdown implements GuildCommand, PMCommand {
    @Override
    public void runCommand(Listener listener, PrivateMessageReceivedEvent event, String[] args) {
        //Permission check
        if (!DataProvider.isBotAdmin(event)) {
            event.getChannel().sendMessage("[Error] You aren't authorized to do this").queue();
            return;
        }

        event.getChannel().sendMessage("Goodbye, master.").complete();
        event.getJDA().shutdown();
        System.exit(0);
    }

    @Override
    public void runCommand(Listener listener, GuildMessageReceivedEvent event, String[] args) {
        //Permission check
        if (!DataProvider.isBotAdmin(event)) {
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