package commands.core_commands;

import commands.GuildCommand;
import commands.PMCommand;
import core.Listener;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Status implements PMCommand, GuildCommand {
    @Override
    public void runCommand(PrivateMessageReceivedEvent event, String[] args) {
        event.getChannel().sendMessage(status()).queue();
    }

    @Override
    public void runCommand(GuildMessageReceivedEvent event, String[] args) {
        event.getChannel().sendMessage(status()).queue();
    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        return "Shows information about the bot";
    }

    String status() {
        Long diff 			 = (new Date().getTime() - Listener.startupTime);
        int days			 = (int) TimeUnit.MILLISECONDS.toDays(diff);
        int hours			 = (int) TimeUnit.MILLISECONDS.toHours(diff) % 24;
        int minutes			 = (int) TimeUnit.MILLISECONDS.toMinutes(diff) % 60;
        int seconds			 = (int) TimeUnit.MILLISECONDS.toSeconds(diff) % 60;
        int noThreads		 = Thread.getAllStackTraces().keySet().size();
        String totalMemory	 = String.format("%.2f",(double) Runtime.getRuntime().maxMemory() / 1024 / 1024);
        String usedMemory	 = String.format("%.2f",(double)(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024);

        String contOut = "```"
                + "Created by          | " + "CMDR Bermos with help from CMDRs Nobkins & SparticA5S. You can PM !!feedback <message> to leave me, well, feedback.\n"
                + "Uptime              | " + String.format("%dd %02d:%02d:%02d\n", days, hours, minutes, seconds)
                + "# Threads           | " + noThreads						 + "\n"
                + "Memory usage        | " + usedMemory + "/" + totalMemory  + " MB\n"
                + "Guilds              | " + Listener.jda.getGuilds().size() + "\n"
                + "Invite link         | " + "https://discordapp.com/oauth2/authorize?client_id=177053144570789888&scope=bot&permissions=3072\n"
                + "Donate (Server-Rent)| " + "http://goo.gl/pFXvvl\n"
                + "Version             | " + Listener.VERSION_NUMBER		 + "```";

        return contOut;
    }
}
