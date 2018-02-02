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
package sernet.gs.ui.rcp.main.bsi.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

import sernet.gs.model.Baustein;
import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.Retriever;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.actions.RightsEnabledAction;
import sernet.gs.ui.rcp.main.bsi.dialogs.AutoBausteinDialog;
import sernet.gs.ui.rcp.main.bsi.views.BSIKatalogInvisibleRoot;
import sernet.gs.ui.rcp.main.bsi.views.BsiModelView;
import sernet.gs.ui.rcp.main.common.model.BuildInput;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.IBSIStrukturElement;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Adds modules to a ITBP element base on the user selection in a dialog.
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class BausteinZuordnungAction extends RightsEnabledAction implements ISelectionListener {

    private static final Logger LOG = Logger.getLogger(BausteinZuordnungAction.class);

    public static final String ID = "sernet.gs.ui.rcp.main.bausteinzuordnungaction"; //$NON-NLS-1$

    private final IWorkbenchWindow window;
    
    public BausteinZuordnungAction(IWorkbenchWindow window) {
        super(ActionRightIDs.BAUSTEINZUORDNUNG, Messages.BausteinZuordnungAction_1);
        this.window = window;
        setId(ID);
        setActionDefinitionId(ID);
        setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.AUTOBAUSTEIN));
        window.getSelectionService().addSelectionListener(this);
        setToolTipText(Messages.BausteinZuordnungAction_2);
    }

    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.actions.RightsEnabledAction#doRun()
     */
    @Override
    public void doRun() {
        IStructuredSelection selection = (IStructuredSelection) window.getSelectionService().getSelection(BsiModelView.ID);
        if (selection == null) {
            return;
        }

        final List<IBSIStrukturElement> selectedElements = getSelectedElements(selection);

        final AutoBausteinDialog dialog = new AutoBausteinDialog(window.getShell());
        if (dialog.open() != Window.OK || dialog.getSelectedSubtype() == null) {
            return;
        }

        try {          
            String[] modulesNumberArray = dialog.getSelectedSubtype().getSplitBausteine();
            for (String moduleNumber : modulesNumberArray) {
                Baustein module = BSIKatalogInvisibleRoot.getInstance().getBausteinByKapitel(moduleNumber);
                if (module == null) {
                    LOG.debug("No mudule found for nr.: " + moduleNumber); //$NON-NLS-1$
                } else {
                    processModule(module, selectedElements);
                }
            }
        } catch (Exception e) {
            LOG.error("Error while adding modules.", e);
            ExceptionUtil.log(e, Messages.BausteinZuordnungAction_4);
        }
    }

    protected void processModule(Baustein module,
            final List<IBSIStrukturElement> selectedElements) {
        // assign baustein to every selected target object:
        for (IBSIStrukturElement target : selectedElements) {
            if (target instanceof CnATreeElement) {
                addModule(module, (CnATreeElement) target);
            }
        }
    }

    protected void addModule(Baustein nodule, CnATreeElement element) {
        CnATreeElement elementInitialized = element;
        if (!element.getChildren().isEmpty()) {
            RetrieveInfo ri = RetrieveInfo.getPropertyChildrenInstance();
            ri.setChildrenProperties(true);
            elementInitialized = Retriever.retrieveElement(element, ri);
        }
        if (!elementInitialized.containsBausteinUmsetzung(nodule.getId())) {
            try {
                CnAElementFactory.getInstance().saveNew(elementInitialized,
                        BausteinUmsetzung.TYPE_ID, new BuildInput<Baustein>(nodule), false);
            } catch (Exception e) {
                LOG.error("Error by saving.", e);
                throw new RuntimeException(e);
            }
        }
    }
    
    @SuppressWarnings("rawtypes")
    protected List<IBSIStrukturElement> getSelectedElements(IStructuredSelection selection) {
        final List<IBSIStrukturElement> selectedElements = new ArrayList<>(selection.size());
        for (Iterator iter = selection.iterator(); iter.hasNext();) {
            Object o = iter.next();
            if (o instanceof IBSIStrukturElement) {
                selectedElements.add((IBSIStrukturElement) o);
            }
        }
        return selectedElements;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void selectionChanged(IWorkbenchPart part, ISelection input) {
        if (isServerRunning()) {
            setEnabled(checkRights());
        }
        if (input instanceof IStructuredSelection) {
            IStructuredSelection selection = (IStructuredSelection) input;
            if (selection.size() < 1) {
                setEnabled(false);
                return;
            }
            for (Iterator iter = selection.iterator(); iter.hasNext();) {
                Object o = iter.next();
                if (!(o instanceof IBSIStrukturElement)) {
                    setEnabled(false);
                    return;
                }
            }
            if(checkRights()){
                setEnabled(true);
            }
            return;
        }
        // no structured selection:
        setEnabled(false);
    }
}
