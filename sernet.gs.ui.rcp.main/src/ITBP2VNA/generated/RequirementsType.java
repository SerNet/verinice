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
 * <p>Java-Klasse für requirementsType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="requirementsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="description" type="{}descriptionType"/>
 *         &lt;element name="mainResponsibleRole" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="furtherResponsibleRoles" type="{}furtherResponsibleRolesType"/>
 *         &lt;element name="basicRequirements" type="{}basicRequirementsType"/>
 *         &lt;element name="standardRequirements" type="{}standardRequirementsType"/>
 *         &lt;element name="highLevelRequirements" type="{}highLevelRequirementsType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "requirementsType", propOrder = {
    "description",
    "mainResponsibleRole",
    "furtherResponsibleRoles",
    "basicRequirements",
    "standardRequirements",
    "highLevelRequirements"
})
public class RequirementsType {

    @XmlElement(required = true)
    protected DescriptionType description;
    @XmlElement(required = true)
    protected String mainResponsibleRole;
    @XmlElement(required = true)
    protected FurtherResponsibleRolesType furtherResponsibleRoles;
    @XmlElement(required = true)
    protected BasicRequirementsType basicRequirements;
    @XmlElement(required = true)
    protected StandardRequirementsType standardRequirements;
    @XmlElement(required = true)
    protected HighLevelRequirementsType highLevelRequirements;

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
     *     {@link FurtherResponsibleRolesType }
     *     
     */
    public FurtherResponsibleRolesType getFurtherResponsibleRoles() {
        return furtherResponsibleRoles;
    }

    /**
     * Legt den Wert der furtherResponsibleRoles-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link FurtherResponsibleRolesType }
     *     
     */
    public void setFurtherResponsibleRoles(FurtherResponsibleRolesType value) {
        this.furtherResponsibleRoles = value;
    }

    /**
     * Ruft den Wert der basicRequirements-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BasicRequirementsType }
     *     
     */
    public BasicRequirementsType getBasicRequirements() {
        return basicRequirements;
    }

    /**
     * Legt den Wert der basicRequirements-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BasicRequirementsType }
     *     
     */
    public void setBasicRequirements(BasicRequirementsType value) {
        this.basicRequirements = value;
    }

    /**
     * Ruft den Wert der standardRequirements-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link StandardRequirementsType }
     *     
     */
    public StandardRequirementsType getStandardRequirements() {
        return standardRequirements;
    }

    /**
     * Legt den Wert der standardRequirements-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link StandardRequirementsType }
     *     
     */
    public void setStandardRequirements(StandardRequirementsType value) {
        this.standardRequirements = value;
    }

    /**
     * Ruft den Wert der highLevelRequirements-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link HighLevelRequirementsType }
     *     
     */
    public HighLevelRequirementsType getHighLevelRequirements() {
        return highLevelRequirements;
    }

    /**
     * Legt den Wert der highLevelRequirements-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link HighLevelRequirementsType }
     *     
     */
    public void setHighLevelRequirements(HighLevelRequirementsType value) {
        this.highLevelRequirements = value;
    }

}
