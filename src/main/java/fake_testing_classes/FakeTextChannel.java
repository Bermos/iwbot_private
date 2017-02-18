package fake_testing_classes;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.MessageHistory;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.managers.ChannelManager;
import net.dv8tion.jda.core.managers.ChannelManagerUpdatable;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.restaction.InviteAction;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

public class FakeTextChannel implements TextChannel {
    String topic;
    String id;
    Guild guild;

    public FakeTextChannel(String id, Guild guild) {
        this.id = id;
        this.guild = guild;
    }

    @Override
    public String getTopic() {
        return this.topic;
    }

    @Override
    public RestAction<Void> deleteMessages(Collection<Message> collection) {
        return null;
    }

    @Override
    public RestAction<Void> deleteMessagesByIds(Collection<String> collection) {
        return null;
    }

    @Override
    public RestAction<List<Webhook>> getWebhooks() {
        return null;
    }

    @Override
    public RestAction<Void> deleteWebhookById(String s) {
        return null;
    }

    @Override
    public boolean canTalk() {
        return false;
    }

    @Override
    public boolean canTalk(Member member) {
        return false;
    }

    @Override
    public int compareTo(TextChannel o) {
        return 0;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public ChannelType getType() {
        return null;
    }

    @Override
    public Guild getGuild() {
        return null;
    }

    @Override
    public List<Member> getMembers() {
        return null;
    }

    @Override
    public int getPosition() {
        return 0;
    }

    @Override
    public int getPositionRaw() {
        return 0;
    }

    @Override
    public JDA getJDA() {
        return null;
    }

    @Override
    public RestAction<Message> sendMessage(String s) {
        return null;
    }

    @Override
    public RestAction<Message> sendMessage(MessageEmbed messageEmbed) {
        return null;
    }

    @Override
    public RestAction<Message> sendMessage(Message message) {
        return null;
    }

    @Override
    public RestAction<Message> sendFile(File file, Message message) throws IOException {
        return null;
    }

    @Override
    public RestAction<Message> sendFile(File file, String s, Message message) throws IOException {
        return null;
    }

    @Override
    public RestAction<Message> sendFile(InputStream inputStream, String s, Message message) {
        return null;
    }

    @Override
    public RestAction<Message> sendFile(byte[] bytes, String s, Message message) {
        return null;
    }

    @Override
    public RestAction<Message> getMessageById(String s) {
        return null;
    }

    @Override
    public RestAction<Void> deleteMessageById(String s) {
        return null;
    }

    @Override
    public MessageHistory getHistory() {
        return null;
    }

    @Override
    public RestAction<MessageHistory> getHistoryAround(Message message, int i) {
        return null;
    }

    @Override
    public RestAction<MessageHistory> getHistoryAround(String s, int i) {
        return null;
    }

    @Override
    public RestAction<Void> sendTyping() {
        return null;
    }

    @Override
    public RestAction<Void> pinMessageById(String s) {
        return null;
    }

    @Override
    public RestAction<Void> unpinMessageById(String s) {
        return null;
    }

    @Override
    public RestAction<List<Message>> getPinnedMessages() {
        return null;
    }

    @Override
    public PermissionOverride getPermissionOverride(Member member) {
        return null;
    }

    @Override
    public PermissionOverride getPermissionOverride(Role role) {
        return null;
    }

    @Override
    public List<PermissionOverride> getPermissionOverrides() {
        return null;
    }

    @Override
    public List<PermissionOverride> getMemberPermissionOverrides() {
        return null;
    }

    @Override
    public List<PermissionOverride> getRolePermissionOverrides() {
        return null;
    }

    @Override
    public ChannelManager getManager() {
        return null;
    }

    @Override
    public ChannelManagerUpdatable getManagerUpdatable() {
        return null;
    }

    @Override
    public RestAction<Void> delete() {
        return null;
    }

    @Override
    public RestAction<PermissionOverride> createPermissionOverride(Member member) {
        return null;
    }

    @Override
    public RestAction<PermissionOverride> createPermissionOverride(Role role) {
        return null;
    }

    @Override
    public InviteAction createInvite() {
        return null;
    }

    @Override
    public RestAction<List<Invite>> getInvites() {
        return null;
    }

    @Override
    public String getAsMention() {
        return "<#" + this.id + ">";
    }

    @Override
    public String getId() {
        return this.id;
    }
}