package ITBP2VNA.generated.module;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "cia", "elementalthreatRef" })
@XmlRootElement(name = "requirement-ref")
public class RequirementRef {

    @XmlElement(name = "elementalthreat-ref")
    protected List<ElementalthreatRef> elementalthreatRef;
    @XmlAttribute(name = "identifier", required = true)
    protected String identifier;
    protected Cia cia;

    /**
     * Gets the value of the elementalthreatRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the elementalthreatRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getElementalthreatRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ElementalthreatRef }
     * 
     * 
     */
    public List<ElementalthreatRef> getElementalthreatRef() {
        if (elementalthreatRef == null) {
            elementalthreatRef = new ArrayList<>();
        }
        return this.elementalthreatRef;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String value) {
        this.identifier = value;
    }

    public Cia getCia() {
        return cia;
    }

    public void setCia(Cia value) {
        this.cia = value;
    }

}
