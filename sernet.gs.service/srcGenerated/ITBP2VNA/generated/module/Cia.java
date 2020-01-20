package ITBP2VNA.generated.module;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {

})
@XmlRootElement(name = "cia")
public class Cia {

    @XmlElement(required = true)
    protected String confidentiality;
    @XmlElement(required = true)
    protected String integrity;
    @XmlElement(required = true)
    protected String availability;

    public String getConfidentiality() {
        return confidentiality;
    }

    public void setConfidentiality(String value) {
        this.confidentiality = value;
    }

    public String getIntegrity() {
        return integrity;
    }

    public void setIntegrity(String value) {
        this.integrity = value;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String value) {
        this.availability = value;
    }

}
