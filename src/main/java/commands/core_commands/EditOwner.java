package commands.core_commands;

import commands.PMCommand;
import provider.DataProvider;
import provider.jda.Discord;
import provider.jda.events.PrivateMessageEvent;

public class EditOwner implements PMCommand {
    @Override
    public void runCommand(PrivateMessageEvent event, Discord discord) {
        String[] args = event.getArgs();

        //Permission check
        if (!(DataProvider.isOwner(event.getAuthor().getId()))) {
            event.replyAsync("[Error] You aren't authorized to do this");
            return;
        }

        if (args.length != 2) {
            event.replyAsync("[Error] unexpected/no arguments provided");
            return;
        }

        if (args[0].equalsIgnoreCase("add")) {
            DataProvider.addOwner(args[1]);
            event.replyAsync("Owner added");
        }
        else if (args[0].equalsIgnoreCase("del")) {
            boolean success = DataProvider.removeOwner(args[1]);

            if (success)
                event.replyAsync("Owner removed");
            else
                event.replyAsync("Id not found. Nothing changed");
        }
        else {
            event.replyAsync("[Error] unexpected arguments provided");
        }
    }
}
