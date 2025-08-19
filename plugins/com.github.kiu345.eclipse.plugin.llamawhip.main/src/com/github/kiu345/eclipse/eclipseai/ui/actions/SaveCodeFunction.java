package com.github.kiu345.eclipse.eclipseai.ui.actions;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;

public class SaveCodeFunction extends BrowserFunction {
    public interface Handler {
        public void onSave(String codeBlock, String fileName);
    }

    private final Handler handler;

    public SaveCodeFunction(Browser browser, String name, Handler handler) {
        super(browser, name);
        if (handler == null) {
            throw new IllegalArgumentException("handler is null");
        }
        this.handler = handler;
    }

    @Override
    public Object function(Object[] arguments) {
        if (arguments == null || arguments.length != 1) {
            return null;
        }
        String codeBlock = (String) arguments[0];

        // Open a file dialog to select the save location
        FileDialog fileDialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
        fileDialog.setFilterPath(System.getProperty("user.home")); // Set default path to user's home directory
        String fileName = fileDialog.open();

        if (fileName != null) {
            handler.onSave(codeBlock, fileName);
        }

        return null;
    }

}
