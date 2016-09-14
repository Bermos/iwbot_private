package misc;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import provider.DiscordInfo;

import java.io.IOException;

/**
 * Created by Bermos on 13.09.2016.
 */
public class INARALookup {
    public static String whois(String username) {
        String info = "Nothing found, sorry.";

        String url = "http://inara.cz/search?location=search&searchglobal=" + username.replaceAll(" ", "+");
        try {
            Connection.Response loginResponse = Jsoup.connect("http://inara.cz/login")
                    .header("User-Agent", "Mozilla")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Host", "inara.cz")
                    .header("Origin", "http://inara.cz")
                    .header("Upgrade-Insecure-Requests", "1")
                    .data("loginid", "Bermos")
                    .data("loginpass", DiscordInfo.getInaraPW())
                    .data("formact", "ENT_LOGIN")
                    .data("location", "intro")
                    .followRedirects(false)
                    .method(Connection.Method.POST)
                    .execute();

            Document doc = Jsoup.connect(url).userAgent("Mozilla").cookie("elitesheet", "21111").cookie("esid", loginResponse.cookie("esid")).ignoreContentType(true).get();
            Elements links = doc.getElementsByTag("a");
            Elements cmdrLinks = new Elements();

            for (Element link : links) {
                if (link.attr("href").contains("cmdr/") && !link.text().equals("CMDR's log"))
                    cmdrLinks.add(link);
            }

            if (cmdrLinks.size() > 0)
                info = "";
            for (Element link : cmdrLinks) {
                if (StringUtils.getJaroWinklerDistance(username, link.text()) > 0.90) {
                    url = "http://inara.cz" + link.attr("href");
                    doc = Jsoup.connect(url).userAgent("Mozilla").cookie("elitesheet", "21111").cookie("esid", loginResponse.cookie("esid")).ignoreContentType(true).get();

                    info += "**" + doc.select("span.pflheadersmall").get(0).parent().text() + "**\n";
                    for (Element image : doc.select("img")) {
                        if (image.parent().hasClass("profileimage"))
                            info += image.absUrl("src") + "\n";
                    }

                    Elements cells = doc.select("span.pflcellname");
                    for (Element cell : cells) {
                        if (!(cell.parent().text().replaceFirst(cell.text(), "").trim().length() < 3))
                            info += ( cell.text() + ": " + cell.parent().text().replaceFirst(cell.text(), "").trim() + "\n");
                    }
                    info += "This information was obtained using Inara.";
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        return info;
    }
}
