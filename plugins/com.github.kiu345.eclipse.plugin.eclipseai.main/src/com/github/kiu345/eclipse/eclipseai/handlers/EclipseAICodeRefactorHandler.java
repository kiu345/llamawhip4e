package com.github.kiu345.eclipse.eclipseai.handlers;

import com.github.kiu345.eclipse.eclipseai.prompt.Prompts;

public class EclipseAICodeRefactorHandler extends EclipseAIHandlerTemplate {
    public EclipseAICodeRefactorHandler() {
        super(Prompts.REFACTOR);
    }
}
