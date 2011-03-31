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
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.actions.OpenPerspectiveAction;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.internal.ChangeToPerspectiveMenu;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.cheatsheets.actions.CheatSheetCategoryBasedSelectionAction;
import org.eclipse.ui.internal.registry.ActionSetRegistry;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;

import sernet.gs.ui.rcp.main.actions.ChangeOwnPasswordAction;
import sernet.gs.ui.rcp.main.actions.ImportCSVAction;
import sernet.gs.ui.rcp.main.actions.ImportGstoolAction;
import sernet.gs.ui.rcp.main.actions.ImportGstoolNotesAction;
import sernet.gs.ui.rcp.main.actions.OpenMultipleViewAction;
import sernet.gs.ui.rcp.main.actions.OpenViewAction;
import sernet.gs.ui.rcp.main.actions.ReloadAction;
import sernet.gs.ui.rcp.main.actions.RunRiskAnalysisAction;
import sernet.gs.ui.rcp.main.actions.ShowAccessControlEditAction;
import sernet.gs.ui.rcp.main.actions.ShowBulkEditAction;
import sernet.gs.ui.rcp.main.actions.ShowKonsolidatorAction;
import sernet.gs.ui.rcp.main.actions.TestAction;
import sernet.gs.ui.rcp.main.bsi.actions.BausteinZuordnungAction;
import sernet.gs.ui.rcp.main.bsi.views.AuditView;
import sernet.gs.ui.rcp.main.bsi.views.BSIMassnahmenView;
import sernet.gs.ui.rcp.main.bsi.views.BrowserView;
import sernet.gs.ui.rcp.main.bsi.views.BsiModelView;
import sernet.gs.ui.rcp.main.bsi.views.DSModelView;
import sernet.gs.ui.rcp.main.bsi.views.DocumentView;
import sernet.gs.ui.rcp.main.bsi.views.FileView;
import sernet.gs.ui.rcp.main.bsi.views.NoteView;
import sernet.gs.ui.rcp.main.bsi.views.RelationView;
import sernet.gs.ui.rcp.main.bsi.views.TodoView;
import sernet.gs.ui.rcp.main.bsi.views.chart.ChartView;
import sernet.gs.ui.rcp.main.preferences.ShowPreferencesAction;
import sernet.verinice.bpm.rcp.OpenTaskViewAction;
import sernet.verinice.bpm.rcp.TaskView;
import sernet.verinice.iso27k.rcp.CatalogView;
import sernet.verinice.iso27k.rcp.ISMView;
import sernet.verinice.iso27k.rcp.Iso27kPerspective;
import sernet.verinice.iso27k.rcp.action.ImportPersonFromLdap;

/**
 * An action bar advisor is responsible for creating, adding, and disposing of
 * the actions added to a workbench window. Each window will be populated with
 * new actions.
 * 
 * @author koderman[at]sernet[dot]de
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

    // Actions - important to allocate these only in makeActions, and then use
    // them
    // in the fill methods. This ensures that the actions aren't recreated
    // when fillActionBars is called with FILL_PROXY.
    private IWorkbenchAction exitAction;

    private IWorkbenchAction aboutAction;

    private IWorkbenchAction newWindowAction;

    private OpenViewAction openBSIViewAction;

    private IWorkbenchAction saveAction;

    private IWorkbenchAction closeAction;

    private IWorkbenchAction closeAllAction;

    private IWorkbenchAction closeOthersAction;

    private OpenViewAction openDSViewAction;

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

    private OpenMultipleViewAction openCatalogAction;
    
    private OpenTaskViewAction openTaskViewAction;

    private OpenPerspectiveAction openIsoPerspective;

    private IWorkbenchAction copyAction;

    private IWorkbenchAction pasteAction;

    private ShowBulkEditAction bulkEditAction;

    private ShowAccessControlEditAction accessControlEditAction;

    private ChangeOwnPasswordAction changeOwnPasswordAction;
    
    private IWorkbenchAction introAction;

    private ShowKonsolidatorAction konsolidatorAction;

    private CheatSheetCategoryBasedSelectionAction showCheatSheetListAction;

    private ImportGstoolAction importGstoolAction;

	private ImportCSVAction importCSVAction;

	private ImportPersonFromLdap importPersonFromLdap;

    private OpenViewAction openDocumentViewAction;

    private BausteinZuordnungAction bausteinZuordnungAction;

	private ImportGstoolNotesAction importGSNotesAction;

    private RunRiskAnalysisAction runRiskAnalysisAction;

    private TestAction testAction;

    public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
        super(configurer);
        removeExtraneousActions();
    }

    protected void makeActions(final IWorkbenchWindow window) {
        // Creates the actions and registers them.
        // Registering is needed to ensure that key bindings work.
        // The corresponding commands keybindings are defined in the plugin.xml
        // file.
        // Registering also provides automatic disposal of the actions when
        // the window is closed.

        exitAction = ActionFactory.QUIT.create(window);
        register(exitAction);

        copyAction = ActionFactory.COPY.create(window);
        register(copyAction);
        
        

        pasteAction = ActionFactory.PASTE.create(window);
        register(pasteAction);

        aboutAction = ActionFactory.ABOUT.create(window);
        register(aboutAction);

        newWindowAction = ActionFactory.OPEN_NEW_WINDOW.create(window);
        register(newWindowAction);

        saveAction = ActionFactory.SAVE.create(window);
        register(saveAction);

        closeAction = ActionFactory.CLOSE.create(window);
        register(closeAction);

        closeAllAction = ActionFactory.CLOSE_ALL.create(window);
        register(closeAllAction);

        closeOthersAction = ActionFactory.CLOSE_OTHERS.create(window);
        register(closeOthersAction);

        openBSIBrowserAction = new OpenViewAction(window, Messages.ApplicationActionBarAdvisor_0, BrowserView.ID, ImageCache.VIEW_BROWSER);
        register(openBSIBrowserAction);

        openNoteAction = new OpenViewAction(window, Messages.ApplicationActionBarAdvisor_1, NoteView.ID, ImageCache.VIEW_NOTE);
        register(openNoteAction);

        openFileAction = new OpenViewAction(window, Messages.ApplicationActionBarAdvisor_2, FileView.ID, ImageCache.ATTACH);
        register(openFileAction);

        openCatalogAction = new OpenMultipleViewAction(window, Messages.ApplicationActionBarAdvisor_3, CatalogView.ID, ImageCache.WRENCH);
        register(openCatalogAction);

        openRelationViewAction = new OpenViewAction(window, Messages.ApplicationActionBarAdvisor_4, RelationView.ID, ImageCache.LINKS);
        register(openRelationViewAction);

        openBSIViewAction = new OpenViewAction(window, Messages.ApplicationActionBarAdvisor_5, BSIMassnahmenView.ID, ImageCache.VIEW_MASSNAHMEN);
        register(openBSIViewAction);

        openBSIModelViewAction = new OpenViewAction(window, Messages.ApplicationActionBarAdvisor_6, BsiModelView.ID, ImageCache.VIEW_BSIMODEL);
        register(openBSIModelViewAction);

        openISMViewAction = new OpenViewAction(window, Messages.ApplicationActionBarAdvisor_7, ISMView.ID, ImageCache.VIEW_ISMVIEW);
        register(openISMViewAction);

        openDSViewAction = new OpenViewAction(window, Messages.ApplicationActionBarAdvisor_8, DSModelView.ID, ImageCache.VIEW_DSMODEL);
        register(openDSViewAction);

        openTodoViewAction = new OpenViewAction(window, Messages.ApplicationActionBarAdvisor_9, TodoView.ID, ImageCache.VIEW_TODO);
        register(openTodoViewAction);

        openDocumentViewAction = new OpenViewAction(window, Messages.ApplicationActionBarAdvisor_10, DocumentView.ID, ImageCache.VIEW_DOCUMENT);
        register(openDocumentViewAction);

        openAuditViewAction = new OpenViewAction(window, Messages.ApplicationActionBarAdvisor_12, AuditView.ID, ImageCache.VIEW_AUDIT);
        register(openAuditViewAction);
        
        openTaskViewAction = new OpenTaskViewAction(window);
        register(openTaskViewAction);

        reloadAction = new ReloadAction(window, Messages.ApplicationActionBarAdvisor_14);
        register(reloadAction);

        importGstoolAction = new ImportGstoolAction(window, Messages.ApplicationActionBarAdvisor_15);
        register(importGstoolAction);
        
        importCSVAction = new ImportCSVAction(window, Messages.ApplicationActionBarAdvisor_30);
        register(importCSVAction);
        
        importPersonFromLdap = new ImportPersonFromLdap(window,Messages.ApplicationActionBarAdvisor_32);
        register(importPersonFromLdap);

		importGSNotesAction = new ImportGstoolNotesAction(window, Messages.ApplicationActionBarAdvisor_27);
		register(importGSNotesAction);

        showPreferencesAction = new ShowPreferencesAction();
        // showPreferencesAction = ActionFactory.PREFERENCES.create(window);
        register(showPreferencesAction);

        bulkEditAction = new ShowBulkEditAction(window, Messages.ApplicationActionBarAdvisor_16);
        register(bulkEditAction);
        
        runRiskAnalysisAction = new RunRiskAnalysisAction(window);
        register(runRiskAnalysisAction);

        accessControlEditAction = new ShowAccessControlEditAction(window, Messages.ApplicationActionBarAdvisor_17);
        register(accessControlEditAction);

        konsolidatorAction = new ShowKonsolidatorAction(window, Messages.ApplicationActionBarAdvisor_18);
        register(konsolidatorAction);

        bausteinZuordnungAction = new BausteinZuordnungAction(window);
        register(bausteinZuordnungAction);
        
        changeOwnPasswordAction = new ChangeOwnPasswordAction(window, Messages.ApplicationActionBarAdvisor_31);
        

        showCheatSheetListAction = new CheatSheetCategoryBasedSelectionAction(Messages.ApplicationActionBarAdvisor_20);
        
        testAction = new TestAction(window, "test command", "asset", 152);
        
        register(openDocumentViewAction);

        introAction = ActionFactory.INTRO.create(window);
        register(introAction);

    }

    @Override
    protected void fillMenuBar(IMenuManager menuBar) {
        IActionBarConfigurer configurer = getActionBarConfigurer();
        IWorkbenchWindow window = configurer.getWindowConfigurer().getWindow();

        menuBar.add(createFileMenu(window));
        menuBar.add(createEditMenu(window));
        menuBar.add(createWindowMenu(window));
        menuBar.add(createHelpMenu());
    }

    private IContributionItem createHelpMenu() {
        MenuManager helpMenu = new MenuManager(Messages.ApplicationActionBarAdvisor_21,IWorkbenchActionConstants.M_HELP);
        helpMenu.add(introAction);
        helpMenu.add(showCheatSheetListAction);
        helpMenu.add(aboutAction);
        helpMenu.add(new Separator());
        helpMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        return helpMenu;
    }

    private IContributionItem createEditMenu(IWorkbenchWindow window) {
        MenuManager editMenu = new MenuManager(Messages.ApplicationActionBarAdvisor_22, IWorkbenchActionConstants.M_EDIT);

        editMenu.add(bulkEditAction);
        editMenu.add(runRiskAnalysisAction);
        editMenu.add(accessControlEditAction);
        editMenu.add(konsolidatorAction);
        editMenu.add(new Separator());
        editMenu.add(copyAction);
        editMenu.add(pasteAction);
        editMenu.add(new Separator());
        editMenu.add(changeOwnPasswordAction);
        editMenu.add(showPreferencesAction);
        return editMenu;
    }

    private IContributionItem createFileMenu(IWorkbenchWindow window) {
        MenuManager fileMenu = new MenuManager(Messages.ApplicationActionBarAdvisor_23, IWorkbenchActionConstants.M_FILE);

        // MenuManager neuMenu = new MenuManager("Neu...");
        // IContributionItem wizardList =
        // ContributionItemFactory.NEW_WIZARD_SHORTLIST.
        // create(window);
        // neuMenu.add(wizardList);

        // fileMenu.add(neuMenu);
        fileMenu.add(saveAction);
        fileMenu.add(closeAction);
        fileMenu.add(closeAllAction);
        fileMenu.add(closeOthersAction);

        fileMenu.add(new Separator(VeriniceActionConstants.MENU_FILE));
        fileMenu.add(new Separator());
        fileMenu.add(importGstoolAction);
        fileMenu.add(importGSNotesAction);
        fileMenu.add(new Separator());
        fileMenu.add(importCSVAction);
        fileMenu.add(importPersonFromLdap);

        fileMenu.add(new Separator());
        fileMenu.add(exitAction);
        fileMenu.add(testAction);
        return fileMenu;
    }

    private IContributionItem createWindowMenu(IWorkbenchWindow window) {
        // View:
        MenuManager windowMenu = new MenuManager(Messages.ApplicationActionBarAdvisor_24, IWorkbenchActionConstants.M_WINDOW);

        MenuManager viewsMenu = new MenuManager(Messages.ApplicationActionBarAdvisor_25, VeriniceActionConstants.MENU_VIEWS);
        // IContributionItem viewList = ContributionItemFactory.VIEWS_SHORTLIST
        // .create(window);

        viewsMenu.add(openBSIViewAction);
        viewsMenu.add(openBSIModelViewAction);
        viewsMenu.add(openTodoViewAction);
        viewsMenu.add(openAuditViewAction);
        viewsMenu.add(openDSViewAction);
        viewsMenu.add(openDocumentViewAction);
        viewsMenu.add(new Separator());
        
        viewsMenu.add(openISMViewAction);
        viewsMenu.add(openCatalogAction);
        viewsMenu.add(openTaskViewAction);
        viewsMenu.add(new Separator());

        viewsMenu.add(openBSIBrowserAction);
        viewsMenu.add(openNoteAction);
        viewsMenu.add(openFileAction);
        viewsMenu.add(openRelationViewAction);
        
        viewsMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        
        // viewsMenu.add(viewList);
        MenuManager perspectivesMenu = new MenuManager(Messages.ApplicationActionBarAdvisor_26, VeriniceActionConstants.MENU_PERSPECTIVES);
        addPerspectiveMenu(window, perspectivesMenu, Iso27kPerspective.ID);
        addPerspectiveMenu(window, perspectivesMenu, Perspective.ID);
        perspectivesMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        
        IContributionItem perspectiveList = ContributionItemFactory.PERSPECTIVES_SHORTLIST.create(window);
        perspectivesMenu.add(perspectiveList);

        windowMenu.add(newWindowAction);
        windowMenu.add(reloadAction);
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
    private void addPerspectiveMenu(IWorkbenchWindow window, MenuManager perspectivesMenu, String perspectiveId) {
        perspectivesMenu.add(new OpenPerspectiveAction(window, window.getWorkbench().getPerspectiveRegistry().findPerspectiveWithId(perspectiveId), new ChangeToPerspectiveMenu(window, perspectiveId)));
    }

    @Override
    protected void fillCoolBar(ICoolBarManager coolBar) {
        IToolBarManager myToolbar = new ToolBarManager(coolBar.getStyle());
        coolBar.add(new ToolBarContributionItem(myToolbar,VeriniceActionConstants.TOOLBAR));
        myToolbar.add(saveAction);
        myToolbar.add(new Separator(VeriniceActionConstants.TOOLBAR_REPORT));
        myToolbar.add(new Separator());
        myToolbar.add(bulkEditAction);
        myToolbar.add(accessControlEditAction);
        myToolbar.add(konsolidatorAction);

        myToolbar.add(reloadAction);
        myToolbar.add(runRiskAnalysisAction);

        myToolbar.add(new Separator());
        // Grundschutz items
        myToolbar.add(openBSIViewAction);
        myToolbar.add(openBSIModelViewAction);
        myToolbar.add(openTodoViewAction);
        myToolbar.add(openAuditViewAction);
        myToolbar.add(openDSViewAction);
        myToolbar.add(openDocumentViewAction);

        myToolbar.add(new Separator());
        // ISO 27k items
        myToolbar.add(openISMViewAction);
        myToolbar.add(openCatalogAction);
        myToolbar.add(openTaskViewAction);

        myToolbar.add(new Separator());
        // common items
        myToolbar.add(openBSIBrowserAction);
        myToolbar.add(openNoteAction);
        myToolbar.add(openFileAction);
        myToolbar.add(openRelationViewAction);
    }

    /**
     * This removes some actions that we inherit from org.eclipse.ui.ide which
     * we don't want.
     * 
     */
    private void removeExtraneousActions() {
        ActionSetRegistry reg = WorkbenchPlugin.getDefault().getActionSetRegistry();

        // removing gotoLastPosition message
        removeStandardAction(reg, "org.eclipse.ui.edit.text.actionSet.navigation"); //$NON-NLS-1$

        removeStandardAction(reg, "org.eclipse.ui.NavigateActionSet"); //$NON-NLS-1$

        // "Open File..." in 3.2.1:
        removeStandardAction(reg, "org.eclipse.ui.edit.text.actionSet.openExternalFile"); //$NON-NLS-1$

        // "Open File" in 3.4M4:
        removeStandardAction(reg, "org.eclipse.ui.actionSet.openFiles"); //$NON-NLS-1$

        // removeStandardAction(reg,
        // "org.eclipse.ui.preferencePages.Workbench");

        removeStandardAction(reg, "org.eclipse.ui.edit.text.actionSet.annotationNavigation"); //$NON-NLS-1$

        // Removing Convert Line Delimiters Tool menu
        removeStandardAction(reg, "org.eclipse.ui.edit.text.actionSet.convertLineDelimitersTo"); //$NON-NLS-1$

        // remove working sets
        removeStandardAction(reg, "org.eclipse.ui.WorkingSetActionSet"); //$NON-NLS-1$
    }

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

}
