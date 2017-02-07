package commands.iw_commands;

import commands.GuildCommand;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import provider.Connections;
import provider.DataProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * This command class is for keeping track of
 * applicants
 *
 * TODO
 */
public class Applicant implements GuildCommand {
    private final Connection connection = new Connections().getConnection();

    @Override
    public void runCommand(GuildMessageReceivedEvent event, String[] args) {
        //Permission check
        if (!(DataProvider.isOwner(event) || DataProvider.isAdmin(event))) {
            event.getChannel().sendMessage("[Error] You aren't authorized to do this").queue();
            return;
        }

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
            combat(event);

        if (Arrays.binarySearch(args, "mission") > -1)
            mission(event);

        if (Arrays.binarySearch(args, "status") > -1)
            status(event);

        if (Arrays.binarySearch(args, "del") > -1)
            delete(event);
    }

    private void delete(GuildMessageReceivedEvent event) {
        User uApplicant = event.getMessage().getMentionedUsers().get(0);

        try {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM applicants WHERE id = ?");
            ps.setString(1, uApplicant.getId());

            if (ps.executeUpdate() == 1) {
                event.getChannel().sendMessage("Applicant removed").queue();
            } else {
                event.getChannel().sendMessage("Applicant not found. Has he been registered via 'applicant new, ...' ?").queue();
            }

        } catch (SQLException e) {
            event.getChannel().sendMessage("Something went wrong. Couldn't find applicant to delete").queue();
            e.printStackTrace();
        }
    }

    private void status(GuildMessageReceivedEvent event) {
        User uApplicant = event.getMessage().getMentionedUsers().get(0);
        Member mApplicant = event.getGuild().getMember(uApplicant);

        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM applicants WHERE id = ?");
            ps.setString(1, uApplicant.getId());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String out = mApplicant.getEffectiveName() + " progress:\n";
                out += "Eval: " + rs.getInt("eval") + "\n";
                out += "Missions: " + rs.getInt("missions") + "\n";

                event.getChannel().sendMessage(out).queue();
            } else {
                event.getChannel().sendMessage("Applicant not found. Has he been registered via 'applicant new, ...' ?").queue();
            }

        } catch (SQLException e) {
            event.getChannel().sendMessage("Something went wrong. Couldn't get status of applicant").queue();
            e.printStackTrace();
        }
    }

    private void mission(GuildMessageReceivedEvent event) {
        User uApplicant = event.getMessage().getMentionedUsers().get(0);

        try {
            PreparedStatement ps = connection.prepareStatement("UPDATE applicants SET missions = missions + 1 WHERE id = ? AND missions < 2");
            ps.setString(1, uApplicant.getId());

            if (ps.executeUpdate() == 1) {
                event.getChannel().sendMessage("Added mission done").queue();
            } else {
                event.getChannel().sendMessage("No mission added. Either applicant is already at 2 or he wasn't found.").queue();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void combat(GuildMessageReceivedEvent event) {
        User uApplicant = event.getMessage().getMentionedUsers().get(0);

        try {
            PreparedStatement ps = connection.prepareStatement("UPDATE applicants SET eval = eval + 1 WHERE id = ? AND eval < 1");
            ps.setString(1, uApplicant.getId());

            if (ps.executeUpdate() == 1) {
                event.getChannel().sendMessage("Added combat eval done").queue();
            } else {
                event.getChannel().sendMessage("No combat eval added. Either applicant already had his or he wasn't found.").queue();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void newApplicant(GuildMessageReceivedEvent event, String[] args) {
        User applicant = event.getMessage().getMentionedUsers().get(0);

        try {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO applicants (id) VALUES (?)");
            ps.setString(1, applicant.getId());

            if (ps.executeUpdate() == 1) {
                Role pc = event.getGuild().getRoleById("268146248404566026");
                Role xbox = event.getGuild().getRoleById("268146417883807746");
                Role appl = event.getGuild().getRolesByName("Applicant", true).get(0);
                Member applicantMem = event.getGuild().getMember(applicant);

                Arrays.sort(args);
                if (Arrays.binarySearch(args, "pc") > -1) {
                    System.out.println("here");
                    event.getGuild().getController().addRolesToMember(applicantMem, pc).queue();
                }
                if (Arrays.binarySearch(args, "xbox") > -1) {
                    System.out.println("and here");
                    event.getGuild().getController().addRolesToMember(applicantMem, xbox).queue();
                }
                event.getGuild().getController().addRolesToMember(applicantMem, appl).queue();

                event.getChannel().sendMessage("Added new applicant").queue();
            } else {
                event.getChannel().sendMessage("This applicant is already registered").queue();
            }

        } catch (SQLException e) {
            event.getChannel().sendMessage("Something went wrong. No new applicant saved.").queue();
            e.printStackTrace();
        }
    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        return "";
    }
}
