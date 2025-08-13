package com.github.kiu345.eclipse.eclipseai;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.github.kiu345.eclipse.eclipseai.config.PluginConfiguration;


public class Activator extends AbstractUIPlugin {
	private static Activator plugin = null;

	private static final String DEBUG_PROP = "qdevzone.eclipseai.debug";
	private static Boolean DO_DEBUG = null;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		IEclipseContext ctxEclipse = EclipseContextFactory.getServiceContext(context);
		var config = ContextInjectionFactory.make(PluginConfiguration.class, ctxEclipse);
		ctxEclipse.set(PluginConfiguration.class, config);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * This method returns the bundle context of the plugin.
	 * <p>
	 * The bundle context is used to access various services and resources provided
	 * by the OSGi framework.
	 * </p>
	 */
	public static BundleContext getBundleContext() {
		return getDefault().getBundle().getBundleContext();
	}

	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * start eclipse with `-Ddevzone.eclipseai.debug=true` for extended debugging
	 * output
	 */
	public static boolean isDebugEnabled() {
		if (DO_DEBUG == null) {
			String val = System.getProperty(DEBUG_PROP, "false");
			DO_DEBUG = Boolean.parseBoolean(val);
		}
		return DO_DEBUG;
	}
}
