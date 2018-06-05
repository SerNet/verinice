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

import org.eclipse.core.runtime.IExtension;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.StatusLineContributionItem;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.actions.OpenPerspectiveAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.internal.ChangeToPerspectiveMenu;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.cheatsheets.actions.CheatSheetCategoryBasedSelectionAction;
import org.eclipse.ui.internal.registry.ActionSetRegistry;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;

import sernet.gs.ui.rcp.gsimport.GstoolImportMappingView;
import sernet.gs.ui.rcp.main.actions.ChangeOwnPasswordAction;
import sernet.gs.ui.rcp.main.actions.GSMBasicSecurityCheckAction;
import sernet.gs.ui.rcp.main.actions.ImportCSVAction;
import sernet.gs.ui.rcp.main.actions.ImportGstoolAction;
import sernet.gs.ui.rcp.main.actions.ImportGstoolNotesAction;
import sernet.gs.ui.rcp.main.actions.OpenMultipleViewAction;
import sernet.gs.ui.rcp.main.actions.OpenSearchViewAction;
import sernet.gs.ui.rcp.main.actions.OpenViewAction;
import sernet.gs.ui.rcp.main.actions.ReloadAction;
import sernet.gs.ui.rcp.main.actions.ShowAccessControlEditAction;
import sernet.gs.ui.rcp.main.actions.ShowBulkEditAction;
import sernet.gs.ui.rcp.main.actions.ShowKonsolidatorAction;
import sernet.gs.ui.rcp.main.actions.TestAction;
import sernet.gs.ui.rcp.main.bsi.actions.BausteinZuordnungAction;
import sernet.gs.ui.rcp.main.bsi.actions.GSMBausteinZuordnungAction;
import sernet.gs.ui.rcp.main.bsi.views.AuditView;
import sernet.gs.ui.rcp.main.bsi.views.BSIMassnahmenView;
import sernet.gs.ui.rcp.main.bsi.views.BrowserView;
import sernet.gs.ui.rcp.main.bsi.views.BsiModelView;
import sernet.gs.ui.rcp.main.bsi.views.DocumentView;
import sernet.gs.ui.rcp.main.bsi.views.FileView;
import sernet.gs.ui.rcp.main.bsi.views.NoteView;
import sernet.gs.ui.rcp.main.bsi.views.RelationView;
import sernet.gs.ui.rcp.main.bsi.views.TodoView;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.preferences.ShowPreferencesAction;
import sernet.verinice.bp.rcp.BaseProtectionView;
import sernet.verinice.bpm.rcp.OpenTaskViewAction;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.iso27k.rcp.CatalogView;
import sernet.verinice.iso27k.rcp.ISMView;
import sernet.verinice.iso27k.rcp.Iso27kPerspective;
import sernet.verinice.iso27k.rcp.action.ImportPersonFromLdap;
import sernet.verinice.rcp.ProfileEditAction;
import sernet.verinice.rcp.ServerConnectionToggleAction;
import sernet.verinice.rcp.account.AccountView;
import sernet.verinice.rcp.accountgroup.AccountGroupView;
import sernet.verinice.rcp.bp.BaseProtectionPerspective;
import sernet.verinice.rcp.risk.RiskAnalysisAction;
import sernet.verinice.report.rcp.ReportDepositView;
import sernet.verinice.validation.CnAValidationView;

/**
 * An action bar advisor is responsible for creating, adding, and disposing of
 * the actions added to a workbench window. Each window will be populated with
 * new actions.
 *
 * @author koderman[at]sernet[dot]de
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

    private static final String WARNING_RESTRICTION = "restriction"; //$NON-NLS-1$

    // Actions - important to allocate these only in makeActions, and then use
    // them
    // in the fill methods. This ensures that the actions aren't recreated
    // when fillActionBars is called with FILL_PROXY.
    private IWorkbenchAction exitAction;

    private IWorkbenchAction aboutAction;

    private IWorkbenchAction newWindowAction;

    private OpenViewAction openBSIViewAction;

    private IWorkbenchAction saveAction;

    private IWorkbenchAction saveAsAction;

    private IWorkbenchAction closeAction;

    private IWorkbenchAction closeAllAction;

    private IWorkbenchAction closeOthersAction;

    private OpenViewAction openBSIModelViewAction;

    private OpenViewAction openISMViewAction;

    private OpenViewAction openTodoViewAction;

    private OpenViewAction openAuditViewAction;

    private ReloadAction reloadAction;

    private ShowPreferencesAction showPreferencesAction;

    private OpenViewAction openBSIBrowserAction;

    private OpenViewAction openNoteAction;

    private OpenViewAction openFileAction;

    private OpenViewAction openRelationViewAction;

    private OpenViewAction openValidationViewAction;

    private OpenViewAction openGroupViewAction;

    private OpenMultipleViewAction openSearchViewAction;

    private OpenMultipleViewAction openCatalogAction;

    private OpenTaskViewAction openTaskViewAction;

    private OpenViewAction openAccountViewAction;

    private OpenViewAction openReportdepositViewAction;

    private IWorkbenchAction copyAction;

    private IWorkbenchAction pasteAction;

    private ShowBulkEditAction bulkEditAction;

    private ShowAccessControlEditAction accessControlEditAction;

    private ProfileEditAction profileEditAction;

    private ChangeOwnPasswordAction changeOwnPasswordAction;

    private IWorkbenchAction introAction;

    private ShowKonsolidatorAction konsolidatorAction;

    private CheatSheetCategoryBasedSelectionAction showCheatSheetListAction;

    private ImportGstoolAction importGstoolAction;

    private ImportCSVAction importCSVAction;

    private ImportPersonFromLdap importPersonFromLdap;

    private OpenViewAction openDocumentViewAction;

    private ImportGstoolNotesAction importGSNotesAction;

    private RiskAnalysisAction runRiskAnalysisAction;

    private ServerConnectionToggleAction serverConnectionToggleAction;

    private OpenViewAction openGSToolMappingViewAction;
    
    private OpenViewAction openBpViewAction;

    private TestAction testAction;

    private OpenViewAction openCatalogViewAction;

    public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
        super(configurer);
        removeExtraneousActions();
        removeObsoletePerspectives();
    }


    @SuppressWarnings(WARNING_RESTRICTION)
    @Override
    protected void makeActions(final IWorkbenchWindow window) {
        window.addPerspectiveListener(new IPerspectiveListener() {
            
            @Override
            public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String arg2) {
            }
            
            @Override
            public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
                runRiskAnalysisAction.setEnabled(!Perspective.ID.equals(perspective.getId()));
            }
        });
         
        BausteinZuordnungAction bausteinZuordnungAction;
        GSMBausteinZuordnungAction gsmbausteinZuordnungAction;
        GSMBasicSecurityCheckAction gsmbasicsecuritycheckAction;

        // Creates the actions and registers them.
        // Registering is needed to ensure that key bindings work.
        // The corresponding commands keybindings are defined in the plugin.xml
        // file.
        // Registering also provides automatic disposal of the actions when
        // the window is closed.

        this.exitAction = ActionFactory.QUIT.create(window);
        this.copyAction = ActionFactory.COPY.create(window);
        this.pasteAction = ActionFactory.PASTE.create(window);
        this.aboutAction = ActionFactory.ABOUT.create(window);
        this.newWindowAction = ActionFactory.OPEN_NEW_WINDOW.create(window);
        this.saveAction = ActionFactory.SAVE.create(window);
        this.saveAsAction = ActionFactory.SAVE_AS.create(window);
        this.closeAction = ActionFactory.CLOSE.create(window);
        this.closeAllAction = ActionFactory.CLOSE_ALL.create(window);
        this.closeOthersAction = ActionFactory.CLOSE_OTHERS.create(window);
        this.openGroupViewAction = new OpenViewAction(window,Messages.ApplicationActionBarAdvisor_36, AccountGroupView.ID, ImageCache.GROUP_VIEW, ActionRightIDs.ACCOUNTSETTINGS); //$NON-NLS-1$
        this.openBSIBrowserAction = new OpenViewAction(window, Messages.ApplicationActionBarAdvisor_0, BrowserView.ID, ImageCache.VIEW_BROWSER, ActionRightIDs.BSIBROWSER);
        this.openNoteAction = new OpenViewAction(window, Messages.ApplicationActionBarAdvisor_1, NoteView.ID, ImageCache.VIEW_NOTE, ActionRightIDs.NOTES);
        this.openFileAction = new OpenViewAction(window, Messages.ApplicationActionBarAdvisor_2, FileView.ID, ImageCache.ATTACH, ActionRightIDs.FILES);
        this.openCatalogAction = new OpenMultipleViewAction(window, Messages.ApplicationActionBarAdvisor_3, CatalogView.ID, ImageCache.WRENCH, ActionRightIDs.ISMCATALOG);
        this.openRelationViewAction = new OpenViewAction(window, Messages.ApplicationActionBarAdvisor_4, RelationView.ID, ImageCache.LINKS, ActionRightIDs.RELATIONS);
        this.openBSIViewAction = new OpenViewAction(window, Messages.ApplicationActionBarAdvisor_5, BSIMassnahmenView.ID, ImageCache.VIEW_MASSNAHMEN, ActionRightIDs.BSIMASSNAHMEN);
        this.openBSIModelViewAction = new OpenViewAction(window, Messages.ApplicationActionBarAdvisor_6, BsiModelView.ID, ImageCache.VIEW_BSIMODEL, ActionRightIDs.BSIMODELVIEW);
        this.openISMViewAction = new OpenViewAction(window, Messages.ApplicationActionBarAdvisor_7, ISMView.ID, ImageCache.VIEW_ISMVIEW, ActionRightIDs.ISMVIEW);
        this.openTodoViewAction = new OpenViewAction(window, Messages.ApplicationActionBarAdvisor_9, TodoView.ID, ImageCache.VIEW_TODO, ActionRightIDs.TODO);
        this.openDocumentViewAction = new OpenViewAction(window, Messages.ApplicationActionBarAdvisor_10, DocumentView.ID, ImageCache.VIEW_DOCUMENT, ActionRightIDs.DOCUMENTVIEW);
        this.openAuditViewAction = new OpenViewAction(window, Messages.ApplicationActionBarAdvisor_12, AuditView.ID, ImageCache.VIEW_AUDIT, ActionRightIDs.AUDITVIEW);
        this.openTaskViewAction = new OpenTaskViewAction(window, ActionRightIDs.TASKVIEW);
        this.openValidationViewAction = new OpenViewAction(window, Messages.ApplicationActionBarAdvisor_35, CnAValidationView.ID, ImageCache.VIEW_VALIDATION, ActionRightIDs.CNAVALIDATION);
        this.openAccountViewAction = new OpenViewAction(window, Messages.ApplicationActionBarAdvisor_38, AccountView.ID, ImageCache.PERSON, ActionRightIDs.ACCOUNTSETTINGS);
        this.openReportdepositViewAction = new OpenViewAction(window, Messages.ApplicationActionBarAdvisor_41, ReportDepositView.ID, ImageCache.REPORT_DEPOSIT, ActionRightIDs.REPORTDEPOSIT);
        this.openSearchViewAction = new OpenSearchViewAction(window, Messages.ApplicationActionBarAdvisor_42);
        this.openGSToolMappingViewAction = new OpenViewAction(window, Messages.ApplicationActionBarAdvisor_43, GstoolImportMappingView.ID, ImageCache.VIEW_GSMAPPING, ActionRightIDs.GSTOOLIMPORT);
        this.openBpViewAction = new OpenViewAction(window, Messages.ApplicationActionBarAdvisor_45, BaseProtectionView.ID, ImageCache.VIEW_BASE_PROTECTION, ActionRightIDs.BASEPROTECTIONVIEW);
        this.reloadAction = new ReloadAction(window, Messages.ApplicationActionBarAdvisor_14);
        this.importGstoolAction = new ImportGstoolAction(window, Messages.ApplicationActionBarAdvisor_15);
        this.importCSVAction = new ImportCSVAction(window, Messages.ApplicationActionBarAdvisor_30);
        this.importPersonFromLdap = new ImportPersonFromLdap(window,Messages.ApplicationActionBarAdvisor_32);
        this.importGSNotesAction = new ImportGstoolNotesAction(window, Messages.ApplicationActionBarAdvisor_27);
        this.showPreferencesAction = new ShowPreferencesAction();
        this.bulkEditAction = new ShowBulkEditAction(window, Messages.ApplicationActionBarAdvisor_16);
        this.runRiskAnalysisAction = new RiskAnalysisAction(window);
        this.accessControlEditAction = new ShowAccessControlEditAction(window, Messages.ApplicationActionBarAdvisor_17);
        this.profileEditAction = new ProfileEditAction(window, Messages.ApplicationActionBarAdvisor_33);
        this.konsolidatorAction = new ShowKonsolidatorAction(window, Messages.ApplicationActionBarAdvisor_18);
        gsmbasicsecuritycheckAction = new GSMBasicSecurityCheckAction(window, Messages.ApplicationActionBarAdvisor_34);
        bausteinZuordnungAction = new BausteinZuordnungAction(window);
        gsmbausteinZuordnungAction = new GSMBausteinZuordnungAction(window);
        this.changeOwnPasswordAction = new ChangeOwnPasswordAction(window, Messages.ApplicationActionBarAdvisor_31);

        this.showCheatSheetListAction = new CheatSheetCategoryBasedSelectionAction(Messages.ApplicationActionBarAdvisor_20);

        this.serverConnectionToggleAction = new ServerConnectionToggleAction();

        this.testAction = new TestAction(window, "Import BSI-Compendium", "asset", 152); //$NON-NLS-1$ //$NON-NLS-2$
        this.introAction = ActionFactory.INTRO.create(window);
        
        this.openCatalogViewAction = new OpenViewAction(window, Messages.ApplicationActionBarAdvisor_CatalogView, 
                sernet.verinice.rcp.catalog.CatalogView.ID, ImageCache.VIEW_CATALOG, ActionRightIDs.CATALOGVIEW);

        IAction actions[] = new IAction[]{this.exitAction, this.copyAction, this.pasteAction,
                this.aboutAction, this.newWindowAction, this.saveAction, this.saveAsAction,
                this.closeAction, this.closeAllAction,
                this.closeOthersAction, this.openBSIBrowserAction, this.openNoteAction, this.openFileAction,
                this.openCatalogAction, this.openRelationViewAction, this.openBSIViewAction,
                this.openBSIModelViewAction, this.openISMViewAction,
                this.openTodoViewAction, this.openAuditViewAction, this.openTaskViewAction,
                this.openValidationViewAction, this.reloadAction, this.importGstoolAction,
                this.importCSVAction, this.importPersonFromLdap, this.importGSNotesAction,
                this.showPreferencesAction, this.bulkEditAction, this.runRiskAnalysisAction,
                this.accessControlEditAction, this.profileEditAction, this.konsolidatorAction,
                gsmbasicsecuritycheckAction,bausteinZuordnungAction,
                gsmbausteinZuordnungAction, this.openDocumentViewAction,
                this.introAction, this.openGroupViewAction, this.openReportdepositViewAction,
                this.openSearchViewAction, this.openGSToolMappingViewAction, this.openBpViewAction,
                this.openCatalogViewAction,
                this.testAction
         };
        registerActions(actions);

    }

    private void registerActions(IAction[] actions){
        for(IAction action : actions){
            register(action);
        }
    }

    @Override
    protected void fillMenuBar(IMenuManager menuBar) {
        IActionBarConfigurer configurer = getActionBarConfigurer();
        IWorkbenchWindow window = configurer.getWindowConfigurer().getWindow();

        menuBar.add(createFileMenu());
        menuBar.add(createEditMenu());
        menuBar.add(createWindowMenu(window));
        menuBar.add(createHelpMenu());
    }

    @Override
    protected void fillStatusLine(IStatusLineManager statusLine) {
        final int statusItemCharWidth = 100;
        if(isServerMode()) {
            StatusLineContributionItem statusItem = new StatusLineContributionItem("server-url",statusItemCharWidth); //$NON-NLS-1$
            statusItem.setText(Messages.ApplicationActionBarAdvisor_40 + getShortServerUrl());
            statusLine.add(statusItem);
        }
    }

    private String getShortServerUrl() {
        final int httpURLLength = 7;
        final int httpsURLLength = 8;
        String url = getServerUrlPreference();
        if(url!=null && !url.isEmpty()) {
            if(url.startsWith("http://")) { //$NON-NLS-1$
                url = url.substring(httpURLLength);
            }
            if(url.startsWith("https://")) { //$NON-NLS-1$
                url = url.substring(httpsURLLength);
            }
        }
        return url;
    }

    private IContributionItem createHelpMenu() {
        MenuManager helpMenu = new MenuManager(Messages.ApplicationActionBarAdvisor_21,IWorkbenchActionConstants.M_HELP);
        helpMenu.add(this.introAction);
        helpMenu.add(this.showCheatSheetListAction);
        helpMenu.add(this.aboutAction);
        helpMenu.add(new Separator());
        helpMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        return helpMenu;
    }

    private IContributionItem createEditMenu() {
        MenuManager editMenu = new MenuManager(Messages.ApplicationActionBarAdvisor_22, IWorkbenchActionConstants.M_EDIT);

        editMenu.add(this.bulkEditAction);
        editMenu.add(this.runRiskAnalysisAction);
        editMenu.add(this.accessControlEditAction);
        editMenu.add(this.profileEditAction);
        editMenu.add(this.konsolidatorAction);
        editMenu.add(new Separator());
        editMenu.add(this.copyAction);
        editMenu.add(this.pasteAction);
        editMenu.add(new Separator());
        editMenu.add(this.changeOwnPasswordAction);
        editMenu.add(this.showPreferencesAction);
        return editMenu;
    }

    private IContributionItem createFileMenu() {
        MenuManager fileMenu = new MenuManager(Messages.ApplicationActionBarAdvisor_23, IWorkbenchActionConstants.M_FILE);

        fileMenu.add(this.saveAction);
        fileMenu.add(this.saveAsAction);
        fileMenu.add(this.closeAction);
        fileMenu.add(this.closeAllAction);
        fileMenu.add(this.closeOthersAction);

        fileMenu.add(new Separator(VeriniceActionConstants.MENU_FILE));
        fileMenu.add(new Separator());
        fileMenu.add(this.importGstoolAction);
        fileMenu.add(this.importGSNotesAction);
        fileMenu.add(new Separator());
        fileMenu.add(this.importCSVAction);
        fileMenu.add(this.importPersonFromLdap);

        fileMenu.add(new Separator());
        fileMenu.add(this.serverConnectionToggleAction);
        fileMenu.add(this.exitAction);
        return fileMenu;
    }

    private IContributionItem createWindowMenu(IWorkbenchWindow window) {
        // View:
        MenuManager windowMenu = new MenuManager(Messages.ApplicationActionBarAdvisor_24, IWorkbenchActionConstants.M_WINDOW);

        MenuManager viewsMenu = new MenuManager(Messages.ApplicationActionBarAdvisor_25, VeriniceActionConstants.MENU_VIEWS);
        
        // old IT-Baseline protection

        viewsMenu.add(this.openBSIModelViewAction);
        viewsMenu.add(this.openBSIViewAction);
        viewsMenu.add(this.openTodoViewAction);
        viewsMenu.add(this.openAuditViewAction);
        viewsMenu.add(this.openGSToolMappingViewAction);
        viewsMenu.add(new Separator());

        // modernized IT-Baseline protection
        
        viewsMenu.add(this.openBpViewAction);
        viewsMenu.add(new Separator());

        // ISM
        
        viewsMenu.add(this.openISMViewAction);
        viewsMenu.add(this.openCatalogAction);
        viewsMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        // marker for including views from samt-plugin
        
        // VDA - done by samt-plugin
        viewsMenu.add(new Separator());
        
        // global
        viewsMenu.add(this.openCatalogViewAction);
        viewsMenu.add(this.openDocumentViewAction);
        viewsMenu.add(this.openBSIBrowserAction);
        viewsMenu.add(this.openNoteAction);
        viewsMenu.add(this.openFileAction);
        viewsMenu.add(this.openRelationViewAction);
        viewsMenu.add(this.openValidationViewAction);
        viewsMenu.add(this.openSearchViewAction);
        viewsMenu.add(new Separator());
        
        viewsMenu.add(this.openAccountViewAction);
        viewsMenu.add(this.openGroupViewAction);
        viewsMenu.add(this.openTaskViewAction);
        viewsMenu.add(this.openReportdepositViewAction);
        viewsMenu.add(new Separator());

        viewsMenu.add(new Separator());


        
        MenuManager perspectivesMenu = new MenuManager(Messages.ApplicationActionBarAdvisor_26, VeriniceActionConstants.MENU_PERSPECTIVES);
        addPerspectiveMenu(window, perspectivesMenu, Iso27kPerspective.ID);
        addPerspectiveMenu(window, perspectivesMenu, Perspective.ID);
        addPerspectiveMenu(window, perspectivesMenu, BaseProtectionPerspective.ID);
        perspectivesMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        IContributionItem perspectiveList = ContributionItemFactory.PERSPECTIVES_SHORTLIST.create(window);
        perspectivesMenu.add(perspectiveList);

        windowMenu.add(this.newWindowAction);
        windowMenu.add(this.reloadAction);
        windowMenu.add(new Separator());
        windowMenu.add(perspectivesMenu);
        windowMenu.add(viewsMenu);
        return windowMenu;
    }

    /**
     * @param window
     * @param perspectivesMenu
     * @param id
     */
    @SuppressWarnings(WARNING_RESTRICTION)
    private void addPerspectiveMenu(IWorkbenchWindow window, MenuManager perspectivesMenu, String perspectiveId) {
        perspectivesMenu.add(new OpenPerspectiveAction(window, window.getWorkbench().getPerspectiveRegistry().findPerspectiveWithId(perspectiveId), new ChangeToPerspectiveMenu(window, perspectiveId)));
    }

    @Override
    protected void fillCoolBar(ICoolBarManager coolBar) {
        IToolBarManager myToolbar = new ToolBarManager(coolBar.getStyle());
        coolBar.add(new ToolBarContributionItem(myToolbar,VeriniceActionConstants.TOOLBAR));
        myToolbar.add(this.saveAction);
        myToolbar.add(this.saveAsAction);
        myToolbar.add(new Separator(VeriniceActionConstants.TOOLBAR_REPORT));
        myToolbar.add(new Separator());
        myToolbar.add(this.bulkEditAction);
        myToolbar.add(this.accessControlEditAction);
        myToolbar.add(this.profileEditAction);
        myToolbar.add(this.konsolidatorAction);

        myToolbar.add(this.reloadAction);
        myToolbar.add(this.runRiskAnalysisAction);
        myToolbar.add(new Separator());
        // Grundschutz items
        myToolbar.add(this.openBSIViewAction);
        myToolbar.add(this.openBSIModelViewAction);
        myToolbar.add(this.openTodoViewAction);
        myToolbar.add(this.openAuditViewAction);
        myToolbar.add(this.openDocumentViewAction);

        myToolbar.add(new Separator());
        // ISO 27k items
        myToolbar.add(this.openISMViewAction);
        myToolbar.add(this.openCatalogAction);
        myToolbar.add(this.openTaskViewAction);

        myToolbar.add(new Separator());
        // common items
        myToolbar.add(this.openAccountViewAction);
        myToolbar.add(this.openGroupViewAction);
        myToolbar.add(this.openReportdepositViewAction);
        myToolbar.add(this.openBSIBrowserAction);
        myToolbar.add(this.openNoteAction);
        myToolbar.add(this.openFileAction);
        myToolbar.add(this.openRelationViewAction);
        myToolbar.add(this.openValidationViewAction);
        myToolbar.add(this.openSearchViewAction);
    }

    /**
     * This removes some actions that we inherit from org.eclipse.ui.ide which
     * we don't want.
     *
     */
    @SuppressWarnings(WARNING_RESTRICTION)
    private void removeExtraneousActions() {
        ActionSetRegistry reg = WorkbenchPlugin.getDefault().getActionSetRegistry();

        // removing gotoLastPosition message
        removeStandardAction(reg, "org.eclipse.ui.edit.text.actionSet.navigation"); //$NON-NLS-1$

        removeStandardAction(reg, "org.eclipse.ui.NavigateActionSet"); //$NON-NLS-1$

        // "Open File..." in 3.2.1:
        removeStandardAction(reg, "org.eclipse.ui.edit.text.actionSet.openExternalFile"); //$NON-NLS-1$

        // "Open File" in 3.4M4:
        removeStandardAction(reg, "org.eclipse.ui.actionSet.openFiles"); //$NON-NLS-1$

        removeStandardAction(reg, "org.eclipse.ui.edit.text.actionSet.annotationNavigation"); //$NON-NLS-1$

        // Removing Convert Line Delimiters Tool menu
        removeStandardAction(reg, "org.eclipse.ui.edit.text.actionSet.convertLineDelimitersTo"); //$NON-NLS-1$

        // remove working sets
        removeStandardAction(reg, "org.eclipse.ui.WorkingSetActionSet"); //$NON-NLS-1$
    }

    private void removeObsoletePerspectives() {
        for (IPerspectiveDescriptor perspective : PlatformUI.getWorkbench().getPerspectiveRegistry()
                .getPerspectives()) {
            String perspectiveId = perspective.getId();
            if (perspectiveId.startsWith("<") && perspectiveId.endsWith(">")) {
                PlatformUI.getWorkbench().getPerspectiveRegistry().deletePerspective(perspective);
            }
        }
    }
    
    @SuppressWarnings(WARNING_RESTRICTION)
    private void removeStandardAction(ActionSetRegistry reg, String actionSetId) {
        IActionSetDescriptor[] actionSets = reg.getActionSets();
        for (int i = 0; i < actionSets.length; i++) {
            if (!actionSets[i].getId().equals(actionSetId)) {
                continue;
            }
            IExtension ext = actionSets[i].getConfigurationElement().getDeclaringExtension();
            reg.removeExtension(ext, new Object[] { actionSets[i] });
        }
    }

    public static boolean isServerMode() {
        return PreferenceConstants.OPERATION_MODE_REMOTE_SERVER.equals(getPreferenceStore().getString(PreferenceConstants.OPERATION_MODE));
    }

    private String getServerUrlPreference() {
        return getPreferenceStore().getString(PreferenceConstants.VNSERVER_URI);
    }

    private static IPreferenceStore getPreferenceStore() {
        return Activator.getDefault().getPreferenceStore();
    }

}
