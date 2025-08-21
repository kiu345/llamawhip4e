package com.github.kiu345.eclipse.llamawhip.ui.handlers;

import org.eclipse.core.commands.IHandler;

import com.github.kiu345.eclipse.llamawhip.prompt.Prompts;

public class UnitTestHandler extends HandlerTemplate implements IHandler {

    public UnitTestHandler() {
        super(Prompts.TEST_CASE);
    }
}
