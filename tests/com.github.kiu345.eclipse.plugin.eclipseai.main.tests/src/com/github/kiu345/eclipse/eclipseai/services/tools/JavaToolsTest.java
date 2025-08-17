package com.github.kiu345.eclipse.eclipseai.services.tools;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.ILog;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.kiu345.eclipse.util.MockUtils;

class JavaToolsTest {
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
    void testGetJavaSource() throws Exception {
        IWorkspace workspace = MockUtils.createIDEEnv(JavaTools.class);
        JavaTools service = ContextInjectionFactory.make(JavaTools.class, context);
        IJavaProject project = JavaCore.create(workspace.getRoot().getProject("Test Project"));
        String data = service.getAttachedSource("src.com.example.Test", project);
        assertThat(data)
                .isNotBlank()
                .startsWith("package src.com.example;");
    }

}
