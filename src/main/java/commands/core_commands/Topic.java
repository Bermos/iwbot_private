package commands.core_commands;

import commands.GuildCommand;
import commands.iw_commands.Missions;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class Topic implements GuildCommand {
    @Override
    public void runCommand(GuildMessageReceivedEvent event, String[] args) {
        if (args.length == 1 && (args[0].equalsIgnoreCase("status") || args[0].equalsIgnoreCase("s"))) {
            if (Missions.getChannel(event.getChannel().getId()).getVoteMessageId() == null) {
                event.getChannel().sendMessage("No vote found, initiate one with /topic").queue();
                return;
            }

            String id = Missions.getChannel(event.getChannel().getId()).getVoteMessageId();
            String out = "Yes: ";
            for (User user : event.getChannel().getMessageById(id).complete().getReactions().get(0).getUsers().complete()) {
                if (!user.isBot())
                    out += "\n" + user.getName();
            }

            out += "\n\nNo: ";
            for (User user : event.getChannel().getMessageById(id).complete().getReactions().get(1).getUsers().complete()) {
                if (!user.isBot())
                    out += "\n" + user.getName();
            }

            event.getChannel().sendMessage(out).queue();
            return;
        }

        String topic = event.getChannel().getTopic().isEmpty() ? "This channel has no topic." : event.getChannel().getTopic();
        Message message = event.getChannel().sendMessage(topic).complete();

        if (event.getChannel().getName().contains("mission")) {
            message.addReaction("\uD83C\uDDFE").queue();
            message.addReaction("\uD83C\uDDF3").queue();

            if (Missions.getChannel(event.getChannel().getId()) == null)
                return;

            Missions.getChannel(event.getChannel().getId()).setVoteMessageId(message.getId());
        }
    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        return "Shows the channel details";
    }
}
