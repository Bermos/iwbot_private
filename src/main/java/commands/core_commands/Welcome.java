package commands.core_commands;

import commands.GuildCommand;
import provider.DataProvider;
import provider.jda.Discord;
import provider.jda.events.GuildMessageEvent;

public class Welcome implements GuildCommand {
    @Override
    public void runCommand(GuildMessageEvent event, Discord discord) {
        //Permission check
        if (!DataProvider.isAdmin(event.getGuild().getMember(event.getAuthor()).getRoles())) {
            event.replyAsync("[Error] You aren't authorized to do this");
            return;
        }

        String name = event.getMember().getEffectiveName();

        event.replyAsync( welcome(name, event.getArgs()) );

    }

    @Override
    public String getHelp(GuildMessageEvent event) {
        //Permission check
        if (!(DataProvider.isOwner(event.getAuthor().getId()) || DataProvider.isAdmin(event.getGuild().getMember(event.getAuthor()).getRoles())))
            return "";
        return "<information?> - sets information for new players or shows it";
    }

    String welcome(String name, String[] args) {

        if (args.length == 0) {
            String welcomeMsg = DataProvider.getNewMemberInfo().replaceAll("<user>", name);
            return welcomeMsg.isEmpty() ? "[Error] No welcome message set." : welcomeMsg;

        } else {
            String welcomeMsg = String.join(", ", args);
            DataProvider.setNewMemberInfo(welcomeMsg);
            return "[Success] New member message changed";
        }
    }
}
