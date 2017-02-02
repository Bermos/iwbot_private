package iw_core;

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

        if (Arrays.binarySearch(args, "new") == -1) {
            event.getChannel().sendMessage("Hey, this works");
        }

        if (args[0].equals("new"))
            newApplicant(event, args);

        if (args[0].equals("combat"))
            combat(event, args);

        if (args[0].equals("mission"))
            mission(event, args);
    }

    private void mission(GuildMessageReceivedEvent event, String[] args) {
    }

    private void combat(GuildMessageReceivedEvent event, String[] args) {
    }

    private void newApplicant(GuildMessageReceivedEvent event, String[] args) {
    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        return "";
    }
}
