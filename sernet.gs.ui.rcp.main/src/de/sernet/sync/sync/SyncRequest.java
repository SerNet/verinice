
package de.sernet.sync.sync;

import java.io.File;
import java.util.zip.ZipInputStream;
import sernet.springclient.SpringClientPlugin;
import de.sernet.sync.data.SyncData;
import de.sernet.sync.mapping.SyncMapping;


@SuppressWarnings("serial")
public class SyncRequest /*implements java.io.Serializable*/ {

    protected SyncData syncData;
    protected SyncMapping syncMapping;
    protected String sourceId;
    protected Boolean insert;
    protected Boolean update;
    protected Boolean delete;
    
    
    public SyncRequest() {
	}
    
    public SyncRequest(String sourceId, boolean insert, boolean update, boolean delete, SyncData syncData, SyncMapping syncMapping) {
    	this.sourceId = sourceId;
    	this.insert = insert;
    	this.update = update;
    	this.delete = delete;
    	this.syncData = syncData;
    	this.syncMapping = syncMapping;
    }
    
    public SyncRequest(String sourceId, boolean insert, boolean update, boolean delete, /*ZipInputStream*/ File dataStream, /*ZipInputStream*/ File mappingStream) {
    	this.sourceId = sourceId;
    	this.insert = insert;
    	this.update = update;
    	this.delete = delete;
    	this.syncData = new SyncData();
    	this.syncData.fill(dataStream);
    	this.syncMapping = new SyncMapping();
    	this.syncMapping.fill(mappingStream);
    }

    /**
     * Gets the value of the syncData property.
     * 
     * @return
     *     possible object is
     *     {@link SyncData }
     *     
     */
    public SyncData getSyncData() {
        return syncData;
    }

    /**
     * Sets the value of the syncData property.
     * 
     * @param value
     *     allowed object is
     *     {@link SyncData }
     *     
     */
    public void setSyncData(SyncData value) {
        this.syncData = value;
    }

    /**
     * Gets the value of the syncMapping property.
     * 
     * @return
     *     possible object is
     *     {@link SyncMapping }
     *     
     */
    public SyncMapping getSyncMapping() {
        return syncMapping;
    }

    /**
     * Sets the value of the syncMapping property.
     * 
     * @param value
     *     allowed object is
     *     {@link SyncMapping }
     *     
     */
    public void setSyncMapping(SyncMapping value) {
        this.syncMapping = value;
    }

    /**
     * Gets the value of the sourceId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSourceId() {
        return sourceId;
    }

    /**
     * Sets the value of the sourceId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSourceId(String value) {
        this.sourceId = value;
    }

    /**
     * Gets the value of the insert property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isInsert() {
        if (insert == null) {
            return true;
        } else {
            return insert;
        }
    }

    /**
     * Sets the value of the insert property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setInsert(Boolean value) {
        this.insert = value;
    }

    /**
     * Gets the value of the update property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isUpdate() {
        if (update == null) {
            return true;
        } else {
            return update;
        }
    }

    /**
     * Sets the value of the update property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setUpdate(Boolean value) {
        this.update = value;
    }

    /**
     * Gets the value of the delete property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDelete() {
        if (delete == null) {
            return false;
        } else {
            return delete;
        }
    }

    /**
     * Sets the value of the delete property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDelete(Boolean value) {
        this.delete = value;
    }

}
