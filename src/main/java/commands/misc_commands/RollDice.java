package commands.misc_commands;

import commands.GuildCommand;
import commands.PMCommand;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;

import java.util.Random;

public class RollDice implements PMCommand, GuildCommand{
    @Override
    public void runCommand(PrivateMessageReceivedEvent event, String[] args) {
        int max = 6;
        if (args.length == 1)
            max = Integer.parseInt(args[0]);

        event.getChannel().sendMessage("Your rolled a " + roll(max)).queue();
    }

    @Override
    public void runCommand(GuildMessageReceivedEvent event, String[] args) {
        int max = 6;
        boolean negative = false;
        if (args.length == 1) {
            try {
                max = Integer.parseInt(args[0]);

                if (max == 0) {
                    event.getChannel().sendMessage("You rolled a 0, surprise dumbass!");
                    return;
                } else if (max < 0) {
                    negative = true;
                    max = 0 - max;
                }
            } catch (NumberFormatException e) {
                event.getChannel().sendMessage("[Error] " + args[0] + " is not a valid number.");
            }
        }

        int roll = negative ? 0 - roll(max) : roll(max);

        event.getChannel().sendMessage("Your rolled a " + roll).queue();
    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        return "<###?> rolls a dice between 1 and 6 or the specified number";
    }

    private int roll (int max) {
        return (new Random().nextInt(max) + 1);
    }
}
