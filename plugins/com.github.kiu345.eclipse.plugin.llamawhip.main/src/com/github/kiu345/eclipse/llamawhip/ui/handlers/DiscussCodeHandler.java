package com.github.kiu345.eclipse.llamawhip.ui.handlers;

import org.eclipse.core.commands.IHandler;

import com.github.kiu345.eclipse.llamawhip.prompt.Prompts;

public class DiscussCodeHandler extends HandlerTemplate implements IHandler {
    public DiscussCodeHandler() {
        super(Prompts.DISCUSS);
    }
}
