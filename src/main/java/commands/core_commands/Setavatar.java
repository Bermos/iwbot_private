package commands.core_commands;

import commands.GuildCommand;
import commands.PMCommand;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import provider.DataProvider;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class Setavatar implements PMCommand, GuildCommand {
    @Override
    public void runCommand(PrivateMessageReceivedEvent event, String[] args) {
        //Permission check
        if (!(DataProvider.isOwner(event))) {
            event.getChannel().sendMessage("[Error] You aren't authorized to do this").queue();
            return;
        }

        event.getChannel().sendMessage(setavatar(event.getJDA(), event.getMessage())).queue();
    }

    @Override
    public void runCommand(GuildMessageReceivedEvent event, String[] args) {
        //Permission check
        if (!(DataProvider.isOwner(event) || DataProvider.isAdmin(event))) {
            event.getChannel().sendMessage("[Error] You aren't authorized to do this").queue();
            return;
        }

        event.getChannel().sendMessage(setavatar(event.getJDA(), event.getMessage())).queue();
    }

    @Override
    public String getHelp(GuildMessageReceivedEvent event) {
        //Permission check
        if (!(DataProvider.isOwner(event) || DataProvider.isAdmin(event)))
            return "";
        return "Upload desired pic to discord and enter command in the description prompt";
    }

    private String setavatar(JDA jda, Message message) {
        if (!message.getAttachments().isEmpty()) {
            File avatarFile;
            Message.Attachment attachment = message.getAttachments().get(0);
            attachment.download(avatarFile = new File("./temp/newavatar.jpg"));
            try {
                Icon avatar = Icon.from(avatarFile);
                jda.getSelfUser().getManager().setAvatar(avatar).queue();
            } catch (UnsupportedEncodingException e) {
                return "[Error] Filetype";
            } catch (IOException e) {
                e.printStackTrace();
            }

            //noinspection ResultOfMethodCallIgnored
            avatarFile.delete();
            return "[Success] Avatar changed.";
        }
        else {
            return "[Error] No image attached";
        }
    }
}
