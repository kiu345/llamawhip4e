package com.github.kiu345.eclipse.eclipseai.services.tools;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.core.runtime.ILog;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.kiu345.eclipse.util.MockUtils;

class WebToolsTest {

    private ILog logMock;
    private IEclipseContext context;

    @BeforeEach
    void setUp() {
        logMock = MockUtils.createLogMock();

        context = EclipseContextFactory.create();
        context.set(ILog.class, logMock);
    }

    @AfterEach
    void tearDown() {
        context.dispose();
    }

    @Test
    void testWebSearch() {
        WebTools service = ContextInjectionFactory.make(WebTools.class, context);
        String result = service.webSearch("Ollama");
        System.out.println(result);
        assertThat(result)
                .isNotEmpty()
                .contains("ollama.com");
    }

    @Test
    void testReadWebPage() {
        WebTools service = ContextInjectionFactory.make(WebTools.class, context);
        String result = service.readWebPage("https://ollama.com");
        System.out.println(result);
        assertThat(result)
                .isNotEmpty()
                .contains("Ollama Inc");
    }

}
