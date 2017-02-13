package iw_bot;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import org.apache.commons.lang3.RandomStringUtils;
import provider.Connections;
import provider.DataProvider;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Main {

	public static void main(String[] args) throws SQLException, FileNotFoundException {

		if (DataProvider.getToken().isEmpty())
			install();

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

	private static void install() throws SQLException, FileNotFoundException {
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
		DataProvider.addConnection("mysql", ip, db, us, pw);

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
		System.out.print("Discord token: ");
		DataProvider.setDiscordToken(scanner.nextLine());
		System.out.println();

        System.out.println("Please chose a prefix for the commands. Best would be something that isn't in use yet: ");
        DataProvider.setPrefix(scanner.nextLine());

		System.out.println("Setup complete. You may still want to go into the data.json and add missing information");
	}

}
