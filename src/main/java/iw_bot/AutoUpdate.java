package iw_bot;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.MessageEmbed;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;

class AutoUpdate {
    class Push {
        class Author {
            String username;
        }
        class Commit {
            Author author;
            String message;
        }

        String ref;
        int size;
        Commit commits[];
    }

    AutoUpdate() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(1701), 0);
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

            Gson gson = new Gson();
            JsonReader jReader = new JsonReader(new InputStreamReader(t.getRequestBody()));

            Push push = gson.fromJson(jReader, Push.class);
            String commits = "";
            for (Push.Commit commit : push.commits) {
                commits += "Author: " + commit.author.username + "\n";
                commits += "Message: " + commit.message + "\n\n";
            }

            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("New push repository")
                    .setColor(jda.getGuildById("142749481530556416").getMember(jda.getSelfUser()).getRoles().get(0).getColor())
                    .addField("Push", "ref: " + push.ref + "\n"
                            + "size: " + push.size + "\n\n"
                            + commits, false);
            MessageEmbed embed = eb.build();

            jda.getGuildById("142749481530556416").getTextChannelById("217344072111620096").sendMessage(embed).queue();

            t.sendResponseHeaders(200, 0);
            OutputStream os = t.getResponseBody();
            os.write("".getBytes());
            os.close();
        }
    }
}
