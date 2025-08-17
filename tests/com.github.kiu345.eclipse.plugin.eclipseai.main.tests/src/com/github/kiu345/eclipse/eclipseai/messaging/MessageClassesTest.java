package com.github.kiu345.eclipse.eclipseai.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.github.kiu345.eclipse.eclipseai.messaging.Msg.Source;

/**
 * Baseline test to verify basic functionality
 */
class MessageClassesTest {
    static Stream<Class<? extends Msg>> messageClassesProvider() {
        return Stream.of(
                AgentMsg.class,
                SystemMsg.class,
                UserMsg.class,
                ToolsMsg.class
        );
    }

    private static Msg[] createTestMessages(Class<? extends Msg> clazz) throws Exception {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        Constructor<? extends Msg> ctor = clazz.getConstructor(UUID.class);
        Msg[] result = new Msg[] { ctor.newInstance(id1), ctor.newInstance(id2), ctor.newInstance(id2) };
        ((MessageBase) result[0]).setMessage("a");
        ((MessageBase) result[1]).setMessage("b");
        ((MessageBase) result[2]).setMessage("b");
        return result;
    }

    @ParameterizedTest
    @MethodSource("messageClassesProvider")
    void testHashCode(Class<? extends Msg> clazz) throws Exception {
        Msg[] msg = createTestMessages(clazz);

        assertThat(msg[0]).doesNotHaveSameHashCodeAs(msg[1]).doesNotHaveSameHashCodeAs(msg[2]);
        assertThat(msg[1]).hasSameHashCodeAs(msg[2]).doesNotHaveSameHashCodeAs(msg[0]);
        assertThat(msg[2]).hasSameHashCodeAs(msg[1]).doesNotHaveSameHashCodeAs(msg[0]);
    }

    @ParameterizedTest
    @MethodSource("messageClassesProvider")
    void testEqualsObject(Class<? extends Msg> clazz) throws Exception {
        Msg[] msg = createTestMessages(clazz);

        assertThat(msg[0]).isNotEqualTo(msg[1]).isNotEqualTo(msg[2]);
        assertThat(msg[1]).isEqualTo(msg[2]).isNotEqualTo(msg[0]);
        assertThat(msg[2]).isEqualTo(msg[1]).isNotEqualTo(msg[0]);
    }

    @ParameterizedTest
    @MethodSource("messageClassesProvider")
    void testGetSource(Class<? extends Msg> clazz) throws Exception {
        UUID id = UUID.randomUUID();
        Msg msg = clazz.getConstructor(UUID.class).newInstance(id);

        assertThat(msg.getSource()).isNotNull();

        if (msg instanceof AgentMsg typedMsg)
            assertThat(typedMsg.getSource()).isEqualByComparingTo(Source.AGENT);
        if (msg instanceof SystemMsg typedMsg)
            assertThat(typedMsg.getSource()).isEqualByComparingTo(Source.SYSTEM);
        if (msg instanceof UserMsg typedMsg)
            assertThat(typedMsg.getSource()).isEqualByComparingTo(Source.USER);
        if (msg instanceof ToolsMsg typedMsg)
            assertThat(typedMsg.getSource()).isEqualByComparingTo(Source.TOOL);
    }

    @ParameterizedTest
    @MethodSource("messageClassesProvider")
    void testGetMessageAndUuid(Class<? extends Msg> clazz) throws Exception {
        Msg[] msg = createTestMessages(clazz);

        assertThat(msg[0].getMessageId()).isNotNull();
        assertThat(msg[0].getMessage()).isNotNull().isEqualTo("a");
    }

}
