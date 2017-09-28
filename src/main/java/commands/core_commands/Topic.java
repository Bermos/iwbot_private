package commands.core_commands;

import commands.GuildCommand;
import core.Listener;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class Topic implements GuildCommand {
    @Override
    public void runCommand(Listener listener, GuildMessageReceivedEvent event, String[] args) {
        String topic = event.getChannel().getTopic().isEmpty() ? "This channel has no topic." : event.getChannel().getTopic();
        event.getChannel().sendMessage(topic).queue();
    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        return "Shows the channel details";
    }
}
