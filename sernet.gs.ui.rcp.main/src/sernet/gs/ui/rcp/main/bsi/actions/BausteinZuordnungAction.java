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
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.actions.RightsEnabledAction;
import sernet.gs.ui.rcp.main.bsi.dialogs.AutoBausteinDialog;
import sernet.gs.ui.rcp.main.bsi.views.BSIKatalogInvisibleRoot;
import sernet.gs.ui.rcp.main.bsi.views.BsiModelView;
import sernet.gs.ui.rcp.main.common.model.BuildInput;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.IInternalServerStartListener;
import sernet.verinice.interfaces.InternalServerEvent;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.IBSIStrukturElement;
import sernet.verinice.model.common.CnATreeElement;

public class BausteinZuordnungAction extends RightsEnabledAction implements ISelectionListener {

    private static final Logger LOG = Logger.getLogger(BausteinZuordnungAction.class);

    public static final String ID = "sernet.gs.ui.rcp.main.bausteinzuordnungaction"; //$NON-NLS-1$

    private final IWorkbenchWindow window;
    
    private boolean serverIsRunning = true;

    public BausteinZuordnungAction(IWorkbenchWindow window) {
        this.window = window;
        setText(Messages.BausteinZuordnungAction_1);
        setId(ID);
        setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.AUTOBAUSTEIN));
        window.getSelectionService().addSelectionListener(this);
        setToolTipText(Messages.BausteinZuordnungAction_2);
        setRightID(ActionRightIDs.BAUSTEINZUORDNUNG);
        if(Activator.getDefault().isStandalone()  && !Activator.getDefault().getInternalServer().isRunning()){
            serverIsRunning = false;
            IInternalServerStartListener listener = new IInternalServerStartListener(){
                @Override
                public void statusChanged(InternalServerEvent e) {
                    if(e.isStarted()){
                        serverIsRunning = true;
                        setEnabled(checkRights());
                    }
                }

            };
            Activator.getDefault().getInternalServer().addInternalServerStatusListener(listener);
        } else {
            setEnabled(checkRights());
        }
    }

    @Override
    public void run() {
        IStructuredSelection selection = (IStructuredSelection) window.getSelectionService().getSelection(BsiModelView.ID);
        if (selection == null) {
            return;
        }

        final List<IBSIStrukturElement> selectedElements = new ArrayList<IBSIStrukturElement>();
        for (Iterator iter = selection.iterator(); iter.hasNext();) {
            Object o = iter.next();
            if (o instanceof IBSIStrukturElement) {
                selectedElements.add((IBSIStrukturElement) o);
            }
        }

        final AutoBausteinDialog dialog = new AutoBausteinDialog(window.getShell());
        if (dialog.open() != Window.OK || dialog.getSelectedSubtype() == null) {
            return;
        }

        try {
            String[] bausteine = dialog.getSelectedSubtype().getSplitBausteine();
            for (String bst : bausteine) {
                Baustein baustein = BSIKatalogInvisibleRoot.getInstance().getBausteinByKapitel(bst);
                if (baustein == null) {
                    LOG.debug("Kein Baustein gefunden fuer Nr.: " + bst); //$NON-NLS-1$
                } else {
                    // assign baustein to every selected target object:
                    for (IBSIStrukturElement target : selectedElements) {
                        if (target instanceof CnATreeElement) {
                            CnATreeElement targetElement = (CnATreeElement) target;
                            if (!targetElement.containsBausteinUmsetzung(baustein.getId())) {
                                try {                              
                            CnAElementFactory.getInstance().saveNew(targetElement, BausteinUmsetzung.TYPE_ID, new BuildInput<Baustein>(baustein));
                                }catch (Exception e) {
                                    LOG.error("Error by saving.", e);
                                    throw new RuntimeException(e);
                                } 
                            }
                    }
                }
            }
        }
        }catch (Exception e) {
            ExceptionUtil.log(e, Messages.BausteinZuordnungAction_4);
        }
    }

    public void selectionChanged(IWorkbenchPart part, ISelection input) {
        if (serverIsRunning) {
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
