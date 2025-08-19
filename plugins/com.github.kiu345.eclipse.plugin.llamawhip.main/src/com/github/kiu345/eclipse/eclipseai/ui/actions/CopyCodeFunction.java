package com.github.kiu345.eclipse.eclipseai.ui.actions;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;

public class CopyCodeFunction extends BrowserFunction {
    public interface Handler {
        public void onCopyCode(String codeBlock);
    }

    private final Handler handler;

    public CopyCodeFunction(Browser browser, String name, Handler handler) {
        super(browser, name);
        if (handler == null) {
            throw new IllegalArgumentException("handler is null");
        }
        this.handler = handler;
    }

    @Override
    public Object function(Object[] arguments) {
        if (arguments.length > 0 && arguments[0] instanceof String) {
            String codeBlock = (String) arguments[0];
            if (StringUtils.isNotBlank(codeBlock)) {
                handler.onCopyCode(codeBlock);
            }
        }
        return null;
    }

}
