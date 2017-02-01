package commands.misc_commands;

import commands.GuildCommand;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.util.ArrayList;
import java.util.List;

public class Stripme implements GuildCommand {
    @Override
    public void runCommand(GuildMessageReceivedEvent event, String[] args) {
        User author = event.getAuthor();
        Guild guild = event.getGuild();
        List<Role> rolesToStrip = new ArrayList<>();

        for (Role role : guild.getRoles()) {
            for (String roleName : args) {
                if (role.getName().equalsIgnoreCase(roleName.trim()))
                    rolesToStrip.add(role);
            }
        }

        String output = "```Removed roles: ";
        for (Role role : rolesToStrip) {
            guild.getController().removeRolesFromMember(guild.getMember(author), role).queue();
            output += "\n" + role.getName();
        }
        output += "```";

        event.getChannel().sendMessage(output).queue();
    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        return "Removes the specified roles";
    }
}
