package commands.core_commands;

import commands.GuildCommand;
import commands.PMCommand;
import iw_bot.LogUtil;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import provider.DataProvider;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Update implements GuildCommand, PMCommand {

    @Override
    public void runCommand(GuildMessageReceivedEvent event, String[] args) {
        //Permission check
        if (!(DataProvider.isOwner(event.getAuthor().getId()) || event.getMember().getRoles().contains(event.getGuild().getRolesByName("Bot Wizard", true).get(0)))) {
            event.getChannel().sendMessage("[Error] You aren't authorized to do this").queue();
            return;
        }

        String branch = args.length == 0 ? "master" : args[0];
        update(event.getChannel(), branch);
    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        return "Updates the bot from the desired branch";
    }

    public static void update(MessageChannel chan, String branch) {
        try {
            final File WORKDIR = new File("/home/bermos/Documents/git-test/iwbot-private");
            chan.sendMessage("Downloading new sources...").queue();
            String token = DataProvider.getGithubToken();
            Runtime rt = Runtime.getRuntime();
            rt.exec("git clone https://" + token + "@github.com/Bermos/iwbot_private.git", new String[]{}, new File("/home/bermos/Documents/git-test"));
            Process p = rt.exec("git branch -a", new String[]{}, WORKDIR);
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            List<String> branches = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                branches.add(line.replace("remotes/origin/", "").replace(" -> origin/master", "").trim().toLowerCase());
                System.out.println(line);
            }

            if (!branches.contains(branch)) {
                chan.sendMessage("Branch not found, these are available:\n" + String.join("\n", branches)).queue();
                return;
            } else {
                rt.exec("git checkout " + branch, new String[]{}, WORKDIR);
            }

            chan.sendMessage("Download finished, compiling...").queue();
            p = rt.exec("mvn package", new String[]{}, WORKDIR);
            br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            line = null;
            while ((line = br.readLine()) != null) {
                if (line.contains("Building..."))
                    chan.sendMessage(line).queue();

                else if (line.contains("T E S T S"))
                    chan.sendMessage("Build complete, running tests...").queue();

                else if (line.contains("Tests run:")) {
                    String[] tests = line.split(", ");
                    int total    = Integer.parseInt(tests[0].replace("Tests run: ", ""));
                    int failures = Integer.parseInt(tests[1].replace("Failures: ", ""));
                    int errors   = Integer.parseInt(tests[2].replace("Errors: ", ""));
                    int skipped  = Integer.parseInt(tests[3].replace("Skipped: ", ""));

                    if (failures == 0 && errors == 0 && skipped == 0)
                        chan.sendMessage(total + " tests successful").queue();
                    else {
                        chan.sendMessage("**" + line + "**\n**Abort update**").queue();
                        return;
                    }
                }
                else if (line.contains("BUILD SUCCESS"))
                    chan.sendMessage("Build successful, updating now...").queue();

                else if (line.contains("COMPILATION ERROR")) {
                    chan.sendMessage("**Compilation failed. Update aborted**").queue();
                    return;
                }
            }

            chan.sendMessage("Update installed, restarting...").queue();
            //System.exit(1);
        } catch (Exception e) {
            LogUtil.logErr(e);
        }
    }

    @Override
    public void runCommand(PrivateMessageReceivedEvent event, String[] args) {
        //Permission check
        if (!(DataProvider.isOwner(event.getAuthor().getId()))) {
            event.getChannel().sendMessage("[Error] You aren't authorized to do this").queue();
            return;
        }

        String branch = args.length == 0 ? "master" : args[0];
        update(event.getChannel(), branch);
    }
}
