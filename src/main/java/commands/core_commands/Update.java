package commands.core_commands;

import commands.GuildCommand;
import commands.PMCommand;
import iw_bot.LogUtil;
import provider.DataProvider;
import provider.jda.Discord;
import provider.jda.channel.Channel;
import provider.jda.events.GuildMessageEvent;
import provider.jda.events.PrivateMessageEvent;

import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class Update implements GuildCommand, PMCommand {

    @Override
    public void runCommand(PrivateMessageEvent event, Discord discord) {
        //Permission check
        if (!(DataProvider.isOwner(event.getAuthor().getId()))) {
            event.replyAsync("[Error] You aren't authorized to do this");
            return;
        }

        String branch = event.getArgs().length == 0 ? "master" : event.getArgs()[0];
        update(event.getChannel(), branch);
    }

    @Override
    public void runCommand(GuildMessageEvent event, Discord discord) {
        //Permission check
        if (!(DataProvider.isOwner(event.getAuthor().getId()) || event.getMember().hasRole(event.getGuild().getRolesByName("Bot Wizard", true).get(0)))) {
            event.replyAsync("[Error] You aren't authorized to do this");
            return;
        }

        String branch = event.getArgs().length == 0 ? "master" : event.getArgs()[0];
        update(event.getChannel(), branch);
    }

    @Override
    public String getHelp(GuildMessageEvent event) {
        return "Updates the bot from the desired branch";
    }

    public static void update(final Channel chan, String branch) {
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
            chan.sendMessageAsync("Downloading new sources...");
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
                chan.sendMessageAsync("Branch not found, these are available:\n" + String.join("\n", branches));
                return;
            }
            rt.exec("git checkout origin/" + branch, envp, WORKDIR).waitFor();

            // Compile code and make sure it works
            chan.sendMessageAsync("Download finished, compiling...");
            p = rt.exec("mvn package", new String[]{"JAVA_HOME=" + DataProvider.getJavaHome()}, WORKDIR);
            fw.write("------Build log " + new Date() + "------\n");
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while((line = br.readLine()) != null) {
                try { fw.append(line).append("\n"); } catch (IOException ignored) {}

                if (line.contains("Building iwbot"))
                    chan.sendMessageAsync(line.replace("[INFO] ", ""));

                else if (line.contains("T E S T S"))
                    chan.sendMessageAsync("Build complete, running tests...");

                else if (line.contains("Tests run:") && line.split(", ").length == 4) {
                    String[] tests = line.split(", ");
                    int total    = Integer.parseInt(tests[0].replace("Tests run: ", ""));
                    int failures = Integer.parseInt(tests[1].replace("Failures: ", ""));
                    int skipped  = Integer.parseInt(tests[3].replace("Skipped: ", ""));
                    int errors   = Integer.parseInt(tests[2].replace("Errors: ", ""));

                    if (failures == 0 && errors == 0 && skipped == 0)
                        chan.sendMessageAsync(total + " tests successful");
                    else {
                        chan.sendMessageAsync("**" + line + "**\n**Abort update**");
                        return;
                    }
                }
                else if (line.contains("BUILD SUCCESS"))
                    chan.sendMessage("Build successful, updating now...");

                else if (line.contains("COMPILATION ERROR")) {
                    chan.sendMessageAsync("**Compilation failed. Update aborted**");
                    return;
                }
            }
            fw.close();
            p.waitFor();
            rt.exec("git checkout master", envp, WORKDIR).waitFor();
            System.exit(1);
        } catch (Exception e) {
            LogUtil.logErr(e);
        }
    }
}
