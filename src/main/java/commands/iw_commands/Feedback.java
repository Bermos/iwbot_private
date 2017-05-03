package commands.iw_commands;

import commands.PMCommand;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;

public class Feedback implements PMCommand {
    private static final String feedbackChanId = "309302132325220353";
    private static final String iwModRoleId = "199021330236964864";

    @Override
    public void runCommand(PrivateMessageReceivedEvent event, String[] args) {

        //Put together message that might have been spilt if it contained ','
        String message = "";
        for (String arg : args)
            message = String.join(", ", message, arg);
        message = message.replaceFirst(", ", "");

        //Send out feedback and inform user of command success
        event.getJDA().getTextChannelById(feedbackChanId).sendMessage("<@&" + iwModRoleId+ "> new feedback:\n" + message).queue();
        event.getChannel().sendMessage("Thanks for your feedback, it has been sent to the mods.").queue();
    }
}
