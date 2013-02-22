/*******************************************************************************
 * Copyright (c) 2012 Julia Haas.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Julia Haas <jh[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.taskcommands;

/**
 * @author Julia Haas <jh[at]sernet[dot]de>
 *
 */

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.ChangeLoggingCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IChangeLoggingCommand;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;

public class GSMKonsolidatorCommand extends ChangeLoggingCommand implements IChangeLoggingCommand {

    
    public static final List<String> PROPERTY_TYPE_BLACKLIST = Arrays.asList(BausteinUmsetzung.P_NAME, MassnahmenUmsetzung.P_SIEGEL);
    private List<String> propertyTypeBlacklist;
    private static transient IBaseDao<CnATreeElement, Serializable> dao;
    private List<BausteinUmsetzung> selectedElements;
    private BausteinUmsetzung source;
    private String stationId;
    private List<CnATreeElement> changedElements;

    public GSMKonsolidatorCommand(List<BausteinUmsetzung> selectedElements, BausteinUmsetzung source) {
        this.selectedElements = selectedElements;
        this.source = source;
        this.stationId = ChangeLogEntry.STATION_ID;
        setPropertyTypeBlacklist(PROPERTY_TYPE_BLACKLIST);
    }

    public void execute() {
        IBaseDao<BausteinUmsetzung, Serializable> internalDao = getDaoFactory().getDAO(BausteinUmsetzung.class);
        internalDao.reload(source, source.getDbId());

        changedElements = new LinkedList<CnATreeElement>();
        // for every target:
        for (BausteinUmsetzung target : selectedElements) {
            // do not copy source onto itself:
            if (source.equals(target)){
                continue;
            }
            internalDao.reload(target, target.getDbId()); //anschauen was noch geladen ist!
            // set values:
            target = (BausteinUmsetzung) getDao().findByUuid(target.getUuid(), RetrieveInfo.getPropertyChildrenInstance());
            source = (BausteinUmsetzung) getDao().findByUuid(source.getUuid(), RetrieveInfo.getPropertyChildrenInstance());
            for (MassnahmenUmsetzung mn: target.getMassnahmenUmsetzungen()) {
                MassnahmenUmsetzung sourceMn = source.getMassnahmenUmsetzung(mn.getUrl());
                if (sourceMn != null) {
                    mn.getEntity().copyEntity(sourceMn.getEntity(), propertyTypeBlacklist);
                    getDao().merge(target);
                    changedElements.add(target);
                }
            }         
        }
        // remove elements to make object smaller for transport back to client
        selectedElements = null;
        source = null;
    }
    
    
    
    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.interfaces.IChangeLoggingCommand#getStationId()
     */
    @Override
    public String getStationId() {
        return stationId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.interfaces.IChangeLoggingCommand#getChangedElements()
     */
    @Override
    public List<CnATreeElement> getChangedElements() {
        return changedElements;
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.interfaces.IChangeLoggingCommand#getChangeType()
     */
    @Override
    public int getChangeType() {
        return ChangeLogEntry.TYPE_UPDATE;
    }
    /**
     * @param propertyTypeBlacklist the propertyTypeBlacklist to set
     */
    protected void setPropertyTypeBlacklist(List<String> propertyTypeBlacklist) {
        this.propertyTypeBlacklist = propertyTypeBlacklist;
    }

    protected IBaseDao<CnATreeElement, Serializable> getDao() {
        if(dao==null) {
            dao = getDaoFactory().getDAO(CnATreeElement.class);
        }
        return dao;
    }
}