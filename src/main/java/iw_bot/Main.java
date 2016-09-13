package iw_bot;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.JDABuilder;
import provider.DiscordInfo;

public class Main {

	public static void main(String[] args) {

		try {
			
			new JDABuilder()
			.setBotToken(DiscordInfo.getToken())
			.addListener(new Listener())
			.buildBlocking();
			
		} catch (LoginException e) {
			System.out.println("[Error] invalid bot token.");
		} catch (IllegalArgumentException e) {
			System.out.println("[Error] no bot token found.");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
