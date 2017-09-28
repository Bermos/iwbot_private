package commands.ed_commands;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import commands.GuildCommand;
import commands.PMCommand;
import core.Listener;
import core.LogUtil;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.SocketException;

public class Distance implements PMCommand, GuildCommand {
    private class EDSystem {
        public String name;
        Coords coords;
        class Coords {
            public float x;
            public float y;
            public float z;
        }
    }

    @Override
    public void runCommand(Listener listener, PrivateMessageReceivedEvent event, String[] args) {
        event.getChannel().sendMessage(distance(args)).queue();
    }

    @Override
    public void runCommand(Listener listener, GuildMessageReceivedEvent event, String[] args) {
        event.getChannel().sendMessage(distance(args)).queue();
    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        return "<system1>, <system2> - Gives the distance between those systems.";
    }

    private String distance(String[] args) {
        if (args.length < 2) {
            return "Sorry, I need 2 systems, separated by a ',' to give you what you want.";
        }
        Gson gson = new Gson();
        EDSystem sys1;
        EDSystem sys2;
        String jsonSys1;
        String jsonSys2;
        String urlSys1 = "http://www.edsm.net/api-v1/system?sysname=" + args[0].trim().replaceAll(" ", "+") + "&coords=1";
        String urlSys2 = "http://www.edsm.net/api-v1/system?sysname=" + args[1].trim().replaceAll(" ", "+") + "&coords=1";

        try {
            Document docSys1 = Jsoup.connect(urlSys1).ignoreContentType(true).get();
            Document docSys2 = Jsoup.connect(urlSys2).ignoreContentType(true).get();

            jsonSys1 = docSys1.body().text();
            jsonSys2 = docSys2.body().text();

            if (jsonSys1.contains("[]")) {
                return args[0].trim().toUpperCase() + " not found.";
            }
            if (jsonSys2.contains("[]")) {
                return args[1].trim().toUpperCase() + " not found.";
            }

            sys1 = gson.fromJson(jsonSys1, EDSystem.class);
            sys2 = gson.fromJson(jsonSys2, EDSystem.class);

            if (sys1.coords == null) {
                return args[0].trim().toUpperCase() + " found but coordinates not in db.";
            }
            if (sys2.coords == null) {
                return args[1].trim().toUpperCase() + " found but coordinates not in db.";
            }

            float x = sys2.coords.x - sys1.coords.x;
            float y = sys2.coords.y - sys1.coords.y;
            float z = sys2.coords.z - sys1.coords.z;

            double dist = Math.sqrt(x*x + y*y + z*z);

            return String.format("Distance: %.1f ly", dist);
        } catch (JsonSyntaxException e) {
            return "[Error] Processing edsm result failed. Please contact Bermos.";
        } catch (SocketException e) {
            LogUtil.logErr(e);
            return "[Error] Failed connecting to edsm. You might want to retry in a few";
        } catch (IOException e) {
            LogUtil.logErr(e);
            return "[Error] Processing data failed";
        }
    }
}
