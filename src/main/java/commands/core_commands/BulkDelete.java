package commands.core_commands;

import commands.GuildCommand;
import net.dv8tion.jda.core.MessageHistory;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import provider.DiscordInfo;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class BulkDelete implements GuildCommand {
    @Override
    public void runCommand(GuildMessageReceivedEvent event, String[] args) {
        event.getChannel().sendTyping();
        //Permission check
        if (!(DiscordInfo.isOwner(event.getAuthor().getId()) || DiscordInfo.isAdmin(event.getGuild().getMember(event.getAuthor()).getRoles()))) {
            event.getChannel().sendMessage("[Error] You aren't authorized to do this").queue();
            return;
        }

        if (args.length == 0) {
            event.getChannel().sendMessage("[Error] Please specify the number of messages you want to delete").queue();
        } else if (args[0].contains("h") || args[0].contains("m")){
            int deleted = 0;
            long diff = Integer.parseInt(args[0].toLowerCase().replace("m", "").replace("h", ""));
            if (args[0].toLowerCase().contains("h"))
                diff *= 60;

            OffsetDateTime upTo = OffsetDateTime.now().minusMinutes(diff);

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
        } else {
            int number = Integer.parseInt(args[0]);
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
        if (!(DiscordInfo.isOwner(event.getAuthor().getId()) || DiscordInfo.isAdmin(event.getGuild().getMember(event.getAuthor()).getRoles())))
            return "";
        return "<###|##t> - deletes either the last ## messages or the last ##h/##m of messages";
    }
}
