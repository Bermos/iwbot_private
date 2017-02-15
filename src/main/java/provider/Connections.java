package provider;

import iw_bot.LogUtil;

import java.sql.Connection;
import java.sql.DriverManager;

public class Connections {
	private static Connection SQLConnection;

	private void connect() {
		
		try {
			DataProvider.ConData info = DataProvider.getConData("mysql");

			SQLConnection = DriverManager.getConnection(
					"jdbc:mysql://" + info.IP +
					"/" + info.DB +
					"?user=" + info.US +
					"&password=" + info.PW);

		} catch (Exception e) {
			LogUtil.logErr(e);
		}
	}
	
	public Connection getConnection() {
		if (SQLConnection == null) {
			connect();
		}
		
		return SQLConnection;
	}

	public static Connection getLocalCon(String us, String pw) {
		try {
			return DriverManager.getConnection("jdbc:mysql://localhost:3306", us, pw);
		} catch (Exception e) {
			System.out.println("Something went wrong");
		}

		return null;
	}

}
