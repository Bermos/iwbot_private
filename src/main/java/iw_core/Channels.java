package iw_core;

import java.util.ArrayList;
import java.util.List;

import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.events.channel.text.TextChannelUpdatePositionEvent;
import provider.DiscordInfo;

public class Channels {
	private static boolean locked = false;
	private static long lastChanged = 0;
	

	public static void lock(List<TextChannel> textChannels) {
		List<String> channelIDs = new ArrayList<String>();
		
		for (TextChannel chan : textChannels)
			channelIDs.add(chan.getId());
		
		DiscordInfo.setChannels(channelIDs);
		locked = true;
	}
	
	public static void unlock() {
		locked = false;
	}

	public static void changed(TextChannelUpdatePositionEvent event) {
		if (locked && (lastChanged + 10000) < System.currentTimeMillis()) {
			List<String> channelIDs = DiscordInfo.getChannels();
			for (int i = 0; i < channelIDs.size(); i++) {
				event.getJDA().getTextChannelById(channelIDs.get(i)).getManager().setPosition(i).update();
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			lastChanged = System.currentTimeMillis();
			event.getJDA().getTextChannelById("207302898831458304").sendMessageAsync("Someone changed the channels again. I changed them back.", null);
		}
	}
	
	
}
