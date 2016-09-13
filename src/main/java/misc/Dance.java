package misc;

import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;

public class Dance extends Thread{
	public enum ASCII {
		DANCE
	}
	
	private GuildMessageReceivedEvent event;
	private Message message;
	private ASCII iAnimation;
	
	public Dance(GuildMessageReceivedEvent event) {
		this.event = event;
	}

	public void run() {
		Thread.currentThread().setName("BOT - MISC - Dance");
		if (iAnimation == ASCII.DANCE) {
			for (int i = 0; i < 15; i++) {
				if (i == 0) {
					message  = event.getChannel().sendMessage("/o/");
				}
				else if (i%2 == 0) {
					message.updateMessage("/o/");
				}
				else {
					message.updateMessage("\\o\\");
				}
				try {
					Thread.sleep(800);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			message.updateMessage("\\o/");
		}
	}

	public void setDance(ASCII iAnimation) {
		this.iAnimation = iAnimation;
	}
}