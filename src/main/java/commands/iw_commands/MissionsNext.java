package commands.iw_commands;

import commands.GuildCommand;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class MissionsNext implements GuildCommand {
    @Override
    public void runCommand(GuildMessageReceivedEvent event, String[] args) {
        if (args.length < 1) {
            Missions.nextListEntry(event.getChannel().getId());
            event.getMessage().deleteMessage();
        }
    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        return "Moves the pointer for the Nav list one forward";
    }
}
