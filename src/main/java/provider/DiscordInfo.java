package provider;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import net.dv8tion.jda.core.entities.Role;

public class DiscordInfo {
	private static Info info;
	private class Info {
		String token;
		String inaraPW;
		String googleToken;
		List<String> idOwner;
		List<String> idRoles;
		String newMember;
		String adminChanID;
		List<String> channels;
	}
	
	private static void getInfo() {
		try {
			Gson gson = new Gson();
			JsonReader jReader = new JsonReader(new FileReader("./discord.json"));
			info = gson.fromJson(jReader, Info.class);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private static void setInfo() {
		try {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			JsonWriter jWriter = new JsonWriter(new FileWriter("./discord.json"));
			jWriter.setHtmlSafe(false);
			jWriter.setIndent("	");
			gson.toJson(info, Info.class, jWriter);
			jWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns the token necessary to login to Discord
	 * 
	 * @return the token to login to Discord
	 * @throws FileNotFoundException
	 */
	public static String getToken() {
		if (info == null)
			getInfo();
		return info.token;
	}
	
	/**
	 * Returns a list of all owners that are allowed
	 * to perform critical changes
	 * 
	 * @return list of ID strings
	 * @throws FileNotFoundException
	 */
	public static List<String> getOwnerIDs() {
		if (info == null)
			getInfo();
		return info.idOwner;
	}
	
	/**
	 * 
	 * @param id of the owner to add
	 * @throws IOException
	 */
	public static void addOwner(String id) {
		if (info == null)
			getInfo();
		info.idOwner.add(id);
		setInfo();
	}
	
	/**
	 * 
	 * @param id of the owner to remove
	 * @throws IOException
	 */
	public static void removeOwner(String id) {
		if (info == null)
			getInfo();
		info.idOwner.remove(info.idOwner.indexOf(id));
		setInfo();
	}
	
	/**
	 * Get the saved message for new members.
	 * 
	 * @return message as string
	 * @throws FileNotFoundException
	 */
	public static String getNewMemberInfo() {
		if (info == null)
			getInfo();
		return info.newMember;
	}
	
	/**
	 * Save a new message for the new members.
	 * 
	 * @param message as string
	 * @throws IOException
	 */
	public static void setNewMemberInfo(String message) {
		if (info == null)
			getInfo();
		info.newMember = message;
		setInfo();
	}

	
	/**
	 * 
	 * @return the admin channel id as string
	 * @throws FileNotFoundException
	 */
	public static String getAdminChanID() {
		if (info == null)
			getInfo();
		return info.adminChanID;
	}
	
	/**
	 * 
	 * @param id of the channel used for admins
	 * @throws IOException
	 */
	public static void setAdminChanID(String id) {
		if (info == null)
			getInfo();
		info.adminChanID = id;
		setInfo();
	}
	
	/**
	 * 
	 * @return
	 * @throws FileNotFoundException
	 */
	public static List<String> getAdminRoleIDs() {
		if (info == null)
			getInfo();
		return info.idRoles;
	}
	
	/**
	 * 
	 * @param id of the admin role
	 * @throws IOException
	 */
	public static void addAdminRoleID(String id) {
		if (info == null)
			getInfo();
		info.idRoles.add(id);
		setInfo();
	}
	
	public static void removeAdminRoleID(String id) {
		if (info == null)
			getInfo();
		info.idRoles.remove(id);
		setInfo();
	}
	
	public static boolean isOwner(String id) {
		return getOwnerIDs().contains(id);
	}
	
	public static boolean isAdmin(List<Role> roles) {
		boolean isAdmin = false;
		for (Role role : roles) {
			if (getAdminRoleIDs().contains(role.getId()))
				isAdmin = true;
		}
		return isAdmin;
	}

	public static void setChannels (List<String> channelIDs) {
		if (info == null)
			getInfo();
		info.channels = channelIDs;
		setInfo();
	}
	
	public static List<String> getChannels() {
		if (info == null)
			getInfo();
		return info.channels;
	}

	public static String getInaraPW() {
		if (info == null)
			getInfo();
		return info.inaraPW;
	}

	public static String getGoogleToken() {
		if (info == null)
			getInfo();
		return info.googleToken;
	}
}
