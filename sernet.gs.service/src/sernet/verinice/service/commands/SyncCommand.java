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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXB;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import sernet.gs.service.RuntimeCommandException;
import sernet.gs.service.TimeFormatter;
import sernet.gs.service.VeriniceCharset;
import sernet.verinice.interfaces.ChangeLoggingCommand;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IAuthAwareCommand;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.IChangeLoggingCommand;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.sync.IVeriniceArchive;
import sernet.verinice.service.sync.PureXml;
import sernet.verinice.service.sync.VeriniceArchive;
import sernet.verinice.service.sync.VnaSchemaVersion;
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
    
    private String path;
    
    private byte[] fileData;

    private transient IAuthService authService;

    private String stationId;

    private SyncParameter parameter;

    private int inserted, potentiallyUpdated, deleted;

    private List<String> errors = new ArrayList<String>();

    private Set<CnATreeElement> importRootObject;

    private Set<CnATreeElement> elementSet = null;
    
    private transient IVeriniceArchive veriniceArchive = null;

    private Status status = Status.OK;

    private Exception errorCause;

    public enum Status {OK, FAILED};

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
        this.fileData = (syncRequestSerialized!=null) ? syncRequestSerialized.clone() : null;
        this.stationId = ChangeLogEntry.STATION_ID;
    }
    
    public SyncCommand(SyncParameter parameter, String path) {
        this.parameter = parameter;
        this.path = path;
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
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        JAXB.marshal(sr, bos);

        this.fileData = bos.toByteArray();
        this.stationId = ChangeLogEntry.STATION_ID;
    }

    /**
     * Called by soap web-service
     * 
     * @param sr SyncRequest
     * @throws SyncParameterException 
     */
    public SyncCommand(SyncRequest sr) throws SyncParameterException {
        PureXml pureXml = new PureXml();
        pureXml.setSyncRequest(sr);
        veriniceArchive = pureXml;
        this.veriniceArchive.setSourceId(sr.getSourceId());
        this.veriniceArchive.setSyncData(sr.getSyncData());
        this.veriniceArchive.setSyncMapping(sr.getSyncMapping());
        this.stationId = ChangeLogEntry.STATION_ID;
        this.parameter = new SyncParameter(sr.isInsert(), sr.isUpdate(), sr.isDelete(), false,  SyncParameter.EXPORT_FORMAT_XML_PURE);
    }

    @Override
    public void execute() {
        try {
            long start = getStartTimestamp();
            
            if(path!=null && fileData==null) {
                fileData =  FileUtils.readFileToByteArray(new File(path));
            }
            
            if (veriniceArchive == null) {
                loadVeriniceArchive(fileData);
                fileData = null;
            }
            
            VnaSchemaVersion vnaSchemaVersion = getCommandService().getVnaSchemaVersion();

            if (!veriniceArchive.isCompatible(vnaSchemaVersion)){
                status = Status.FAILED;
                errorCause = veriniceArchive.getErrorCause();
                return;
            }
                        
            doInsertAndUpdate();
            doDelete();
            
            logRuntime(start);
        } catch (RuntimeException e) {
            status = Status.FAILED;
            errorCause = e;
            log.error("Error while importing", e);
            errors.add("Insert/Update failed.");
            throw e;
        } catch (Exception e) {
            status = Status.FAILED;
            errorCause = e;
            log.error("Error while importing", e);
            errors.add("Insert/Update failed.");
            throw new RuntimeCommandException(e);
        }
        finally {
            clear();
        }
    }
  

    private void logRuntime(long start) {
        if (getLog().isInfoEnabled()) {
            long time = System.currentTimeMillis() - start;
            getLog().info("Runtime: " + TimeFormatter.getHumanRedableTime(time));
        }
    }

    private void doInsertAndUpdate() throws CommandException, IOException {
        SyncInsertUpdateCommand cmdInsertUpdate = new SyncInsertUpdateCommand(veriniceArchive.getSourceId(), veriniceArchive.getSyncData(), veriniceArchive.getSyncMapping(), getAuthService().getUsername(), parameter, errors);           
        cmdInsertUpdate.setRisk(veriniceArchive.getSyncRiskAnalysis());
        cmdInsertUpdate.setTempDirName(veriniceArchive.getTempDirName());
        cmdInsertUpdate = getCommandService().executeCommand(cmdInsertUpdate);
        if (isVeriniceArchive()) {
            cmdInsertUpdate.importFileData(veriniceArchive);
        }
        
        importRootObject = new HashSet<CnATreeElement>(cmdInsertUpdate.getContainerMap().values());
        elementSet = cmdInsertUpdate.getElementSet();
   
        inserted += cmdInsertUpdate.getInserted();
        potentiallyUpdated += cmdInsertUpdate.getUpdated();
    }
    
    private void doDelete() throws CommandException {
        if (parameter.isDelete()) {
            SyncDeleteCommand cmdDelete = new SyncDeleteCommand(veriniceArchive.getSourceId(), veriniceArchive.getSyncData(), errors);
            cmdDelete = getCommandService().executeCommand(cmdDelete);
            deleted += cmdDelete.getDeleted();
        }
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.GenericCommand#clear()
     */
    @Override
    public void clear() {
        super.clear();
        if(veriniceArchive!=null) {
            veriniceArchive.clear();
        }
    }

    private long getStartTimestamp() {
        long start = 0;
        if (getLog().isInfoEnabled()) {
            start = System.currentTimeMillis();
        }
        return start;
    }

    private void loadVeriniceArchive(byte[] syncRequestSerialized) {
        byte[] request = (syncRequestSerialized!=null) ? syncRequestSerialized.clone() : null;
        if(isVeriniceArchive()) {
            veriniceArchive = new VeriniceArchive(request);
        }
        if(SyncParameter.EXPORT_FORMAT_XML_PURE.equals(parameter.getFormat())) {
            veriniceArchive = new PureXml(request);
        }
        logXml();
    }

    public int getInserted() {
        return inserted;
    }

    public int getPotentiallyUpdated() {
        return potentiallyUpdated;
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
     * @see sernet.verinice.interfaces.IChangeLoggingCommand#getChangeType()
     */
    @Override
    public int getChangeType() {
        return ChangeLogEntry.TYPE_INSERT;
    }

    /*
     * @see sernet.verinice.interfaces.IChangeLoggingCommand#getChangedElements()
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
     * @see sernet.verinice.interfaces.IChangeLoggingCommand#getStationId()
     */
    @Override
    public String getStationId() {
        return stationId;
    }

    /*
     * @see sernet.gs.ui.rcp.main.service.commands.IAuthAwareCommand#getAuthService()
     */
    @Override
    public IAuthService getAuthService() {
        return authService;
    }

    /*
     * @see  sernet.gs.ui.rcp.main.service.commands.IAuthAwareCommand#setAuthService
     * (sernet.gs.ui.rcp.main.service.IAuthService)
     */
    @Override
    public void setAuthService(IAuthService service) {
        this.authService = service;
    }
    
    private boolean isVeriniceArchive() {
        return SyncParameter.EXPORT_FORMAT_VERINICE_ARCHIV.equals(parameter.getFormat());
    }
    
    private void logXml() {
        if (getLog().isDebugEnabled()) {
            if( veriniceArchive.getVeriniceXml()!=null) {          
                String xml = new String(veriniceArchive.getVeriniceXml(),VeriniceCharset.CHARSET_UTF_8);
                getLog().debug("### Importing data begin ###");
                getLog().debug(xml);
                getLog().debug("### Importing data end ####");
            }
        }
    }

    /**
     * If import is aborted or an exception occurred this is set to failed.
     *
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Encapsulates exception.
     *
     * @return Returns null if {@link #getStatus()} returns OK.
     */
    public Exception getErrorCause() {
        return errorCause;
    }
}
