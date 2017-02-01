package commands.core_commands;

import commands.GuildCommand;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class Topic implements GuildCommand {
    @Override
    public void runCommand(GuildMessageReceivedEvent event, String[] args) {
        event.getChannel().sendMessage(event.getChannel().getTopic()).queue();
    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        return "Shows the channel details";
    }
}
