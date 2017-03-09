package test_helper;

import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;

public class PTestCase {
    public PrivateMessageReceivedEvent event;
    public String[] args;
    public String output;

    public PTestCase(PrivateMessageReceivedEvent event, String[] args, String output) {
        this.event = event;
        this.args = args;
        this.output = output;
    }
}