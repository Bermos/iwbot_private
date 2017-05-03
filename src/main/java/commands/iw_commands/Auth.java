package commands.iw_commands;

import commands.PMCommand;
import core.LogUtil;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import provider.Connections;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Auth implements PMCommand {

    public void runCommand(PrivateMessageReceivedEvent event, String[] args) {
        SecureRandom sRandom = new SecureRandom();
        final char[] hexArray = "0123456789abcdef".toCharArray();
        byte [] bytehash;
        String password;
        String hashedpw;
        String salt;

        password = 	new BigInteger(60, sRandom).toString(32);
        salt = 		new BigInteger(80, sRandom).toString(32);

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            bytehash = digest.digest((password + salt).getBytes("UTF-8"));

            char[] hexChars = new char[bytehash.length * 2];
            for ( int j = 0; j < bytehash.length; j++ ) {
                int v = bytehash[j] & 0xFF;
                hexChars[j * 2] = hexArray[v >>> 4];
                hexChars[j * 2 + 1] = hexArray[v & 0x0F];
            }

            hashedpw = new String(hexChars);
            PreparedStatement ps = new Connections().getConnection()
                    .prepareStatement("UPDATE user SET sessionkey = ?, salt = ?, password = ? WHERE iduser = ?");
            ps.setString(1, new BigInteger(40, sRandom).toString(32));
            ps.setString(2, salt);
            ps.setString(3, hashedpw);
            ps.setLong	(4, Long.parseLong(event.getAuthor().getId()));
            ps.executeUpdate();
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException | SQLException e) {
            event.getChannel().sendMessage("[Error] Something went terribly worong. I don't think I can create your account at the moment.").queue();
            LogUtil.logErr(e);
        }

        event.getChannel().sendMessage("Account created. You have been issued the following password. Change it on the website by clicking on your avatar in the top right corner.").queue();
        event.getChannel().sendMessage(password).queue();
    }
}
