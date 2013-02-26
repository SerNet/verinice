/*******************************************************************************
 * Copyright (c) 2009 Andreas Becker <andreas.r.becker@rub.de>.
 * Copyright (c) 2010 Robert Schuster <r.schuster@tarent.de>
 * Copyright (c) 2010 Daniel Murygin <dm[a]sernet[dot]de>
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
 *     Andreas Becker <andreas.r.becker[at]rub[dot]de> - initial API and implementation
 *     Robert Schuster <r.schuster[a]tarent[dot]de> - conversion to verinice command  
 *     Daniel Murygin <dm[a]sernet[dot]de> - Bugfixing
 ******************************************************************************/
package sernet.verinice.service.commands;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXB;

import org.apache.log4j.Logger;

import sernet.gs.service.RuntimeCommandException;
import sernet.gs.service.VeriniceCharset;
import sernet.verinice.interfaces.ChangeLoggingCommand;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IAuthAwareCommand;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.IChangeLoggingCommand;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.sync.VeriniceArchive;
import de.sernet.sync.data.SyncData;
import de.sernet.sync.mapping.SyncMapping;
import de.sernet.sync.sync.SyncRequest;

@SuppressWarnings("serial")
public class SyncCommand extends ChangeLoggingCommand implements IChangeLoggingCommand, IAuthAwareCommand {
    private transient Logger log = Logger.getLogger(SyncCommand.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(SyncCommand.class);
        }
        return log;
    }

    private String sourceId;

    private transient IAuthService authService;

    private String stationId;

    private SyncParameter parameter;

    private byte[] syncRequestSerialized;

    private transient SyncData syncData;

    private transient SyncMapping syncMapping;

    private int inserted, updated, deleted;

    private List<String> errors = new ArrayList<String>();

    private Set<CnATreeElement> importRootObject;

    private Set<CnATreeElement> elementSet = null;
    
    private transient VeriniceArchive veriniceArchive;

    /**
     * Creates an instance of the SyncCommand where the {@link SyncRequest}
     * object is already serialized to a byte array.
     * 
     * <p>
     * Usage of this constructor is needed in all cases where command is going
     * to be serialized/deserialized. This in turn would cause the same being
     * done to the {@link SyncRequest} object which unfortunately is not
     * possible (through default Spring HttpInvoker mechanism at least).
     * </p>
     * 
     * @param insert
     * @param update
     * @param delete
     * @param syncRequestSerialized
     */
    public SyncCommand(SyncParameter parameter, byte[] syncRequestSerialized) {     
        this.parameter = parameter;    
        this.syncRequestSerialized = (syncRequestSerialized != null) ? syncRequestSerialized.clone() : null;
        this.stationId = ChangeLogEntry.STATION_ID;
    }

    /**
     * Works like
     * {@link #SyncCommand(String, boolean, boolean, boolean, byte[])} but does
     * the JAXB serialization under the hood automatically.
     * 
     * Called by ImportCSVWizard
     * 
     * @param insert
     * @param update
     * @param delete
     * @param sr
     */
    public SyncCommand(SyncParameter parameter, SyncRequest sr) {
        this.parameter = parameter; 
        
        // TODO: dm, SyncRequest marshal is called in contructor and unmarshal is called in execute...
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        JAXB.marshal(sr, bos);

        this.syncRequestSerialized = bos.toByteArray();
        this.stationId = ChangeLogEntry.STATION_ID;
    }

    /**
     * Called by soap web-service
     * 
     * @param sr SyncRequest
     */
    public SyncCommand(SyncRequest sr) {
        this.sourceId = sr.getSourceId();      
        this.syncData = sr.getSyncData();
        this.syncMapping = sr.getSyncMapping();
        this.syncRequestSerialized = null;
        this.stationId = ChangeLogEntry.STATION_ID;
        this.parameter = new SyncParameter(sr.isInsert(), sr.isUpdate(), sr.isDelete(), false,  SyncParameter.EXPORT_FORMAT_XML_PURE);
    }

    @Override
    public void execute() {
        
        long start = 0;
        if (getLog().isInfoEnabled()) {
           start = System.currentTimeMillis();
        }
        
        getDataFromRequest();
        
        SyncInsertUpdateCommand cmdInsertUpdate = new SyncInsertUpdateCommand(
        		sourceId, 
        		syncData, 
        		syncMapping,
        		getAuthService().getUsername(),
        		parameter,
        		errors);

        try {
            cmdInsertUpdate = getCommandService().executeCommand(cmdInsertUpdate);
            if(isVeriniceArchive()) {
                cmdInsertUpdate.importFileData(syncRequestSerialized);
            }
            // clear memory
            syncRequestSerialized = null;
        } catch (RuntimeException e) {
            log.error("Error while importing", e);
            errors.add("Insert/Update failed.");
            throw e;
        } catch (Exception e) {
            log.error("Error while importing", e);
            errors.add("Insert/Update failed.");
            throw new RuntimeCommandException(e);
        }

        importRootObject = new HashSet<CnATreeElement>(cmdInsertUpdate.getContainerMap().values());
        elementSet = cmdInsertUpdate.getElementSet();

        inserted += cmdInsertUpdate.getInserted();
        updated += cmdInsertUpdate.getUpdated();

        if (parameter.isDelete()) {
            SyncDeleteCommand cmdDelete = new SyncDeleteCommand(sourceId, syncData, errors);

            try {
                cmdDelete = getCommandService().executeCommand(cmdDelete);
            } catch (CommandException e) {
                errors.add("Delete failed.");
                return;
            }

            deleted += cmdDelete.getDeleted();
        }
        if (getLog().isInfoEnabled()) {
            long time = System.currentTimeMillis() - start;
            getLog().info("Runtime: " + time +" ms");
         }

    }

    private SyncRequest getDataFromRequest() {
        byte[] xmlData = null;
        if(isVeriniceArchive()) {
            veriniceArchive = new VeriniceArchive(syncRequestSerialized);
            xmlData = veriniceArchive.getVeriniceXml();
        }
        if(SyncParameter.EXPORT_FORMAT_XML_PURE.equals(parameter.getFormat())) {
            xmlData = syncRequestSerialized;
        }
        
        SyncRequest sr = null;
        if( xmlData!=null) {
            if (getLog().isDebugEnabled()) {
                String xml = new String(xmlData,VeriniceCharset.CHARSET_UTF_8);
                getLog().debug("### Importing data begin ###");
                getLog().debug(xml);
                getLog().debug("### Importing data end ####");
            }
       
            sr = JAXB.unmarshal(new ByteArrayInputStream(xmlData), SyncRequest.class);
            sourceId = sr.getSourceId();
            syncData = sr.getSyncData();
            syncMapping = sr.getSyncMapping();
        }
        return sr;
    }

    public int getInserted() {
        return inserted;
    }

    public int getUpdated() {
        return updated;
    }

    public int getDeleted() {
        return deleted;
    }

    public List<String> getErrors() {
        return errors;
    }

    /**
     * See {@link SyncInsertUpdateCommand#getImportRootObject()}.
     * 
     * @return
     */
    public Set<CnATreeElement> getImportRootObject() {
        return importRootObject;
    }

    public Set<CnATreeElement> getElementSet() {
        return elementSet;
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.interfaces.IChangeLoggingCommand#getChangeType()
     */
    @Override
    public int getChangeType() {
        return ChangeLogEntry.TYPE_INSERT;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.interfaces.IChangeLoggingCommand#getChangedElements()
     */
    @Override
    public List<CnATreeElement> getChangedElements() {
        List<CnATreeElement> changedElements = new LinkedList<CnATreeElement>();
        if (importRootObject != null) {
            changedElements.addAll(importRootObject);
        }
        if (elementSet != null) {
            changedElements.addAll(elementSet);
        }
        return changedElements;
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
     * sernet.gs.ui.rcp.main.service.commands.IAuthAwareCommand#getAuthService()
     */
    @Override
    public IAuthService getAuthService() {
        return authService;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.gs.ui.rcp.main.service.commands.IAuthAwareCommand#setAuthService
     * (sernet.gs.ui.rcp.main.service.IAuthService)
     */
    @Override
    public void setAuthService(IAuthService service) {
        this.authService = service;
    }
    
    private boolean isVeriniceArchive() {
        return SyncParameter.EXPORT_FORMAT_VERINICE_ARCHIV.equals(parameter.getFormat());
    }

}
