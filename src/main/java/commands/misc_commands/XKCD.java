package commands.misc_commands;

import commands.GuildCommand;
import iw_bot.LogUtil;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Random;

public class XKCD implements GuildCommand {
    @Override
    public void runCommand(GuildMessageReceivedEvent event, String[] args) {
        Image image;
        File file;
        Random RNJesus = new Random();
        int iRandom = RNJesus.nextInt(1791);
        String url = "http://xkcd.com/" + iRandom + "/";

        try {
            Document doc = Jsoup.connect(url).get();
            Elements images = doc.select("img[src$=.png]");

            for(Element eImage : images) {
                if(eImage.attr("src").contains("comic")) {
                    URL uRl = new URL("http:" + eImage.attr("src"));
                    image = ImageIO.read(uRl);
                    ImageIO.write((RenderedImage) image, "png", file = new File("./temp/" + iRandom + ".png"));
                    event.getChannel().sendFile(file, null).queue();
                    //noinspection ResultOfMethodCallIgnored
                    file.delete();
                }
            }
        } catch (IOException e) {
            LogUtil.logErr(e);
        }
    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        return "Post a random XKCD comic";
    }
}
