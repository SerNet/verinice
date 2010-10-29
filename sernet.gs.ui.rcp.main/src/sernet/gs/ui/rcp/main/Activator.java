/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.internal.p2.console.ProvisioningHelper;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;

import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.ProgressAdapter;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.migrationcommands.DbVersion;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.IInternalServer;
import sernet.verinice.interfaces.IMain;
import sernet.verinice.interfaces.IVersionConstants;
import sernet.verinice.interfaces.oda.IVeriniceOdaDriver;
import sernet.verinice.iso27k.rcp.JobScheduler;
import sernet.verinice.rcp.StatusResult;


/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin implements IMain {

	private static final Logger LOG = Logger.getLogger(Activator.class);
	
	// The plug-in ID
	public static final String PLUGIN_ID = "sernet.gs.ui.rcp.main"; //$NON-NLS-1$

	private static final String PAX_WEB_SYMBOLIC_NAME = "org.ops4j.pax.web.pax-web-bundle"; //$NON-NLS-1$
	
	private static final String REPORT_SERVICE_SYMBOLIC_NAME = "sernet.verinice.report.service"; //$NON-NLS-1$

    private static final String LOCAL_UPDATE_SITE_URL = "/Verinice-Update-Site-2010"; //$NON-NLS-1$
    
    public static final String UPDATE_SITE_URL = "http://update.verinice.org/pub/verinice/Verinice-Update-Site-2010"; //$NON-NLS-1$
    
    public static final String DERBY_LOG_FILE_PROPERTY = "derby.stream.error.file";
    
    public static final String DERBY_LOG_FILE = "verinice" + File.separatorChar + "verinice-derby.log";
    
	// The shared instance
	private static Activator plugin;

	private static VeriniceContext.State state;

	private IInternalServer internalServer;
	
	private IVeriniceOdaDriver odaDriver;
	
	private BundleContext context;
	
	private boolean runsAsApplication = false;
	
	/**
	 * The constructor
	 */
	public Activator() {
		plugin = this;
	}

	public static IWorkbenchPage getActivePage() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null)
			return null;

		IWorkbenchPage page = window.getActivePage();
		if (page == null)
			return null;

		return page;
	}

	/**
	 * Brings the bundle (not the whole RCP application) in a usable state.
	 * 
	 */
	@SuppressWarnings("restriction")
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		
		this.context = context;
		
		if (LOG.isInfoEnabled()) {
		    final Bundle bundle = context.getBundle();
            LOG.info("Starting bundle " + bundle.getSymbolicName() + " " + bundle.getVersion());
        }
		
		// Makes a representation of this bundle as a service available.
		context.registerService(IMain.class.getName(), this, null);
	}
	
	/**
	 * Starts everything that is needed for the whole application.
	 * 
	 * <p>Note: This method can only be called once.</p>
	 * 
	 * <p>Note: This method is solely to be called from {@link Application}.</p>
	 * 
	 * <p>Note: This method is *NOT* to be called when verinice is being used as a library
	 * (IOW when being run during the report design phase).</p>
	 * 
	 * @throws BundleException
	 */
	@SuppressWarnings("restriction")
	void startApplication() throws BundleException
	{
		runsAsApplication = true;
		
		Bundle bundle = Platform.getBundle(REPORT_SERVICE_SYMBOLIC_NAME);
		if (bundle == null)
		{
			LOG.warn("Report service bundle is not available!");
		}
		else
		{
			bundle.start();
		}
		
		// set workdir preference:
		CnAWorkspace.getInstance().prepareWorkDir();

		Preferences prefs = getPluginPreferences();

		// set service factory location to local / remote according to
		// preferences:
		boolean standalone = prefs.getString(PreferenceConstants.OPERATION_MODE).equals(PreferenceConstants.OPERATION_MODE_INTERNAL_SERVER);

		// Start server only when it is needed.
		if (standalone) {
			bundle = Platform.getBundle("sernet.gs.server"); //$NON-NLS-1$
			if (bundle == null)
				LOG.warn("verinice server bundle is not available. Assuming it is started separately."); //$NON-NLS-1$
			else if (bundle.getState() == Bundle.INSTALLED || bundle.getState() == Bundle.RESOLVED) {
				LOG.debug("Manually starting GS Server"); //$NON-NLS-1$
				bundle.start();
			}

			ServiceReference sr = context.getServiceReference(IInternalServer.class.getName());
			if (sr == null)
				throw new IllegalStateException("Cannot retrieve internal server service."); //$NON-NLS-1$

			internalServer = (IInternalServer) context.getService(sr);
			if (LOG.isInfoEnabled()) {
				LOG.info("Preference " + PreferenceConstants.OPERATION_MODE + "=" + PreferenceConstants.OPERATION_MODE_INTERNAL_SERVER + ": Using internal server."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		} else {
			internalServer = new ServerDummy();
			if (LOG.isDebugEnabled()) {
				LOG.debug("Internal server is not used."); //$NON-NLS-1$
			}
			// Pax Web Http Service (embedded jetty) is starting automatically after loading and starting
			// the bundle PAX_WEB_SYMBOLIC_NAME which you can not prevent
			// When internal server is not used bundle and Http Service is stopped here
			try {
				Bundle paxWebBundle = Platform.getBundle(PAX_WEB_SYMBOLIC_NAME);
				if(paxWebBundle!=null) {
					paxWebBundle.stop();
				}
			} catch (Exception e) {
				LOG.error("Error while stopping pax-web http-service.", e); //$NON-NLS-1$
			}
		}

		if (prefs.getString(PreferenceConstants.GSACCESS).equals(PreferenceConstants.GSACCESS_DIR)) {
			try {
				internalServer.setGSCatalogURL(new File(prefs.getString(PreferenceConstants.BSIDIR)).toURI().toURL());
			} catch (MalformedURLException mfue) {
				LOG.warn("Stored GS catalog dir is an invalid URL."); //$NON-NLS-1$
			}
		} else {
			try {
				internalServer.setGSCatalogURL(new File(prefs.getString(PreferenceConstants.BSIZIPFILE)).toURI().toURL());
			} catch (MalformedURLException mfue) {
				LOG.warn("Stored GS catalog zip file path is an invalid URL."); //$NON-NLS-1$
			}

		}
		try {
			internalServer.setDSCatalogURL(new File(prefs.getString(PreferenceConstants.DSZIPFILE)).toURI().toURL());
		} catch (MalformedURLException mfue) {
			LOG.warn("Stored DS catalog zip file path is an invalid URL."); //$NON-NLS-1$
		}

		// Set the derby log file path
		System.setProperty(DERBY_LOG_FILE_PROPERTY, System.getProperty("user.home") + File.separatorChar + DERBY_LOG_FILE);
		
		// Provide initial DB connection details to server.
		internalServer.configure(prefs.getString(PreferenceConstants.DB_URL), prefs.getString(PreferenceConstants.DB_USER), prefs.getString(PreferenceConstants.DB_PASS), prefs.getString(PreferenceConstants.DB_DRIVER), prefs.getString(PreferenceConstants.DB_DIALECT));

		
		// prepare client's workspace:
		CnAWorkspace.getInstance().prepare();

		try {
			ServiceFactory.openCommandService();
		} catch (Exception e) {
			// if this fails, try rewriting config:
			LOG.error("Exception while connection to command service, forcing recreation of " + "service factory configuration from preferences.", e); //$NON-NLS-1$ //$NON-NLS-2$
			CnAWorkspace.getInstance().prepare(true);
		}

		// When the service factory is initialized the client's work objects can
		// be accessed.
		// The line below initializes the VeriniceContext initially.
		VeriniceContext.setState(state = ServiceFactory.getClientWorkObjects());

		// Make command service available as an OSGi service
		context.registerService(
				ICommandService.class.getName(),
				VeriniceContext.get(VeriniceContext.COMMAND_SERVICE),
				null);
		
		Job repositoryJob = new Job("add-repository") {
		    /* (non-Javadoc)
		     * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		     */
		    protected IStatus run(IProgressMonitor monitor) {
		        try {
                    addUpdateRepository();
                } catch (URISyntaxException e) {
                    LOG.error("Error while adding update repository.");
                }
		        return Status.OK_STATUS;

		    };
		};
		repositoryJob.schedule();	
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
		CnAElementHome.getInstance().close();
		plugin = null;
		super.stop(context);
	}
	
	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public IInternalServer getInternalServer() {
		return internalServer;
	}
	
	public IVeriniceOdaDriver getOdaDriver() {
		return odaDriver;
	}
	
	public static void initDatabase() {
		initDatabase(JobScheduler.getInitMutex(), new StatusResult());
	}

	public static void initDatabase(ISchedulingRule mutex, final StatusResult result) {
		WorkspaceJob initDbJob = new WorkspaceJob(Messages.Activator_InitDatabase) {
			@Override
			public IStatus runInWorkspace(final IProgressMonitor monitor) {
				IStatus status = Status.OK_STATUS;
				try {
					monitor.beginTask(Messages.Activator_InitDatabase, IProgressMonitor.UNKNOWN);
					// If server could not be started for whatever reason do not
					// try to
					// load the model either.
					if (result.status == Status.CANCEL_STATUS) {
						status = Status.CANCEL_STATUS;
					} else {
						CnAWorkspace.getInstance().createDatabaseConfig();
						Activator.inheritVeriniceContextState();
						Activator.checkDbVersion();
					}
				} catch (Exception e) {
					LOG.error("Error while initializing database.", e); //$NON-NLS-1$
					status = new Status(IStatus.ERROR, "sernet.gs.ui.rcp.main", Messages.Activator_29, e); //$NON-NLS-1$
				} finally {
					monitor.done();
				}
				return status;
			}
		};
		JobScheduler.scheduleJob(initDbJob, mutex, JobScheduler.getInitProgressMonitor());
	}

	public static void createModel() {
		createModel(JobScheduler.getInitMutex(), new StatusResult());
	}

	public static void createModel(ISchedulingRule mutex, final StatusResult serverStartResult) {
		WorkspaceJob job = new WorkspaceJob(Messages.Activator_LoadModel) {
			@Override
			public IStatus runInWorkspace(final IProgressMonitor monitor) {
				IStatus status = Status.OK_STATUS;
				try {
					// If server could not be started for whatever reason do not
					// try to
					// load the model either.
					if (serverStartResult.status == Status.CANCEL_STATUS) {
						status = Status.CANCEL_STATUS;
					}
					Activator.inheritVeriniceContextState();
					monitor.beginTask(Messages.Activator_LoadModel, IProgressMonitor.UNKNOWN);
					monitor.setTaskName(Messages.Activator_LoadModel);
					CnAElementFactory.getInstance().loadOrCreateModel(new ProgressAdapter(monitor));
					CnAElementFactory.getInstance().getISO27kModel();
				} catch (Exception e) {
					LOG.error("Error while loading model.", e); //$NON-NLS-1$
					status = new Status(IStatus.ERROR, "sernet.gs.ui.rcp.main", Messages.Activator_31, e); //$NON-NLS-1$
				} finally {
					monitor.done();
				}
				return status;
			}
		};
		JobScheduler.scheduleJob(job, mutex, JobScheduler.getInitProgressMonitor());
	}

	public static void checkDbVersion() throws CommandException {
		final boolean[] done = new boolean[1];
		done[0] = false;
		Thread timeout = new Thread() {
			@Override
			public void run() {
				long startTime = System.currentTimeMillis();
				while (!done[0]) {
					try {
						sleep(1000);
						long now = System.currentTimeMillis();
						if (now - startTime > 30000) {
							ExceptionUtil.log(new Exception(sernet.gs.ui.rcp.main.Messages.Activator_8), sernet.gs.ui.rcp.main.Messages.Activator_10);
							return;
						}
					} catch (InterruptedException e) {
					}
				}
			}
		};
		timeout.start();
		try {
			DbVersion command = new DbVersion(IVersionConstants.COMPATIBLE_CLIENT_VERSION);
			command = ServiceFactory.lookupCommandService().executeCommand(command);
			done[0] = true;
		} catch (CommandException e) {
			done[0] = true;
			throw e;
		} catch (RuntimeException re) {
			done[0] = true;
			throw re;
		}
	}

	public static void showDerbyWarning(Shell shell) {
		if (getDefault().getPluginPreferences().getBoolean(PreferenceConstants.FIRSTSTART)) {
			Preferences prefs = getDefault().getPluginPreferences();
			prefs.setValue(PreferenceConstants.FIRSTSTART, false);

			if (getDefault().getPluginPreferences().getString(PreferenceConstants.DB_DRIVER).equals(PreferenceConstants.DB_DRIVER_DERBY)) {

				// Do not show dialog if remote server is configured instead of
				// internal server.
				if (prefs.getString(PreferenceConstants.OPERATION_MODE).equals(PreferenceConstants.OPERATION_MODE_REMOTE_SERVER))
					return;

				MessageDialog.openInformation(new Shell(shell), Messages.Activator_26, Messages.Activator_27);

			}
		}
	}
	
	@SuppressWarnings("restriction")
    private void addUpdateRepository() throws URISyntaxException {  
	    Preferences prefs = Activator.getDefault().getPluginPreferences();
	    URI repoUri = null;
	    if (prefs.getString(PreferenceConstants.OPERATION_MODE).equals(PreferenceConstants.OPERATION_MODE_REMOTE_SERVER)) {
            repoUri = new URI(createUpdateSiteUrl(prefs.getString(PreferenceConstants.VNSERVER_URI)));
	    } else {
	        repoUri = new URI(UPDATE_SITE_URL); 
	    }
        removeRepository();    
        try {
            ProvisioningHelper.addMetadataRepository(repoUri);
            if (LOG.isDebugEnabled()) {
                LOG.debug("MetadataRepository added: " + repoUri);
            }
        } catch( Exception e ) {
            LOG.warn("Can not add update repository: " + repoUri);
            if (LOG.isDebugEnabled()) {
                LOG.debug("stacktrace: ", e);
            }
        }
        try {
            ProvisioningHelper.addArtifactRepository(repoUri);
            if (LOG.isDebugEnabled()) {
                LOG.debug("ArtifactRepository added: " + repoUri);
            }
        } catch( Exception e ) {
            LOG.warn("Can not add update repository: " + repoUri);
            if (LOG.isDebugEnabled()) {
                LOG.debug("stacktrace: ", e);
            }
        }  
    }
	


    private void removeRepository() {
        URI[] uriArray = ProvisioningHelper.getArtifactRepositories();
        if(uriArray!=null) {
    	    for (int i = 0; i < uriArray.length; i++) {
                URI uri = uriArray[i];
                if(uri.toString().equals(UPDATE_SITE_URL) || uri.toString().endsWith(LOCAL_UPDATE_SITE_URL)) {
                    ProvisioningHelper.removeArtifactRepository(uri);
                }
            }
        }
        uriArray = ProvisioningHelper.getMetadataRepositories();
    	if(uriArray!=null) {
            for (int i = 0; i < uriArray.length; i++) {
                URI uri = uriArray[i];
                if(uri.toString().equals(UPDATE_SITE_URL) || uri.toString().endsWith(LOCAL_UPDATE_SITE_URL)) {
                    ProvisioningHelper.removeMetadataRepository(uri);
                }
            }
        }
            
    }
	
	/**
     * @param string
     * @return
     */
    private String createUpdateSiteUrl(String serverUrl) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(serverUrl);
        stringBuilder.append(LOCAL_UPDATE_SITE_URL);
        return stringBuilder.toString();
    }

	public static StatusResult startServer() {
		return startServer(JobScheduler.getInitMutex(), new StatusResult());
	}

	/**
	 * Tries to start the internal server via a workspace thread and returns a
	 * result object for that operation.
	 * 
	 * @param mutex
	 * 
	 * @return
	 */
	public static StatusResult startServer(ISchedulingRule mutex, final StatusResult result) {
		final IInternalServer internalServer = getDefault().getInternalServer();
		if (!internalServer.isRunning()) {
			WorkspaceJob job = new WorkspaceJob("") { //$NON-NLS-1$
				@Override
				public IStatus runInWorkspace(final IProgressMonitor monitor) {
					inheritVeriniceContextState();
					try {
						if (!internalServer.isRunning()) {
							monitor.beginTask(Messages.Activator_1, IProgressMonitor.UNKNOWN);
							internalServer.start();
						}
						result.status = Status.OK_STATUS;
					} catch (Exception e) {
						ExceptionUtil.log(e, Messages.Activator_2);
						result.status = new Status(IStatus.ERROR, "sernet.gs.ui.rcp.main", Messages.Activator_3, e); //$NON-NLS-1$
					} finally {
						monitor.done();
					}
					return result.status;
				}
			};
			JobScheduler.scheduleJob(job, JobScheduler.getInitMutex(), JobScheduler.getInitProgressMonitor());
		} else
			result.status = Status.OK_STATUS;

		return result;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	/**
	 * Initializes the current thread with the VeriniceContext.State of the
	 * client application.
	 * 
	 * <p>
	 * Calling this method is needed when the Activator was run on a different
	 * thread then the Application class.
	 * </p>
	 */
	public static void inheritVeriniceContextState() {
		VeriniceContext.setState(state);
	}

	/**
	 * Implementation of {@link IInternalServer} which does nothing.
	 * 
	 * <p>
	 * An instance of this class exists when the client does not use the
	 * internal server. There are however codepaths where methods from an
	 * <code>IInternalServer</code> instance are invoked unconditionally.
	 * </p>
	 * 
	 */
	private static class ServerDummy implements IInternalServer {

		public void configure(String url, String user, String pass, String driver, String dialect) {
			// Intentionally do nothing.
		}

		public void start() {
			// Intentionally do nothing.
		}

		public void stop() {
			// Intentionally do nothing.
		}

		public boolean isRunning() {
			return true;
		}

		public void setGSCatalogURL(URL _) {
			// Intentionally do nothing.
		}

		public void setDSCatalogURL(URL _) {
			// Intentionally do nothing.
		}

	}

	/**
	 * Allows setting the server URI from another bundle (actually it is the
	 * ODA driver which does that).
	 * 
	 * <p>That method is needed during the report design phase to forward the server
	 * settings from the designer into the main bundle.</p>
	 * 
	 * <p>Due to the design of ODA drivers this method is also called when the driver
	 * is used through verinice itself (during report generation). However in that
	 * case the method call has no effect.</p>
	 */
	@Override
	public void updateServerURI(String uri) {
		if (!runsAsApplication)
		{
			LOG.info("verinice runs in designer mode - retrieving server configuration from ODA driver.");
			ClientPropertyPlaceholderConfigurer.setRemoteServerMode(uri);
			try {
				ServiceFactory.openCommandService();
			} catch (MalformedURLException e) {
				throw new IllegalStateException(e);
			}
			VeriniceContext.setState(state = ServiceFactory.getClientWorkObjects());
			
			// Make command service available as an OSGi service
			context.registerService(
					ICommandService.class.getName(),
					VeriniceContext.get(VeriniceContext.COMMAND_SERVICE),
					null);
		}
	}
	
}
