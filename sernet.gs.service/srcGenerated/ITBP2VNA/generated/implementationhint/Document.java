//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2017.09.11 um 03:36:26 PM CEST 
//


package ITBP2VNA.generated.implementationhint;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java-Klasse für anonymous complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;all>
 *         &lt;element name="fullTitle" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="identifier" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="title" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="lastChange" type="{http://www.w3.org/2001/XMLSchema}date"/>
 *         &lt;element name="lastCheck" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="draftVersion" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="headerIcon" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="description">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="introduction" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="lifecycle" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="safeguards">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;all>
 *                   &lt;element name="basicSafeguards">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element ref="{}safeguard" maxOccurs="unbounded" minOccurs="0"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="standardSafeguards">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element ref="{}safeguard" maxOccurs="unbounded" minOccurs="0"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="highLevelSafeguards">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element ref="{}safeguard" maxOccurs="unbounded" minOccurs="0"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/all>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="advancedInformationText" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="bibliography" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element ref="{}bibItem" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/all>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {

})
@XmlRootElement(name = "document")
public class Document {

    @XmlElement(required = true)
    protected String fullTitle;
    @XmlElement(required = true)
    protected String identifier;
    @XmlElement(required = true)
    protected String title;
    @XmlElement(required = true)
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar lastChange;
    @XmlElement(required = true)
    protected String lastCheck;
    @XmlElement(required = true)
    protected String draftVersion;
    @XmlElement(required = true)
    protected String headerIcon;
    @XmlElement(required = true)
    protected Document.Description description;
    @XmlElement(required = true)
    protected Document.Safeguards safeguards;
    @XmlElement(required = true)
    protected String advancedInformationText;
    protected Document.Bibliography bibliography;

    /**
     * Ruft den Wert der fullTitle-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFullTitle() {
        return fullTitle;
    }

    /**
     * Legt den Wert der fullTitle-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFullTitle(String value) {
        this.fullTitle = value;
    }

    /**
     * Ruft den Wert der identifier-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Legt den Wert der identifier-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdentifier(String value) {
        this.identifier = value;
    }

    /**
     * Ruft den Wert der title-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTitle() {
        return title;
    }

    /**
     * Legt den Wert der title-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTitle(String value) {
        this.title = value;
    }

    /**
     * Ruft den Wert der lastChange-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getLastChange() {
        return lastChange;
    }

    /**
     * Legt den Wert der lastChange-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setLastChange(XMLGregorianCalendar value) {
        this.lastChange = value;
    }

    /**
     * Ruft den Wert der lastCheck-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLastCheck() {
        return lastCheck;
    }

    /**
     * Legt den Wert der lastCheck-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLastCheck(String value) {
        this.lastCheck = value;
    }

    /**
     * Ruft den Wert der draftVersion-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDraftVersion() {
        return draftVersion;
    }

    /**
     * Legt den Wert der draftVersion-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDraftVersion(String value) {
        this.draftVersion = value;
    }

    /**
     * Ruft den Wert der headerIcon-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHeaderIcon() {
        return headerIcon;
    }

    /**
     * Legt den Wert der headerIcon-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHeaderIcon(String value) {
        this.headerIcon = value;
    }

    /**
     * Ruft den Wert der description-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Document.Description }
     *     
     */
    public Document.Description getDescription() {
        return description;
    }

    /**
     * Legt den Wert der description-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Document.Description }
     *     
     */
    public void setDescription(Document.Description value) {
        this.description = value;
    }

    /**
     * Ruft den Wert der safeguards-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Document.Safeguards }
     *     
     */
    public Document.Safeguards getSafeguards() {
        return safeguards;
    }

    /**
     * Legt den Wert der safeguards-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Document.Safeguards }
     *     
     */
    public void setSafeguards(Document.Safeguards value) {
        this.safeguards = value;
    }

    /**
     * Ruft den Wert der advancedInformationText-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAdvancedInformationText() {
        return advancedInformationText;
    }

    /**
     * Legt den Wert der advancedInformationText-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAdvancedInformationText(String value) {
        this.advancedInformationText = value;
    }

    /**
     * Ruft den Wert der bibliography-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Document.Bibliography }
     *     
     */
    public Document.Bibliography getBibliography() {
        return bibliography;
    }

    /**
     * Legt den Wert der bibliography-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Document.Bibliography }
     *     
     */
    public void setBibliography(Document.Bibliography value) {
        this.bibliography = value;
    }


    /**
     * <p>Java-Klasse für anonymous complex type.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element ref="{}bibItem" maxOccurs="unbounded" minOccurs="0"/>
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
        "bibItem"
    })
    public static class Bibliography {

        protected List<BibItem> bibItem;

        /**
         * Gets the value of the bibItem property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the bibItem property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getBibItem().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link BibItem }
         * 
         * 
         */
        public List<BibItem> getBibItem() {
            if (bibItem == null) {
                bibItem = new ArrayList<BibItem>();
            }
            return this.bibItem;
        }

    }


    /**
     * <p>Java-Klasse für anonymous complex type.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="introduction" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="lifecycle" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
        "introduction",
        "lifecycle"
    })
    public static class Description {

        @XmlElement(required = true)
        protected String introduction;
        @XmlElement(required = true)
        protected String lifecycle;

        /**
         * Ruft den Wert der introduction-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getIntroduction() {
            return introduction;
        }

        /**
         * Legt den Wert der introduction-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setIntroduction(String value) {
            this.introduction = value;
        }

        /**
         * Ruft den Wert der lifecycle-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getLifecycle() {
            return lifecycle;
        }

        /**
         * Legt den Wert der lifecycle-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setLifecycle(String value) {
            this.lifecycle = value;
        }

    }


    /**
     * <p>Java-Klasse für anonymous complex type.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;all>
     *         &lt;element name="basicSafeguards">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element ref="{}safeguard" maxOccurs="unbounded" minOccurs="0"/>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="standardSafeguards">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element ref="{}safeguard" maxOccurs="unbounded" minOccurs="0"/>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="highLevelSafeguards">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element ref="{}safeguard" maxOccurs="unbounded" minOccurs="0"/>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/all>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {

    })
    public static class Safeguards {

        @XmlElement(required = true)
        protected Document.Safeguards.BasicSafeguards basicSafeguards;
        @XmlElement(required = true)
        protected Document.Safeguards.StandardSafeguards standardSafeguards;
        @XmlElement(required = true)
        protected Document.Safeguards.HighLevelSafeguards highLevelSafeguards;

        /**
         * Ruft den Wert der basicSafeguards-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link Document.Safeguards.BasicSafeguards }
         *     
         */
        public Document.Safeguards.BasicSafeguards getBasicSafeguards() {
            return basicSafeguards;
        }

        /**
         * Legt den Wert der basicSafeguards-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link Document.Safeguards.BasicSafeguards }
         *     
         */
        public void setBasicSafeguards(Document.Safeguards.BasicSafeguards value) {
            this.basicSafeguards = value;
        }

        /**
         * Ruft den Wert der standardSafeguards-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link Document.Safeguards.StandardSafeguards }
         *     
         */
        public Document.Safeguards.StandardSafeguards getStandardSafeguards() {
            return standardSafeguards;
        }

        /**
         * Legt den Wert der standardSafeguards-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link Document.Safeguards.StandardSafeguards }
         *     
         */
        public void setStandardSafeguards(Document.Safeguards.StandardSafeguards value) {
            this.standardSafeguards = value;
        }

        /**
         * Ruft den Wert der highLevelSafeguards-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link Document.Safeguards.HighLevelSafeguards }
         *     
         */
        public Document.Safeguards.HighLevelSafeguards getHighLevelSafeguards() {
            return highLevelSafeguards;
        }

        /**
         * Legt den Wert der highLevelSafeguards-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link Document.Safeguards.HighLevelSafeguards }
         *     
         */
        public void setHighLevelSafeguards(Document.Safeguards.HighLevelSafeguards value) {
            this.highLevelSafeguards = value;
        }


        /**
         * <p>Java-Klasse für anonymous complex type.
         * 
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       &lt;sequence>
         *         &lt;element ref="{}safeguard" maxOccurs="unbounded" minOccurs="0"/>
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
            "safeguard"
        })
        public static class BasicSafeguards {

            protected List<Safeguard> safeguard;

            /**
             * Gets the value of the safeguard property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the safeguard property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getSafeguard().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link Safeguard }
             * 
             * 
             */
            public List<Safeguard> getSafeguard() {
                if (safeguard == null) {
                    safeguard = new ArrayList<Safeguard>();
                }
                return this.safeguard;
            }

        }


        /**
         * <p>Java-Klasse für anonymous complex type.
         * 
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       &lt;sequence>
         *         &lt;element ref="{}safeguard" maxOccurs="unbounded" minOccurs="0"/>
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
            "safeguard"
        })
        public static class HighLevelSafeguards {

            protected List<Safeguard> safeguard;

            /**
             * Gets the value of the safeguard property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the safeguard property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getSafeguard().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link Safeguard }
             * 
             * 
             */
            public List<Safeguard> getSafeguard() {
                if (safeguard == null) {
                    safeguard = new ArrayList<Safeguard>();
                }
                return this.safeguard;
            }

        }


        /**
         * <p>Java-Klasse für anonymous complex type.
         * 
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       &lt;sequence>
         *         &lt;element ref="{}safeguard" maxOccurs="unbounded" minOccurs="0"/>
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
            "safeguard"
        })
        public static class StandardSafeguards {

            protected List<Safeguard> safeguard;

            /**
             * Gets the value of the safeguard property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the safeguard property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getSafeguard().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link Safeguard }
             * 
             * 
             */
            public List<Safeguard> getSafeguard() {
                if (safeguard == null) {
                    safeguard = new ArrayList<Safeguard>();
                }
                return this.safeguard;
            }

        }

    }

}
