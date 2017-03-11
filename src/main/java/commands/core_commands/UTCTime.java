package commands.core_commands;

import commands.GuildCommand;
import commands.PMCommand;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import provider.jda.Discord;
import provider.jda.events.PrivateMessageEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class UTCTime implements PMCommand, GuildCommand {
    @Override //PM part
    public void runCommand(PrivateMessageEvent event, Discord discord) {
        event.replyAsync(time());
    }

    @Override //Guild part
    public void runCommand(GuildMessageReceivedEvent event, String[] args) {
        event.getChannel().sendMessage(time()).queue();
    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        return "UTC date & time now";
    }

    String time() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        return "UTC time:\n" + sdf.format(date);
    }
}
