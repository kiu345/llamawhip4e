package com.github.kiu345.eclipse.eclipseai.ui.actions;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;

public class SendPromptFunction extends BrowserFunction {
    public interface Handler {
        public void onSend(String message, boolean predefinedPrompt);
    }

    private final Handler handler;

    public SendPromptFunction(Browser browser, String name, Handler handler) {
        super(browser, name);
        if (handler == null) {
            throw new IllegalArgumentException("handler is null");
        }
        this.handler = handler;
    }

    @Override
    public Object function(Object[] arguments) {
        if (arguments.length > 0 && (arguments[0] instanceof String userPrompt)) {
            Boolean isPreDefinedPormpt = false;
            if (arguments.length > 1 && arguments[1] instanceof Boolean) {
                isPreDefinedPormpt = Boolean.valueOf(arguments[1].toString());
            }
            handler.onSend(userPrompt, isPreDefinedPormpt);
        }
        return null;
    }

}
