//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2017.07.28 um 04:46:44 PM CEST 
//


package ITBP2VNA.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für documentType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="documentType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="fullTitle" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="identifier" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="title" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="lastChange" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="lastCheck" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="draftVersion" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="headerIcon" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="description" type="{}descriptionType"/>
 *         &lt;element name="threatScenario" type="{}threatScenarioType"/>
 *         &lt;element name="requirements" type="{}requirementsType"/>
 *         &lt;element name="advancedInformationText" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="bibliography" type="{}bibliographyType"/>
 *         &lt;element name="elementalThreats" type="{}elementalThreatsType"/>
 *         &lt;element name="crossreferences" type="{}crossreferencesType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "documentType", propOrder = {
    "fullTitle",
    "identifier",
    "title",
    "lastChange",
    "lastCheck",
    "draftVersion",
    "headerIcon",
    "description",
    "threatScenario",
    "requirements",
    "advancedInformationText",
    "bibliography",
    "elementalThreats",
    "crossreferences"
})
public class DocumentType {

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
    protected DescriptionType description;
    @XmlElement(required = true)
    protected ThreatScenarioType threatScenario;
    @XmlElement(required = true)
    protected RequirementsType requirements;
    @XmlElement(required = true)
    protected String advancedInformationText;
    @XmlElement(required = true)
    protected BibliographyType bibliography;
    @XmlElement(required = true)
    protected ElementalThreatsType elementalThreats;
    @XmlElement(required = true)
    protected CrossreferencesType crossreferences;

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
     *     {@link DescriptionType }
     *     
     */
    public DescriptionType getDescription() {
        return description;
    }

    /**
     * Legt den Wert der description-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link DescriptionType }
     *     
     */
    public void setDescription(DescriptionType value) {
        this.description = value;
    }

    /**
     * Ruft den Wert der threatScenario-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ThreatScenarioType }
     *     
     */
    public ThreatScenarioType getThreatScenario() {
        return threatScenario;
    }

    /**
     * Legt den Wert der threatScenario-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ThreatScenarioType }
     *     
     */
    public void setThreatScenario(ThreatScenarioType value) {
        this.threatScenario = value;
    }

    /**
     * Ruft den Wert der requirements-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link RequirementsType }
     *     
     */
    public RequirementsType getRequirements() {
        return requirements;
    }

    /**
     * Legt den Wert der requirements-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link RequirementsType }
     *     
     */
    public void setRequirements(RequirementsType value) {
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
     *     {@link BibliographyType }
     *     
     */
    public BibliographyType getBibliography() {
        return bibliography;
    }

    /**
     * Legt den Wert der bibliography-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BibliographyType }
     *     
     */
    public void setBibliography(BibliographyType value) {
        this.bibliography = value;
    }

    /**
     * Ruft den Wert der elementalThreats-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ElementalThreatsType }
     *     
     */
    public ElementalThreatsType getElementalThreats() {
        return elementalThreats;
    }

    /**
     * Legt den Wert der elementalThreats-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ElementalThreatsType }
     *     
     */
    public void setElementalThreats(ElementalThreatsType value) {
        this.elementalThreats = value;
    }

    /**
     * Ruft den Wert der crossreferences-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link CrossreferencesType }
     *     
     */
    public CrossreferencesType getCrossreferences() {
        return crossreferences;
    }

    /**
     * Legt den Wert der crossreferences-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CrossreferencesType }
     *     
     */
    public void setCrossreferences(CrossreferencesType value) {
        this.crossreferences = value;
    }

}
