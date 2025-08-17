package com.github.kiu345.eclipse.eclipseai.prompt;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.core.runtime.ILog;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.kiu345.eclipse.util.MockUtils;

class PromptLoaderTest {
    private IEclipseContext context;
    private ILog logMock;

    @BeforeEach
    void setUp() {
        context = EclipseContextFactory.create();
        logMock = MockUtils.createLogMock();
        context.set(ILog.class, logMock);
    }

    @AfterEach
    void tearDown() {
        context.dispose();
    }

    @Test
    void testGetDefaultPrompt() {
        PromptLoader promptLoader = ContextInjectionFactory.make(PromptLoader.class, context);

        String prompt = promptLoader.getDefaultPrompt(Prompts.SYSTEM.getFileName());
        assertThat(prompt).isNotEmpty();
    }

    @Test
    void testUpdatePromptText() {
        PromptLoader promptLoader = ContextInjectionFactory.make(PromptLoader.class, context);
        String result = promptLoader.updatePromptText("[fileName] = ${fileName}", "${fileName}", "Testfile.java");
        assertThat(result)
                .isNotEmpty()
                .contains("[fileName] = Testfile.java");
    }

}
