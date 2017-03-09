package commands.core_commands;

import provider.DataProvider;
import provider.jda.Discord;
import provider.jda.PrivateMessageEvent;

public class SendMessage implements commands.PMCommand {
    @Override
    public void runCommand(PrivateMessageEvent event, Discord discord) {
        if (!DataProvider.isOwner(event.getAuthor().getId())){
            event.replyAsync("You ain't allowed to do that");
            return;
        }

        String[] args = event.getArgs();
        if (args.length == 2) {
            discord.sendGMessageAsync(args[1], args[2]);
            event.replyAsync("Sent");
        }
    }
}
