package commands.core_commands;

import commands.GuildCommand;
import core.JDAUtil;
import core.Listener;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class MessageChannel implements GuildCommand {
    @Override
    public void runCommand(Listener listener, GuildMessageReceivedEvent event, String[] args) {
        //Permission check
        if (!JDAUtil.isAuthorized(event)) {
            event.getChannel().sendMessage("[Error] You aren't authorized to do this").queue();
            return;
        }

        if (args.length == 0){
            event.getChannel().sendMessage("Admin channel is: <#" + listener.gh.getMessageChannel(event.getGuild().getId()) + ">").queue();
        }
        else if (!event.getMessage().getMentionedChannels().isEmpty()) {
            listener.gh.setMessageChannel(event.getGuild().getId(), event.getMessage().getMentionedChannels().get(0).getId());
            event.getChannel().sendMessage("[Success] Admin channel saved").queue();
        }
        else {
            TextChannel chan = event.getGuild().getTextChannels().stream().filter(vChan -> vChan.getName().equalsIgnoreCase(args[0].trim()))
                    .findFirst().orElse(null);
            if (chan == null) {
                event.getChannel().sendMessage("Channel not found").queue();
                return;
            } else
                listener.gh.setMessageChannel(event.getGuild().getId(), chan.getId());
            event.getChannel().sendMessage("[Success] Admin channel saved").queue();
        }
    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        //Permission check
        if (!JDAUtil.isAuthorized(event))
            return "";
        return "<channel> - sets admin channel";
    }
}
