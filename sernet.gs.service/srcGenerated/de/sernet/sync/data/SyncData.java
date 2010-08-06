
package de.sernet.sync.data;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="syncObject" maxOccurs="unbounded">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="syncAttribute" maxOccurs="unbounded">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                           &lt;attribute name="value" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *                 &lt;attribute name="extId" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="extObjectType" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "syncObject"
})
@XmlRootElement(name = "syncData")
public class SyncData {

    @XmlElement(required = true)
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


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="syncAttribute" maxOccurs="unbounded">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *                 &lt;attribute name="value" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/sequence>
     *       &lt;attribute name="extId" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="extObjectType" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "syncAttribute"
    })
    public static class SyncObject {

        @XmlElement(required = true)
        protected List<SyncData.SyncObject.SyncAttribute> syncAttribute;
        @XmlAttribute(name = "extId", required = true)
        protected String extId;
        @XmlAttribute(name = "extObjectType", required = true)
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


        /**
         * <p>Java class for anonymous complex type.
         * 
         * <p>The following schema fragment specifies the expected content contained within this class.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
         *       &lt;attribute name="value" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class SyncAttribute {

            @XmlAttribute(name = "name", required = true)
            protected String name;
            @XmlAttribute(name = "value", required = true)
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

        }

    }

}
