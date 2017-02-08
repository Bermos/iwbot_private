package commands.core_commands;

import commands.GuildCommand;
import net.dv8tion.jda.core.MessageHistory;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.PermissionOverride;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.managers.PermOverrideManagerUpdatable;
import provider.DataProvider;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * This command class is for bulk deleting messages
 * in a channel.
 * It can be called via 'prefix'clear (##(h|m))|all
 *
 * - ### : the number of messages one want's to delete
 *         or the amount of time.
 * - h|m : means the number is a timeframe, hours or
 *         minutes allowed.
 * - all : will delete the channel and recreate a exact
 *         copy of it. This is much more efficient than
 *         deleting the messages in batches.
 */
public class BulkDelete implements GuildCommand {
    @Override
    public void runCommand(GuildMessageReceivedEvent event, String[] args) {
        //Permission check
        if (!(DataProvider.isOwner(event.getAuthor().getId()) || DataProvider.isAdmin(event.getGuild().getMember(event.getAuthor()).getRoles()))) {
            event.getChannel().sendMessage("[Error] You aren't authorized to do this").queue();
            return;
        }

        //This command needs arguments
        if (args.length == 0) {
            event.getChannel().sendMessage("[Error] Please specify the number of messages you want to delete").queue();
            return;
        }

        //Time part of the command
        if (args[0].contains("h") || args[0].contains("m")){
            int deleted = 0;
            long diff = Integer.parseInt(args[0].toLowerCase().replace("m", "").replace("h", ""));
            if (args[0].toLowerCase().contains("h"))
                diff *= 60;

            //check for excessive usage and give alternative
            if (diff > 7*24*60) {
                event.getChannel().sendMessage("You are trying to delete over 7 days worth of messages. Have you thought about just using 'clear all'?\n" +
                        "If that is not a option, you'll have to use this command it 7-day-batches.").queue();
                return;
            }

            //Get oldest date that will be deleted
            OffsetDateTime upTo = OffsetDateTime.now().minusMinutes(diff);

            //Go through the messages and delete them in 100er batches
            outer: while (true) {
                List<Message> toDelete = new ArrayList<>();
                for (Message message : new MessageHistory(event.getChannel()).retrievePast(100).complete()) {
                    if (message.getCreationTime().isAfter(upTo)) {
                        toDelete.add(message);
                    }
                    else {
                        deleted += toDelete.size();
                        event.getChannel().deleteMessages(toDelete).queue();
                        break outer;
                    }
                }
                deleted += toDelete.size();
                event.getChannel().deleteMessages(toDelete).queue();
            }

            event.getChannel().sendMessage("Last " + deleted + " messages deleted").queue();
        }
        //Channel clear part
        else if (args[0].equalsIgnoreCase("all")) {
            //Save all channel metadata
            TextChannel channel = event.getChannel();
            //int pos = channel.getPosition();
            String name = channel.getName();
            String topic = channel.getTopic();
            List<PermissionOverride> memberOverrides = channel.getMemberPermissionOverrides();
            List<PermissionOverride> roleOverrides = channel.getRolePermissionOverrides();

            channel.delete().complete();

            //Recreate equivalent channel
            channel = (TextChannel) event.getGuild().getController().createTextChannel(name).complete();
            channel.getManager().setTopic(topic).queue();

            for (PermissionOverride override : memberOverrides) {
                PermOverrideManagerUpdatable permManager = channel.createPermissionOverride(override.getMember()).complete().getManagerUpdatable();
                permManager.grant(override.getAllowed());
                permManager.deny(override.getDenied());
                permManager.update().queue();
            }

            for (PermissionOverride override : roleOverrides) {
                PermOverrideManagerUpdatable permManager = channel.createPermissionOverride(override.getRole()).complete().getManagerUpdatable();
                permManager.grant(override.getAllowed());
                permManager.deny(override.getDenied());
                permManager.update().queue();
            }

            channel.sendMessage(event.getAuthor().getAsMention() + " Here's your new channel. You might need to put it in it's old place.").queue();
        }
        //Number based clearing
        else {
            int number = Integer.parseInt(args[0]);

            //Check to make sure number is within reasonable bounds
            if (number > 500) {
                event.getChannel().sendMessage("It looks like you are trying to delete over 500 messages. I recommend using 'clear all' instead which will " +
                        "delete and recreate the channel so it is wiped. Chances are you are overestimating the number of messages there are to delete.\n" +
                        "In case that is not true and you are sure you need to prune over 500 message you have to do it in baches of that size.").queue();
                return;
            }

            int secondNum = (int) Math.floor(number/100.0);
            int rest = number % 100;

            for (int i = 0; i < secondNum; i++) {
                List<Message> hist = new MessageHistory(event.getChannel()).retrievePast(100).complete();
                event.getChannel().deleteMessages(hist).queue();
                System.out.println("del " + hist.size());
            }
            if (rest != 0) {
                List<Message> hist = new MessageHistory(event.getChannel()).retrievePast(rest).complete();
                event.getChannel().deleteMessages(hist).queue();
                System.out.println("del " + hist.size());
            }

            if (number == 1)
                event.getChannel().sendMessage("Last message deleted. But why use me for that you lazy son of a @&?%!\nbtw, that was only the command you typed. Jeez, people...").queue();
            else
                event.getChannel().sendMessage("Last " + number + " messages deleted").queue();
        }
    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        //Permission check
        if (!(DataProvider.isOwner(event.getAuthor().getId()) || DataProvider.isAdmin(event.getGuild().getMember(event.getAuthor()).getRoles())))
            return "";
        return "<###|##t> - deletes either the last ## messages or the last ##h/##m of messages";
    }
}
