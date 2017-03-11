package commands.core_commands;

import commands.GuildCommand;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import provider.DataProvider;
import provider.jda.Discord;
import provider.jda.events.GuildMessageEvent;

public class AdminChannel implements GuildCommand {
    @Override
    public void runCommand(GuildMessageEvent event, Discord discord) {
        //Permission check
        if (!(DataProvider.isOwner(event.getAuthor().getId()) || DataProvider.isAdmin(event.getMember().getRoles()))) {
            event.replyAsync("[Error] You aren't authorized to do this");
            return;
        }

        String[] args = event.getArgs();
        if (args.length == 0) {
            event.replyAsync("Admin channel is: <#" + DataProvider.getAdminChanID() + ">");
        }
        else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("this")) {
                DataProvider.setAdminChanID(event.getChannel().getId());
                event.replyAsync("[Success] Admin channel set");
            } else {
                event.replyAsync("[Error] Please either mention a channel or write 'this' to set this channel as admin channel.");
            }
        }
        else if (!event.getMessage().getMentionedChannels().isEmpty()) {
            DataProvider.setAdminChanID(event.getMessage().getMentionedChannels().get(0).getId());
            event.replyAsync("[Success] Admin channel saved");
        }
        else {
            event.replyAsync(getHelp(event));
        }
    }

    @Override
    public String getHelp(GuildMessageEvent event) {
        //Permission check
        if (!(DataProvider.isOwner(event.getAuthor().getId()) || DataProvider.isAdmin(event.getGuild().getMember(event.getAuthor()).getRoles())))
            return "";
        return "<channel> - sets admin channel, #channelmention or 'this' for the channel you write in";
    }
}
