/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

import sernet.gs.model.Baustein;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.dialogs.AutoBausteinDialog;
import sernet.gs.ui.rcp.main.bsi.dialogs.KonsolidatorDialog;
import sernet.gs.ui.rcp.main.bsi.model.BSIMassnahmenModel;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.IBSIStrukturElement;
import sernet.gs.ui.rcp.main.bsi.views.BSIKatalogInvisibleRoot;
import sernet.gs.ui.rcp.main.bsi.views.BsiModelView;
import sernet.gs.ui.rcp.main.common.model.BuildInput;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.hui.common.connect.EntityType;

public class BausteinZuordnungAction extends Action implements
		ISelectionListener {

	public static final String ID = "sernet.gs.ui.rcp.main.bausteinzuordnungaction";

	private final IWorkbenchWindow window;

	public BausteinZuordnungAction(IWorkbenchWindow window) {
		this.window = window;
		setText("Bausteine automatisch zuordnen...");
		setId(ID);
		setActionDefinitionId(ID);
		setImageDescriptor(ImageCache.getInstance().getImageDescriptor(
				ImageCache.AUTOBAUSTEIN));
		window.getSelectionService().addSelectionListener(this);
		setToolTipText("Ordnet den markierten Zielobjekten eine Vorauswahl typischer Bausteine zu.");
	}

	public void run() {
		IStructuredSelection selection = (IStructuredSelection) window
				.getSelectionService().getSelection(BsiModelView.ID);
		if (selection == null)
			return;

		final List<IBSIStrukturElement> selectedElements = new ArrayList<IBSIStrukturElement>();
		for (Iterator iter = selection.iterator(); iter.hasNext();) {
			Object o = iter.next();
			if (o instanceof IBSIStrukturElement)
				selectedElements.add((IBSIStrukturElement) o);
		}

		final AutoBausteinDialog dialog = new AutoBausteinDialog(window
				.getShell());
		if (dialog.open() != InputDialog.OK
				|| dialog.getSelectedSubtype() == null)
			return;

		try {
			String[] bausteine = dialog.getSelectedSubtype()
					.getSplitBausteine();
			for (String bst : bausteine) {
				Baustein baustein = BSIKatalogInvisibleRoot.getInstance()
						.getBausteinByKapitel(bst);
				if (baustein == null) {
					Logger.getLogger(this.getClass()).debug("Kein Baustein gefunden fuer Nr " + bst);
				}
				else {
					// assign baustein to every selected target object:
					for (IBSIStrukturElement target : selectedElements) {
						if (target instanceof CnATreeElement) {
							CnATreeElement targetElement = (CnATreeElement) target;
							CnAElementFactory.getInstance().saveNew(targetElement,
									BausteinUmsetzung.TYPE_ID,
									new BuildInput<Baustein>(baustein));
						}
					}
				}
			}
		} catch (Exception e) {
			ExceptionUtil.log(e, "Fehler beim Zuordnen von Baustein.");
		}
	}

	private void dispose() {
		window.getSelectionService().removeSelectionListener(this);
	}

	public void selectionChanged(IWorkbenchPart part, ISelection input) {

		if (input instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) input;

			if (selection.size() < 1) {
				setEnabled(false);
				return;
			}

			String kapitel = null;
			for (Iterator iter = selection.iterator(); iter.hasNext();) {
				Object o = iter.next();
				if (!(o instanceof IBSIStrukturElement)) {
					setEnabled(false);
					return;
				}
			}
			setEnabled(true);
			return;
		}
		// no structured selection:
		setEnabled(false);

	}

}
