package ITBP2VNA.generated.implementationhint;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {

})
@XmlRootElement(name = "bibItem")
public class BibItem {

    protected String shortHand;
    protected String title;
    protected Description description;

    public String getShortHand() {
        return shortHand;
    }

    public void setShortHand(String value) {
        this.shortHand = value;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String value) {
        this.title = value;
    }

    public Description getDescription() {
        return description;
    }

    public void setDescription(Description value) {
        this.description = value;
    }

}
