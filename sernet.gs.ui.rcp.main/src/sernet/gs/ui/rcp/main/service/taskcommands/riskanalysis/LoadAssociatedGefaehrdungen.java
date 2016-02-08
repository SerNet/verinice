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
package sernet.gs.ui.rcp.main.service.taskcommands.riskanalysis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.model.Baustein;
import sernet.gs.model.Gefaehrdung;
import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.RuntimeCommandException;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzungFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadReportLinkedElements;
import sernet.gs.ui.rcp.main.service.grundschutzparser.LoadBausteine;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IAuthAwareCommand;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.iso27k.service.Retriever;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUtil;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Permission;

public class LoadAssociatedGefaehrdungen extends GenericCommand implements IAuthAwareCommand {

	private CnATreeElement cnaElement;
	private List<Baustein> alleBausteine;
	private List<GefaehrdungsUmsetzung> associatedGefaehrdungen;
	
    private transient Logger log;

	private String language;

    private transient IAuthService authService;

	public LoadAssociatedGefaehrdungen(CnATreeElement cnaElement, String language) {
		this.cnaElement = cnaElement;
	}

	public void execute() {
        associatedGefaehrdungen = new ArrayList<GefaehrdungsUmsetzung>();

        IBaseDao<Object, Serializable> dao = getDaoFactory().getDAOforTypedElement(cnaElement);
        dao.reload(cnaElement, cnaElement.getDbId());

        try {

            /*
             * look for associated Gefaehrdung via children of cnaelement
             */
            associatedGefaehrdungen.addAll(getAssociatedGefaehrdungenViaChildren());

            /*
             * look for associated Gefaehrdung via downlinks of cnaelement
             */
            associatedGefaehrdungen.addAll(getAssociatedGefaehrdungenViaLinks());
            
            for (GefaehrdungsUmsetzung gefaehrdungsUmsetzung : associatedGefaehrdungen) {
                if (authService.isPermissionHandlingNeeded()) {
                    gefaehrdungsUmsetzung.setPermissions(Permission.clonePermissionSet(gefaehrdungsUmsetzung, cnaElement.getPermissions()));
                }              
            }
        } catch (CommandException e) {
            getLog().error("Something went wrong on computing associated Gefaehrdungen via link for element:\t" + cnaElement.getUuid(), e);
            throw new RuntimeException(e);
        }

    }

    private Set<GefaehrdungsUmsetzung> getAssociatedGefaehrdungenViaLinks() throws CommandException {
        Set<GefaehrdungsUmsetzung> associatedGefaehrdungen = new HashSet<>();
        Set<BausteinUmsetzung> linkedBausteinUmsetzungen = findLinkedBausteinUmsetzungen(cnaElement);
        for (BausteinUmsetzung linkedBausteinUmsetzung : linkedBausteinUmsetzungen) {
            associatedGefaehrdungen.addAll(getGefaehrdungsUmsetzungenFromBausteinUmsetzung(linkedBausteinUmsetzung));
        }
        return associatedGefaehrdungen;
    }

    private Set<GefaehrdungsUmsetzung> getAssociatedGefaehrdungenViaChildren() {
        Set<CnATreeElement> children = cnaElement.getChildren();
        Set<GefaehrdungsUmsetzung> associatedGefaehrdungen = new HashSet<>();
		for (CnATreeElement cnATreeElement : children) {
			if (!(cnATreeElement instanceof BausteinUmsetzung)){
				continue;
			}
			BausteinUmsetzung bausteinUmsetzung = (BausteinUmsetzung) cnATreeElement;
            associatedGefaehrdungen.addAll(getGefaehrdungsUmsetzungenFromBausteinUmsetzung(bausteinUmsetzung));
        }
		
        return associatedGefaehrdungen;
    }

    private Set<GefaehrdungsUmsetzung> getGefaehrdungsUmsetzungenFromBausteinUmsetzung(BausteinUmsetzung bausteinUmsetzung) {
        Set<GefaehrdungsUmsetzung> associatedGefaehrdungen = new HashSet<>();
        Baustein baustein = findBausteinForId(bausteinUmsetzung.getKapitel());
        if (baustein == null) {
            return associatedGefaehrdungen;
        }
        for (Gefaehrdung gefaehrdung : baustein.getGefaehrdungen()) {
            if (!GefaehrdungsUtil.listContainsById(this.associatedGefaehrdungen, gefaehrdung)) {
                associatedGefaehrdungen.add(GefaehrdungsUmsetzungFactory.build(null, gefaehrdung, language));
            }
        }

        return associatedGefaehrdungen;

    }

    private Set<BausteinUmsetzung> findLinkedBausteinUmsetzungen(CnATreeElement sourceElement) throws CommandException {
        LoadReportLinkedElements linkLoader = new LoadReportLinkedElements(BausteinUmsetzung.TYPE_ID, sourceElement.getDbId());
        linkLoader = getCommandService().executeCommand(linkLoader);
        Set<BausteinUmsetzung> linkedBausteinUmsetzungen = new HashSet<>(linkLoader.getElements().size());
        for (CnATreeElement foundElement : linkLoader.getElements()) {
            if (BausteinUmsetzung.TYPE_ID.equals(foundElement.getTypeId())) {
                foundElement = Retriever.retrieveElement(foundElement, RetrieveInfo.getPropertyInstance());
                linkedBausteinUmsetzungen.add((BausteinUmsetzung) foundElement);
            }
        }
        return linkedBausteinUmsetzungen;
    }

	private Baustein findBausteinForId(String id) {
		if (alleBausteine == null) {
			LoadBausteine bstsCommand = new LoadBausteine();
			try {
				bstsCommand = getCommandService().executeCommand(bstsCommand);
			} catch (CommandException e) {
				throw new RuntimeCommandException(e);
			}
			alleBausteine = bstsCommand.getBausteine();
		}
		
		if(alleBausteine != null){ 
    		for (Baustein baustein : alleBausteine) {
    			if (baustein.getId().equals(id)){
    				return baustein;
    			}
    		}
		}
		return null;
	}

	public void clear() {
		alleBausteine = null;
		cnaElement = null;
	}

	public List<GefaehrdungsUmsetzung> getAssociatedGefaehrdungen() {
		return associatedGefaehrdungen;
	}

    private Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(LoadAssociatedGefaehrdungen.class);
        }
        return log;
    }

    @Override
    public IAuthService getAuthService() {
        return authService;
    }

    @Override
    public void setAuthService(IAuthService service) {
        this.authService = service;
    }

}
