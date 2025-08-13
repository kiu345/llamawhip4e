package com.github.kiu345.eclipse.eclipseai.ai;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.github.kiu345.eclipse.eclipseai.adapter.ollama.OllamaAdapter;
import com.github.kiu345.eclipse.eclipseai.messaging.UserMsg;
import com.github.kiu345.eclipse.eclipseai.prompt.PromptLoader;
import com.github.kiu345.eclipse.eclipseai.prompt.Prompts;
import com.github.kiu345.eclipse.util.MockUtils;

import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.CompleteToolCall;
import dev.langchain4j.model.chat.response.PartialThinking;
import dev.langchain4j.model.chat.response.PartialToolCall;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;

@SuppressWarnings("unused")
@Disabled
class AIServiceIT {
    private class ChatModelDataHanlder implements ChatModelListener {

        @Override
        public void onRequest(ChatModelRequestContext requestContext) {
            System.out.println("AIServiceTest.ChatModelDataHanlder.onRequest()");
        }

        @Override
        public void onResponse(ChatModelResponseContext responseContext) {
            System.out.println("AIServiceTest.ChatModelDataHanlder.onResponse()");
        }

        @Override
        public void onError(ChatModelErrorContext errorContext) {
            System.out.println("AIServiceTest.ChatModelDataHanlder.onError()");
        }

    }

    private class StreamHandler implements StreamingChatResponseHandler {

        @Override
        public void onPartialResponse(String partialResponse) {
//            System.out.println("AIServiceTest.StreamHandler.onPartialResponse()");
            System.out.print(partialResponse);
        }

        @Override
        public void onPartialThinking(PartialThinking partialThinking) {
            System.out.println("---");
            System.out.println("AIServiceTest.StreamHandler.onPartialThinking()");
            System.out.println(partialThinking.text());
        }

        @Override
        public void onPartialToolCall(PartialToolCall partialToolCall) {
            System.out.println("---");
            System.out.println("AIServiceTest.StreamHandler.onPartialToolCall()");
        }

        @Override
        public void onCompleteToolCall(CompleteToolCall completeToolCall) {
            System.out.println("---");
            System.out.println("AIServiceTest.StreamHandler.onCompleteToolCall()");
        }

        @Override
        public void onCompleteResponse(ChatResponse completeResponse) {
            System.out.println("---");
            System.out.println("AIServiceTest.StreamHandler.onCompleteResponse()");
            System.out.println(completeResponse.finishReason().name());
            System.out.println("---");
            System.out.println(completeResponse.aiMessage().thinking());
            System.out.println("---");
            System.out.println(completeResponse.aiMessage().text());
            System.out.println("---");
        }

        @Override
        public void onError(Throwable error) {
            System.out.println("---");
            System.out.println("---");
            System.out.println("AIServiceTest.StreamHandler.onError()");
            System.out.println(error.getMessage());
        }
    }

    private static String TESTJAVA = """
            package com.github.kiu345.eclipse.eclipseai;
            public class Demo {
                private static String DEMOTEXT = "Hello world";

                public void showMessage() {
                    System.out.println(DEMOTEXT);
                }
            }
            """;

    private static String TESTJAVA2 = """
            package com.github.kiu345.eclipse.eclipseai;
            public class Demo {
                private static String DEMOTEXT = "Hello world";

                public void showMessage() {
                    System.out.println(DEMOTEXT)
                }
            }
            """;

    private static String TEST3_PREFIX = """
            package com.github.kiu345.eclipse.eclipseai;

            import static org.assertj.core.api.Assertions.assertThat;


            class BasicAuthTest {
                @Mock
                private AuthService service;

            """;

    private static String TEST3_SUFFIX = """

                    void testLogout() {
                        service.logout();
                        assertThat(service.getUser()).isNull();
                    }
                }
            """;

// private static   String modelName = "qwen3:4b";
    private static String chatModel = "gpt-oss:20b";
// private static String modelName = "tinydolphin:latest";
// private static  String modelName = "all-minilm:latest";
    private static String generateModel = "qwen3:4b";

    @Test
    @Disabled
    void testAIService() throws Exception {

        var config = new OllamaAdapter.Config("http://localhost:11434", null);
        ILog log = MockUtils.createLogMock();

        OllamaAdapter adapter = new OllamaAdapter(log, config);

        var ollamaModels = adapter.getModels();

        var model = ollamaModels.stream().filter(e -> chatModel.equals(e.name())).findAny().get();

        URL baseUrl = getClass().getClassLoader().getResource("prompts/");
        assertThat(baseUrl).isNotNull();

        PromptLoader promptLoader = new PromptLoader(log, baseUrl.toString());

        String date = DateTimeFormatter.ISO_DATE.format(LocalDate.now());
        String salutation = "informal";
        String language = "German";

        var systemPrompt = promptLoader.getDefaultPrompt(Prompts.SYSTEM, date, language, salutation);
        assertThat(systemPrompt).isNotEmpty();

        UserMsg message = new UserMsg("Welches Datum haben wir");

        adapter.config().setSystemPrompt(systemPrompt);
        adapter.config().setThinkingAllowed(false);
        System.out.println("--------------------------------");

        CompletableFuture<IStatus> future;

//        future = adapter.chatRequest(model, (e) -> {}, message)
//                .onError(e -> e.printStackTrace())
//                .onPartialThinkingResponse(System.err::print)
//                .onPartialResponse(System.out::print)
//                .onCompleteResponse(e -> {
//                    System.out.println("--------------------------------");
//                    System.out.println("\n" + e.finishReason().name());
//                })
//                .onError(e -> {
//                    e.printStackTrace();
//                })
//                .exec();
//        System.out.println(future.join());
//
//        System.out.println("\n--------------------------------");
//
//        message = new UserMsg("""
//                Was ist das für eine Datei?
//                <|FILE=Demo.java|>
//                %s
//                </|FILE|>
//                """.formatted(TESTJAVA));
//
//        adapter.config().setSystemPrompt(systemPrompt);
//        adapter.config().setThinkingAllowed(true);
//        future = adapter.chatRequest(model, (e) -> {}, message)
//                .onError(e -> e.printStackTrace())
//                .onPartialThinkingResponse(System.err::print)
//                .onPartialResponse(System.out::print)
//                .onCompleteResponse(e -> {
//                    System.out.println("--------------------------------");
//                    System.out.println("\n" + e.finishReason().name());
//                })
//                .onError(e -> {
//                    e.printStackTrace();
//                })
//                .exec();
//        System.out.println(future.join());
//
//        System.out.println("\n--------------------------------");
//
//        message = new UserMsg("""
//                Wo ist der Fehler?
//                <|FILE=Demo.java|>
//                %s
//                </|FILE|>
//                """.formatted(TESTJAVA2));
//
//        adapter.config().setSystemPrompt(systemPrompt);
//        adapter.config().setThinkingAllowed(true);
//        future = adapter.chatRequest(model, (e) -> {}, message)
//                .onError(e -> e.printStackTrace())
//                .onPartialThinkingResponse(System.err::print)
//                .onPartialResponse(System.out::print)
//                .onCompleteResponse(e -> {
//                    System.out.println("--------------------------------");
//                    System.out.println("\n" + e.finishReason().name());
//                })
//                .onError(e -> {
//                    e.printStackTrace();
//                })
//                .exec();
//
//        System.out.println("\n--------------------------------");

        message = new UserMsg("""
                This is a code completion request form a IDE.
                Create a code completion ideas for code at the location ${CURSOR}!
                Only return the code without any markup!
                <|CONTEXT|>
                %s
                        ${CURSOR}
                %s
                </|CONTEXT|>
                """.formatted(TEST3_PREFIX, TEST3_SUFFIX));

        adapter.config().setSystemPrompt(systemPrompt);
        adapter.config().setThinkingAllowed(true);
        future = adapter.chatRequest(model, (e) -> {}, message)
                .onError(e -> e.printStackTrace())
                .onPartialThinkingResponse(System.err::print)
                .onPartialResponse(System.out::print)
                .onCompleteResponse(e -> {
                    System.out.println("--------------------------------");
                    System.out.println("\n" + e.finishReason().name());
                })
                .onError(e -> {
                    e.printStackTrace();
                })
                .exec();
        System.out.println(future.join());
    }

    @Test
    // @Disabled
    void testAIGenerate() throws Exception {
        var config = new OllamaAdapter.Config("http://localhost:11434", null);
        ILog log = MockUtils.createLogMock();

        OllamaAdapter adapter = new OllamaAdapter(log, config);

        var ollamaModels = adapter.getModels();

        var model = ollamaModels.stream().filter(e -> generateModel.equals(e.name())).findAny().get();

        URL baseUrl = getClass().getClassLoader().getResource("prompts/");
        assertThat(baseUrl).isNotNull();

        PromptLoader promptLoader = new PromptLoader(log, baseUrl.toString());

        String date = DateTimeFormatter.ISO_DATE.format(LocalDate.now());
        String salutation = "informal";
        String language = "German";

        var systemPrompt = promptLoader.getDefaultPrompt(Prompts.SYSTEM, date, language, salutation);
        assertThat(systemPrompt).isNotEmpty();

        UserMsg message = new UserMsg("Welches Datum haben wir");

        adapter.config().setSystemPrompt(systemPrompt);
        adapter.config().setThinkingAllowed(false);
        System.out.println("--------------------------------");

        message = new UserMsg("""
                This is a code completion request form a IDE.
                Create a code completion ideas for code at the location ${CURSOR}!
                Only return the code without any markup!
                <|CONTEXT|>
                %s
                        ${CURSOR}
                %s
                </|CONTEXT|>
                """.formatted(TEST3_PREFIX, TEST3_SUFFIX));

        adapter.config().setSystemPrompt(systemPrompt);
        adapter.config().setThinkingAllowed(true);
        var response = adapter.generate(model, message.getMessage());
        System.out.println(response.getMessage());
    }

}
