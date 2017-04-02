package commands.core_commands;

import commands.GuildCommand;
import commands.PMCommand;
import iw_bot.LogUtil;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import provider.DataProvider;

import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class Update implements GuildCommand, PMCommand {

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
            // Declare constants & variables
            final String[] envp = new String[]{};
            final FileWriter fw = new FileWriter(new File("build.log"));
            final File WORKDIR  = new File("./iwbot_private");
            final String token  = DataProvider.getGithubToken();
            final Runtime rt    = Runtime.getRuntime();

            Process p;

            // Make sure we are not in a detached state
            if (WORKDIR.exists())
                rt.exec("git checkout master", envp, WORKDIR).waitFor();

            // Download source code from GitHub
            chan.sendMessage("Downloading new sources...").queue();
            if (WORKDIR.exists())
                rt.exec("git fetch", envp, WORKDIR).waitFor();
            else
                rt.exec("git clone https://" + token + "@github.com/Bermos/iwbot_private.git").waitFor();

            // Switch to specified branch
            p = rt.exec("git branch -a", envp, WORKDIR);
            List<String> branches = new BufferedReader(new InputStreamReader(p.getInputStream())).lines()
                    .filter(l -> !l.contains("detached"))
                    .map(i -> i.replace("remotes/origin/", "").replace(" -> origin/master", "").trim())
                    .collect(Collectors.toList());

            p.waitFor();
            if (!branches.contains(branch)) {
                chan.sendMessage("Branch not found, these are available:\n" + String.join("\n", branches)).queue();
                return;
            }
            rt.exec("git checkout origin/" + branch, envp, WORKDIR).waitFor();

            // Compile code and make sure it works
            chan.sendMessage("Download finished, compiling...").queue();
            p = rt.exec("mvn package", new String[]{"JAVA_HOME=" + DataProvider.getJavaHome()}, WORKDIR);
            fw.write("------Build log " + new Date() + "------\n");
            new BufferedReader(new InputStreamReader(p.getInputStream())).lines().forEach( l -> {
                try { fw.append(l + "\n"); } catch (IOException ignored) {}

                if (l.contains("Building iwbot"))
                    chan.sendMessage(l.replace("[INFO] ", "")).queue();

                else if (l.contains("T E S T S"))
                    chan.sendMessage("Build complete, running tests...").queue();

                else if (l.contains("Tests run:") && l.split(", ").length == 4) {
                    String[] tests = l.split(", ");
                    int total    = Integer.parseInt(tests[0].replace("Tests run: ", ""));
                    int failures = Integer.parseInt(tests[1].replace("Failures: ", ""));
                    int errors   = Integer.parseInt(tests[2].replace("Errors: ", ""));
                    int skipped  = Integer.parseInt(tests[3].replace("Skipped: ", ""));

                    if (failures == 0 && errors == 0 && skipped == 0)
                        chan.sendMessage(total + " tests successful").queue();
                    else {
                        chan.sendMessage("**" + l + "**\n**Abort update**").queue();
                        return;
                    }
                }
                else if (l.contains("BUILD SUCCESS"))
                    chan.sendMessage("Build successful, updating now...").complete();

                else if (l.contains("COMPILATION ERROR")) {
                    chan.sendMessage("**Compilation failed. Update aborted**").queue();
                    return;
                }
            });
            fw.close();
            p.waitFor();
            rt.exec("git checkout master", envp, WORKDIR).waitFor();
            System.exit(1);
        } catch (Exception e) {
            LogUtil.logErr(e);
        }
    }
}
