package provider;

import iw_bot.Listener;
import iw_bot.LogUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
			if (Listener.isTest)
				fakeConnect();
			else
				connect();
		}
		else {
			try {
				if (!SQLConnection.isValid(1000)) {
                    SQLConnection = null;
                    connect();
				}
			} catch (SQLException e) {
				//This can't possibly happen, why do I even have to catch it Oo
			}
		}
		
		return SQLConnection;
	}

	private void fakeConnect() {
		SQLConnection = new FakeConnection();
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
