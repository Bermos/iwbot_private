package commands.core_commands;

import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import provider.DataProvider;

public class EditOwner implements commands.PMCommand {
    @Override
    public void runCommand(PrivateMessageReceivedEvent event, String[] args) {
        //Permission check
        if (!(DataProvider.isOwner(event.getAuthor().getId()))) {
            event.getChannel().sendMessage("[Error] You aren't authorized to do this").queue();
            return;
        }

        if (args.length != 2) {
            event.getChannel().sendMessage("[Error] unexpected/no arguments provided").queue();
            return;
        }

        if (args[0].equalsIgnoreCase("add")) {
            DataProvider.addOwner(args[1]);
            event.getChannel().sendMessage("Owner added").queue();
        }
        else if (args[0].equalsIgnoreCase("del")) {
            boolean success = DataProvider.removeOwner(args[1]);

            if (success)
                event.getChannel().sendMessage("Owner removed").queue();
            else
                event.getChannel().sendMessage("Id not found. Nothing changed").queue();
        }
        else {
            event.getChannel().sendMessage("[Error] unexpected arguments provided").queue();
        }
    }
}
