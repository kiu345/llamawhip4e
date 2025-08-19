package com.github.kiu345.eclipse.llamawhip.config;

/**
 * Constant definitions for plug-in preferences
 */
public class PreferenceConstants {
    public static final String PLUGIN_ID = "com.github.kiu345.eclipse.plugin.eclipseai.main";

    private static final String PREFIX = "com.github.kiu345.eclipse.plugin.eclipseai.";

    public static final String PROFILES = PREFIX + "profiles";

    
    public static final String LANGUAGE = PREFIX + "language";
    public static final String DEFAULT_PROVIDER = PREFIX + "defaultprovider";
    public static final String DEFAULT_MODEL =  PREFIX + "defaultmodel";

    public static final String CC_ENABLED = PREFIX + "completion.enabled";
    public static final String CC_MODEL = PREFIX + "completion.model";
    public static final String CC_THINK = PREFIX + "completion.thinking";
    public static final String CC_TOOLS = PREFIX + "completion.tools";
    public static final String CC_CONTEXT = PREFIX + "completion.ctx.enabled";
    public static final String CC_CTX_BEFORE = PREFIX + "completion.ctx.before";
    public static final String CC_CTX_AFTER = PREFIX + "completion.ctx.after";
    public static final String CC_TIMEOUT = PREFIX + "completion.ctx.timeout";
}
