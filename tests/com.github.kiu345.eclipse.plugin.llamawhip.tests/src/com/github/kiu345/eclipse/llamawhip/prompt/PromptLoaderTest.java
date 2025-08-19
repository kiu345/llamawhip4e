package com.github.kiu345.eclipse.llamawhip.prompt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.ILog;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.github.kiu345.eclipse.llamawhip.Activator;
import com.github.kiu345.eclipse.util.MockUtils;

class PromptLoaderTest {

    private IEclipseContext context;
    private Bundle bundle;
    private BundleContext bundleContext;
    private ILog logMock;

    @BeforeEach
    void setUp() throws Exception {
        bundle = Mockito.mock(Bundle.class);
        when(bundle.getSymbolicName()).thenReturn(Activator.PLUGIN_ID);
        when(bundle.getEntry(anyString())).thenAnswer(invocation -> {
            String arg = invocation.getArgument(0);
            return getClass().getClassLoader().getResource(arg);
        });

        bundleContext = Mockito.mock(BundleContext.class);
        when(bundleContext.getBundle()).thenReturn(bundle);

        context = EclipseContextFactory.create();
        logMock = MockUtils.createLogMock();
        context.set(ILog.class, logMock);

        Activator a = new Activator();
        a.setDebugging(true);
        a.start(bundleContext);
    }

    @AfterEach
    void tearDown() {
        context.dispose();
    }

    @Test
    void testGetDefaultPrompt() {
        PromptLoader promptLoader = ContextInjectionFactory.make(PromptLoader.class, context);

        String prompt = promptLoader.getDefaultPrompt(Prompts.SYSTEM);
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
