package commands.core_commands;

import commands.GuildCommand;
import commands.PMCommand;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import provider.DataProvider;

public class Setname implements PMCommand, GuildCommand {
    @Override
    public void runCommand(PrivateMessageReceivedEvent event, String[] args) {
        //Permission check
        if (!(DataProvider.isOwner(event))) {
            event.getChannel().sendMessage("[Error] You aren't authorized to do this").queue();
            return;
        }

        event.getChannel().sendMessage(setname(event.getJDA(), args));
    }

    @Override
    public void runCommand(GuildMessageReceivedEvent event, String[] args) {
        //Permission check
        if (!(DataProvider.isOwner(event) || DataProvider.isAdmin(event))) {
            event.getChannel().sendMessage("[Error] You aren't authorized to do this").queue();
            return;
        }

        event.getChannel().sendMessage(setname(event.getJDA(), args));
    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        //Permission check
        if (!(DataProvider.isOwner(event) || DataProvider.isAdmin(event)))
            return "";
        return "<name>";
    }

    private String setname(JDA jda, String[] args) {

        if (args.length == 0) {
            return "[Error] No name stated";
        }

        jda.getSelfUser().getManager().setName(args[0]).queue();
        return "[Success] Name changed";
    }
}
