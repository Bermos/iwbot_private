package commands.ed_commands;

import com.google.gson.Gson;
import commands.GuildCommand;
import commands.PMCommand;
import iw_bot.JDAUtil;
import iw_bot.LogUtil;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import provider.DataProvider;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

class CombatLogger {
    CombatLogger (String a, String b, String c, String d) {
        this.name = a;
        this.no_of_logs = b;
        this.type_of_log = c;
        this.platform = d;
    }
    String name;
    String no_of_logs;
    String type_of_log;
    String platform;
}

class LoggerSheet {
    List<List<String>> values;
}

class InaraUser {
    String avatarUrl;
    String pageUrl;
    String name;
    String info;
}

public class CMDRLookup implements PMCommand, GuildCommand {

    private static long last_lookup;
    private static List<CombatLogger> combat_loggers = new ArrayList<>();

    @Override
    public void runCommand(PrivateMessageReceivedEvent event, String[] args) {
        JDAUtil.getPrivateChannel(event.getAuthor()).sendMessage(lookup(args)).queue();
    }

    @Override
    public void runCommand(GuildMessageReceivedEvent event, String[] args) {
        //event.getChannel().sendMessage(lookup(args)).queue();
        event.getChannel().sendMessage("Sorry, this functions is currently broken. Bermos is working on fixing it, thanks for your patience!").queue();
    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        return "Finds all the information available on inara.cz and r/EliteCombatLoggers";
    }

    private MessageEmbed lookup(String[] args) {
        InaraUser user = inara(args[0]);
        String logger = "Nothing found";
        if (args.length == 1) {
            logger = loggers(args[0], false);
        }
        else if (args.length == 2) {
            logger = loggers(args[0], args[1].equalsIgnoreCase("update"));
        }

        EmbedBuilder eb = new EmbedBuilder();
        if (user != null) {
            eb.setAuthor(user.name, user.pageUrl, user.avatarUrl)
                    .addField("Inara", user.info, false)
                    .setImage(user.avatarUrl);
        } else {
            eb.setTitle(args[0])
                    .addField("Inara", "Nothing found", false);
        }
        eb.addField("r/EliteCombatLoggers", logger, false);

        //Should not happen but just in case
        return eb.build();
    }

    private static InaraUser inara(String username) {
        InaraUser user = new InaraUser();
        user.info = "Nothing found";

        String url = "https://inara.cz/search?location=search&searchglobal=" + username.replaceAll(" ", "+");
        try {
            Connection.Response loginResponse = Jsoup.connect("https://inara.cz/login")
                    .header("User-Agent", "Mozilla")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Host", "inara.cz")
                    .header("Origin", "http://inara.cz")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("Cookie", "elitesheet=21111; esid=cc6746691b3b5359c5d887bdae12a148")
                    .data("loginid", "Bermos")
                    .data("loginpass", DataProvider.getInaraPW())
                    .data("formact", "ENT_LOGIN")
                    .data("location", "intro")
                    .followRedirects(false)
                    .method(Connection.Method.POST)
                    .execute();


            System.out.println(loginResponse.cookie("esid"));

            //Use the inara search function to get candidates
            Document doc = Jsoup.connect(url)
                    .header("User-Agent", "Mozilla")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Cookie", "esid=cc6746691b3b5359c5d887bdae12a148; elitesheet=21111")
                    .ignoreContentType(true).get();

            //Find closest match from search results
            double closestScore = 0.0;
            Element closest = null;

            for (Element link : doc.getElementsByTag("a")) {
                if (link.attr("href").contains("cmdr/") && !link.text().equals("CMDR's log")) {
                    double score = StringUtils.getJaroWinklerDistance(username, link.text());
                    if (score > closestScore && score > 0.8) {
                        closest = link;
                    }
                }
            }

            //If there is a closest (maybe no one was found) format the findings for output
            if (closest != null) {
                url = "http://inara.cz" + closest.attr("href");
                user.pageUrl = url;
                user.info = "";
                doc = Jsoup.connect(url)
                        .header("User-Agent", "Mozilla")
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .header("Cookie", "esid=cc6746691b3b5359c5d887bdae12a148; elitesheet=21111").ignoreContentType(true).get();

                user.name = doc.select("span.pflheadersmall").get(0).parent().text();
                for (Element image : doc.select("img")) {
                    if (image.parent().hasClass("profileimage"))
                        user.avatarUrl = image.absUrl("src");
                }

                Elements cells = doc.select("span.pflcellname");
                for (Element cell : cells) {
                    if (!(cell.parent().text().replaceFirst(cell.text(), "").trim().length() < 3))
                        user.info += ( cell.text() + ": " + cell.parent().text().replaceFirst(cell.text(), "").trim() + "\n");
                }

                return user;
            }

        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.logErr(e);
        }

        return null;
    }

    private static String loggers(String username, boolean force_update) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm zz");
        String info = "Nothing found. Last updated: " + sdf.format(last_lookup);

        //If the last lookup was done over 3h ago or the user forces it we update
        //This is to increase performance and don't hit google api rate limits
        if ((last_lookup + (3*60*60*1000) < System.currentTimeMillis()) || force_update) {
            try {
                //Get doc from google
                URL url = new URL("https://sheets.googleapis.com/v4/spreadsheets/16A8s5WFXI2sjOEIlZhcz_KAlO3jI7RWXZlbsOYxzF7E/values/A2:D600?key=" + DataProvider.getGoogleToken());
                Scanner scanner = new Scanner( url.openConnection().getInputStream() );

                String json = "";
                while (scanner.hasNext()) {
                    json += scanner.nextLine();
                }

                Gson gson = new Gson();
                LoggerSheet sheet = gson.fromJson(json, LoggerSheet.class);

                //Refresh new last lookup time
                last_lookup = System.currentTimeMillis();
                info = "Nothing found. Last updated: " + sdf.format(last_lookup);

                //Update local data to new readings from google doc
                combat_loggers.clear();

                for (List<String> row : sheet.values) {
                    if (row.get(0).isEmpty())
                        break;

                    if (row.get(0).equalsIgnoreCase(username)) {
                        info = "Exact CMDR name: " + row.get(0) + "\n"
                                + "No of logs: " + row.get(1) + "\n"
                                + "Method: " + row.get(2) + "\n"
                                + "Platform: " + row.get(3) + "\n"
                                + "This information was updated on: " + sdf.format(last_lookup);
                    }
                    combat_loggers.add(new CombatLogger(row.get(0), row.get(1), row.get(2), row.get(3)));
                }
            }
            catch (IOException e)
            {
                LogUtil.logErr(e);
            }
        }
        else {
            for (CombatLogger logger : combat_loggers) {
                if (logger.name.equalsIgnoreCase(username)) {
                    info = "Exact CMDR name: " + logger.name + "\n"
                            + "No of logs: " + logger.no_of_logs + "\n"
                            + "Method: " + logger.type_of_log + "\n"
                            + "Platform: " + logger.platform + "\n"
                            + "This information was updated on: " + sdf.format(last_lookup);
                    return info;
                }
            }
        }

        return info;
    }

    /*private static String reddit(String username) {
        //TODO
        return null;
    } */
}
