package com.github.kiu345.eclipse.eclipseai.prompt;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.ILog;
import org.eclipse.e4.core.di.annotations.Creatable;

import jakarta.inject.Singleton;

/**
 * A singleton class responsible for loading prompt text from resource files and applying substitutions.
 * The resource files are located in the "prompts" folder of the plugin.
 */
@Creatable
@Singleton
public class PromptLoader {
    private static final String BASE_URL = "platform:/plugin/com.github.kiu345.eclipse.plugin.eclipseai.main/prompts/";
    public static final String TEMPLATE_PROMPT = "${prompt}";
    public static final String TEMPLATE_DATE = "${date}";
    public static final String TEMPLATE_LANGUAGE = "${language}";
    public static final String TEMPLATE_SALUTATION = "${salutationType}";
    private static final String DEFAULT_BASE_TEMPLATE = TEMPLATE_PROMPT;

    private final ILog log;

    private String baseURL = BASE_URL;

    public PromptLoader(ILog log) {
        this.log = log;
        ;
    }

    public PromptLoader(ILog log, String baseURL) {
        this(log);
        this.baseURL = baseURL;
    }

    public String updatePromptText(String promptText, String... substitutions) {
        if (substitutions.length % 2 != 0) {
            throw new IllegalArgumentException("Expecting key, value pairs");

        }
        for (int i = 0; i < substitutions.length; i = i + 2) {
            promptText = promptText.replace(substitutions[i], substitutions[i + 1]);
        }
        return promptText;
    }

    @SuppressWarnings("unused")
    private static final Set<Prompts> EXTENDED_PROMPTS = Set.of(Prompts.SYSTEM);

    private URL buildFileURL(String filename) throws IOException {
        return FileLocator.toFileURL(URI.create(baseURL + filename).toURL());
    }

    public String getBaseTemplate() {
        try (var dis = new DataInputStream(buildFileURL("base.txt").openStream())) {
            return new String(dis.readAllBytes(), StandardCharsets.UTF_8);
        }
        catch (IOException e) {
            log.warn("failed to load base.txt prompt, using fallback", e);
        }
        return DEFAULT_BASE_TEMPLATE;
    }

    public String getDefaultPrompt(Prompts prompt) {
//        String wrapper = (EXTENDED_PROMPTS.contains(prompt)) ? getBaseTemplate() : DEFAULT_BASE_TEMPLATE;
        String wrapper = DEFAULT_BASE_TEMPLATE;
        String resourceFile = prompt.getFileName();
        try (var dis = new DataInputStream(buildFileURL(resourceFile).openStream())) {
            var promptTxt = new String(dis.readAllBytes(), StandardCharsets.UTF_8);
            return wrapper.replace(DEFAULT_BASE_TEMPLATE, promptTxt);
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public String getDefaultPrompt(Prompts prompt, String date, String language, String salutation) {
        String promptTxt = getDefaultPrompt(prompt);
        return replaceVariables(promptTxt, date, language, salutation);
    }

    public String replaceVariables(String input, String date, String language, String salutation) {
        return input.replace(TEMPLATE_DATE, date)
                .replace(TEMPLATE_LANGUAGE, language)
                .replace(TEMPLATE_SALUTATION, salutation);
    }

}
