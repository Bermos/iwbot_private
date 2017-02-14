package commands.core_commands;
import commands.GuildCommand;
import commands.PMCommand;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import provider.DataProvider;
/**
 * Created by Jim and Hannah on 14/02/2017.
 */
public class Shutdown implements PMCommand, GuildCommand {
    @Override
    public void runCommand(PrivateMessageReceivedEvent event, String[] args) {
        //Permission check

        Member author = event.getJDA().getGuildById("142749481530556416").getMember(event.getAuthor());
        if ( !( DataProvider.isOwner(event) || (author != null && DataProvider.isAdmin(author.getRoles())) ) ) {
            event.getChannel().sendMessage("[Error] You aren't authorized to do this").queue();
            return;
        }

        event.getChannel().sendMessage("Goodbye world :(").queue();
        event.getJDA().shutdown();
        System.exit(0);
    }

    @Override
    public void runCommand(GuildMessageReceivedEvent event, String[] args) {
        //Permission check
        if (!(DataProvider.isOwner(event) || DataProvider.isAdmin(event))) {
            event.getChannel().sendMessage("[Error] You aren't authorized to do this").queue();
            return;
        }

        event.getChannel().sendMessage("Goodbye world :(").queue();
        event.getJDA().shutdown();
        System.exit(0);
    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        if (!(DataProvider.isOwner(event) || DataProvider.isAdmin(event))) {
            return "";
        }
        return "Shutsdown the bot";
    }
}