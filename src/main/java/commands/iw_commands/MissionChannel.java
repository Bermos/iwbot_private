package commands.iw_commands;

import java.util.ArrayList;
import java.util.List;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.impl.TextChannelImpl;

public class MissionChannel extends TextChannelImpl {
	private Message message;
	private int next = 0;
	private String delRequestID = null;
	private List<NavSystem> systems = new ArrayList<>();
	private String voteMessageId;

	private class NavSystem {
		String name;
		boolean scoopable;
	}

	MissionChannel(String id, Guild guild) {
		super(id, guild);
	}
	
	void add(String list) {
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
	
	void next() {
		next++;
		print(false);
	}
	
	void print(boolean newMessage) {
		if (systems.isEmpty()) {
			sendMessage("You have to add systems before starting your journey").queue();
			return;
		}
			
		String content = "```";
		
		for (int i = 0; i < systems.size(); i++) {
			String scoop = (systems.get(i).scoopable ? "|" : ":");
			content += (i == next ? "->" : "  ") + scoop + systems.get(i).name + "\n";
		}
		content += "```";
		
		if (message == null || newMessage)
			sendMessage(content).queue(m -> message = m);
		else
			message.editMessage(content).queue();
	}

	void primeForDelete(String id) {
		delRequestID = id;
	}
	
	boolean isPrimed(String id) {
		return delRequestID != null && delRequestID.equals(id);
	}

	public String getVoteMessageId() {
		return voteMessageId;
	}

	public void setVoteMessageId(String voteMessageId) {
		this.voteMessageId = voteMessageId;
	}
}