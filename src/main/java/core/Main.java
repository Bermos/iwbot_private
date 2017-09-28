package core;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import org.apache.commons.lang3.RandomStringUtils;
import provider.Connections;
import provider.DataProvider;
import provider.DataProvider.Bot;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.regex.Pattern;

public class Main {
	public static final String CONFIG_LOC = "./config.json";
	public static final SimpleDateFormat SDF_TIME = new SimpleDateFormat("HH:mm:ss");
	public static final long startupTime = new Date().getTime();

	public static void main(String[] args) throws SQLException, FileNotFoundException {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

		for (Entry<String, Bot> bot : DataProvider.getBots().entrySet()) {
			try {
			    // Load correct listener for each bot
                // Add listeners here for new bots
			    Listener listener;
				switch (bot.getKey()) {
                    case "TEBot": listener = new ListenerIWBot(bot.getKey()); break;
                    case "IWBot": listener = new ListenerIWBot(bot.getKey()); break;
                    case "EDBot": listener = new ListenerEDBot(bot.getKey()); break;
                    case "KKBot": listener = new ListenerKKBot(bot.getKey()); break;

                    default: listener = new Listener(bot.getKey());
                }

                if (bot.getValue().token.isEmpty())
                    install(bot.getKey());

                System.out.println("Starting " + bot.getKey());
                new JDABuilder(AccountType.BOT)
						.setToken(bot.getValue().token)
						.addListener(listener)
						.buildBlocking();

			} catch (LoginException e) {
				System.out.println("[Error] invalid bot token.");
			} catch (IllegalArgumentException e) {
				System.out.println("[Error] no bot token found.");
			} catch (InterruptedException | RateLimitedException e) {
				System.out.println("[Error] listener class could not be loaded.");
				e.printStackTrace();
			}
		}
	}

	private static void install(String botName) throws SQLException, FileNotFoundException {
		Scanner scanner = new Scanner(System.in);
		Connection con = null;
		String us, pw, ip, db;

		System.out.println("It seems like you are starting the bot for the first time");
		System.out.println("Let's get it all set up...");

		while (con == null) {
			System.out.print("Please enter the admin user for your local mysql server (usually 'root'): ");
			us = scanner.nextLine();
			System.out.print("Password: ");
			pw = scanner.nextLine();

			con = Connections.getLocalCon(us, pw);
			if (con == null)
				System.out.println("Seems like I could not connect to the database. Please try again or abort with ctrl+C");
		}
		System.out.println("MySQL connection established.");

		ip = "localhost:3306";
		db = "iw_dev";
		us = "iwbot";
		Pattern sozPat = Pattern.compile("[!@*?()$]");
		Pattern numPat= Pattern.compile("[0123456789]");
		System.out.println("Creating pw for mysql user...");
		do {
			pw = RandomStringUtils.random(20, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@*?()$");
			// Create new random passwords until they contain both numbers and special characters
		} while (!numPat.matcher(pw).find() || !sozPat.matcher(pw).find());
		DataProvider.addConnection(botName,"mysql", ip, db, us, pw);

		PreparedStatement ps = con.prepareStatement("CREATE USER 'iwbot'@'localhost' IDENTIFIED BY ?;");
		ps.setString(1, pw);
		ps.execute();

		System.out.println("Mysql user created. Setting up database and tables...");

		String sql = "";
		Scanner sc = new Scanner(new File("./create_dev_db.sql"));
		while(sc.hasNext())
			sql += sc.nextLine() + "\n";

		String[] statements = sql.split(";");
		for (int i = 0; i < statements.length - 1; i++) {
			System.out.println(statements[i]);
			con.prepareStatement(statements[i]).execute();
		}


		System.out.println("Database set up.");

		JDA jda = null;

		do {
            System.out.print("Discord token: ");
            DataProvider.setDiscordToken(botName, scanner.nextLine());
            System.out.println();
            System.out.println("Testing token...");

            try {
                jda = new JDABuilder(AccountType.BOT)
                        .setToken(DataProvider.getToken())
                        .buildBlocking();
                jda.shutdown();
            } catch (LoginException e) {
                System.out.println("Your bot token was not accepted. Please retry.");
            } catch (InterruptedException | RateLimitedException e) {
                e.printStackTrace();
            }

        } while (jda == null);

        System.out.println("Please chose a prefix for the commands. Best would be something that isn't in use yet: ");
        GuildHandler.setGuildPrefix("*" ,scanner.nextLine());

		System.out.println("Setup complete. You may still want to go into the data.json and add missing information");
	}

}
