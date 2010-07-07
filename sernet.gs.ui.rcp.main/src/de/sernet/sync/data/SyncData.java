package de.sernet.sync.data;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipInputStream;

import javax.xml.transform.Source;

import org.jdom.Element;
import org.jdom.Namespace;

import de.sernet.sync.sync.XmlParser;



public class SyncData {
	
    protected List<SyncData.SyncObject> syncObject;

    /**
     * Gets the value of the syncObject property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the syncObject property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSyncObject().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SyncData.SyncObject }
     * 
     * 
     */
    public List<SyncData.SyncObject> getSyncObject() {
        if (syncObject == null) {
            syncObject = new ArrayList<SyncData.SyncObject>();
        }
        return this.syncObject;
    }


    // SyncObject ---------------------------------------
    public static class SyncObject {

        protected List<SyncData.SyncObject.SyncAttribute> syncAttribute;
        protected String extId;
        protected String extObjectType;

        /**
         * Gets the value of the syncAttribute property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the syncAttribute property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getSyncAttribute().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link SyncData.SyncObject.SyncAttribute }
         * 
         * 
         */
        public List<SyncData.SyncObject.SyncAttribute> getSyncAttribute() {
            if (syncAttribute == null) {
                syncAttribute = new ArrayList<SyncData.SyncObject.SyncAttribute>();
            }
            return this.syncAttribute;
        }

        /**
         * Gets the value of the extId property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getExtId() {
            return extId;
        }

        /**
         * Sets the value of the extId property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setExtId(String value) {
            this.extId = value;
        }

        /**
         * Gets the value of the extObjectType property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getExtObjectType() {
            return extObjectType;
        }

        /**
         * Sets the value of the extObjectType property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setExtObjectType(String value) {
            this.extObjectType = value;
        }
        
        @SuppressWarnings("unchecked")
		public void fill(Element syncObject){
        	setExtId(syncObject.getAttributeValue("extId"));
        	setExtObjectType(syncObject.getAttributeValue("extObjectType"));
        	
        	List<SyncData.SyncObject.SyncAttribute> syncObjectList = this.getSyncAttribute();
        	
        	Namespace ns = syncObject.getNamespace();
        	List<Element> syncAttributeListe = syncObject.getChildren("syncAttribute", ns);
        	
        	for(int i = 0; i < syncAttributeListe.size(); i++){
        		SyncAttribute sa = new SyncAttribute();
        		sa.fill(syncAttributeListe.get(i));
        		syncObjectList.add(sa);
        	}
        }


        // Attribute ---------------------------------------
        public static class SyncAttribute {
            protected String name;
            protected String value;

            /**
             * Gets the value of the name property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getName() {
                return name;
            }

            /**
             * Sets the value of the name property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setName(String value) {
                this.name = value;
            }

            /**
             * Gets the value of the value property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getValue() {
                return value;
            }

            /**
             * Sets the value of the value property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setValue(String value) {
                this.value = value;
            }
            
         
            public void fill(Element syncAttribut){
            	this.setName(syncAttribut.getAttributeValue("name"));
            	this.setValue(syncAttribut.getAttributeValue("value"));
            }

        }

    }
    
    // Methode um syncData mit der data.xml zu fuellen
    public void fill(/*ZipInputStream*/ File file){
    	List<SyncData.SyncObject> syncObjectList = this.getSyncObject();
    	List<Element> xmlListe = XmlParser.parseData(file);
    	
    	for(int i = 0; i < xmlListe.size(); i++){
    		SyncObject so = new SyncObject();
    		so.fill(xmlListe.get(i));
    		syncObjectList.add(so);
    	}
    }
}
