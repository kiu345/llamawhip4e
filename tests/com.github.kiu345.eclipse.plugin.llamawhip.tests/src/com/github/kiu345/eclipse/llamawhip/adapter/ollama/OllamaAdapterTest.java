package com.github.kiu345.eclipse.llamawhip.adapter.ollama;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mockito.Mockito;

import com.github.kiu345.eclipse.llamawhip.adapter.AbstractAdapterTest;
import com.github.kiu345.eclipse.llamawhip.config.PluginConfiguration;
import com.github.kiu345.eclipse.llamawhip.messaging.UserMsg;
import com.github.kiu345.eclipse.llamawhip.prompt.PromptLoader;
import com.github.kiu345.eclipse.llamawhip.prompt.Prompts;
import com.github.kiu345.eclipse.util.MockUtils;

@EnabledIfEnvironmentVariable(named = OllamaAdapterTest.ENV_OLLAMA_URL, matches = "http.*", disabledReason = "OLLAMA_URL not defined")
@Tag("adapter")
class OllamaAdapterTest extends AbstractAdapterTest {

    public static final String ENV_OLLAMA_URL = "OLLAMA_URL";
    private String serverURL;

    @BeforeEach
    void setUp() throws Exception {
        serverURL = System.getenv(ENV_OLLAMA_URL);
    }

    @AfterEach
    void tearDown() throws Exception {
    }

    private static String chatModel = "qwen3:4b";
    private static String generateModel = "qwen3:4b";

    @Test
    void testGetModels() throws Exception {

        var config = new OllamaAdapter.Config(serverURL, null);
        ILog log = MockUtils.createLogMock();

        OllamaAdapter adapter = new OllamaAdapter(log, config);

        var ollamaModels = adapter.getModels();
        assertThat(ollamaModels).isNotEmpty();

        var model = ollamaModels.stream().filter(e -> chatModel.equals(e.name())).findAny().orElse(null);
        assertThat(model).isNotNull();

    }

    @Test
    void testAIService() throws Exception {

        var config = new OllamaAdapter.Config(serverURL, null);
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

        var result = future.orTimeout(15, TimeUnit.SECONDS).join();
        assertThat(result.isOK()).isTrue();
        assertThat(result.getMessage()).isEqualTo("OK");
    }

    @Test
    void testAIGenerate() throws Exception {
        PluginConfiguration configMock = Mockito.mock(PluginConfiguration.class);

        PluginConfiguration.inject(configMock);

        var config = new OllamaAdapter.Config(serverURL, null);
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
                """.formatted(TEST_JAVA_PREFIX, TEST_JAVA_SUFFIX));

        adapter.config().setSystemPrompt(systemPrompt);
        adapter.config().setThinkingAllowed(true);
        var response = adapter.generate(model, message.getMessage());
        assertThat(response.getMessage()).isNotEmpty();
    }

}
