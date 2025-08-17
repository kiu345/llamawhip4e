package com.github.kiu345.eclipse.eclipseai;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.github.kiu345.eclipse.eclipseai.preferences.PromptsPreferencePresenter;

public class Activator extends AbstractUIPlugin {
    private static Activator plugin = null;
    private static BundleContext context = null;

    private static final String DEBUG_PROP = "qdevzone.eclipseai.debug";
    private static Boolean DO_DEBUG = null;

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        Activator.context = context;
        plugin = this;
    }

    public static BundleContext getBundleContext() {
        return context;
    }

    public static Activator getDefault() {
        return plugin;
    }

    /** start eclipse with `-Ddevzone.eclipseai.debug=true` for extended debugging output */
    public static boolean isDebugEnabled() {
        if (DO_DEBUG == null) {
            String val = System.getProperty(DEBUG_PROP, "false");
            DO_DEBUG = Boolean.parseBoolean(val);
        }
        return DO_DEBUG;
    }

    // rest of the class code goes here
    public PromptsPreferencePresenter getPromptsPreferncePresenter() {
        PromptsPreferencePresenter presenter = new PromptsPreferencePresenter(getDefault().getPreferenceStore());
        return presenter;
    }
}
