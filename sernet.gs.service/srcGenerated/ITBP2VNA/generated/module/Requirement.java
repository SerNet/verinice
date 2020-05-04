package ITBP2VNA.generated.module;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {

})
@XmlRootElement(name = "requirement")
public class Requirement {

    protected String identifier;
    protected String title;
    protected ResponsibleRoles responsibleRoles;
    protected Cia cia;
    protected Description description;

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

    public ResponsibleRoles getResponsibleRoles() {
        return responsibleRoles;
    }

    public void setResponsibleRoles(ResponsibleRoles value) {
        this.responsibleRoles = value;
    }

    public Cia getCia() {
        return cia;
    }

    public void setCia(Cia value) {
        this.cia = value;
    }

    public Description getDescription() {
        return description;
    }

    public void setDescription(Description value) {
        this.description = value;
    }

}
