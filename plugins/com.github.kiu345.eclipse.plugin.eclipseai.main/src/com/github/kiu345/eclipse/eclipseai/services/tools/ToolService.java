package com.github.kiu345.eclipse.eclipseai.services.tools;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.ILog;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Creatable;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Creatable
@Singleton
public class ToolService {
    @Inject
    private ILog log;
    @Inject
    private IEclipseContext iEclipseContext;

    private Class<?>[] classes;

    public static class ToolInfo {
        private Method method;
        private ToolSpecification tool;

        public Method getMethod() {
            return method;
        }

        public ToolSpecification getTool() {
            return tool;
        }
    }

    public ToolService() {
        classes = new Class<?>[] {
                SimpleAITools.class,
                WebTools.class,
                IDETools.class,
                JavaTools.class
        };
    }

    public void setToolClasses(Class<?>... classes) {
        this.classes = classes;
    }

    public List<ToolInfo> findTools(boolean allowRemote) {

        if (classes == null || classes.length == 0) {
            return Lists.newArrayList();
        }
        List<ToolInfo> result = new LinkedList<>();
        try {
            for (Class<?> classInfo : classes) {
                List<Method> methods = stream(classInfo.getDeclaredMethods())
                        .filter(method -> method.isAnnotationPresent(Tool.class) && (allowRemote || !method.isAnnotationPresent(WebAccess.class)))
                        .collect(toList());
                for (Method m : methods) {
                    ToolInfo info = new ToolInfo();
                    info.method = m;
                    info.tool = ToolSpecifications.toolSpecificationFrom(m);
                    result.add(info);
                }
            }
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    public String executeTool(List<ToolInfo> tools, ToolExecutionRequest request) throws IOException {
        log.info("tool call: " + request.name());
        for (ToolInfo tool : tools) {
            if (tool.tool.name().contentEquals(request.name())) {
                try {
                    Object result = executeTool(tool, request.arguments());
                    if (result == null) {
                        return "{\"result\": \"OK\"}";
                    }
                    if (result instanceof String val) {
                        return val;
                    }
                    ObjectMapper mapper = new ObjectMapper();
                    return mapper.writeValueAsString(result);
                }
                catch (ReflectiveOperationException e) {
                    throw new IOException("Could not invoke tool", e);
                }
            }
        }
        throw new IllegalArgumentException(request.name());
    }

    private Object executeTool(ToolInfo tool, String arguments) throws IOException, ReflectiveOperationException {
        Map<String, Object> params = argumentsAsMap(arguments);
        if (tool.method.getParameterCount() != params.size()) {
            return "{ \"error\": \"invalid parameter count\"}";
        }

        Object serviceObj = tool.method.getDeclaringClass().getDeclaredConstructor().newInstance();
        ContextInjectionFactory.inject(serviceObj, iEclipseContext);
        Object[] methodParams = params.values().toArray();
        return tool.method.invoke(serviceObj, methodParams);
    }

    private static Map<String, Object> argumentsAsMap(String arguments) throws IOException {
        if (StringUtils.isBlank(arguments)) {
            return Map.of();
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(arguments, new TypeReference<Map<String, Object>>() {
        });
    }
}
