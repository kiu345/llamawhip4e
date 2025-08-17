package com.github.kiu345.eclipse.eclipseai.messaging;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.eclipse.e4.core.di.annotations.Creatable;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.kiu345.eclipse.eclipseai.messaging.ConversationManager.Listener.ChangeType;
import com.github.kiu345.eclipse.eclipseai.util.json.InstantToStringSerializer;
import com.github.kiu345.eclipse.eclipseai.util.json.StringToInstantDeserializer;

@Creatable
/**
 * Conversation message storage
 */
public class ConversationManager {
    /**
     * Listener for reacting on message storage modification events
     */
    public interface Listener {
        public enum ChangeType {
            ADDED,
            REMOVED,
            MODIFIED
        }

        void onModification(ChangeType type, Msg e);
    }

    /**
     * Serialize helper class
     */
    public static class JSONWrapper {
        List<Msg> messages;

        public JSONWrapper() {
        }

        JSONWrapper(List<Msg> messages) {
            this.messages = messages;
        }

        public List<Msg> getMessages() {
            return messages;
        }

        public void setMessages(List<Msg> messages) {
            this.messages = messages;
        }
    }

    private ConcurrentLinkedDeque<Msg> conversation = new ConcurrentLinkedDeque<>();
    private CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<>();

    public void addListener(Listener l) {
        listeners.add(Objects.requireNonNull(l));
    }

    public void removeListener(Listener l) {
        listeners.remove(l);
    }

    private void notify(Listener.ChangeType type, Msg message) {
        listeners.forEach(e -> e.onModification(type, message));
    }

    public boolean isEmpty() {
        return conversation.isEmpty();
    }
    
    public int size() {
        return conversation.size();
    }

    public void clear() {
        Msg msg;
        while ((msg = conversation.pollFirst()) != null) {
            notify(ChangeType.REMOVED, msg);
        }
    }

    public void addFirst(Msg message) {
        conversation.addFirst(Objects.requireNonNull(message));
        notify(ChangeType.ADDED, message);
    }

    public void addLast(Msg message) {
        conversation.addLast(Objects.requireNonNull(message));
        notify(ChangeType.ADDED, message);
    }

    public Msg get(int id) {
        return conversation.stream().skip(id).findFirst().orElse(null);
    }

    public void remove(Msg message) {
        if (conversation.remove(Objects.requireNonNull(message))) {
            notify(ChangeType.REMOVED, message);
        }
    }

    /**
     * Removes all elements that matches the function
     * 
     * @param checkFunction a function which should return true if the element has to be removed
     */
    public void removeAll(Predicate<Msg> checkFunction) {
        Iterator<Msg> it = conversation.iterator();
        while (it.hasNext()) {
            Msg m = it.next();
            if (checkFunction.test(m)) {
                it.remove();
                notify(ChangeType.REMOVED, m);
            }
        }
    }

    /**
     * Returns a unmodifiable list of all messages
     */
    public List<Msg> messages() {
        return Collections.unmodifiableList(conversation.stream().toList());
    }

    public Optional<Msg> removeFirst() {
        Msg message = conversation.pollFirst();
        if (message != null) {
            notify(ChangeType.REMOVED, message);
        }
        return Optional.ofNullable(message);
    }

    public Optional<Msg> removeLast() {
        Msg message = conversation.pollLast();
        if (message != null) {
            notify(ChangeType.REMOVED, message);
        }
        return Optional.ofNullable(message);
    }

    public Optional<Msg> firstMessage() {
        return Optional.ofNullable(conversation.peekFirst());
    }

    public Optional<Msg> lastMessage() {
        return Optional.ofNullable(conversation.peekLast());
    }

    /**
     * Removes all messages until the check function returns false, starting with the first element
     */
    public synchronized void trimStart(Predicate<Msg> checkFunction) {
        while (!conversation.isEmpty() && checkFunction.test(conversation.peekFirst())) {
            notify(ChangeType.REMOVED, conversation.removeFirst());
        }
    }

    /**
     * Removes all messages until the check function returns false, starting with the last element
     */
    public synchronized void trimEnd(Predicate<Msg> checkFunction) {
        while (!conversation.isEmpty() && checkFunction.test(conversation.peekLast())) {
            notify(ChangeType.REMOVED, conversation.removeLast());
        }
    }

    public boolean contains(Predicate<Msg> checkFunction) {
        return conversation.stream().anyMatch(checkFunction);
    }

    /**
     * Serializes the conversation, compresses it with GZIP and returns a
     * Base64 encoded string.
     *
     * @return Base64 string
     * @throws IOException if serialization fails
     */
    public String serializeConversation() throws IOException {
        JSONWrapper msgList = new JSONWrapper(new ArrayList<>(conversation));

        SimpleModule module = new SimpleModule();
        module.addSerializer(Instant.class, new InstantToStringSerializer());
        module.addDeserializer(Instant.class, new StringToInstantDeserializer());

        JsonMapper mapper = JsonMapper.builder()
                .configure(MapperFeature.REQUIRE_HANDLERS_FOR_JAVA8_TIMES, false)
                .addModule(module)
                .build();
        String json = mapper.writeValueAsString(msgList);

        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOut = new GZIPOutputStream(byteOut)) {
            gzipOut.write(json.getBytes(StandardCharsets.UTF_8));
        }

        return Base64.getEncoder().encodeToString(byteOut.toByteArray());
    }

    /**
     * Loads a conversation from a Base64 encoded, GZIP compressed string.
     *
     * @param encoded Base64 string
     * @throws IOException            if decompression fails
     * @throws ClassNotFoundException if the message class cannot be found
     */
    public void deserializeConversation(String encoded)
            throws IOException, ClassNotFoundException {
        if (encoded == null || encoded.isEmpty()) {
            return;
        }

        byte[] compressed = Base64.getDecoder().decode(encoded);

        String json;
        try (
                GZIPInputStream gzipIn = new GZIPInputStream(new ByteArrayInputStream(compressed));
                InputStreamReader reader = new InputStreamReader(gzipIn, StandardCharsets.UTF_8)) {
            StringBuilder sb = new StringBuilder();
            char[] buf = new char[1024];
            int len;
            while ((len = reader.read(buf)) != -1) {
                sb.append(buf, 0, len);
            }
            json = sb.toString();
        }

        SimpleModule module = new SimpleModule();
        module.addSerializer(Instant.class, new InstantToStringSerializer());
        module.addDeserializer(Instant.class, new StringToInstantDeserializer());

        JsonMapper mapper = JsonMapper.builder()
                .configure(MapperFeature.REQUIRE_HANDLERS_FOR_JAVA8_TIMES, false)
                .addModule(module)
                .build();

        JSONWrapper wrapper = mapper.readValue(json, JSONWrapper.class);

        if (wrapper == null) {
            throw new IOException("invalid data error");
        }

        clear();
        wrapper.getMessages().forEach(this::addLast);
    }

}
