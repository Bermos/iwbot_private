package commands.core_commands;

import commands.GuildCommand;
import commands.PMCommand;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Bermos on 31.01.2017.
 */
public class UTCTime implements PMCommand, GuildCommand {
    @Override //PM part
    public void runCommand(PrivateMessageReceivedEvent event, String[] args) {
        event.getChannel().sendMessage(time()).queue();
    }

    @Override //Guild part
    public void runCommand(GuildMessageReceivedEvent event, String[] args) {
        event.getChannel().sendMessage(time()).queue();
    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        return null;
    }

    private String time() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        return "UTC time:\n" + sdf.format(date);
    }
}
