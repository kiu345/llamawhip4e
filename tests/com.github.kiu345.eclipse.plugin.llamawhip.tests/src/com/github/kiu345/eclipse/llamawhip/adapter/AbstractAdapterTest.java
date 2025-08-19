package com.github.kiu345.eclipse.llamawhip.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;

import org.eclipse.core.runtime.ILog;

import com.github.kiu345.eclipse.llamawhip.prompt.PromptLoader;

import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.CompleteToolCall;
import dev.langchain4j.model.chat.response.PartialThinking;
import dev.langchain4j.model.chat.response.PartialToolCall;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;

public abstract class AbstractAdapterTest {
    protected static final String TEST_JAVA_OK = """
            package com.example;
            public class Demo {
                private static String DEMOTEXT = "Hello world";

                public void showMessage() {
                    System.out.println(DEMOTEXT);
                }
            }
            """;

    protected static final String TEST_JAVA_ERROR = """
            package com.example;
            public class Demo {
                private static String DEMOTEXT = "Hello world";

                public void showMessage() {
                    System.out.println(DEMOTEXT)
                }
            }
            """;

    protected static final String TEST_JAVA_PREFIX = """
            package com.example;

            import static org.assertj.core.api.Assertions.assertThat;


            class BasicAuthTest {
                @Mock
                private AuthService service;

            """;

    protected static final String TEST_JAVA_SUFFIX = """

                    void testLogout() {
                        service.logout();
                        assertThat(service.getUser()).isNull();
                    }
                }
            """;

    protected class ChatModelDataHanlder implements ChatModelListener {

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

    protected class StreamHandler implements StreamingChatResponseHandler {

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

    
    protected PromptLoader createPromptLoader(ILog log) {
        URL baseUrl = getClass().getClassLoader().getResource("prompts/");
        assertThat(baseUrl).isNotNull();
        return new PromptLoader(log, baseUrl.toString());
    }
}
