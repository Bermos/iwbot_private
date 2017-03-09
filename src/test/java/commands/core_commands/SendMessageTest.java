package commands.core_commands;

import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class SendMessageTest {
    PrivateMessageReceivedEvent pmre;

    @Before
    public void setUp() throws Exception {
        pmre = Mockito.mock(PrivateMessageReceivedEvent.class);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void runCommand() throws Exception {
        SendMessage sm = new SendMessage();

        sm.runCommand(pmre, new String[]{});

        Mockito.verify(pmre.getChannel()).sendMessage("Hi");
    }

}