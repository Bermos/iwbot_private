package misc;

import iw_bot.LogUtil;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.managers.Presence;
import provider.Connections;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class StatusGenerator extends Thread {
	private Presence presence;
	private Connections connections = new Connections();
	
	public void run() {
		Thread.currentThread().setName("BOT - MISC - StatusGenerator");

		//TODO replace with TimerTask
		//noinspection InfiniteLoopStatement
		while(true) {
			try {
				PreparedStatement ps = connections.getConnection().prepareStatement("SELECT word1, word3, word5 FROM markov WHERE char_length(word1) > 3 AND char_length(word3) > 3 AND char_length(word5) > 3 ORDER BY rand() LIMIT 1");
				ResultSet rs = ps.executeQuery();
				
				rs.next();
				String newStatus = rs.getString("word1") + " " + rs.getString("word3") + " " + rs.getString("word5");
				
				presence.setGame(Game.of(newStatus.trim()));
				
				Thread.sleep(5*60*1000);
			} catch (Exception e) {
				LogUtil.logErr(e);
				try {
					Thread.sleep(60 * 1000);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	
	public StatusGenerator (Presence presence) {
		this.presence = presence;
		
		this.start();
	}

}
