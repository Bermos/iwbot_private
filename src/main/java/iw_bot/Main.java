package iw_bot;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import provider.DataProvider;

public class Main {

	public static void main(String[] args) {

		try {
			
			new JDABuilder(AccountType.BOT)
			.setToken(DataProvider.getToken())
			.addListener(new Listener())
			.buildBlocking();
			
		} catch (LoginException e) {
			System.out.println("[Error] invalid bot token.");
		} catch (IllegalArgumentException e) {
			System.out.println("[Error] no bot token found.");
		} catch (InterruptedException | RateLimitedException e) {
			e.printStackTrace();
		}
	}

}
