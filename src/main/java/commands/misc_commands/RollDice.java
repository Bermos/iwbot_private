package commands.misc_commands;

import commands.GuildCommand;
import commands.PMCommand;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import provider.jda.Discord;
import provider.jda.events.PrivateMessageEvent;

import java.util.Random;

public class RollDice implements PMCommand, GuildCommand{
    @Override
    public void runCommand(PrivateMessageEvent event, Discord discord) {
        event.replyAsync(roll(event.getArgs()));
    }

    @Override
    public void runCommand(GuildMessageReceivedEvent event, String[] args) {
        event.getChannel().sendMessage(roll(args)).queue();
    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        return "<###?> rolls a dice between 1 and 6 or the specified number";
    }

    String roll (String[] args) {
        int max = 6;
        boolean negative = false;

        if (args.length == 1) {
            try {
                max = Integer.parseInt(args[0]);

                if (max == 0) {
                    return "You rolled a 0. Surprise, dumbass!";
                } else if (max < 0) {
                    negative = true;
                    max = 0 - max;
                }
            } catch (NumberFormatException e) {
                return "[Error] " + args[0] + " is not a valid number.";
            }


        }
        if (args.length > 1)
            return "What do you want me to do with all those arguments?";

        int roll = negative ? 0 - (new Random().nextInt(max) + 1) : new Random().nextInt(max) + 1;

        return "You rolled a " + roll;
    }
}
