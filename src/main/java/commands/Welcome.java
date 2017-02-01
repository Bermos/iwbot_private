package commands;

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import provider.DiscordInfo;

public class Welcome implements GuildCommand {
    @Override
    public void runCommand(GuildMessageReceivedEvent event, String[] args) {
        //Permission check
        if (!(DiscordInfo.isOwner(event.getAuthor().getId()) || DiscordInfo.isAdmin(event.getGuild().getMember(event.getAuthor()).getRoles()))) {
            event.getChannel().sendMessage("[Error] You aren't authorized to do this").queue();
            return;
        }

        if (args.length == 0) {
            event.getChannel().sendMessage(DiscordInfo.getNewMemberInfo().replaceAll("<user>", event.getMember().getEffectiveName())).queue();
        }
        else {

            DiscordInfo.setNewMemberInfo(event.getMessage().getRawContent().replaceFirst("/new", "").trim());
            event.getChannel().sendMessage("[Success] New member message changed").queue();
        }
    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        //Permission check
        if (!(DiscordInfo.isOwner(event.getAuthor().getId()) || DiscordInfo.isAdmin(event.getGuild().getMember(event.getAuthor()).getRoles())))
            return "";
        return "<information?> - sets information for new players or shows it";
    }
}
