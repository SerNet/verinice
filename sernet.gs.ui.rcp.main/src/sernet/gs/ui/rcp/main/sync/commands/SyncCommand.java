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
package sernet.gs.ui.rcp.main.sync.commands;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXB;

import org.apache.log4j.Logger;

import sernet.gs.service.VeriniceCharset;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IAuthAwareCommand;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.IChangeLoggingCommand;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;
import de.sernet.sync.data.SyncData;
import de.sernet.sync.mapping.SyncMapping;
import de.sernet.sync.sync.SyncRequest;

@SuppressWarnings("serial")
public class SyncCommand extends GenericCommand implements IChangeLoggingCommand, IAuthAwareCommand {
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

    private boolean insert, update, delete;

    private byte[] syncRequestSerialized;

    private transient SyncData syncData;

    private transient SyncMapping syncMapping;

    private int inserted, updated, deleted;

    private List<String> errors = new ArrayList<String>();

    private Set<CnATreeElement> importRootObject;

    private Set<CnATreeElement> elementSet = null;

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
    public SyncCommand(boolean insert, boolean update, boolean delete, byte[] syncRequestSerialized) {
        this.insert = insert;
        this.update = update;
        this.delete = delete;

        this.syncRequestSerialized = syncRequestSerialized;
        this.stationId = ChangeLogEntry.STATION_ID;
    }

    /**
     * Works like
     * {@link #SyncCommand(String, boolean, boolean, boolean, byte[])} but does
     * the JAXB serialization under the hood automatically.
     * 
     * @param insert
     * @param update
     * @param delete
     * @param sr
     */
    public SyncCommand(boolean insert, boolean update, boolean delete, SyncRequest sr) {
        this.insert = insert;
        this.update = update;
        this.delete = delete;

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        JAXB.marshal(sr, bos);

        this.syncRequestSerialized = bos.toByteArray();
        this.stationId = ChangeLogEntry.STATION_ID;
    }

    public SyncCommand(SyncRequest sr) {
        this.sourceId = sr.getSourceId();

        this.insert = sr.isInsert();
        this.update = sr.isUpdate();
        this.delete = sr.isDelete();

        this.syncData = sr.getSyncData();
        this.syncMapping = sr.getSyncMapping();

        this.syncRequestSerialized = null;
        this.stationId = ChangeLogEntry.STATION_ID;
    }

    @Override
    public void execute() {
        if (getLog().isDebugEnabled()) {
            String xml = new String(syncRequestSerialized,VeriniceCharset.CHARSET_UTF_8);
            getLog().debug("### Importing data begin ###");
            getLog().debug(xml);
            getLog().debug("### Importing data end ####");
        }

        if (syncRequestSerialized != null) {
            SyncRequest sr = JAXB.unmarshal(new ByteArrayInputStream(syncRequestSerialized), SyncRequest.class);
            sourceId = sr.getSourceId();
            syncData = sr.getSyncData();
            syncMapping = sr.getSyncMapping();
        } else if (syncData == null || syncMapping == null) {
            throw new IllegalStateException("Command serialized but " + SyncRequest.class.getName() + " not provided pre-serialized. Check constructor usage!");
        }

        SyncInsertUpdateCommand cmdInsertUpdate = new SyncInsertUpdateCommand(
        		sourceId, 
        		syncData, 
        		syncMapping,
        		getAuthService().getUsername(),
        		insert, 
        		update, 
        		errors);

        try {
            cmdInsertUpdate = getCommandService().executeCommand(cmdInsertUpdate);
        } catch (CommandException e) {
            errors.add("Insert/Update failed.");
            return;
        }

        importRootObject = new HashSet<CnATreeElement>(cmdInsertUpdate.getContainerMap().values());
        elementSet = cmdInsertUpdate.getElementSet();

        inserted += cmdInsertUpdate.getInserted();
        updated += cmdInsertUpdate.getUpdated();

        if (delete) {
            SyncDeleteCommand cmdDelete = new SyncDeleteCommand(sourceId, syncData, errors);

            try {
                cmdDelete = getCommandService().executeCommand(cmdDelete);
            } catch (CommandException e) {
                errors.add("Delete failed.");
                return;
            }

            deleted += cmdDelete.getDeleted();
        }

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

}
