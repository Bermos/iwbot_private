package commands.iw_commands;

import commands.GuildCommand;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.util.Arrays;

public class Applicant implements GuildCommand {
    @Override
    public void runCommand(GuildMessageReceivedEvent event, String[] args) {
        if (args.length == 0) {
            event.getChannel().sendMessage("[Error] Please use at least one argument for this command").queue();
            return;
        }

        if (event.getMessage().getMentionedUsers().isEmpty()) {
            event.getChannel().sendMessage("[Error] Please mention a user").queue();
            return;
        }

        Arrays.sort(args);
        if (Arrays.binarySearch(args, "new") > -1)
            newApplicant(event, args);

        if (Arrays.binarySearch(args, "combat") > -1)
            combat(event, args);

        if (Arrays.binarySearch(args, "mission") > -1)
            mission(event, args);
    }

    private void mission(GuildMessageReceivedEvent event, String[] args) {
        event.getChannel().sendMessage("Added mission done").queue();
    }

    private void combat(GuildMessageReceivedEvent event, String[] args) {
        event.getChannel().sendMessage("Added combat eval done").queue();
    }

    private void newApplicant(GuildMessageReceivedEvent event, String[] args) {
        event.getChannel().sendMessage("Added new applicant").queue();
    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        return "";
    }
}
