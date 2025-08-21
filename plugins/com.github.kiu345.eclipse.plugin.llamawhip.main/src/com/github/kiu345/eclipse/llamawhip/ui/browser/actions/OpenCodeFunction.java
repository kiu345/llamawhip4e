package com.github.kiu345.eclipse.llamawhip.ui.browser.actions;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;

public class OpenCodeFunction extends BrowserFunction {
    public interface Handler {
        public void onOpenCode(String type, String codeBlock);
    }

    private final Handler handler;

    public OpenCodeFunction(Browser browser, String name, Handler handler) {
        super(browser, name);
        if (handler == null) {
            throw new IllegalArgumentException("handler is null");
        }
        this.handler = handler;
    }

    @Override
    public Object function(Object[] arguments) {
        if (arguments.length > 0 && arguments[0] instanceof String) {
            String type = (String) arguments[0];
            String codeBlock = (String) arguments[1];
            if (StringUtils.isNotBlank(codeBlock)) {
                handler.onOpenCode(type, codeBlock);
            }
        }
        return null;
    }

}
