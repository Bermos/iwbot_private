package commands.core_commands;


import commands.GuildCommand;
import commands.PMCommand;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import provider.DataProvider;

public class Shutdown implements GuildCommand, PMCommand {

    @Override
    public void runCommand(GuildMessageReceivedEvent event, String [] args) {
        //check for owner
        if (!(DataProvider.isOwner(event))) {
            event.getChannel().sendMessage("[Error] You aren't my master. I owe no allegiance to you").queue();
            return;
        }

        event.getChannel().sendMessage("[Flush] Whyyyyyyyyyyyyyyyyyyyyyy...").queue();
        event.getJDA().shutdown();
        System.exit(0);
    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        return "To do your daily 'duties'";
    }

    @Override
    public void runCommand(PrivateMessageReceivedEvent event, String[] args) {

    }
}
