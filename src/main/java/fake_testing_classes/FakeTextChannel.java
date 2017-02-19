package fake_testing_classes;

import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.impl.TextChannelImpl;
import net.dv8tion.jda.core.requests.RestAction;
import provider.DataProvider;

public class FakeTextChannel extends TextChannelImpl {
    private String topic;

    public FakeTextChannel(String id, Guild guild) {
        super(id, guild);
    }

    @Override
    public RestAction.EmptyRestAction<Message> sendMessage(String text) {
        DataProvider.lastMessageSent = text;
        return this.sendMessage((new MessageBuilder()).append(text).build());
    }

    @Override
    public RestAction.EmptyRestAction<Message> sendMessage(MessageEmbed embed) {
        return this.sendMessage((new MessageBuilder()).setEmbed(embed).build());
    }

    @Override
    public RestAction.EmptyRestAction<Message> sendMessage(Message msg) {
        return new RestAction.EmptyRestAction<>(msg);
    }

    @Override
    public FakeTextChannel setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    @Override
    public String getTopic() {
        return this.topic;
    }
}