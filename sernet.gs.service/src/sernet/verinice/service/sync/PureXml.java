/*******************************************************************************
 * Copyright (c) 2013 Daniel Murygin.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.sync;

import java.io.ByteArrayInputStream;

import javax.xml.bind.JAXB;

import de.sernet.sync.data.SyncData;
import de.sernet.sync.mapping.SyncMapping;
import de.sernet.sync.sync.SyncRequest;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class PureXml implements IVeriniceArchive {

    private byte[] veriniceXml;
    
    private String sourceId;
    
    private SyncData syncData;
    
    private SyncMapping syncMapping;
    
    public PureXml(byte[] veriniceXml) {
        super();
        setVeriniceXml(veriniceXml);
    }

    public PureXml() {
        super();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.service.sync.IPureXml#getVeriniceXml()
     */
    @Override
    public byte[] getVeriniceXml() {
        return veriniceXml;
    }

    public void setVeriniceXml(byte[] veriniceXml) {
        this.veriniceXml = (veriniceXml!=null) ? veriniceXml.clone() : null;
    }
    
    @Override
    public String getSourceId() {
        if(sourceId==null) {
            unmarshal();
        }
        return sourceId;
    }

    @Override
    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    @Override
    public SyncData getSyncData() {
        if(syncData==null) {
            unmarshal();
        }
        return syncData;
    }

    @Override
    public void setSyncData(SyncData syncData) {
        this.syncData = syncData;
    }
    
    @Override
    public SyncMapping getSyncMapping() {
        if(syncMapping==null) {
            unmarshal();
        }
        return syncMapping;
    }
    
    @Override
    public void setSyncMapping(SyncMapping syncMapping) {
        this.syncMapping = syncMapping;
    }

    private void unmarshal() {
        SyncRequest sr = JAXB.unmarshal(new ByteArrayInputStream(getVeriniceXml()), SyncRequest.class);
        sourceId = sr.getSourceId();
        syncData = sr.getSyncData();
        syncMapping = sr.getSyncMapping();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.service.sync.IVeriniceArchive#getFileData(java.lang.String)
     */
    @Override
    public byte[] getFileData(String fileName) {
        return null;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.service.sync.IVeriniceArchive#clear()
     */
    @Override
    public void clear() {
        // TODO Auto-generated method stub
        
    }

}
