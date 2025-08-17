package com.github.kiu345.eclipse.eclipseai.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class ConversationManagerTest {

    @Test
    void testSize() {
        ConversationManager converation = new ConversationManager();

        converation.addFirst(new UserMsg());
        converation.addLast(new SystemMsg());

        assertThat(converation.size()).isEqualTo(2);
    }

    @Test
    void testClear() {
        ConversationManager converation = new ConversationManager();

        converation.addFirst(new UserMsg());
        converation.addLast(new SystemMsg());
        converation.clear();

        assertThat(converation.size()).isEqualTo(0);
        assertThat(converation.messages()).isEmpty();
    }

    @Test
    void testAddFirst() {
        ConversationManager converation = new ConversationManager();

        AgentMsg searchMsg1 = new AgentMsg();
        AgentMsg searchMsg2 = new AgentMsg();
        AgentMsg searchMsg3 = new AgentMsg();
        AgentMsg searchMsg4 = new AgentMsg();
        AgentMsg searchMsg5 = new AgentMsg();

        converation.addFirst(searchMsg1);
        converation.addLast(searchMsg2);
        converation.addFirst(searchMsg3);
        converation.addLast(searchMsg4);
        converation.addFirst(searchMsg5);

        assertThat(converation.messages())
                .isNotNull()
                .startsWith(searchMsg5);
    }

    @Test
    void testAddLast() {
        ConversationManager converation = new ConversationManager();

        AgentMsg searchMsg1 = new AgentMsg();
        AgentMsg searchMsg2 = new AgentMsg();
        AgentMsg searchMsg3 = new AgentMsg();
        AgentMsg searchMsg4 = new AgentMsg();
        AgentMsg searchMsg5 = new AgentMsg();

        converation.addFirst(searchMsg1);
        converation.addLast(searchMsg2);
        converation.addFirst(searchMsg3);
        converation.addLast(searchMsg4);
        converation.addLast(searchMsg5);

        assertThat(converation.messages())
                .isNotNull()
                .endsWith(searchMsg5);
    }

    @Test
    void testGet() {
        ConversationManager converation = new ConversationManager();

        AgentMsg searchMsg3 = new AgentMsg("3");
        AgentMsg searchMsg1 = new AgentMsg("1");
        AgentMsg searchMsg2 = new AgentMsg("2");
        AgentMsg searchMsg4 = new AgentMsg("4");

        converation.addFirst(searchMsg1);
        converation.addLast(searchMsg2);
        converation.addFirst(searchMsg3);
        converation.addLast(searchMsg4);

        assertThat(converation.get(1))
                .isNotNull()
                .isEqualTo(searchMsg1);
    }

    @Test
    void testRemoveMsg() {
        ConversationManager converation = new ConversationManager();

        AgentMsg searchMsg1 = new AgentMsg();
        AgentMsg searchMsg2 = new AgentMsg();
        AgentMsg searchMsg3 = new AgentMsg();
        AgentMsg searchMsg4 = new AgentMsg();

        converation.addFirst(searchMsg1);
        converation.addLast(searchMsg2);
        converation.addFirst(searchMsg3);
        converation.addLast(searchMsg4);

        converation.remove(searchMsg2);
        assertThat(converation.messages())
                .hasSize(3)
                .doesNotContain(searchMsg2);
    }

    @Test
    void testRemoveAllFunctionOfMsgBoolean() {
        ConversationManager converation = new ConversationManager();

        AgentMsg searchMsg1 = new AgentMsg("a");
        AgentMsg searchMsg2 = new AgentMsg("b");
        AgentMsg searchMsg3 = new AgentMsg("c");
        AgentMsg searchMsg4 = new AgentMsg("d");

        converation.addFirst(searchMsg1);
        converation.addLast(searchMsg2);
        converation.addFirst(searchMsg3);
        converation.addLast(searchMsg4);

        converation.removeAll(e -> e.getMessage().equals("c"));
        assertThat(converation.messages())
                .hasSize(3)
                .doesNotContain(searchMsg3);
    }

    @Test
    void testMessages() {
        ConversationManager converation = new ConversationManager();

        AgentMsg searchMsg3 = new AgentMsg("3");
        AgentMsg searchMsg1 = new AgentMsg("1");
        AgentMsg searchMsg2 = new AgentMsg("2");
        AgentMsg searchMsg4 = new AgentMsg("4");

        converation.addFirst(searchMsg1);
        converation.addLast(searchMsg2);
        converation.addFirst(searchMsg3);
        converation.addLast(searchMsg4);

        assertThat(converation.messages())
                .hasSize(4)
                .startsWith(searchMsg3, searchMsg1);
    }

    @Test
    void testFirstMessage() {
        ConversationManager converation = new ConversationManager();

        AgentMsg searchMsg3 = new AgentMsg("3");
        AgentMsg searchMsg1 = new AgentMsg("1");
        AgentMsg searchMsg2 = new AgentMsg("2");
        AgentMsg searchMsg4 = new AgentMsg("4");

        converation.addFirst(searchMsg1);
        converation.addLast(searchMsg2);
        converation.addFirst(searchMsg3);
        converation.addLast(searchMsg4);

        var msg = converation.firstMessage();

        assertThat(converation.messages())
                .hasSize(4);
        assertThat(msg).isNotEmpty().contains(searchMsg3);
    }

    @Test
    void testLastMessage() {
        ConversationManager converation = new ConversationManager();

        AgentMsg searchMsg3 = new AgentMsg("3");
        AgentMsg searchMsg1 = new AgentMsg("1");
        AgentMsg searchMsg2 = new AgentMsg("2");
        AgentMsg searchMsg4 = new AgentMsg("4");

        converation.addFirst(searchMsg1);
        converation.addLast(searchMsg2);
        converation.addFirst(searchMsg3);
        converation.addLast(searchMsg4);

        var msg = converation.lastMessage();

        assertThat(converation.messages())
                .hasSize(4);
        assertThat(msg).isNotEmpty().contains(searchMsg4);
    }

    @Test
    void testRemoveFirst() {
        ConversationManager converation = new ConversationManager();

        AgentMsg searchMsg3 = new AgentMsg("3");
        AgentMsg searchMsg1 = new AgentMsg("1");
        AgentMsg searchMsg2 = new AgentMsg("2");
        AgentMsg searchMsg4 = new AgentMsg("4");

        converation.addFirst(searchMsg1);
        converation.addLast(searchMsg2);
        converation.addFirst(searchMsg3);
        converation.addLast(searchMsg4);

        var msg = converation.removeFirst();

        assertThat(converation.messages())
                .hasSize(3)
                .startsWith(searchMsg1, searchMsg2);
        assertThat(msg).isNotEmpty().contains(searchMsg3);
    }

    @Test
    void testRemoveLast() {
        ConversationManager converation = new ConversationManager();

        AgentMsg searchMsg3 = new AgentMsg("3");
        AgentMsg searchMsg1 = new AgentMsg("1");
        AgentMsg searchMsg2 = new AgentMsg("2");
        AgentMsg searchMsg4 = new AgentMsg("4");

        converation.addFirst(searchMsg1);
        converation.addLast(searchMsg2);
        converation.addFirst(searchMsg3);
        converation.addLast(searchMsg4);

        var msg = converation.removeLast();

        assertThat(converation.messages())
                .hasSize(3)
                .endsWith(searchMsg1, searchMsg2);
        assertThat(msg).isNotEmpty().contains(searchMsg4);
    }

    @Test
    void testTrimStart() {
        ConversationManager converation = new ConversationManager();

        AgentMsg searchMsg3 = new AgentMsg("c");
        AgentMsg searchMsg1 = new AgentMsg("a");
        AgentMsg searchMsg2 = new AgentMsg("b");
        AgentMsg searchMsg4 = new AgentMsg("d");

        converation.addFirst(searchMsg1);
        converation.addLast(searchMsg2);
        converation.addFirst(searchMsg3);
        converation.addLast(searchMsg4);

        converation.trimStart(e -> !e.getMessage().equals("a"));
        assertThat(converation.messages())
                .hasSize(3)
                .startsWith(searchMsg1);
    }

    @Test
    void testTrimEnd() {
        ConversationManager converation = new ConversationManager();

        AgentMsg searchMsg3 = new AgentMsg("c");
        AgentMsg searchMsg1 = new AgentMsg("a");
        AgentMsg searchMsg2 = new AgentMsg("b");
        AgentMsg searchMsg4 = new AgentMsg("d");

        converation.addFirst(searchMsg1);
        converation.addLast(searchMsg2);
        converation.addFirst(searchMsg3);
        converation.addLast(searchMsg4);

        converation.trimEnd(e -> !e.getMessage().equals("a"));
        assertThat(converation.messages())
                .hasSize(2)
                .endsWith(searchMsg1);
    }

    @Test
    void testContains() {
        ConversationManager converation = new ConversationManager();

        AgentMsg searchMsg = new AgentMsg();
        searchMsg.setMessage("test");

        converation.addFirst(new UserMsg());
        converation.addFirst(searchMsg);
        converation.addLast(new SystemMsg());

        assertThat(converation.contains(e -> "test".equals(e.getMessage()))).isTrue();
        assertThat(converation.contains(e -> "test2".equals(e.getMessage()))).isFalse();
    }

    @Test
    void testListenerNotifications() {
        AtomicInteger added = new AtomicInteger();
        AtomicInteger removed = new AtomicInteger();
        ConversationManager manager = new ConversationManager();
        var msg1 = new UserMsg(UUID.randomUUID(), "Hello");
        var msg2 = new AgentMsg(UUID.randomUUID(), "What?");

        ConversationManager.Listener listener = (type, msg) -> {
            if (type == ConversationManager.Listener.ChangeType.ADDED)
                added.incrementAndGet();
            if (type == ConversationManager.Listener.ChangeType.REMOVED)
                removed.incrementAndGet();
        };

        manager.addListener(listener);
        manager.addFirst(msg1);
        manager.addLast(msg2);
        manager.remove(msg1);
        manager.removeLast();

        assertEquals(2, added.get());
        assertEquals(2, removed.get());
    }

    @Test
    void testSerialization() throws IOException, ClassNotFoundException {
        ConversationManager manager = new ConversationManager();
        var msg1 = new UserMsg(UUID.randomUUID(), "Hello");
        var msg2 = new AgentMsg(UUID.randomUUID(), "What?");

        manager.addLast(msg1);
        manager.addLast(msg2);
        String encoded = manager.serializeConversation();
        ConversationManager newManager = new ConversationManager();
        newManager.deserializeConversation(encoded);

        List<Msg> msgs = newManager.messages();
        assertThat(msgs)
                .hasSize(2)
                .contains(msg1, msg2);
    }

}
