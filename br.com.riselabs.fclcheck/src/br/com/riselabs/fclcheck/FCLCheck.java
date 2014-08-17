package br.com.riselabs.fclcheck;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class FCLCheck extends AbstractUIPlugin {
	// The plug-in ID
	public static final String PLUGIN_ID = "br.com.riselabs.fclcheck"; //$NON-NLS-1$
	public static final String PLUGIN_NAME = "FCLCheck";
	

	private static FCLCheck instance;

	public static FCLCheck getDefault() {
		return instance;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		instance = this;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		instance = null;
		super.stop(context);
	}


}
