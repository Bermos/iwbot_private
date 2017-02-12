package iw_bot;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import provider.DataProvider;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

class AutoUpdate {
    class Author {
        String username;
    }
    class Commit {
        Author author;
        String message;
    }
    class Push {
        String ref;
        Commit commits[];
    }

    AutoUpdate() {
        try {
            int port = DataProvider.isDev() ? 1702 : 1701;

            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/update", new GitHookHandler());
            server.setExecutor(null); // creates a default executor
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class GitHookHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            JDA jda = Listener.jda;
            boolean isDev = DataProvider.isDev();
            TextChannel chan = jda.getGuildById("142749481530556416").getTextChannelById("217344072111620096");

            Gson gson = new Gson();
            JsonReader jReader = new JsonReader(new InputStreamReader(t.getRequestBody()));

            Push push = gson.fromJson(jReader, Push.class);


            if ((isDev && push.ref.contains("development")) || (!isDev && push.ref.contains("master"))) {
                String commits = "";
                for (Commit commit : push.commits) {
                    commits += "Author: " + commit.author.username + "\n";
                    commits += "Message: " + commit.message + "\n\n";
                }
                if (commits.length() > 1020)
                    commits = commits.substring(0, 1020) + "...";

                EmbedBuilder eb = new EmbedBuilder()
                        .setTitle("New push to repository")
                        .setColor(jda.getGuildById("142749481530556416").getMember(jda.getSelfUser()).getRoles().get(0).getColor())
                        .addField("Reference", push.ref, true)
                        .addField("Commits", commits, false);
                MessageEmbed embed = eb.build();

                chan.sendMessage(embed).queue();
            }

            t.sendResponseHeaders(200, 0);
            OutputStream os = t.getResponseBody();
            os.write("".getBytes());
            os.close();

            String folder = isDev ? "development" : "production";
            if ((isDev && push.ref.contains("development")) || (!isDev && push.ref.contains("master"))) {
                try {
                    URL jarurl = new URL("https://api.github.com/repos/Bermos/iwbot_private/contents/out/" + folder + "/discordbot.jar?ref=" + DataProvider.getGithubBranch());
                    URLConnection con = jarurl.openConnection();
                    con.setRequestProperty("Authorization", "token " + DataProvider.getGithubToken());
                    con.setRequestProperty("Accept", "application/vnd.github.v3.raw");
                    ReadableByteChannel rbc = Channels.newChannel(con.getInputStream());
                    FileOutputStream fos = new FileOutputStream("./discordbot.jar");
                    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

                    System.out.println("successfully downloaded");

                    chan.sendMessage("Finished download of new version. Updating now...").complete();
                    System.exit(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
