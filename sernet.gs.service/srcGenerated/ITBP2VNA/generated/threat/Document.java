package ITBP2VNA.generated.threat;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

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
    protected Document.Cia cia;
    @XmlAnyElement(lax = true)
    protected List<Object> description;

    public String getFullTitle() {
        return fullTitle;
    }

    public void setFullTitle(String value) {
        this.fullTitle = value;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String value) {
        this.identifier = value;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String value) {
        this.title = value;
    }

    public Document.Cia getCia() {
        return cia;
    }

    public void setCia(Document.Cia value) {
        this.cia = value;
    }

    public List<Object> getDescription() {
        return description;
    }

    public void setDescription(List<Object> value) {
        this.description = value;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {

    })
    public static class Cia {

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

}
