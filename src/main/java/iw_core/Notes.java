package iw_core;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import provider.Connections;

public class Notes {
	private static Connection connect;
	
	private static void connect() {
		if (connect == null)
			connect = new Connections().getConnection();
	}

	public static String get (String name, String id) {
		connect();
		
		try {
			PreparedStatement ps = connect.prepareStatement("SELECT content FROM notes WHERE (is_public = 1 OR authorid = ?) AND name = ?");
			ps.setString(1, id);
			ps.setString(2, name);
			ResultSet rs = ps.executeQuery();
			
			if (rs.next())
				return rs.getString("content");
		} catch (SQLException e) {
			e.printStackTrace();
			return "$Error";
		}
		
		return null;
	}
	
	public static boolean add (String name, String id, String content, boolean isPublic) {
		connect();
		
		try {
			if (get (name, id) != null)
				return false;
			
			PreparedStatement ps = connect.prepareStatement("INSERT INTO notes (authorid, name, is_public, content) VALUES (?, ?, ?, ?)");
			ps.setString (1, id);
			ps.setString (2, name);
			ps.setBoolean(3, isPublic);
			ps.setString (4, content);
			
			if (ps.executeUpdate() == 1)
				return true;
						
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public static boolean edit (String name, String id, String content, boolean canEditPublic) {
		connect();
		
		try {
			if (get (name, id) == null)
				return false;
			
			PreparedStatement ps = connect.prepareStatement("UPDATE notes SET content = ? WHERE (is_public <= ? AND authorid = ?) AND name = ?");
			ps.setString (1, content);
			ps.setBoolean(2, canEditPublic);
			ps.setString (3, id);
			ps.setString (4, name);
			
			if (ps.executeUpdate() == 1)
				return true;
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public static boolean delete (String name, String id, boolean canDelPublic) {
		connect();
		
		try {
			if (get (name, id) == null)
				return false;
			
			PreparedStatement ps = connect.prepareStatement("DELETE FROM notes WHERE (is_public <= ? AND authorid = ?) AND name = ?");
			ps.setBoolean(1, canDelPublic);
			ps.setString (2, id);
			ps.setString (3, name);
			
			if (ps.executeUpdate() == 1)
				return true;
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return false;
	}
}
