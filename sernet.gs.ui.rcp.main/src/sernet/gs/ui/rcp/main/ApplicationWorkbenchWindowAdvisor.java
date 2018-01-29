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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PerspectiveAdapter;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.internal.ViewIntroAdapterPart;

import sernet.gs.ui.rcp.main.actions.ShowCheatSheetAction;
import sernet.gs.ui.rcp.main.bsi.views.OpenCataloguesJob;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.hui.common.VeriniceContext;
import sernet.springclient.RightsServiceClient;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.IInternalServerStartListener;
import sernet.verinice.interfaces.InternalServerEvent;
import sernet.verinice.interfaces.updatenews.IUpdateNewsService;
import sernet.verinice.iso27k.rcp.Iso27kPerspective;
import sernet.verinice.model.updateNews.UpdateNewsException;
import sernet.verinice.model.updateNews.UpdateNewsMessageEntry;
import sernet.verinice.rcp.UpdateNewsDialog;

/**
 * Workbench Window advisor.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev: 39 $ $LastChangedDate: 2007-11-27 12:26:19 +0100 (Di, 27 Nov
 *          2007) $ $LastChangedBy: koderman $
 * 
 */
public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {
    
    private static final Logger LOG = Logger.getLogger(ApplicationWorkbenchWindowAdvisor.class);
    
    private IUpdateNewsService updateNewsService;
    
    public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        super(configurer);
    }

    @Override
    public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
        return new ApplicationActionBarAdvisor(configurer);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#preWindowOpen()
     */
    @Override
    public void preWindowOpen() {
        final int pointX = 1100;
        final int pointY = 768;
        final int perspectiveBarSize = 360;
        try {
            IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
            configurer.setInitialSize(new Point(pointX, pointY));
            configurer.setShowCoolBar(true);
            configurer.setShowStatusLine(true);
            configurer.setShowProgressIndicator(true);
            IPreferenceStore apiStore = PlatformUI.getPreferenceStore();
            apiStore.setValue(IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS, false);
            configurer.setShowPerspectiveBar(true);
            configurer.setTitle(getCurrentUserName());
            // Set the preference toolbar to the left place
            // If other menus exists then this will be on the left of them
            apiStore.setValue(IWorkbenchPreferenceConstants.DOCK_PERSPECTIVE_BAR, "TOP_LEFT");
            apiStore.setValue(IWorkbenchPreferenceConstants.PERSPECTIVE_BAR_EXTRAS, getInitialPerspectiveBarList());
            apiStore.setValue(IWorkbenchPreferenceConstants.PERSPECTIVE_BAR_SIZE, perspectiveBarSize);
        } catch(Exception t) {
            LOG.error("Error while configuring window.", t);
        }
    }
    
    private String getInitialPerspectiveBarList() {
        StringBuffer sb = new StringBuffer();
        sb.append(Iso27kPerspective.ID);
        sb.append(",");
        sb.append(Perspective.ID); // IT Basline Protection perspective
        sb.append(",");
        sb.append("sernet.verinice.samt.rcp.SamtPerspective");
        
        return sb.toString();
    }
    
    private String getCurrentUserName(){
        String titleString = "verinice";
        boolean standalone =  Activator.getDefault().getPluginPreferences().getString(PreferenceConstants.OPERATION_MODE).equals(PreferenceConstants.OPERATION_MODE_INTERNAL_SERVER);
        if(!standalone){
            IAuthService service = (IAuthService) VeriniceContext.get(VeriniceContext.AUTH_SERVICE);
            titleString = titleString + ".PRO - " + service.getUsername();
        }
        return titleString;
    }

    @Override
    public void postWindowOpen() {
        if (Activator.getDefault().getPluginPreferences().getBoolean(PreferenceConstants.FIRSTSTART)) {
            Activator.getDefault().getPluginPreferences().setValue(PreferenceConstants.FIRSTSTART, false);

            MessageDialog.openInformation(Display.getCurrent().getActiveShell(), Messages.ApplicationWorkbenchWindowAdvisor_0, Messages.ApplicationWorkbenchWindowAdvisor_1);
        }

        showFirstSteps();
        preloadDBMapper();
        for(IWorkbenchPage page : PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPages()){
            initPerspective();
        }
        closeUnallowedViews();
        showUpdateNews();
    }
    
    private void preloadDBMapper() {
        WorkspaceJob job = new WorkspaceJob(Messages.ApplicationWorkbenchWindowAdvisor_2) {
            @Override
            public IStatus runInWorkspace(IProgressMonitor monitor) {
                Activator.inheritVeriniceContextState();
                CnAElementHome.getInstance().preload(CnAWorkspace.getInstance().getConfDir());
                return Status.OK_STATUS;
            }
        };
        job.schedule();
        
    }

    private void showFirstSteps() {
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().addPartListener(new IPartListener() {

            @Override
            public void partActivated(IWorkbenchPart part) {
            }

            @Override
            public void partBroughtToTop(IWorkbenchPart part) {
            }

            @Override
            public void partClosed(IWorkbenchPart part) {
                if (part instanceof ViewIntroAdapterPart &&
                        Activator.getDefault().getPluginPreferences().getBoolean(PreferenceConstants.FIRSTSTART)) {
                    Preferences prefs = Activator.getDefault().getPluginPreferences();
                    prefs.setValue(PreferenceConstants.FIRSTSTART, false);

                    ShowCheatSheetAction action = new ShowCheatSheetAction(Messages.ApplicationWorkbenchWindowAdvisor_3);
                    action.run();
                }
            }

            @Override
            public void partDeactivated(IWorkbenchPart part) {
            }

            @Override
            public void partOpened(IWorkbenchPart part) {
            }
        });

    }

    public void loadBsiCatalogues() {
        try {
            WorkspaceJob job = new OpenCataloguesJob(Messages.ApplicationWorkbenchWindowAdvisor_20);
            job.setUser(false);
            job.schedule();
        } catch (Exception e) {
            Logger.getLogger(this.getClass()).error(Messages.ApplicationWorkbenchWindowAdvisor_22, e);
        }
    }
    
    public void closeUnallowedViews(){
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().addPerspectiveListener(new PerspectiveAdapter(){
           @Override
           public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor descriptor){
               super.perspectiveActivated(page, descriptor);
               initPerspective();
           }
           @Override
           public void perspectiveOpened(IWorkbenchPage page,
                   IPerspectiveDescriptor perspective){
               super.perspectiveOpened(page, perspective);
               initPerspective();
           }
        });
    }
    
    private void initPerspective(){
        Activator.inheritVeriniceContextState();
        Vector<String> openViews = new Vector<String>();
        String rightID = "";
        IViewReference chosenRef = null;
        for(IViewReference ref : PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getViewReferences()){
            openViews.add(ref.getId());
            final IViewPart part = ref.getView(true);
            if (part!=null) {
                for (Method m : part.getClass().getDeclaredMethods()){
                    if (m.getName().equals("getRightID")){
                        try {
                            Object o = m.invoke(part, null);
                            if (o instanceof String){
                                rightID = (String)o;
                                chosenRef = ref;
                                break;
                            }
                        } catch (InvocationTargetException e) {
                            LOG.error("Error while retrieving rightID from view " + ref.getId(), e);
                        } catch (IllegalArgumentException e) {
                            LOG.error("Error while retrieving rightID from view " + ref.getId(), e);
                        } catch (IllegalAccessException e) {
                            LOG.error("Error while retrieving rightID from view " + ref.getId(), e);
                        }
                    }
                }
            }
            final String rID = rightID;
            final IViewReference rRef = chosenRef;
            if (Activator.getDefault().isStandalone() && !Activator.getDefault().getInternalServer().isRunning()){
                IInternalServerStartListener listener = new IInternalServerStartListener(){
                    @Override
                    public void statusChanged(InternalServerEvent e) {
                        if (e.isStarted()){
                            hideView(part, rID, rRef);
                        }
                    }
                };
                Activator.getDefault().getInternalServer().addInternalServerStatusListener(listener);
            }  else {
                hideView(part, rID, rRef);
            }
        }
    }

    private void hideView(final IViewPart part, final String actionId, final IViewReference viewReference) {
        Activator.inheritVeriniceContextState();
        if (!((RightsServiceClient)VeriniceContext.get(VeriniceContext.RIGHTS_SERVICE)).isEnabled(actionId)){
            IWorkbench workbench = PlatformUI.getWorkbench();
            IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
            if(window != null){
                IWorkbenchPage page = window.getActivePage();
                //            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                if (page.isPartVisible(part)) {
                    page.hideView(viewReference);
                }
            }
        }
    }
    
    private void showUpdateNews(){
        boolean showNewsDialog = !Activator.getDefault().getPreferenceStore()
                .getBoolean(PreferenceConstants.SHOW_UPDATE_NEWS_DIALOG);
        if(showNewsDialog){
            handleOpenDialogByServerStatus();
        }
    }

    private void handleOpenDialogByServerStatus() {
        if (Activator.getDefault().isStandalone() ) {
            if(!Activator.getDefault().getInternalServer().isRunning()){
                IInternalServerStartListener listener = new IInternalServerStartListener() {
                    @Override
                    public void statusChanged(InternalServerEvent e) {
                        if (e.isStarted()){
                            openNewsDialog();
                        }
                    }
                };
                Activator.getDefault().getInternalServer().addInternalServerStatusListener(listener);
            } else if(Activator.getDefault().getInternalServer().isRunning()){
                    openNewsDialog();
            }
        }
    }
    
    private void openNewsDialog(){
        try{
            Activator.inheritVeriniceContextState();
            String newsRepo = getNewsRepository();
            UpdateNewsMessageEntry newsEntry = getUpdateNewsService().getNewsFromRepository(newsRepo);
            if (newsEntry != null){ // equals null in servermode
                openNewsDialog(newsEntry);
            }
        } catch (UpdateNewsException e){
            LOG.error("Problem occurred during loading the verinice-update-news", e);
        } catch (Exception t){
            LOG.error("Problem occurred", t);
        }
    }

    private void openNewsDialog(UpdateNewsMessageEntry newsEntry) throws UpdateNewsException {
        final String text = newsEntry.getMessage(Locale.getDefault());
        String installedVersion = getApplicationVersionFromAboutText();
        LOG.debug("installed Version:\t" + installedVersion);
        boolean updateNecessary = getUpdateNewsService().isUpdateNecessary(installedVersion);
        LOG.debug("update necessary:\t" + updateNecessary);
        if (StringUtils.isNotEmpty(installedVersion) && getUpdateNewsService().isUpdateNecessary(installedVersion)){
            openNewsDialog(text);
        }
    }

    private void openNewsDialog(final String text) throws UpdateNewsException {
        final URL updateSiteURL;
        try{
            updateSiteURL = new URL(getUpdateNewsService().getNewsFromRepository(getNewsRepository()).getUpdateSite());
        } catch (MalformedURLException e){
            LOG.error("Updatesite not parseable", e);
            throw new UpdateNewsException("Malformed URL of updatesite", e);
        }
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                Shell dialogShell = new Shell(Display.getCurrent().getActiveShell());
                UpdateNewsDialog newsDialog = new UpdateNewsDialog(dialogShell,
                        text, updateSiteURL);
                newsDialog.open();
            }
        });
    }
    
    /**
     * this reads a hardcoded preferencevalue (from the preferencestore
     * or from the preferenceInitializer if it is not existant already)
     * if its not replaced by the user manually.
     * 
     * To replace / change it manually add the following line to the
     * end of the file verinice.ini:
     * 
     * -Dstandalone_updatenews_url=http://url.of/your/choice.txt
     * 
     */
    private String getNewsRepository(){
        String repo = Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.STANDALONE_UPDATENEWS_URL); 
        String repoSetBySystem = System.getProperty(PreferenceConstants.STANDALONE_UPDATENEWS_URL);
        if(StringUtils.isNotEmpty(repoSetBySystem)){
            repo = repoSetBySystem;
        }
        return repo;
    }
    
    /**
     * @param parent
     * @return
     */
    private String getApplicationVersionFromAboutText() {
        final IProduct product = Platform.getProduct();
        final String aboutText = product.getProperty("aboutText");
        String version = "";
        if (aboutText!=null) {
            String lines[] = aboutText.split("\\r?\\n");
            if (lines!=null && lines.length>0) {
                final String firstLine = lines[0];
                final Pattern p = Pattern.compile(
                        IUpdateNewsService.VERINICE_VERSION_PATTERN);
                final Matcher matcher = p.matcher(firstLine);
                if (matcher.find()){
                    version = matcher.group();
                }
            }
        }
        LOG.debug("Read versionnumber " + version + " from prodcut-description");
        return version;
    }
    
    private IUpdateNewsService getUpdateNewsService() {
        if(updateNewsService==null) {
            updateNewsService = (IUpdateNewsService) VeriniceContext.get(VeriniceContext.UPDATE_NEWS_SERVICE);
        }
        return updateNewsService;
    }

}
