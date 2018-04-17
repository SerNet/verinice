//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2017.09.11 um 03:35:00 PM CEST 
//


package ITBP2VNA.generated.module;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


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
 *         &lt;element name="lastChange" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="lastCheck" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="draftVersion" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="headerIcon" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="description">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;all>
 *                   &lt;element name="introduction" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="purpose" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="differentiation" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                 &lt;/all>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="threatScenario">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;all>
 *                   &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="specificThreats">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element ref="{}specificThreat" maxOccurs="unbounded"/>
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
 *         &lt;element name="requirements">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;all>
 *                   &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="mainResponsibleRole" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="furtherResponsibleRoles" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="role" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="basicRequirements">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element ref="{}requirement" maxOccurs="unbounded" minOccurs="0"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="standardRequirements">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element ref="{}requirement" maxOccurs="unbounded" minOccurs="0"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="highLevelRequirements">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element ref="{}requirement" maxOccurs="unbounded" minOccurs="0"/>
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
 *         &lt;element name="elementalThreats">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="elementalThreat" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="crossreferences">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element ref="{}requirement-ref" maxOccurs="unbounded" minOccurs="0"/>
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
    protected String lastChange;
    @XmlElement(required = true)
    protected String lastCheck;
    @XmlElement(required = true)
    protected String draftVersion;
    @XmlElement(required = true)
    protected String headerIcon;
    @XmlElement(required = true)
    protected Document.Description description;
    @XmlElement(required = true)
    protected Document.ThreatScenario threatScenario;
    @XmlElement(required = true)
    protected Document.Requirements requirements;
    @XmlElement(required = true)
    protected String advancedInformationText;
    protected Document.Bibliography bibliography;
    @XmlElement(required = true)
    protected Document.ElementalThreats elementalThreats;
    @XmlElement(required = true)
    protected Document.Crossreferences crossreferences;

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
     *     {@link String }
     *     
     */
    public String getLastChange() {
        return lastChange;
    }

    /**
     * Legt den Wert der lastChange-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLastChange(String value) {
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
     * Ruft den Wert der threatScenario-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Document.ThreatScenario }
     *     
     */
    public Document.ThreatScenario getThreatScenario() {
        return threatScenario;
    }

    /**
     * Legt den Wert der threatScenario-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Document.ThreatScenario }
     *     
     */
    public void setThreatScenario(Document.ThreatScenario value) {
        this.threatScenario = value;
    }

    /**
     * Ruft den Wert der requirements-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Document.Requirements }
     *     
     */
    public Document.Requirements getRequirements() {
        return requirements;
    }

    /**
     * Legt den Wert der requirements-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Document.Requirements }
     *     
     */
    public void setRequirements(Document.Requirements value) {
        this.requirements = value;
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
     * Ruft den Wert der elementalThreats-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Document.ElementalThreats }
     *     
     */
    public Document.ElementalThreats getElementalThreats() {
        return elementalThreats;
    }

    /**
     * Legt den Wert der elementalThreats-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Document.ElementalThreats }
     *     
     */
    public void setElementalThreats(Document.ElementalThreats value) {
        this.elementalThreats = value;
    }

    /**
     * Ruft den Wert der crossreferences-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Document.Crossreferences }
     *     
     */
    public Document.Crossreferences getCrossreferences() {
        return crossreferences;
    }

    /**
     * Legt den Wert der crossreferences-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Document.Crossreferences }
     *     
     */
    public void setCrossreferences(Document.Crossreferences value) {
        this.crossreferences = value;
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
     *         &lt;element ref="{}requirement-ref" maxOccurs="unbounded" minOccurs="0"/>
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
        "requirementRef"
    })
    public static class Crossreferences {

        @XmlElement(name = "requirement-ref")
        protected List<RequirementRef> requirementRef;

        /**
         * Gets the value of the requirementRef property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the requirementRef property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getRequirementRef().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link RequirementRef }
         * 
         * 
         */
        public List<RequirementRef> getRequirementRef() {
            if (requirementRef == null) {
                requirementRef = new ArrayList<RequirementRef>();
            }
            return this.requirementRef;
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
     *         &lt;element name="introduction" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="purpose" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="differentiation" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
    public static class Description {

        @XmlAnyElement(lax = true)
        protected List<Object> introduction;
        @XmlAnyElement(lax = true)
        protected List<Object> purpose;
        @XmlAnyElement(lax = true)
        protected List<Object> differentiation;

        /**
         * Ruft den Wert der introduction-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public List<Object> getIntroduction() {
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
        public void setIntroduction(List<Object> value) {
            this.introduction = value;
        }

        /**
         * Ruft den Wert der purpose-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public List<Object> getPurpose() {
            return purpose;
        }

        /**
         * Legt den Wert der purpose-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setPurpose(List<Object>  value) {
            this.purpose = value;
        }

        /**
         * Ruft den Wert der differentiation-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public List<Object> getDifferentiation() {
            return differentiation;
        }

        /**
         * Legt den Wert der differentiation-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setDifferentiation(List<Object> value) {
            this.differentiation = value;
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
     *         &lt;element name="elementalThreat" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
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
        "elementalThreat"
    })
    public static class ElementalThreats {

        protected List<String> elementalThreat;

        /**
         * Gets the value of the elementalThreat property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the elementalThreat property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getElementalThreat().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * 
         * 
         */
        public List<String> getElementalThreat() {
            if (elementalThreat == null) {
                elementalThreat = new ArrayList<String>();
            }
            return this.elementalThreat;
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
     *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="mainResponsibleRole" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="furtherResponsibleRoles" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="role" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="basicRequirements">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element ref="{}requirement" maxOccurs="unbounded" minOccurs="0"/>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="standardRequirements">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element ref="{}requirement" maxOccurs="unbounded" minOccurs="0"/>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="highLevelRequirements">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element ref="{}requirement" maxOccurs="unbounded" minOccurs="0"/>
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
    public static class Requirements {

        protected String description;
        @XmlElement(required = true)
        protected String mainResponsibleRole;
        protected Document.Requirements.FurtherResponsibleRoles furtherResponsibleRoles;
        @XmlElement(required = true)
        protected Document.Requirements.BasicRequirements basicRequirements;
        @XmlElement(required = true)
        protected Document.Requirements.StandardRequirements standardRequirements;
        @XmlElement(required = true)
        protected Document.Requirements.HighLevelRequirements highLevelRequirements;

        /**
         * Ruft den Wert der description-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getDescription() {
            return description;
        }

        /**
         * Legt den Wert der description-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setDescription(String value) {
            this.description = value;
        }

        /**
         * Ruft den Wert der mainResponsibleRole-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getMainResponsibleRole() {
            return mainResponsibleRole;
        }

        /**
         * Legt den Wert der mainResponsibleRole-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setMainResponsibleRole(String value) {
            this.mainResponsibleRole = value;
        }

        /**
         * Ruft den Wert der furtherResponsibleRoles-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link Document.Requirements.FurtherResponsibleRoles }
         *     
         */
        public Document.Requirements.FurtherResponsibleRoles getFurtherResponsibleRoles() {
            return furtherResponsibleRoles;
        }

        /**
         * Legt den Wert der furtherResponsibleRoles-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link Document.Requirements.FurtherResponsibleRoles }
         *     
         */
        public void setFurtherResponsibleRoles(Document.Requirements.FurtherResponsibleRoles value) {
            this.furtherResponsibleRoles = value;
        }

        /**
         * Ruft den Wert der basicRequirements-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link Document.Requirements.BasicRequirements }
         *     
         */
        public Document.Requirements.BasicRequirements getBasicRequirements() {
            return basicRequirements;
        }

        /**
         * Legt den Wert der basicRequirements-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link Document.Requirements.BasicRequirements }
         *     
         */
        public void setBasicRequirements(Document.Requirements.BasicRequirements value) {
            this.basicRequirements = value;
        }

        /**
         * Ruft den Wert der standardRequirements-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link Document.Requirements.StandardRequirements }
         *     
         */
        public Document.Requirements.StandardRequirements getStandardRequirements() {
            return standardRequirements;
        }

        /**
         * Legt den Wert der standardRequirements-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link Document.Requirements.StandardRequirements }
         *     
         */
        public void setStandardRequirements(Document.Requirements.StandardRequirements value) {
            this.standardRequirements = value;
        }

        /**
         * Ruft den Wert der highLevelRequirements-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link Document.Requirements.HighLevelRequirements }
         *     
         */
        public Document.Requirements.HighLevelRequirements getHighLevelRequirements() {
            return highLevelRequirements;
        }

        /**
         * Legt den Wert der highLevelRequirements-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link Document.Requirements.HighLevelRequirements }
         *     
         */
        public void setHighLevelRequirements(Document.Requirements.HighLevelRequirements value) {
            this.highLevelRequirements = value;
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
         *         &lt;element ref="{}requirement" maxOccurs="unbounded" minOccurs="0"/>
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
            "requirement"
        })
        public static class BasicRequirements {

            protected List<Requirement> requirement;

            /**
             * Gets the value of the requirement property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the requirement property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getRequirement().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link Requirement }
             * 
             * 
             */
            public List<Requirement> getRequirement() {
                if (requirement == null) {
                    requirement = new ArrayList<Requirement>();
                }
                return this.requirement;
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
         *         &lt;element name="role" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
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
            "role"
        })
        public static class FurtherResponsibleRoles {

            protected List<String> role;

            /**
             * Gets the value of the role property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the role property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getRole().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link String }
             * 
             * 
             */
            public List<String> getRole() {
                if (role == null) {
                    role = new ArrayList<String>();
                }
                return this.role;
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
         *         &lt;element ref="{}requirement" maxOccurs="unbounded" minOccurs="0"/>
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
            "requirement"
        })
        public static class HighLevelRequirements {

            protected List<Requirement> requirement;

            /**
             * Gets the value of the requirement property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the requirement property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getRequirement().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link Requirement }
             * 
             * 
             */
            public List<Requirement> getRequirement() {
                if (requirement == null) {
                    requirement = new ArrayList<Requirement>();
                }
                return this.requirement;
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
         *         &lt;element ref="{}requirement" maxOccurs="unbounded" minOccurs="0"/>
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
            "requirement"
        })
        public static class StandardRequirements {

            protected List<Requirement> requirement;

            /**
             * Gets the value of the requirement property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the requirement property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getRequirement().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link Requirement }
             * 
             * 
             */
            public List<Requirement> getRequirement() {
                if (requirement == null) {
                    requirement = new ArrayList<Requirement>();
                }
                return this.requirement;
            }

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
     *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="specificThreats">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element ref="{}specificThreat" maxOccurs="unbounded"/>
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
    public static class ThreatScenario {

        protected ITBP2VNA.generated.module.Description description;

        @XmlElement(required = true)
        protected Document.ThreatScenario.SpecificThreats specificThreats;

        public ITBP2VNA.generated.module.Description getDescription() {
            return description;
        }

        /**
         * Legt den Wert der description-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setDescription(ITBP2VNA.generated.module.Description value) {
            this.description = value;
        }

        /**
         * Ruft den Wert der specificThreats-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link Document.ThreatScenario.SpecificThreats }
         *     
         */
        public Document.ThreatScenario.SpecificThreats getSpecificThreats() {
            return specificThreats;
        }

        /**
         * Legt den Wert der specificThreats-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link Document.ThreatScenario.SpecificThreats }
         *     
         */
        public void setSpecificThreats(Document.ThreatScenario.SpecificThreats value) {
            this.specificThreats = value;
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
         *         &lt;element ref="{}specificThreat" maxOccurs="unbounded"/>
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
            "specificThreat"
        })
        public static class SpecificThreats {

            @XmlElement(required = true)
            protected List<SpecificThreat> specificThreat;

            /**
             * Gets the value of the specificThreat property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the specificThreat property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getSpecificThreat().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link SpecificThreat }
             * 
             * 
             */
            public List<SpecificThreat> getSpecificThreat() {
                if (specificThreat == null) {
                    specificThreat = new ArrayList<SpecificThreat>();
                }
                return this.specificThreat;
            }

        }

    }

}
