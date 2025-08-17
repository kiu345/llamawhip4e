package com.github.kiu345.eclipse.eclipseai.handlers;

import com.github.kiu345.eclipse.eclipseai.prompt.Prompts;

public class EclipseAIUnitTestHandler extends EclipseAIHandlerTemplate {

    public EclipseAIUnitTestHandler() {
        super(Prompts.TEST_CASE);
    }
}
