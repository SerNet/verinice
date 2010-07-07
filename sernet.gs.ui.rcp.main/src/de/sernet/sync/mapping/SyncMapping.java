
package de.sernet.sync.mapping;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipInputStream;

import javax.xml.transform.Source;

import org.jdom.Element;
import org.jdom.Namespace;

import de.sernet.sync.sync.XmlParser;



public class SyncMapping {

    protected List<SyncMapping.MapObjectType> mapObjectType;

    /**
     * Gets the value of the mapObjectType property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the mapObjectType property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMapObjectType().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SyncMapping.MapObjectType }
     * 
     * 
     */
    public List<SyncMapping.MapObjectType> getMapObjectType() {
        if (mapObjectType == null) {
            mapObjectType = new ArrayList<SyncMapping.MapObjectType>();
        }
        return this.mapObjectType;
    }

   
    public static class MapObjectType {

        protected List<SyncMapping.MapObjectType.MapAttributeType> mapAttributeType;
        protected String extId;
        protected String intId;

        /**
         * Gets the value of the mapAttributeType property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the mapAttributeType property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getMapAttributeType().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link SyncMapping.MapObjectType.MapAttributeType }
         * 
         * 
         */
        public List<SyncMapping.MapObjectType.MapAttributeType> getMapAttributeType() {
            if (mapAttributeType == null) {
                mapAttributeType = new ArrayList<SyncMapping.MapObjectType.MapAttributeType>();
            }
            return this.mapAttributeType;
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
         * Gets the value of the intId property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getIntId() {
            return intId;
        }

        /**
         * Sets the value of the intId property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setIntId(String value) {
            this.intId = value;
        }
        
    	@SuppressWarnings("unchecked")
		public void fill(Element mapObjectType){
        	setExtId(mapObjectType.getAttributeValue("extId"));
        	setIntId(mapObjectType.getAttributeValue("intId"));
        	
        	List<SyncMapping.MapObjectType.MapAttributeType> mapAttributeList= this.getMapAttributeType();
        	
        	Namespace ns = mapObjectType.getNamespace();
        	List<Element> mapAttributeListe = mapObjectType.getChildren("mapAttributeType", ns);
        	
        	for(int i = 0; i < mapAttributeListe.size(); i++){
        		MapAttributeType mat = new MapAttributeType();
        		mat.fill(mapAttributeListe.get(i));
        		mapAttributeList.add(mat);
        	}
        }
    	
    	
        public static class MapAttributeType {

           
            protected String extId;
            protected String intId;

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
             * Gets the value of the intId property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getIntId() {
                return intId;
            }

            /**
             * Sets the value of the intId property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setIntId(String value) {
                this.intId = value;
            }
            
            public void fill(Element mapAttributType){
            	this.setExtId(mapAttributType.getAttributeValue("extId"));
            	this.setIntId(mapAttributType.getAttributeValue("intId"));
            
            	
            }

        }
    }
    
    // Methode um syncMapping mit der mapping.xml zu fuellen
    
    public void fill(/*ZipInputStream*/ File file){
    	List<SyncMapping.MapObjectType> syncMappingList = this.getMapObjectType();
    	List<Element> xmlListe = XmlParser.parseMapping(file);
    	
    	for(int i = 0; i < xmlListe.size(); i++){
    		MapObjectType mo = new MapObjectType();
    		mo.fill(xmlListe.get(i));
    		syncMappingList.add(mo);
    	}
    }
}
