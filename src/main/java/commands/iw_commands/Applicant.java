package commands.iw_commands;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import commands.GuildCommand;
import iw_bot.LogUtil;
import provider.Connections;
import provider.DataProvider;
import provider.jda.Discord;
import provider.jda.Member;
import provider.jda.Role;
import provider.jda.User;
import provider.jda.events.GuildMessageEvent;

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
    private final Connections con = new Connections();

    @Override
    public void runCommand(GuildMessageEvent event, Discord discord) {
        //Permission check
        if (!(DataProvider.isOwner(event.getAuthor().getId()) || DataProvider.isAdmin(event.getGuild().getMember(event.getAuthor()).getRoles()))) {
            event.replyAsync("[Error] You aren't authorized to do this");
            return;
        }

        String[] args = event.getArgs();

        if (args.length == 0) {
            event.replyAsync("[Error] Please use at least one argument for this command");
            return;
        }

        if (event.getMessage().getMentionedUsers().isEmpty()) {
            event.replyAsync("[Error] Please mention a user");
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

    private void delete(GuildMessageEvent event) {
        User uApplicant = event.getMessage().getMentionedUsers().get(0);

        try {
            PreparedStatement ps = con.getConnection().prepareStatement("DELETE FROM applicants WHERE id = ?");
            ps.setString(1, uApplicant.getId());

            if (ps.executeUpdate() == 1) {
                event.replyAsync("Applicant removed");
            } else {
                event.replyAsync("Applicant not found. Has he been registered via 'applicant new, ...' ?");
            }

        } catch (SQLException e) {
            event.replyAsync("Something went wrong. Couldn't find applicant to delete");
            LogUtil.logErr(e);
        }
    }

    private void status(GuildMessageEvent event) {
        User uApplicant = event.getMessage().getMentionedUsers().get(0);
        Member mApplicant = event.getGuild().getMember(uApplicant);

        try {
            PreparedStatement ps = con.getConnection().prepareStatement("SELECT * FROM applicants WHERE id = ?");
            ps.setString(1, uApplicant.getId());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String out = mApplicant.getEffectiveName() + " progress:\n";
                out += "Eval: " + rs.getInt("eval") + "\n";
                out += "Missions: " + rs.getInt("missions") + "\n";

                event.replyAsync(out);
            } else {
                event.replyAsync("Applicant not found. Has he been registered via 'applicant new, ...' ?");
            }

        } catch (SQLException e) {
            event.replyAsync("Something went wrong. Couldn't get status of applicant");
            LogUtil.logErr(e);
        }
    }

    private void mission(GuildMessageEvent event) {
        User uApplicant = event.getMessage().getMentionedUsers().get(0);

        try {
            PreparedStatement ps = con.getConnection().prepareStatement("UPDATE applicants SET missions = missions + 1 WHERE id = ? AND missions < 2");
            ps.setString(1, uApplicant.getId());

            if (ps.executeUpdate() == 1) {
                event.replyAsync("Added mission done");
            } else {
                event.replyAsync("No mission added. Either applicant is already at 2 or he wasn't found.");
            }

        } catch (SQLException e) {
            LogUtil.logErr(e);
        }
    }

    private void combat(GuildMessageEvent event) {
        User uApplicant = event.getMessage().getMentionedUsers().get(0);

        try {
            PreparedStatement ps = con.getConnection().prepareStatement("UPDATE applicants SET eval = eval + 1 WHERE id = ? AND eval < 1");
            ps.setString(1, uApplicant.getId());

            if (ps.executeUpdate() == 1) {
                event.replyAsync("Added combat eval done");
            } else {
                event.replyAsync("No combat eval added. Either applicant already had his or he wasn't found.");
            }
        } catch (SQLException e) {
            LogUtil.logErr(e);
        }
    }

    private void newApplicant(GuildMessageEvent event, String[] args) {
        User applicant = event.getMessage().getMentionedUsers().get(0);

        try {
            PreparedStatement ps = con.getConnection().prepareStatement("INSERT INTO applicants (id) VALUES (?)");
            ps.setString(1, applicant.getId());
            ps.executeUpdate();

            Role pc = event.getGuild().getRoleById("268146248404566026");
            Role xbox = event.getGuild().getRoleById("268146417883807746");
            Role appl = event.getGuild().getRolesByName("Applicant", true).get(0);
            Member applicantMem = event.getGuild().getMember(applicant);

            Arrays.sort(args);
            if (Arrays.binarySearch(args, "pc") > -1) {
                event.getGuild().getController().addRolesToMember(applicantMem, pc);
            }
            if (Arrays.binarySearch(args, "xbox") > -1) {
                event.getGuild().getController().addRolesToMember(applicantMem, xbox);
            }
            event.getGuild().getController().addRolesToMember(applicantMem, appl);

            event.replyAsync("Added new applicant");

        } catch (MySQLIntegrityConstraintViolationException e) {
            event.replyAsync("This applicant is already registered");
        } catch (SQLException e) {
            event.replyAsync("Something went wrong. No new applicant saved.");
            LogUtil.logErr(e);
        }
    }

    @Override
    public String getHelp(GuildMessageEvent event) {
        return "";
    }
}
