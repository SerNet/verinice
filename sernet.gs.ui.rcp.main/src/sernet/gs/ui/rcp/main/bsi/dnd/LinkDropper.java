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
package sernet.gs.ui.rcp.main.bsi.dnd;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.model.IBSIStrukturElement;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.HitroUtil;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.taskcommands.CreateScenario;
import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.HuiRelation;
import sernet.verinice.iso27k.model.IISO27kElement;
import sernet.verinice.iso27k.model.IncidentScenario;
import sernet.verinice.iso27k.model.IncidentScenarioGroup;
import sernet.verinice.iso27k.model.Organization;
import sernet.verinice.iso27k.model.Threat;
import sernet.verinice.iso27k.model.Vulnerability;

public class LinkDropper {

	private static final Logger LOG = Logger.getLogger(LinkDropper.class);
	protected static final String NO_COMMENT = "";

	public boolean dropLink(final List<CnATreeElement> toDrop, final CnATreeElement target) {

		if (LOG.isDebugEnabled()) {
			LOG.debug("dropLink...");
		}
		// Prevent creation of new link when parent is not allowed to be
		// modified.
		if (!CnAElementHome.getInstance().isWriteAllowed(target))
			return false;

		try {
			// close all editors first:
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(true /*
																									 * ask
																									 * save
																									 */);

			Job dropJob = new Job(Messages.getString("LinkDropper.0")) { //$NON-NLS-1$
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						Activator.inheritVeriniceContextState();
						createLink(target, toDrop);
					} catch (Exception e) {
						LOG.error("Drop failed", e); //$NON-NLS-1$
						return Status.CANCEL_STATUS;
					}
					return Status.OK_STATUS;
				}
			};
			dropJob.schedule();
		} catch (Exception e) {
			LOG.error(Messages.getString("LinkDropper.2"), e); //$NON-NLS-1$
			return false;
		}
		return true;
	}

	private void createLink(final CnATreeElement dropTarget, final List<CnATreeElement> toDrop) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("createLink...");
		}
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				Activator.inheritVeriniceContextState();
				List<CnALink> newLinks = new ArrayList<CnALink>();
				allDragged: for (CnATreeElement dragged : toDrop) {
					try {
						// ISO 27k elements are only linked using XML-defined relations:
						if (dropTarget instanceof IISO27kElement && dragged instanceof IISO27kElement) {

						    // special case: threats and vulnerabilities can create a new scenario when dropped:
						    if (dropTarget instanceof Threat && dragged instanceof Vulnerability) {
						        Threat threat;
						        Vulnerability vuln;
						        threat = (Threat) dropTarget;
						        vuln = (Vulnerability) dragged;
						        createScenario(threat, vuln);
						    } 
						    else if (dropTarget instanceof Vulnerability && dragged instanceof Threat) {
						        Threat threat;
						        Vulnerability vuln;
						        vuln = (Vulnerability) dropTarget;
						        threat = (Threat) dragged;
						        createScenario(threat, vuln);
						    }
						    
							Set<HuiRelation> possibleRelations = HitroUtil.getInstance().getTypeFactory()
								.getPossibleRelations(dropTarget.getEntityType().getId(), dragged.getEntityType().getId());
							// try to link from target to dragged elements first:
							// use first relation type (user can change it later):
							if (!possibleRelations.isEmpty()) {
								boolean linkCreated = createTypedLink(newLinks, dropTarget, dragged, possibleRelations.iterator().next().getId(), NO_COMMENT);
								if (linkCreated)
									continue allDragged;
							}
							
							// if none found: try reverse direction from dragged element to target (link is always modelled from one side only)
							possibleRelations = HitroUtil.getInstance().getTypeFactory()
								.getPossibleRelations(dragged.getEntityType().getId(), dropTarget.getEntityType().getId());
							if ( !possibleRelations.isEmpty()) {
								// use first relation type (user can change it later):
								boolean linkCreated = createTypedLink(newLinks, dragged, dropTarget, possibleRelations.iterator().next().getId(), NO_COMMENT);
								if (linkCreated)
									continue allDragged;
							}
						} // end for ISO 27k elements
						
						// backwards compatibility: BSI elements can be linked without a defined relation type, but we use one if present:
						if (dropTarget instanceof IBSIStrukturElement || dragged instanceof IBSIStrukturElement) {
							CnATreeElement from = dropTarget;
							CnATreeElement to = dragged;
							Set<HuiRelation> possibleRelations = HitroUtil.getInstance().getTypeFactory()
								.getPossibleRelations(from.getEntityType().getId(), to.getEntityType().getId());
							if (possibleRelations.isEmpty()) {
								// try again for reverse direction:
								from = dragged;
								to = dropTarget;
								possibleRelations = HitroUtil.getInstance().getTypeFactory()
								.getPossibleRelations(from.getEntityType().getId(), to.getEntityType().getId());
							}
							if (possibleRelations.isEmpty()) {
								//still nothing found, create untyped link:
								CnALink link = CnAElementHome.getInstance().createLink(dropTarget, dragged);
								newLinks.add(link);
							}
							else {
								// create link with type:
								createTypedLink(newLinks, from, to, possibleRelations.iterator().next().getId(), NO_COMMENT);
							}
						}
					} catch (Exception e) {
						LOG.debug("Saving link failed.", e); //$NON-NLS-1$
					}
				}
		
				// fire model changed events:
				for (CnALink link : newLinks) {
					if (link.getDependant() instanceof ITVerbund) {
						CnAElementFactory.getInstance().reloadModelFromDatabase();
						return;
					} else {
						if (link.getDependant() instanceof IBSIStrukturElement || link.getDependency() instanceof IBSIStrukturElement) {
							CnAElementFactory.getLoadedModel().linkAdded(link);
						}
						if (link.getDependant() instanceof IISO27kElement || link.getDependency() instanceof IISO27kElement) {
							CnAElementFactory.getInstance().getISO27kModel().linkAdded(link);
						}
					}
				}
				DNDItems.clear();
			}
		});
	}

	/**
     * @param threat
     * @param vuln
     */
    protected void createScenario(Threat threat, Vulnerability vuln) {
        boolean confirm = MessageDialog.openQuestion(Display.getDefault().getActiveShell(),
                "Create new scenario?", "Threats and vulnerabilities cannot be connected with each other directly. " +
                		"Do you wish to create a new scenario for the connected threat and vulnerability?");
        if (!confirm)
            return;
        
        try {
            CreateScenario command = new CreateScenario(threat, vuln);
            command = ServiceFactory.lookupCommandService().executeCommand(command);
            IncidentScenario newElement = command.getNewElement();
            CnAElementFactory.getInstance().getISO27kModel().childAdded(newElement.getParent(), newElement);
        } catch (CommandException e) {
            ExceptionUtil.log(e, "Error while creating the new scenario.");
        }
    }



    /**
	 * @param newLinks
	 * @param dropTarget
	 * @param dragged
	 * @param id
	 * @param noComment
	 * @return
	 * @throws CommandException 
	 */
	protected boolean createTypedLink(List<CnALink> newLinks,
			CnATreeElement from, CnATreeElement to, String relationTypeid,
			String comment) throws CommandException {
		// use first one (user can change it later):
		CnALink link = CnAElementHome.getInstance()
			.createLink(from, to, relationTypeid, comment );
		if (link == null)
			return false;
		newLinks.add(link);
		if (LOG.isDebugEnabled())
			LOG.debug("Link created");
		return true;
	}

}
