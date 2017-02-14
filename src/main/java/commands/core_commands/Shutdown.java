package commands.core_commands;

import commands.GuildCommand;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import provider.DataProvider;

public class Shutdown implements GuildCommand {
    @Override
    public void runCommand(GuildMessageReceivedEvent event, String[] args) {
        //Permission check
        if (!(DataProvider.isOwner(event))) {
            event.getChannel().sendMessage("[Error] You aren't authorized to do this").queue();
            return;
        }

        event.getChannel().sendMessage("Goodbye, master.").queue();
        event.getJDA().shutdown();
        System.exit(0);
    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        return "";
    }
}