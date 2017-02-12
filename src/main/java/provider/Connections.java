package provider;

import iw_bot.LogUtil;

import java.sql.Connection;
import java.sql.DriverManager;

public class Connections {
	private static Connection SQLConnection;

	private void connect() {
		
		try {
			DataProvider.Info.ConData info = DataProvider.getConData("mysql");

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

}
