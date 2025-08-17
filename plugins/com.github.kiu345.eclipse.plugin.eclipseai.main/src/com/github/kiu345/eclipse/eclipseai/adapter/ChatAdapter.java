package com.github.kiu345.eclipse.eclipseai.adapter;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import com.github.kiu345.eclipse.eclipseai.adapter.ChatAdapterBase.ChatCall;
import com.github.kiu345.eclipse.eclipseai.messaging.Msg;
import com.github.kiu345.eclipse.eclipseai.model.ModelDescriptor;

import dev.langchain4j.model.ollama.OllamaStreamingChatModel;

public interface ChatAdapter {

    public class FinishHandler {

        private Throwable error;
        private ResponseListener.Event event;
        private Consumer<Throwable> errorHandler;
        private Consumer<ResponseListener.Event> eventHandler;
        private Runnable finallyRun;
        private boolean finished = false;

        public void finished(Throwable error) {
            this.error = error;
            finished = true;
        }

        public void finished(ResponseListener.Event event) {
            this.event = event;
            finished = true;
        }

        public FinishHandler onSuccess() {
            return this;
        }

        public FinishHandler onError(Consumer<Throwable> errorHandler) {
            this.errorHandler = errorHandler;
            return this;
        }

        public FinishHandler finallyDo(Runnable run) {
            finallyRun = run;
            return this;
        }

        public void run() {
            while (!finished) {
                Thread.yield();
            }
            if (error != null && errorHandler != null) {
                errorHandler.accept(error);
            }
            else if (eventHandler != null) {
                eventHandler.accept(event);
            }
            if (finallyRun != null) {
                finallyRun.run();
            }
        }
    }

    List<ModelDescriptor> getModels();

    ChatCall<OllamaStreamingChatModel> chatRequest(ModelDescriptor model, Consumer<Msg> newMessageConsumer, Collection<Msg> messages);

    default void cancelRequests() {
    };

}
