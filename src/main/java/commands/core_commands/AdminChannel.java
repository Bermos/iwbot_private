package commands.core_commands;

import commands.GuildCommand;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import provider.DataProvider;

public class AdminChannel implements GuildCommand {
    @Override
    public void runCommand(GuildMessageReceivedEvent event, String[] args) {
        //Permission check
        if (!(DataProvider.isOwner(event.getAuthor().getId()) || DataProvider.isAdmin(event.getMember().getRoles()))) {
            event.getChannel().sendMessage("[Error] You aren't authorized to do this").queue();
            return;
        }

        if (args.length == 0){
            event.getChannel().sendMessage("Admin channel is: <#" + DataProvider.getAdminChanID() + ">").queue();
        }
        else if (!event.getMessage().getMentionedChannels().isEmpty()) {
            DataProvider.setAdminChanID(event.getMessage().getMentionedChannels().get(0).getId());
            event.getChannel().sendMessage("[Success] Admin channel saved").queue();
        }
        else {
            TextChannel chan = event.getGuild().getTextChannels().stream().filter(vChan -> vChan.getName().equalsIgnoreCase(args[0].trim()))
                    .findFirst().orElse(null);
            if (chan == null) {
                event.getChannel().sendMessage("Channel not found").queue();
                return;
            } else
                DataProvider.setAdminChanID(chan.getId());
            event.getChannel().sendMessage("[Success] Admin channel saved").queue();
        }
    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        //Permission check
        if (!(DataProvider.isOwner(event.getAuthor().getId()) || DataProvider.isAdmin(event.getGuild().getMember(event.getAuthor()).getRoles())))
            return "";
        return "<channel> - sets admin channel";
    }
}
