package iw_core;

import java.util.ArrayList;
import java.util.List;

import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.impl.TextChannelImpl;

class MissionChannel extends TextChannelImpl {
	private Message message;
	private int next = 0;
	private String delRequestID = null;
	private List<NavSystem> systems = new ArrayList<NavSystem>();
	
	class NavSystem {
		String name;
		boolean scoopable;
	}

	public MissionChannel(String id, Guild guild) {
		super(id, guild);
	}
	
	public void add(String list) {
		systems.clear();
		next = 0;
		String[] listSystems = list.split("\n");
		
		for (String system : listSystems) {
			NavSystem navSystem = new NavSystem();
			String[] data = system.split(",");
			navSystem.name = data[0].trim().toUpperCase();
			navSystem.scoopable = data.length == 2;
			systems.add(navSystem);
		}
		
		print(true);
	}
	
	public void next() {
		next++;
		print(false);
	}
	
	public void print(boolean newMessage) {
		if (systems.isEmpty()) {
			sendMessageAsync("You have to add systems before starting your journey", null);
			return;
		}
			
		String content = "```";
		
		for (int i = 0; i < systems.size(); i++) {
			String scoop = (systems.get(i).scoopable ? "|" : ":");
			content += (i == next ? "->" : "  ") + scoop + systems.get(i).name + "\n";
		}
		content += "```";
		
		if (message == null || newMessage)
			message = sendMessage(content);
		else
			message.updateMessageAsync(content, null);
	}

	public void primeForDelete(String id) {
		delRequestID = id;
	}
	
	public boolean isPrimed(String id) {
		if (delRequestID == null)
			return false;
		if (delRequestID.equals(id))
			return true;
		return false;
	}
}