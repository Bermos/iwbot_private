package commands.core_commands;

import commands.GuildCommand;
import core.JDAUtil;
import core.Listener;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import provider.DataProvider;

public class Welcome implements GuildCommand {
    @Override
    public void runCommand(Listener listener, GuildMessageReceivedEvent event, String[] args) {
        //Permission check
        if (!JDAUtil.isAuthorized(event)) {
            event.getChannel().sendMessage("[Error] You aren't authorized to do this").queue();
            return;
        }

        if (args.length == 0) {
            String welcomeMsg = listener.gh.getWelcomeMessage(event.getGuild().getId()).replaceAll("<user>", event.getAuthor().getName());
            event.getChannel().sendMessage(welcomeMsg.isEmpty() ? "[Error] No welcome message set." : welcomeMsg).queue();

        } else {
            String welcomeMsg = String.join(", ", args);
            listener.gh.setWelcomeMessage(event.getGuild().getId(), welcomeMsg);
            event.getChannel().sendMessage("[Success] New member message changed").queue();
        }

    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        //Permission check
        if (!JDAUtil.isAuthorized(event))
            return "";
        return "<information?> - sets information for new players or shows it";
    }

}
