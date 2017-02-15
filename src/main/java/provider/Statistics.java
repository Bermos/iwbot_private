package provider;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.concurrent.TimeUnit;

import iw_bot.LogUtil;
import net.dv8tion.jda.core.entities.Member;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Pong;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class Statistics extends Thread {
	private static Statistics instance;
	private static InfluxDB influxDB;
	private static String dbName = "iw_monitor";
	private static JDA jda;

	private Statistics() {
		
	}
	
	static {
		instance = new Statistics();
	}
	
	public static Statistics getInstance() {
		return instance;
	}
	
	public void connect(JDA jda) {
		DataProvider.ConData info = DataProvider.getConData("influx");

		Statistics.influxDB = InfluxDBFactory.connect(info.IP, info.US, info.PW);
		Statistics.jda = jda;

		boolean connected = false;
		do {
			Pong response;
			try {
				response = influxDB.ping();
				if (!response.getVersion().equalsIgnoreCase("unknown")) {
					connected = true;
				}

				Thread.sleep(10L);
			} catch (Exception e) {
				LogUtil.logErr(e);
			}
		} while (!connected);
		influxDB.enableBatch(2000, 1000, TimeUnit.MILLISECONDS);
		System.out.println("[InfluxDB] connected. Version: " + influxDB.version());

		this.start();
	}
	
	public void run() {
		Thread.currentThread().setName("BOT - PROVIDER - Statistics");
		while (true) {
			try {
				//update statistics
				int onlineUser = 0;
				for(Member member : jda.getGuildById("142749481530556416").getMembers()) {
					if (!member.getOnlineStatus().equals(OnlineStatus.OFFLINE))
						onlineUser++;
				}
				Point users = Point.measurement("users")
						.time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
						.addField("online", onlineUser)
						.addField("total", jda.getUsers().size())
						.build();
				influxDB.write(dbName, "default", users);
				
				Point system = Point.measurement("system")
						.time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
						.addField("used_ram", (double)(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024)
						.addField("total_ram", (double) Runtime.getRuntime().maxMemory() / 1024 / 1024)
						.addField("no_threads", Thread.getAllStackTraces().keySet().size())
						.build();
				influxDB.write(dbName, "default", system);
				
				Thread.sleep(1000);
				
			} catch (Exception e) {
				LogUtil.logErr(e);
			}
		}
	}

	public void logMessage(GuildMessageReceivedEvent event) {
		Point messages = Point.measurement("messages")
				.time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
				.addField("content_length", event.getMessage().getContent().length())
				.addField("author", event.getAuthor().getName())
				.addField("channel", event.getChannel().getName())
				.build();
		influxDB.write(dbName, "default", messages);
	}
	
	public void logCommandReceived(String commandName, String author) {
		Point commands = Point.measurement("commands")
				.time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
				.addField("name", commandName)
				.addField("author", author)
				.build();
		influxDB.write(dbName, "default", commands);
	}

	public void logBGSActivity(long time, String userid, String username, String activity, int amount, String system) {
		Point bgs = Point.measurement("bgs")
				.time(time, TimeUnit.MILLISECONDS)
				.addField("activity", activity)
				.addField("userid", userid)
				.addField("username", username)
				.addField("ammount", amount)
				.addField("system", system)
				.build();
		influxDB.write(dbName, "default", bgs);
	}
}
