package com.github.kiu345.eclipse.llamawhip.adapter.jlama;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.github.kiu345.eclipse.llamawhip.adapter.AbstractAdapterTest;
import com.github.kiu345.eclipse.llamawhip.adapter.ChatAdapterFactory;
import com.github.kiu345.eclipse.llamawhip.config.AIProvider;
import com.github.kiu345.eclipse.llamawhip.config.AIProviderProfile;
import com.github.kiu345.eclipse.llamawhip.config.PluginConfiguration;
import com.github.kiu345.eclipse.llamawhip.messaging.UserMsg;
import com.github.kiu345.eclipse.llamawhip.prompt.PromptLoader;
import com.github.kiu345.eclipse.llamawhip.prompt.Prompts;
import com.github.kiu345.eclipse.util.MockUtils;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.jlama.JlamaChatModel;

class JLamaAdapterTest extends AbstractAdapterTest {

    private static final String chatModel = "tjake/Llama-3.2-1B-Instruct-JQ4";

    @BeforeEach
    void setUp() throws Exception {
    }

    @Test
    void testChatAdapterSupport() {
        ILog log = MockUtils.createLogMock();

        AIProviderProfile profile = new AIProviderProfile();
        profile.setId(UUID.randomUUID());
        profile.setProvider(AIProvider.JLAMA);
        profile.setUrlBase(chatModel);

        var adapter = ChatAdapterFactory.create(log, profile);
        assertThat(adapter).isNotNull().isExactlyInstanceOf(JLamaAdapter.class);
    }

    @Test
    void testGetModels() throws Exception {
        var config = new JLamaAdapter.Config();
        config.setModels("tjake/Llama-3.2-1B-Instruct-JQ4");
        ILog log = MockUtils.createLogMock();

        JLamaAdapter adapter = new JLamaAdapter(log, config);

        var models = adapter.getModels();
        assertThat(models).isNotEmpty();
        var model = models.stream().filter(e -> chatModel.equals(e.model())).findAny().orElse(null);
        assertThat(model).isNotNull();
    }

    @Test
    void testChat() throws Exception {
        PluginConfiguration configMock = Mockito.mock(PluginConfiguration.class);

        PluginConfiguration.inject(configMock);

        PluginConfiguration.inject(configMock);

        var config = new JLamaAdapter.Config();
        config.setModels(chatModel);
        ILog log = MockUtils.createLogMock();

        JLamaAdapter adapter = new JLamaAdapter(log, config);

        var models = adapter.getModels();

        var model = models.stream().filter(e -> chatModel.equals(e.model())).findAny().get();
        assertThat(model).isNotNull().hasFieldOrPropertyWithValue("model", chatModel);

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

        CompletableFuture<IStatus> future;

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

        var result = future.orTimeout(60, TimeUnit.SECONDS).join();
        assertThat(result.isOK()).isTrue();
        assertThat(result.getMessage()).isEqualTo("OK");
    }

    @Test
    void testGenerate() throws Exception {
        PluginConfiguration configMock = Mockito.mock(PluginConfiguration.class);

        PluginConfiguration.inject(configMock);

        var config = new JLamaAdapter.Config();
        config.setModels(chatModel);
        ILog log = MockUtils.createLogMock();

        JLamaAdapter adapter = new JLamaAdapter(log, config);

        var models = adapter.getModels();

        var model = models.stream().filter(e -> chatModel.equals(e.model())).findAny().orElse(null);

        URL baseUrl = getClass().getClassLoader().getResource("prompts/");
        assertThat(baseUrl).isNotNull();

        PromptLoader promptLoader = new PromptLoader(log, baseUrl.toString());

        String date = DateTimeFormatter.ISO_DATE.format(LocalDate.now());
        String salutation = "informal";
        String language = "German";

        var systemPrompt = promptLoader.getDefaultPrompt(Prompts.SYSTEM, date, language, salutation);
        assertThat(systemPrompt).isNotEmpty();

        adapter.config().setSystemPrompt(systemPrompt);
        adapter.config().setThinkingAllowed(false);
        System.out.println("--------------------------------");

//        var message = new UserMsg("""
//                This is a code completion request form a IDE.
//                Create a code completion ideas for code at the location ${CURSOR}!
//                Only return the code without any markup!
//                <|CONTEXT|>
//                %s
//                        ${CURSOR}
//                %s
//                </|CONTEXT|>
//                """.formatted(TEST_JAVA_PREFIX, TEST_JAVA_SUFFIX));
        var message = new UserMsg("""
                Write a short java demo file.
                """);

        adapter.config().setSystemPrompt(systemPrompt);
        adapter.config().setThinkingAllowed(true);
        var response = adapter.generate(model, message.getMessage());
        System.out.println(response);
        assertThat(response.getMessage()).isNotEmpty();
    }

    @Test
    @Disabled
    void test() {

        ChatModel model = JlamaChatModel.builder()
                .modelName("tjake/Llama-3.2-1B-Instruct-JQ4")
                .temperature(0.3f)
                .build();

        ChatResponse chatResponse = model.chat(
                SystemMessage.from("You are helpful chatbot who is a java expert."),
                UserMessage.from("Write a java program to print hello world.")
        );

        System.out.println("\n" + chatResponse.aiMessage().text() + "\n");
    }

}
