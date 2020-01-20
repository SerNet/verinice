package ITBP2VNA.generated.module;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "elementalthreat-ref")
public class ElementalthreatRef {

    @XmlAttribute(name = "identifier", required = true)
    protected String identifier;
    @XmlAttribute(name = "isReferenced", required = true)
    protected String isReferenced;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String value) {
        this.identifier = value;
    }

    public String getIsReferenced() {
        return isReferenced;
    }

    public void setIsReferenced(String value) {
        this.isReferenced = value;
    }

}
