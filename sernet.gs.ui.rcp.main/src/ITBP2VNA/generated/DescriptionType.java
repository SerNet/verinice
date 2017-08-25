//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2017.07.28 um 04:46:44 PM CEST 
//


package ITBP2VNA.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für descriptionType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="descriptionType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="introduction" type="{}introductionType" minOccurs="0"/>
 *         &lt;element name="purpose" type="{}purposeType" minOccurs="0"/>
 *         &lt;element name="differentiation" type="{}differentiationType" minOccurs="0"/>
 *         &lt;element name="p" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "descriptionType", propOrder = {
    "introduction",
    "purpose",
    "differentiation",
    "p"
})
public class DescriptionType {

    protected IntroductionType introduction;
    protected PurposeType purpose;
    protected DifferentiationType differentiation;
    protected String p;

    /**
     * Ruft den Wert der introduction-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link IntroductionType }
     *     
     */
    public IntroductionType getIntroduction() {
        return introduction;
    }

    /**
     * Legt den Wert der introduction-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link IntroductionType }
     *     
     */
    public void setIntroduction(IntroductionType value) {
        this.introduction = value;
    }

    /**
     * Ruft den Wert der purpose-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link PurposeType }
     *     
     */
    public PurposeType getPurpose() {
        return purpose;
    }

    /**
     * Legt den Wert der purpose-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link PurposeType }
     *     
     */
    public void setPurpose(PurposeType value) {
        this.purpose = value;
    }

    /**
     * Ruft den Wert der differentiation-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link DifferentiationType }
     *     
     */
    public DifferentiationType getDifferentiation() {
        return differentiation;
    }

    /**
     * Legt den Wert der differentiation-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link DifferentiationType }
     *     
     */
    public void setDifferentiation(DifferentiationType value) {
        this.differentiation = value;
    }

    /**
     * Ruft den Wert der p-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getP() {
        return p;
    }

    /**
     * Legt den Wert der p-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setP(String value) {
        this.p = value;
    }

}
