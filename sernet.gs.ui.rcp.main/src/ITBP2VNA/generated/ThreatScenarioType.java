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
 * <p>Java-Klasse für threatScenarioType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="threatScenarioType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="description" type="{}descriptionType"/>
 *         &lt;element name="specificThreats" type="{}specificThreatsType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "threatScenarioType", propOrder = {
    "description",
    "specificThreats"
})
public class ThreatScenarioType {

    @XmlElement(required = true)
    protected DescriptionType description;
    @XmlElement(required = true)
    protected SpecificThreatsType specificThreats;

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
     * Ruft den Wert der specificThreats-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link SpecificThreatsType }
     *     
     */
    public SpecificThreatsType getSpecificThreats() {
        return specificThreats;
    }

    /**
     * Legt den Wert der specificThreats-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link SpecificThreatsType }
     *     
     */
    public void setSpecificThreats(SpecificThreatsType value) {
        this.specificThreats = value;
    }

}
