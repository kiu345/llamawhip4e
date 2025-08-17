package com.github.kiu345.eclipse.eclipseai.services.tools;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.eclipse.core.runtime.ILog;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.github.kiu345.eclipse.eclipseai.services.tools.ToolService.ToolInfo;
import com.github.kiu345.eclipse.util.MockUtils;

import dev.langchain4j.agent.tool.ToolExecutionRequest;

@TestMethodOrder(OrderAnnotation.class)
class ToolServiceTest {

    private static final String TEST_TOOL_NAME = "dateAndTime";
    private static final String TEST_WEBTOOL_NAME = "webSearch";

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

    @Order(1)
    @Test
    void testFindTools() {
        ToolService service = ContextInjectionFactory.make(ToolService.class, context);

        service.setToolClasses(SimpleAITools.class, WebTools.class);

        List<ToolInfo> tools;
        tools = service.findTools(true);
        assertThat(tools)
                .isNotNull()
                .anyMatch(e -> e.getTool() != null && TEST_TOOL_NAME.equals(e.getTool().name()))
                .anyMatch(e -> e.getTool() != null && TEST_WEBTOOL_NAME.equals(e.getTool().name()));

        tools = service.findTools(false);
        assertThat(tools)
                .isNotNull()
                .anyMatch(e -> e.getTool() != null && TEST_TOOL_NAME.equals(e.getTool().name()))
                .allMatch(e -> e.getTool() != null && !TEST_WEBTOOL_NAME.equals(e.getTool().name()));
    }

    @Order(2)
    @Test
    void testExecuteTool() throws Exception {
        ToolService service = ContextInjectionFactory.make(ToolService.class, context);

        service.setToolClasses(SimpleAITools.class);

        ToolExecutionRequest request = ToolExecutionRequest.builder()
                .name(TEST_TOOL_NAME)
                .arguments("").build();

        final String year = DateTimeFormatter.ofPattern("uuuu").format(LocalDateTime.now());

        List<ToolInfo> tools = service.findTools(true);
        String result = service.executeTool(tools, request);

        assertThat(result)
                .isNotNull()
                .contains(year);
    }
}
