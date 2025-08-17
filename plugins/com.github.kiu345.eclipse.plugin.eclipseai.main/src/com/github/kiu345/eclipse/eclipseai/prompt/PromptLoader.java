package com.github.kiu345.eclipse.eclipseai.prompt;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.e4.core.di.annotations.Creatable;

import jakarta.inject.Singleton;

/**
 * A singleton class responsible for loading prompt text from resource files and applying substitutions.
 * The resource files are located in the "prompts" folder of the plugin.
 */
@Creatable
@Singleton
public class PromptLoader {
    private final String BASE_URL = "platform:/plugin/com.github.kiu345.eclipse.plugin.eclipseai.main/prompts/";

    private String baseURL = BASE_URL;

    public String updatePromptText(String promptText, String... substitutions) {
        if (substitutions.length % 2 != 0) {
            throw new IllegalArgumentException("Expecting key, value pairs");

        }
        for (int i = 0; i < substitutions.length; i = i + 2) {
            promptText = promptText.replace(substitutions[i], substitutions[i + 1]);
        }
        return promptText;
    }

    public String getDefaultPrompt(String resourceFile) {
        try (var in = FileLocator.toFileURL(URI.create(baseURL + resourceFile).toURL()).openStream(); var dis = new DataInputStream(in);) {
            var prompt = new String(dis.readAllBytes(), StandardCharsets.UTF_8);
            return prompt;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
