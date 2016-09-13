package misc;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import net.dv8tion.jda.MessageBuilder;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;
import structs.Meme;

public class DankMemes {
	private static List<Meme> memes;
	
	public DankMemes() {
		update();
	}
	
	public static void check(GuildMessageReceivedEvent event) {
		List<String> mentionedUsers = new ArrayList<String>();
		if (event.getAuthor().equals(event.getJDA().getSelfInfo()))
			return;
		if (!event.getMessage().getMentionedUsers().isEmpty()) {
			for (User user : event.getMessage().getMentionedUsers()) {
				if (event.getGuild().getNicknameForUser(user) != null)
					mentionedUsers.add(event.getGuild().getNicknameForUser(user).toLowerCase());
				else
					mentionedUsers.add(event.getGuild().getNicknameForUser(user).toLowerCase());
			}
		}
		for (Meme meme : memes) {
			boolean checkSuccessful = false;
			//Check this if the meme requires the command to match exactly
			if (meme.exact) {
				for (String key : meme.keys) {
					if (event.getMessage().getContent().equalsIgnoreCase(key))
						checkSuccessful = true;
				}
			//Check this if it doesn't
			} else {
				for (String key : meme.keys) {
					if (event.getMessage().getContent().toLowerCase().contains(key))
						checkSuccessful = true;
				}
			}
			if (!checkSuccessful)
				continue;

			checkSuccessful = false;
			if (meme.chanIDs.isEmpty())
				checkSuccessful = true;
			else {
				for (String id : meme.chanIDs) {
					if (event.getChannel().getId().equals(id))
						checkSuccessful = true;
				}
			}
			if (!checkSuccessful)
				continue;

			checkSuccessful = false;
			if (mentionedUsers.isEmpty())
				checkSuccessful = true;
			else if (!Collections.disjoint(mentionedUsers, meme.keys)) {
				checkSuccessful = true;
			}
			if (!checkSuccessful)
				continue;

			checkSuccessful = meme.lastUsed + meme.cd < System.currentTimeMillis();
			
			
			//If it is then execute the meme
			if (checkSuccessful)
				execute(event, meme);
		}
	}
	
	private static void execute(GuildMessageReceivedEvent event, Meme meme) {
		if (meme.randomOut) {
			MessageBuilder messageBuild = new MessageBuilder();
			messageBuild.setTTS(meme.tts);
			int rand = new Random().nextInt(meme.outputs.size());
			event.getChannel().sendMessageAsync(messageBuild.appendString(meme.outputs.get(rand)).build(), null);
		} else {
			for (String out : meme.outputs) {
				MessageBuilder messageBuild = new MessageBuilder();
				messageBuild.setTTS(meme.tts);
				event.getChannel().sendMessageAsync(messageBuild.appendString(out).build(), null);
			}
		}
		
		meme.lastUsed = System.currentTimeMillis();
		saveChanges();
	}

	public static void update() {
		try {
			Type listType = new TypeToken<ArrayList<Meme>>(){}.getType();
			Gson gson = new Gson();
			JsonReader jReader = new JsonReader(new FileReader("./memes.json"));
			memes = gson.fromJson(jReader, listType);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void saveChanges() {
		try {
			Type listType = new TypeToken<ArrayList<Meme>>(){}.getType();
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			JsonWriter jWriter = new JsonWriter(new FileWriter("./memes.json"));
			jWriter.setHtmlSafe(false);
			jWriter.setIndent("	");
			gson.toJson(memes, listType, jWriter);
			jWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
