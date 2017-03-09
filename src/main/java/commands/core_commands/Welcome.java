package commands.core_commands;

import commands.GuildCommand;
import iw_bot.JDAUtil;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import provider.DataProvider;

public class Welcome implements GuildCommand {
    @Override
    public void runCommand(GuildMessageReceivedEvent event, String[] args) {
        String name = event.getMember().getEffectiveName();
        String[] roleIds = JDAUtil.getRoleIdStrings(event.getMember());

        event.getChannel().sendMessage(welcome(name, roleIds, args)).queue();

    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        //Permission check
        if (!(DataProvider.isOwner(event.getAuthor().getId()) || DataProvider.isAdmin(event.getGuild().getMember(event.getAuthor()).getRoles())))
            return "";
        return "<information?> - sets information for new players or shows it";
    }

    String welcome(String name, String[] roleIds, String[] args) {
        //Permission check
        if (!DataProvider.isAdmin(roleIds)) {
            return "[Error] You aren't authorized to do this";
        }

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
