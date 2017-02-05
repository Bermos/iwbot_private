package commands.core_commands;

import commands.GuildCommand;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import provider.DataProvider;

public class Welcome implements GuildCommand {
    @Override
    public void runCommand(GuildMessageReceivedEvent event, String[] args) {
        //Permission check
        if (!(DataProvider.isOwner(event.getAuthor().getId()) || DataProvider.isAdmin(event.getGuild().getMember(event.getAuthor()).getRoles()))) {
            event.getChannel().sendMessage("[Error] You aren't authorized to do this").queue();
            return;
        }

        if (args.length == 0) {
            event.getChannel().sendMessage(DataProvider.getNewMemberInfo().replaceAll("<user>", event.getMember().getEffectiveName())).queue();
        }
        else {

            DataProvider.setNewMemberInfo(event.getMessage().getRawContent().replaceFirst("/new", "").trim());
            event.getChannel().sendMessage("[Success] New member message changed").queue();
        }
    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        //Permission check
        if (!(DataProvider.isOwner(event.getAuthor().getId()) || DataProvider.isAdmin(event.getGuild().getMember(event.getAuthor()).getRoles())))
            return "";
        return "<information?> - sets information for new players or shows it";
    }
}
