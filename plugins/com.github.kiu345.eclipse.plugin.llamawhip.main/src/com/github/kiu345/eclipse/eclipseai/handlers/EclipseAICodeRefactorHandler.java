package com.github.kiu345.eclipse.eclipseai.handlers;

import com.github.kiu345.eclipse.llamawhip.prompt.Prompts;

public class EclipseAICodeRefactorHandler extends EclipseAIHandlerTemplate {
    public EclipseAICodeRefactorHandler() {
        super(Prompts.REFACTOR);
    }
}
