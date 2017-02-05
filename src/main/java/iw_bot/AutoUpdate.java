package iw_bot;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class AutoUpdate {
    class Push {
        class Author {

        }
        class commit {

        }
    }

    public AutoUpdate () {
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

            Gson gson = new Gson();
            JsonReader jReader = new JsonReader(new InputStreamReader(t.getRequestBody()));

            gson.fromJson(jReader, )

            String response = "This is the response";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
