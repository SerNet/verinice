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
 *     Henning Heinold <h.heinold@tarent.de> - cascade when deleting CnATreeElement with
 *     FinishedRiskAnalysis
 ******************************************************************************/
package sernet.verinice.service.commands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.service.RuntimeCommandException;
import sernet.gs.service.SecurityException;
import sernet.verinice.interfaces.ChangeLoggingCommand;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IChangeLoggingCommand;
import sernet.verinice.interfaces.INoAccessControl;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.bsi.PersonenKategorie;
import sernet.verinice.model.bsi.risikoanalyse.FinishedRiskAnalysis;
import sernet.verinice.model.bsi.risikoanalyse.FinishedRiskAnalysisLists;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.iso27k.PersonIso;

/**
 * Removes tree-elements.
 * 
 * Children, links and attachments are deleted by hibernate cascading 
 * (see CnATreeElement.hbm.xml)
 * 
 * @author Alexander Koderman <ak[at]sernet[dot]de>.
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 * @param <T>
 */
@SuppressWarnings("serial")
public class RemoveElement<T extends CnATreeElement> extends ChangeLoggingCommand implements IChangeLoggingCommand, INoAccessControl {

    private transient Logger log = Logger.getLogger(RemoveElement.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(RemoveElement.class);
        }
        return log;
    }
    
    private T element;
    private String stationId;
    private Integer elementId;
    private String typeId;

    public RemoveElement(T element) {
        // only transfer id of element to keep footprint small:
        typeId = element.getTypeId();
        elementId = element.getDbId();

        this.stationId = ChangeLogEntry.STATION_ID;
    }

    @Override
    public void execute() {
        try {
            // load element from DB:
            this.element = (T) getDaoFactory().getDAO(typeId).findById(elementId);

            if(this.element==null) {
                if (getLog().isDebugEnabled()) {
                    getLog().debug("Element was not found in db. Type-Id: " + typeId + ", Db-Id: " + elementId);
                }
                return;
            }
            
            if (element instanceof Person || element instanceof PersonIso){
                removeConfiguration(element);
            }
            int listsDbId = 0;
            if (element instanceof GefaehrdungsUmsetzung) {
                listsDbId = element.getParent().getDbId();
            }

            IBaseDao dao = getDaoFactory().getDAOforTypedElement(element);
            element = (T) dao.findById(element.getDbId());

            if (element instanceof ITVerbund) {
                CnATreeElement cat = ((ITVerbund) element).getCategory(PersonenKategorie.TYPE_ID);

                // A defect in the application allowed that ITVerbund instances
                // without a category are
                // created. With this tiny check we can ensure that they can be
                // deleted.
                if (cat != null) {
                    Set<CnATreeElement> personen = cat.getChildren();
                    for (CnATreeElement elmt : personen) {
                        removeConfiguration(elmt);
                    }
                }
            }

            if (element instanceof FinishedRiskAnalysis) {
                FinishedRiskAnalysis analysis = (FinishedRiskAnalysis) element;
                remove(analysis);
            }

            if (element instanceof GefaehrdungsUmsetzung) {
                GefaehrdungsUmsetzung gef = (GefaehrdungsUmsetzung) element;
                removeFromLists(listsDbId, gef);
            }   
            
            /*
             * Special case the deletion of FinishedRiskAnalysis instances:
             * Before the instance is deleted itself their children must be
             * removed manually (otherwise referential integrity is violated and
             * Hibernate reports an error).
             * 
             * Using the children as an array ensure that there won't be a
             * ConcurrentModificationException while deleting the elements.
             */
            CnATreeElement[] children = element.getChildrenAsArray();

            for (int i = 0; i < children.length; i++) {
                if (children[i] instanceof FinishedRiskAnalysis) {
                    RemoveElement<CnATreeElement> command = new RemoveElement<CnATreeElement>(children[i]);
                    getCommandService().executeCommand(command);
                }
            }

            element.remove();
            dao.delete(element);
        } catch (SecurityException e) {
            getLog().error("SecurityException while deleting element: " + element, e);
            throw e;
        } catch (RuntimeException e) {
            getLog().error("RuntimeException while deleting element: " + element, e);
            throw e;
        } catch (Exception e) {
            getLog().error("Exception while deleting element: " + element, e);
            throw new RuntimeCommandException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.gs.ui.rcp.main.service.commands.GenericCommand#clear()
     */
    @Override
    public void clear() {
        element = null;
    }

    /**
     * @param analysis
     * @throws CommandException
     */
    private void remove(FinishedRiskAnalysis analysis) throws CommandException {
        Set<CnATreeElement> children = analysis.getChildren();
        for (CnATreeElement child : children) {
            if (child instanceof GefaehrdungsUmsetzung) {
                GefaehrdungsUmsetzung gef = (GefaehrdungsUmsetzung) child;
                removeFromLists(gef.getParent().getDbId(), gef);
            }
        }
    }

    /**
     * Remove from all referenced lists.
     * 
     * @param element2
     * @throws CommandException
     */
    private void removeFromLists(int analysisId, GefaehrdungsUmsetzung gef) throws CommandException {
        FindRiskAnalysisListsByParentID command = new FindRiskAnalysisListsByParentID(analysisId);
        getCommandService().executeCommand(command);
        FinishedRiskAnalysisLists lists = command.getFoundLists();
        lists.removeGefaehrdungCompletely(gef);
    }

    private void removeConfiguration(CnATreeElement person) throws CommandException {
        LoadConfiguration command = new LoadConfiguration(person);
        command = getCommandService().executeCommand(command);
        Configuration conf = command.getConfiguration();
        if (conf != null) {
            IBaseDao<Configuration, Serializable> confDAO = getDaoFactory().getDAO(Configuration.class);
            confDAO.delete(conf);

            // When a Configuration instance got deleted the server needs to
            // update
            // its cached role map. This is done here.
            getCommandService().discardUserData();
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand#getChangeType
     * ()
     */
    @Override
    public int getChangeType() {
        return ChangeLogEntry.TYPE_DELETE;
    }

    /*
     * (non-Javadoc)
     * 
     * @seesernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand#
     * getChangedElements()
     */
    @Override
    public List<CnATreeElement> getChangedElements() {
        ArrayList<CnATreeElement> result = new ArrayList<CnATreeElement>(1);
        result.add(element);
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand#getStationId
     * ()
     */
    @Override
    public String getStationId() {
        return stationId;
    }

}
