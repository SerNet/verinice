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
import sernet.verinice.interfaces.IFinishedRiskAnalysisListsDao;
import sernet.verinice.interfaces.INoAccessControl;
import sernet.verinice.model.bsi.IBSIStrukturElement;
import sernet.verinice.model.bsi.IBSIStrukturKategorie;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.ImportBsiGroup;
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
    
    private transient IFinishedRiskAnalysisListsDao raListDao;
    
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
            int listsDbId = -1;
            if (element instanceof GefaehrdungsUmsetzung) {
                if (element.getParent() != null) {
                    listsDbId = element.getParent().getDbId();
                }
            }

            IBaseDao dao = getDaoFactory().getDAOforTypedElement(element);
            element = (T) dao.findById(element.getDbId());
            
            if (element instanceof IBSIStrukturElement || element instanceof IBSIStrukturKategorie){
                removeAllRiskAnalyses();
            }

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
                removeAllGefaehrdungsUmsetzungen();
            }

            if (element instanceof FinishedRiskAnalysis) {
                FinishedRiskAnalysis analysis = (FinishedRiskAnalysis) element;
                removeRiskAnalysis(analysis);
            }

            if (element instanceof GefaehrdungsUmsetzung && listsDbId >= 0) {
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
                    removeRiskAnalysis((FinishedRiskAnalysis) children[i]);
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

    /**
     * special handling for deletion of instances of
     * {@link GefaehrdungsUmsetzung} that does not belong to a
     * {@link FinishedRiskAnalysis} should only be called if @param element is
     * of type {@link ITVerbund} and always after all riskanalyses are removed /
     * deleted
     */
    private void removeAllGefaehrdungsUmsetzungen() {
        String hqlQuery = "from CnATreeElement element where element.objectType = ? AND element.scopeId = ?";
        Object[] params = new Object[] { GefaehrdungsUmsetzung.HIBERNATE_TYPE_ID, element.getDbId() };
        List elementsToDelete = getDaoFactory().getDAO(GefaehrdungsUmsetzung.class).findByQuery(hqlQuery, params);
        for (Object o : elementsToDelete) {
            if (o instanceof GefaehrdungsUmsetzung) {
                getDaoFactory().getDAO(GefaehrdungsUmsetzung.class).delete((GefaehrdungsUmsetzung) o);
            }
        }
    }

    private void removeAllRiskAnalyses() throws CommandException {
        if (ImportBsiGroup.TYPE_ID.equals(element.getTypeId())){
            removeRiskAnalysisFromBSIImportGroup(element.getDbId());
        } else if (ITVerbund.TYPE_ID.equals(element.getTypeId())){
            removeRiskAnalysisForScope(element.getScopeId());
        } else if (element instanceof IBSIStrukturKategorie){
            removeRiskAnalysesFromBSICategory();
            
        }
        // handling for instances of ISBSIStrukturElement not necessary
    }

    private void removeRiskAnalysesFromBSICategory() throws CommandException {
        StringBuilder sb = new StringBuilder();
        sb.append("select element.dbId from CnATreeElement element where ");
        sb.append("element.scopeId = :scopeId and element.parentId = :parentId");
        String hql = sb.toString();

        // since the values of the list will be ints, we can't parameterize the list 
        List deleteList = getDaoFactory().getDAO(CnATreeElement.class)
                .findByQuery(hql, new String[]{"scopeId", "parentId"}, 
                        new Object[]{element.getScopeId(), element.getDbId()});
        for (Object o : deleteList) {
            if (o instanceof Integer) {
                LoadRiskAnalyses loadRiskAnalyses = new LoadRiskAnalyses((Integer)o, true);
                List<FinishedRiskAnalysis> riskAnalysesList = getCommandService().
                        executeCommand(loadRiskAnalyses).getRaList();
                for (FinishedRiskAnalysis finishedRiskAnalysis : riskAnalysesList) {
                    removeRiskAnalysis(finishedRiskAnalysis);
                }
            }
        }
    }

    private void removeRiskAnalysisFromBSIImportGroup(Integer groupDbId ) throws CommandException {
        String hql = "from CnATreeElement itv where itv.objectType = 'it-verbund' and itv.parentId = (" +
                " select importbsigroup.dbId from CnATreeElement importbsigroup where "
                + "importbsigroup.objectType = 'import-bsi' and importbsigroup.dbId = :dbId)";
        List deleteList = getDaoFactory().getDAO(CnATreeElement.class).findByQuery(
                hql, new String[]{"dbId"}, new Object[]{groupDbId});
        for(Object o : deleteList){
            if(o instanceof ITVerbund){
                removeRiskAnalysisForScope(((ITVerbund)o).getScopeId());
            }
        }
    }

    private void removeRiskAnalysisForScope(int scopeId) throws CommandException {
        LoadRiskAnalyses loadRiskAnalyses = new LoadRiskAnalyses(scopeId);
        loadRiskAnalyses = getCommandService().executeCommand(loadRiskAnalyses);
        List<FinishedRiskAnalysis> raList = loadRiskAnalyses.getRaList();
        for (FinishedRiskAnalysis finishedRiskAnalysis : raList) {
            removeRiskAnalysis(finishedRiskAnalysis);
        }
    }

    private void removeRiskAnalysis(FinishedRiskAnalysis finishedRiskAnalysis) throws CommandException {
        removeChildren(finishedRiskAnalysis);
        List<FinishedRiskAnalysisLists> list = getRaListDao().
                findByFinishedRiskAnalysisId(finishedRiskAnalysis.getDbId());
        for (FinishedRiskAnalysisLists ra : list) {
            getRaListDao().delete(ra);
        }
    }

    private void removeChildren(FinishedRiskAnalysis analysis) throws CommandException {
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
        if (lists != null) {
            lists.removeGefaehrdungCompletely(gef);
        }
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
    
    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.service.commands.GenericCommand#clear()
     */
    @Override
    public void clear() {
        element = null;
    }

    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand#getChangeType()
     */
    @Override
    public int getChangeType() {
        return ChangeLogEntry.TYPE_DELETE;
    }

    /* (non-Javadoc)
     * @seesernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand#getChangedElements()
     */
    @Override
    public List<CnATreeElement> getChangedElements() {
        ArrayList<CnATreeElement> result = new ArrayList<CnATreeElement>(1);
        result.add(element);
        return result;
    }

    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand#getStationId()
     */
    @Override
    public String getStationId() {
        return stationId;
    }

    
    public IFinishedRiskAnalysisListsDao getRaListDao() {
        if(raListDao==null) {
            raListDao = getDaoFactory().getFinishedRiskAnalysisListsDao();      
        }
        return raListDao;
    }
}
