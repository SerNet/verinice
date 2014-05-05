
package de.sernet.service.vna;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="message" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/>
 *         &lt;element name="inserted" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="updated" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="deleted" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "message",
    "inserted",
    "updated",
    "deleted"
})
@XmlRootElement(name = "response")
public class Response {

    @XmlElement(required = true)
    protected List<String> message;
    protected int inserted;
    protected int updated;
    protected int deleted;

    /**
     * Gets the value of the message property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the message property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMessage().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getMessage() {
        if (message == null) {
            message = new ArrayList<String>();
        }
        return this.message;
    }

    /**
     * Gets the value of the inserted property.
     * 
     */
    public int getInserted() {
        return inserted;
    }

    /**
     * Sets the value of the inserted property.
     * 
     */
    public void setInserted(int value) {
        this.inserted = value;
    }

    /**
     * Gets the value of the updated property.
     * 
     */
    public int getUpdated() {
        return updated;
    }

    /**
     * Sets the value of the updated property.
     * 
     */
    public void setUpdated(int value) {
        this.updated = value;
    }

    /**
     * Gets the value of the deleted property.
     * 
     */
    public int getDeleted() {
        return deleted;
    }

    /**
     * Sets the value of the deleted property.
     * 
     */
    public void setDeleted(int value) {
        this.deleted = value;
    }

}
