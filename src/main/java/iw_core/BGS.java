package iw_core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import provider.Connections;
import provider.Statistics;

public class BGS {
	public enum Activity {
		BOND, BOUNTY, MINING, MISSION, SCAN, SMUGGLING, TRADE;
		
		public String toString() {
			return name().charAt(0) + name().substring(1).toLowerCase();
		}
	}
	private static SimpleDateFormat sqlSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static int getTotalAmount(Activity activity) {
		int total = 0;

		Connection connect = new Connections().getConnection();
		try {
			PreparedStatement ps = connect.prepareStatement("SELECT SUM(ammount) AS total FROM bgs_activity WHERE activity = ?");
			ps.setString(1, activity.toString());
			ResultSet rs = ps.executeQuery();
			
			if (rs.next()) 
				total = rs.getInt("total");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return total;
	}

	public static int getTotalAmount(Activity activity, String userid) {
		int total = 0;

		Connection connect = new Connections().getConnection();
		try {
			PreparedStatement ps = connect.prepareStatement("SELECT SUM(ammount) AS total FROM bgs_activity WHERE activity = ? AND userid = ?");
			ps.setString(1, activity.toString());
			ps.setString(2, userid);
			ResultSet rs = ps.executeQuery();
			
			if (rs.next()) 
				total = rs.getInt("total");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return total;
	}

	public static Map<Activity, Double> getTotalAmount(String userid) {
		Map<Activity, Double> totals = new LinkedHashMap<>();

		Connection connect = new Connections().getConnection();
		try {
			PreparedStatement ps = connect.prepareStatement("SELECT activity, SUM(ammount) AS total FROM bgs_activity WHERE userid = ? GROUP BY activity ORDER BY activity ASC");
			ps.setString(1, userid);
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) 
				totals.put(Activity.valueOf(rs.getString("activity").toUpperCase()), rs.getDouble("total"));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return totals;
	}
	
	public static Map<Activity, Double> getTotalAmount() {
		Map<Activity, Double> totals = new LinkedHashMap<>();

		Connection connect = new Connections().getConnection();
		try {
			PreparedStatement ps = connect.prepareStatement("SELECT activity, SUM(ammount) AS total FROM bgs_activity GROUP BY activity ORDER BY activity ASC");
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) 
				totals.put(Activity.valueOf(rs.getString("activity").toUpperCase()), rs.getDouble("total"));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return totals;
	}
	
	public static Map<Activity, Double> getTotalAmount(Date start, int ticks) {
		Map<Activity, Double> totals = new LinkedHashMap<>();
		Date end = ticks == 0 ? new Date() : new Date(start.getTime() + (ticks*24*60*60*1000));
		
		Connection connect = new Connections().getConnection();
		try {
			PreparedStatement ps = connect.prepareStatement("SELECT activity, SUM(ammount) AS total FROM bgs_activity WHERE timestamp > ? AND timestamp < ? GROUP BY activity ORDER BY activity ASC");
			ps.setString(1, sqlSdf.format(start));
			ps.setString(2, sqlSdf.format(end));
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) 
				totals.put(Activity.valueOf(rs.getString("activity").toUpperCase()), rs.getDouble("total"));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return totals;
	}
	
	public static int getTotalParticipants() {
		int total = 0;

		Connection connect = new Connections().getConnection();
		try {
			PreparedStatement ps = connect.prepareStatement("SELECT COUNT(DISTINCT userid) AS total FROM bgs_activity");
			ResultSet rs = ps.executeQuery();
			
			if (rs.next()) 
				total = rs.getInt("total");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return total;
	}

	public static void logActivity(Activity activity, String userid, String username, int amount) {
		Connection connect = new Connections().getConnection();
		try {
			PreparedStatement ps = connect.prepareStatement("INSERT INTO bgs_activity (username, userid, ammount, activity) VALUES (?, ?, ?, ?)");
			ps.setString(1, username);
			ps.setString(2, userid);
			ps.setInt	(3, amount);
			ps.setString(4, activity.toString());
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		Statistics.getInstance().logBGSActivity(System.currentTimeMillis(), userid, username, activity.toString(), amount);
	}

	public static List<String> getCSVData(Date start, int ticks) {
		List<String> lines = new ArrayList<>();
		Date end = ticks == 0 ? new Date() : new Date(start.getTime() + (ticks*24*60*60*1000));
		
		Connection connect = new Connections().getConnection();
		try {
			PreparedStatement ps = connect
					.prepareStatement("SELECT " +
										"username AS CMDR, " +
									    "from_unixtime(floor((unix_timestamp(timestamp) - (15*60*60))/(24*60*60)) * (24*60*60) + (15*60*60)) AS tick_start, " +
										"SUM( if( activity = 'Bond', ammount, 0 ) ) AS Bonds, " +
										"SUM( if( activity = 'Bounty', ammount, 0 ) ) AS Bounties, " +
										"SUM( if( activity = 'Mining', ammount, 0 ) ) AS Mining, " +
										"SUM( if( activity = 'Mission', ammount, 0 ) ) AS Missions, " +
										"SUM( if( activity = 'Scan', ammount, 0 ) ) AS Scans, " +
										"SUM( if( activity = 'Smuggling', ammount, 0 ) ) AS Smuggling, " +
										"SUM( if( activity = 'Trade', ammount, 0 ) ) AS Trading " +
									"FROM " +
										"bgs_activity " +
									"WHERE " +
										"timestamp >= ? AND timestamp < ? " +
									"GROUP BY " +
										"userid, tick_start");
			ps.setString(1, sqlSdf.format(start));
			ps.setString(2, sqlSdf.format(end));
			ResultSet rs = ps.executeQuery();
			
			String columnNames = "";
			int columnCount = rs.getMetaData().getColumnCount();
			int columnDateTime = -1;
			int columnCMDRName = -1;
			for (int i = 0; i < columnCount; i++) {
				if (rs.getMetaData().getColumnName(i+1).equalsIgnoreCase("username")) {
					columnCMDRName = i;
				} else if (rs.getMetaData().getColumnName(i+1).equalsIgnoreCase("tick_start")) {
					columnDateTime = i;
				}
				columnNames = String.join(", ", columnNames, rs.getMetaData().getColumnName(i+1));
			}
			lines.add(columnNames.replaceFirst(",", "").replace("username", "CMDR"));
			while (rs.next()) {
				String rowValues = "";
				for (int i = 0; i < columnCount; i++) {
					String rowValue;
					if (i == columnCMDRName)
						rowValue = rs.getString(i+1);
					else if (i == columnDateTime)
						rowValue = rs.getString(i+1).replaceAll("-", "/").replace(".0", "");
					else
						rowValue = rs.getString(i+1).equals("0") ? "" : rs.getString(i+1);
					rowValues = String.join(",", rowValues, rowValue);
				}
				lines.add(rowValues.replaceFirst(",", ""));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return lines;
	}
}
