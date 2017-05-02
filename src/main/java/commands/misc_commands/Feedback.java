package commands.misc_commands;

import commands.GuildCommand;
import commands.PMCommand;
import iw_bot.Listener;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import provider.DataProvider;

import java.util.Date;

/**
 * Created by bermos on 02/05/17.
 */
public class Feedback implements GuildCommand, PMCommand {

    @Override
    public void runCommand(PrivateMessageReceivedEvent event, String[] args) {

        //Put together message that might have been spilt if it contained ','
        String message = "";
        for (String arg : args)
            message = String.join(", ", message, arg);
        message = message.replaceFirst(", ", "");

        //Get general information together
        User author = event.getAuthor();
        String channelUrl = "https://discordapp.com/channels/@me/" + author.getPrivateChannel().getId();
        String info = "Timestamp: " + new Date(System.currentTimeMillis()).toString() + "\n" +
                      "Version: " + Listener.VERSION_NUMBER;

        //Build message to myself
        EmbedBuilder eb = new EmbedBuilder()
                .setAuthor(author.getName(), channelUrl, author.getEffectiveAvatarUrl())
                .setDescription(message)
                .addField("Info", info, true);

        //Send feedback to me and a thank to the author
        event.getJDA().getUserById(DataProvider.getOwnerIDs().get(0)).getPrivateChannel().sendMessage(eb.build()).queue();
        event.getChannel().sendMessage("Thanks for the feedback!").queue();
    }

    @Override
    public void runCommand(GuildMessageReceivedEvent event, String[] args) {
        //Put together message that might have been spilt if it contained ','
        String message = "";
        for (String arg : args)
            message = String.join(", ", message, arg);
        message = message.replaceFirst(", ", "");

        //Get general information together
        User author = event.getAuthor();
        String channelUrl = "https://discordapp.com/channels/@me/" + author.getPrivateChannel().getId();
        String info = "Timestamp: " + new Date(System.currentTimeMillis()).toString() + "\n" +
                "Version: " + Listener.VERSION_NUMBER + "\n" +
                "Guild: " + event.getGuild().getName() + " | " + event.getGuild().getId();

        //Build message to myself
        EmbedBuilder eb = new EmbedBuilder()
                .setAuthor(author.getName(), channelUrl, author.getEffectiveAvatarUrl())
                .setDescription(message)
                .addField("Info", info, true);

        //Send feedback to me and a thank to the author
        event.getJDA().getUserById(DataProvider.getOwnerIDs().get(0)).getPrivateChannel().sendMessage(eb.build()).queue();
        event.getChannel().sendMessage("Thanks for the feedback!").queue();
    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        return "Sends feedback directly to my developer";
    }
}
