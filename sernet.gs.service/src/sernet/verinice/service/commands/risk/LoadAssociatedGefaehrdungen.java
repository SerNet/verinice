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
package sernet.verinice.service.commands.risk;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.model.Baustein;
import sernet.gs.model.Gefaehrdung;
import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.Retriever;
import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IAuthAwareCommand;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUtil;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Permission;
import sernet.verinice.service.commands.crud.LoadReportLinkedElements;
import sernet.verinice.service.gstoolimport.GefaehrdungsUmsetzungFactory;
import sernet.verinice.service.parser.LoadBausteine;

/**
 * This command loads all threats (German: Gefaehrdungen) for an elmenent which
 * are either children of the element or linked to the element.
 * 
 * After loading the permissions of the element are inherited to the threats.
 * 
 * @author Alexander Koderman <ak[at]sernet[dot]de>
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class LoadAssociatedGefaehrdungen extends GenericCommand 
    implements IAuthAwareCommand {

    private static final long serialVersionUID = -7092181298463682487L;

    private CnATreeElement cnaElement;
    private List<Baustein> alleBausteine;
    private List<GefaehrdungsUmsetzung> associatedGefaehrdungen;

    private transient Logger log;

    private Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(LoadAssociatedGefaehrdungen.class);
        }
        return log;
    }

    private transient IAuthService authService;

    public LoadAssociatedGefaehrdungen(CnATreeElement cnaElement) {
        this.cnaElement = cnaElement;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        reloadElement();
        try {
            loadAssociatedGefaehrdungen();
            inheritPermissions();
        } catch (CommandException e) {
            getLog().error("Something went wrong on computing associated "
                    + "Gefaehrdungen via link for element:\t"
                    + cnaElement.getUuid(), e);
            throw new RuntimeCommandException(e);
        }

    }

    private void loadAssociatedGefaehrdungen() throws CommandException {
        associatedGefaehrdungen = new ArrayList<>();
        Set<GefaehrdungsUmsetzung> unifiedCollection = new HashSet<>();

        /*
         * look for associated Gefaehrdung via children of cnaelement
         */
        unifiedCollection.addAll(getAssociatedGefaehrdungenViaChildren());

        /*
         * look for associated Gefaehrdung via downlinks of cnaelement
         */
        unifiedCollection.addAll(getAssociatedGefaehrdungenViaLinks());
        
        associatedGefaehrdungen.addAll(unifiedCollection);
    }

    private Set<GefaehrdungsUmsetzung> getAssociatedGefaehrdungenViaLinks()
            throws CommandException {
        Set<GefaehrdungsUmsetzung> gefaehrdungen = new HashSet<>();
        Set<BausteinUmsetzung> linkedBausteinUmsetzungen 
            = findLinkedBausteinUmsetzungen(cnaElement);
        for (BausteinUmsetzung linkedBausteinUmsetzung : linkedBausteinUmsetzungen) {
            gefaehrdungen.addAll(
                    getGefaehrdungsUmsetzungenFromBausteinUmsetzung(
                            linkedBausteinUmsetzung));
        }
        return gefaehrdungen;
    }

    private Set<GefaehrdungsUmsetzung> getAssociatedGefaehrdungenViaChildren() {
        Set<CnATreeElement> children = cnaElement.getChildren();
        Set<GefaehrdungsUmsetzung> gefaehrdungen = new HashSet<>();
        for (CnATreeElement cnATreeElement : children) {
            if (!(cnATreeElement instanceof BausteinUmsetzung)) {
                continue;
            }
            BausteinUmsetzung bausteinUmsetzung = (BausteinUmsetzung) cnATreeElement;
            gefaehrdungen.addAll(
                    getGefaehrdungsUmsetzungenFromBausteinUmsetzung(
                            bausteinUmsetzung));
        }

        return gefaehrdungen;
    }

    private Set<GefaehrdungsUmsetzung> 
        getGefaehrdungsUmsetzungenFromBausteinUmsetzung(
                BausteinUmsetzung bausteinUmsetzung) {
        Set<GefaehrdungsUmsetzung> gefaehrdungen = new HashSet<>();
        Baustein baustein = findBausteinForId(bausteinUmsetzung.getKapitel());
        if (baustein == null) {
            return gefaehrdungen;
        }
        for (Gefaehrdung gefaehrdung : baustein.getGefaehrdungen()) {
            if (!GefaehrdungsUtil.listContainsById(this.associatedGefaehrdungen,
                    gefaehrdung)) {
                gefaehrdungen.add(GefaehrdungsUmsetzungFactory.build(null,
                        gefaehrdung, null));
            }
        }
        return gefaehrdungen;

    }

    private Set<BausteinUmsetzung> findLinkedBausteinUmsetzungen(
            CnATreeElement sourceElement) throws CommandException {
        LoadReportLinkedElements linkLoader = new LoadReportLinkedElements(
                BausteinUmsetzung.TYPE_ID, sourceElement.getDbId());
        linkLoader = getCommandService().executeCommand(linkLoader);
        Set<BausteinUmsetzung> linkedBausteinUmsetzungen = new HashSet<>(
                linkLoader.getElements().size());
        for (CnATreeElement foundElement : linkLoader.getElements()) {
            if (BausteinUmsetzung.TYPE_ID.equals(foundElement.getTypeId())) {
                foundElement = Retriever.retrieveElement(foundElement,
                        RetrieveInfo.getPropertyInstance());
                linkedBausteinUmsetzungen.add((BausteinUmsetzung) foundElement);
            }
        }
        return linkedBausteinUmsetzungen;
    }

    private Baustein findBausteinForId(String id) {
        // FIXME: load only the baustein for the given id, not all!
        if (alleBausteine == null) {
            LoadBausteine bstsCommand = new LoadBausteine();
            try {
                bstsCommand = getCommandService().executeCommand(bstsCommand);
            } catch (CommandException e) {
                throw new RuntimeCommandException(e);
            }
            alleBausteine = bstsCommand.getBausteine();
        }

        if (alleBausteine != null) {
            for (Baustein baustein : alleBausteine) {
                if (baustein.getId().equals(id)) {
                    return baustein;
                }
            }
        }
        return null;
    }

    private void inheritPermissions() {
        for (GefaehrdungsUmsetzung gefaehrdungsUmsetzung : associatedGefaehrdungen) {
            if (authService.isPermissionHandlingNeeded()) {
                gefaehrdungsUmsetzung.setPermissions(Permission.
                        clonePermissionSet(gefaehrdungsUmsetzung,
                                cnaElement.getPermissions()));
            }
        }
    }

    private void reloadElement() {
        IBaseDao<Object, Serializable> dao = getDaoFactory().
                getDAOforTypedElement(cnaElement);
        dao.reload(cnaElement, cnaElement.getDbId());
    }

    public List<GefaehrdungsUmsetzung> getAssociatedGefaehrdungen() {
        return associatedGefaehrdungen;
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.interfaces.GenericCommand#clear()
     */
    @Override
    public void clear() {
        alleBausteine = null;
        cnaElement = null;
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
