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
package sernet.gs.ui.rcp.main.bsi.dnd;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Display;

import sernet.gs.model.Baustein;
import sernet.gs.ui.rcp.main.bsi.dialogs.KonsolidatorDialog;
import sernet.gs.ui.rcp.main.bsi.dialogs.SanityCheckDialog;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.IBSIStrukturElement;
import sernet.gs.ui.rcp.main.bsi.model.IBSIStrukturKategorie;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.bsi.model.Konsolidator;
import sernet.gs.ui.rcp.main.bsi.model.LinkKategorie;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.BuildInput;
import sernet.snutils.ExceptionHandlerFactory;

public class BSIModelViewDropListener extends ViewerDropAdapter {

	public BSIModelViewDropListener(TreeViewer viewer) {
		super(viewer);
	}

	@Override
	public boolean performDrop(Object data) {
		Object toDrop = DNDItems.getItems().get(0);
		if (toDrop != null && toDrop instanceof Baustein) {
			return dropBaustein();
		} else if (toDrop != null && toDrop instanceof IBSIStrukturElement) {
			CnATreeElement target;
			if (getCurrentTarget() instanceof LinkKategorie)
				target = ((LinkKategorie) getCurrentTarget()).getParent();
			else
				target = (CnATreeElement) getCurrentTarget();
			LinkDropper dropper = new LinkDropper();
			return dropper.dropLink(DNDItems.getItems(), target);
		}
		return false;

	}

	private boolean dropBaustein() {
		final CnATreeElement target = (CnATreeElement) getCurrentTarget();
		final List<Baustein> toDrop = DNDItems.getItems();
		Check: for (Baustein baustein : toDrop) {
			int targetSchicht = 0;
			if (target instanceof IBSIStrukturElement)
				targetSchicht = ((IBSIStrukturElement) target).getSchicht();

			if (baustein.getSchicht() != targetSchicht) {
				if (!SanityCheckDialog.checkLayer(super.getViewer()
						.getControl().getShell(), baustein.getSchicht(),
						targetSchicht))
					return false;
				else
					break Check; // user say he knows what he's doing, stop
				// checking.
			}

		}

		try {
			Job dropJob = new Job(Messages
					.getString("BSIModelViewDropListener.3")) { //$NON-NLS-1$
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						createBausteinUmsetzung(toDrop, target);
					} catch (Exception e) {
						Logger.getLogger(this.getClass()).error(
								"Drop failed", e); //$NON-NLS-1$
						return Status.CANCEL_STATUS;
					}
					DNDItems.clear();
					return Status.OK_STATUS;
				}
			};
			dropJob.schedule();
		} catch (Exception e) {
			Logger.getLogger(this.getClass()).error(
					Messages.getString("BSIModelViewDropListener.5"), e); //$NON-NLS-1$
			return false;
		}
		return true;
	}

	private void createBausteinUmsetzung(List<Baustein> toDrop,
			CnATreeElement target) throws Exception {
		for (Baustein baustein : toDrop) {
			CnAElementFactory.getInstance().saveNew(target,
					BausteinUmsetzung.TYPE_ID,
					new BuildInput<Baustein>(baustein));
		}
	}

	@Override
	public boolean validateDrop(Object target, int operation,
			TransferData transferType) {
		// Logger.getLogger(this.getClass()).debug("Drop target: " + target);

		if (target == null)
			return false;

		if (!(target instanceof CnATreeElement || target instanceof LinkKategorie))
			return false;

		if (target instanceof IBSIStrukturKategorie)
			return false;

		List items = DNDItems.getItems();

		// use bstUms as template for bstUmsTarget
		if (DNDItems.getItems().get(0) instanceof BausteinUmsetzung) {
			BausteinUmsetzung sourceBst = (BausteinUmsetzung) DNDItems
					.getItems().get(0);
			if (target instanceof BausteinUmsetzung) {
				BausteinUmsetzung targetBst = (BausteinUmsetzung) target;
				if (targetBst.getKapitel().equals(sourceBst.getKapitel()))
					return true;
			}
			return false;
		}

		// link drop:
		if (DNDItems.getItems().get(0) instanceof IBSIStrukturElement) {
			List itemsToDrop = DNDItems.getItems();
			for (Object item : itemsToDrop) {
				if (target.equals(item))
					return false;

				if (!(item instanceof IBSIStrukturElement))
					return false;

				if (item instanceof IBSIStrukturElement
						&& target instanceof LinkKategorie) {
					if (((LinkKategorie) target).getParent().equals(item)) /*
																			 * is
																			 * same
																			 * object
																			 */
						return false;
				}
			}
			return true;
		}

		// other drop type:
		if (!(target instanceof CnATreeElement))
			return false;

		for (Iterator iter = items.iterator(); iter.hasNext();) {
			Object obj = iter.next();
			// Logger.getLogger(this.getClass()).debug("Drop item: " + obj);
			CnATreeElement cont = (CnATreeElement) target;
			if (!cont.canContain(obj))
				return false;

		}
		return true;
	}

}
