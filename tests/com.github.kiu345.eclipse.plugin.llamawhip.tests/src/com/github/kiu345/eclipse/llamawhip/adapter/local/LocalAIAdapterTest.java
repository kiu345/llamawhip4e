package com.github.kiu345.eclipse.llamawhip.adapter.local;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.eclipse.core.runtime.ILog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mockito.Mockito;

import com.github.kiu345.eclipse.llamawhip.adapter.AbstractAdapterTest;
import com.github.kiu345.eclipse.llamawhip.adapter.ModelDescriptor;
import com.github.kiu345.eclipse.llamawhip.config.PluginConfiguration;
import com.github.kiu345.eclipse.llamawhip.messaging.UserMsg;
import com.github.kiu345.eclipse.llamawhip.prompt.PromptLoader;
import com.github.kiu345.eclipse.llamawhip.prompt.Prompts;
import com.github.kiu345.eclipse.util.MockUtils;

@EnabledIfEnvironmentVariable(named = LocalAIAdapterTest.ENV_LOCALAI_URL, matches = "http.*", disabledReason = "LOCALAI_URL not defined")
@Tag("adapter")
class LocalAIAdapterTest extends AbstractAdapterTest {
    public static final String ENV_LOCALAI_URL = "LOCALAI_URL";
    @SuppressWarnings("unused")
    private String serverURL;

    @BeforeEach
    void setUp() throws Exception {
        serverURL = System.getenv(ENV_LOCALAI_URL);
    }

    @AfterEach
    void tearDown() throws Exception {
    }

    @Test
    @Disabled
    void testGetModels() throws Exception {

        var config = new LocalAIAdapter.Config(serverURL, null);
        ILog log = MockUtils.createLogMock();

        LocalAIAdapter adapter = new LocalAIAdapter(log, config);

        var ollamaModels = adapter.getModels();
        assertThat(ollamaModels).isNotEmpty();
        System.out.println(ollamaModels);

//        var model = ollamaModels.stream().filter(e -> chatModel.equals(e.name())).findAny().orElse(null);
//        assertThat(model).isNotNull();

    }

    @Test
    void testAIGenerate() throws Exception {
        PluginConfiguration configMock = Mockito.mock(PluginConfiguration.class);

        PluginConfiguration.inject(configMock);

        var config = new LocalAIAdapter.Config(serverURL, null);
        ILog log = MockUtils.createLogMock();

        LocalAIAdapter adapter = new LocalAIAdapter(log, config);

        var model = new ModelDescriptor("qwen3-8b", "localai");

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
