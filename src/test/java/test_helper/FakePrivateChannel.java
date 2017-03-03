package test_helper;

import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.impl.PrivateChannelImpl;
import net.dv8tion.jda.core.requests.RestAction;
import provider.DataProvider;

public class FakePrivateChannel extends PrivateChannelImpl {
    public FakePrivateChannel(String id, User user) {
        super(id, user);
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
}
