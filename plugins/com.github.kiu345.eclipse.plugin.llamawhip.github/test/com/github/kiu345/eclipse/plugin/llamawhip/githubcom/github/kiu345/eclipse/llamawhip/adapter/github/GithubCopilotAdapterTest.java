package com.github.kiu345.eclipse.plugin.llamawhip.githubcom.github.kiu345.eclipse.llamawhip.adapter.github;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

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
import com.github.kiu345.eclipse.llamawhip.adapter.ollama.OllamaAdapter;
import com.github.kiu345.eclipse.llamawhip.config.PluginConfiguration;
import com.github.kiu345.eclipse.llamawhip.messaging.UserMsg;
import com.github.kiu345.eclipse.llamawhip.prompt.PromptLoader;
import com.github.kiu345.eclipse.llamawhip.prompt.Prompts;
import com.github.kiu345.eclipse.util.MockUtils;

@EnabledIfEnvironmentVariable(named = GithubCopilotAdapterTest.TOKEN, matches = ".+", disabledReason = "GITHUB_TOKEN not defined")
@Tag("adapter")
class GithubCopilotAdapterTest extends AbstractAdapterTest {
    public static final String TOKEN = "GITHUB_TOKEN";
    private static final String chatModel = "gpt-5-mini";
    private static final String generateModel = "gpt-5-mini";

    private String token;

    @BeforeEach
    void setUp() {
        token = System.getenv(TOKEN);
    }

    @AfterEach
    void tearDown() throws Exception {
    }

    @Test
    @Disabled
    void testGetModels() {

        var config = new GithubCopilotAdapter.Config(token);
        ILog log = MockUtils.createLogMock();

        GithubCopilotAdapter adapter = new GithubCopilotAdapter(log, config);

        var models = adapter.getModels();
        assertThat(models).isNotEmpty();

        var model = models.stream().filter(e -> chatModel.equals(e.name())).findAny().orElse(null);
        assertThat(model).isNotNull();

    }

    @Test
    void testAIGenerate() throws Exception {
        PluginConfiguration configMock = Mockito.mock(PluginConfiguration.class);

        PluginConfiguration.inject(configMock);

        var config = new GithubCopilotAdapter.Config(token);
        ILog log = MockUtils.createLogMock();

        GithubCopilotAdapter adapter = new GithubCopilotAdapter(log, config);

        var ollamaModels = adapter.getModels();

//        var model = ollamaModels.stream().filter(e -> generateModel.equals(e.name())).findAny().get();

//        String date = DateTimeFormatter.ISO_DATE.format(LocalDate.now());
//        String salutation = "informal";
//        String language = "German";

//        var systemPrompt = createPromptLoader(log).getDefaultPrompt(Prompts.SYSTEM, date, language, salutation);
//        assertThat(systemPrompt).isNotEmpty();
//
//        adapter.config().setSystemPrompt(systemPrompt);
        adapter.config().setThinkingAllowed(false);

//        UserMsg message = new UserMsg("""
//                This is a code completion request form a IDE.
//                Create a code completion ideas for code at the location ${CURSOR}!
//                Only return the code without any markup!
//                <|CONTEXT|>
//                %s
//                        ${CURSOR}
//                %s
//                </|CONTEXT|>
//                """.formatted(TEST_JAVA_PREFIX, TEST_JAVA_SUFFIX));
        UserMsg message = new UserMsg("Write a short java example program");

//        adapter.config().setSystemPrompt(systemPrompt);
//        adapter.config().setThinkingAllowed(true);
        var response = adapter.generate(null, message.getMessage());
        System.out.println(response);
        assertThat(response.getMessage()).isNotEmpty();
    }

}
