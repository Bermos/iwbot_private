package commands.iw_commands;

import commands.GuildCommand;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.util.Arrays;

/*TODO
SparticA5S (PC/XB) - 01/28/2017
in regards to applicants, evaluations, and mock runs. I was wondering is it possible to have bot start "profiles" on people to keep track of these things?
My idea was /add applicant, [pc or xbox], [applicant's name](edited)
and it would add the tags we want and create this profile
we need one eval and two mock escorts. Could we have a command for keeping track of those too?
Beu "whalecum" mer - 01/28/2017
can always manually create some tags (combat eval done; mock escort 1/2 done; mock escort 2/2 done)
SparticA5S (PC/XB) - 01/28/2017
then we could do something like /applicant status, [name] to get those figures
I know I'm asking a lot probably
 */

public class Applicant implements GuildCommand {
    @Override
    public void runCommand(GuildMessageReceivedEvent event, String[] args) {
        if (args.length == 0) {
            event.getChannel().sendMessage("[Error] Please use at least one argument for this command").queue();
            return;
        }

        if (event.getMessage().getMentionedUsers().isEmpty()) {
            event.getChannel().sendMessage("[Error] Please mention a user").queue();
            return;
        }

        Arrays.sort(args);
        if (Arrays.binarySearch(args, "new") > -1)
            newApplicant(event, args);

        if (Arrays.binarySearch(args, "combat") > -1)
            combat(event, args);

        if (Arrays.binarySearch(args, "mission") > -1)
            mission(event, args);
    }

    private void mission(GuildMessageReceivedEvent event, String[] args) {
        event.getChannel().sendMessage("Added mission done").queue();
    }

    private void combat(GuildMessageReceivedEvent event, String[] args) {
        event.getChannel().sendMessage("Added combat eval done").queue();
    }

    private void newApplicant(GuildMessageReceivedEvent event, String[] args) {
        event.getChannel().sendMessage("Added new applicant").queue();
    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        return "";
    }
}
