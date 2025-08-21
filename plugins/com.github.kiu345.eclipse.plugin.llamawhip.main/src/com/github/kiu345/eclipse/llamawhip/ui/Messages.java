package com.github.kiu345.eclipse.llamawhip.ui;

import org.eclipse.osgi.util.NLS;

/**
 * Holds the translation keys used by the plugin.
 * <p>
 * The constants are automatically populated by the Eclipse NLS mechanism
 * from the {@code messages.properties} file located in the same package.
 * </p>
 * <p>
 * This class should not be instantiated; it only provides static fields
 * that are referenced throughout the UI code.
 * </p>
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "com.github.kiu345.eclipse.llamawhip.ui.messages";


    public static String title;
    public static String description;

    public static String preferencePage_main_title;
    public static String preferencePage_main_descr;
    public static String preferencePage_models_title;
    public static String preferencePage_models_descr;
    public static String preferencePage_prompts_title;
    public static String preferencePage_prompts_descr;

    public static String button_add;
    public static String button_remove;
    public static String button_defaults;
    public static String button_validate;

    public static String label_name;
    public static String label_provider;
    public static String label_urlBase;
    public static String label_pathApi;
    public static String label_pathModels;
    public static String label_apiKey;
    public static String label_connectTimeout;
    public static String label_requestTimeout;
    public static String label_keepAlive;
    public static String label_organization;
    public static String label_endpoint;

    public static String chat_new;
    public static String chat_new_descr;

    public static String chat_attach;
    public static String chat_stop;
    public static String chat_resend;
    public static String chat_removeLast;
    public static String chat_clear;

    public static String chat_config;
    public static String chat_model;
    public static String chat_temperature;
    public static String chat_repPen;

    public static String chat_allowThinking;
    public static String chat_allowTools;
    public static String chat_allowWeb;

    public static String chat_refresh;



    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // nothing to do here
    }
}
