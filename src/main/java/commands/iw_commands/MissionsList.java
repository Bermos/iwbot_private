package commands.iw_commands;

import commands.GuildCommand;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class MissionsList implements GuildCommand {
    @Override
    public void runCommand(GuildMessageReceivedEvent event, String[] args) {
        if (args.length < 1) {
            Missions.getList(event.getChannel().getId());
        }
        else if ((args[0].equalsIgnoreCase("new") || args[0].equalsIgnoreCase("add")) && args.length > 1) {
            String list = "";
            for (int i = 1; i < args.length; i++) {
                list = String.join(", ", list, args[i]);
            }
            list = list.replaceFirst(", ", "");
            Missions.newList(event.getChannel(), list);
        }
        else if (args[0].equalsIgnoreCase("next")) {
            Missions.nextListEntry(event.getChannel().getId());
            event.getMessage().deleteMessage();
        }
    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        return "Nav list for missions";
    }
}
