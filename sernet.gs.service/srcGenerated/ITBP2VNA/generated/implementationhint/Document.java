package ITBP2VNA.generated.implementationhint;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

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
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar lastChange;
    @XmlElement(required = true)
    protected String lastCheck;
    @XmlElement(required = true)
    protected String draftVersion;
    @XmlElement(required = true)
    protected String headerIcon;
    @XmlElement(required = true)
    protected Document.Description description;
    @XmlElement(required = true)
    protected Document.Safeguards safeguards;
    @XmlElement(required = true)
    protected String advancedInformationText;
    protected Document.Bibliography bibliography;

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

    public XMLGregorianCalendar getLastChange() {
        return lastChange;
    }

    public void setLastChange(XMLGregorianCalendar value) {
        this.lastChange = value;
    }

    public String getLastCheck() {
        return lastCheck;
    }

    public void setLastCheck(String value) {
        this.lastCheck = value;
    }

    public String getDraftVersion() {
        return draftVersion;
    }

    public void setDraftVersion(String value) {
        this.draftVersion = value;
    }

    public String getHeaderIcon() {
        return headerIcon;
    }

    public void setHeaderIcon(String value) {
        this.headerIcon = value;
    }

    public Document.Description getDescription() {
        return description;
    }

    public void setDescription(Document.Description value) {
        this.description = value;
    }

    public Document.Safeguards getSafeguards() {
        return safeguards;
    }

    public void setSafeguards(Document.Safeguards value) {
        this.safeguards = value;
    }

    public String getAdvancedInformationText() {
        return advancedInformationText;
    }

    public void setAdvancedInformationText(String value) {
        this.advancedInformationText = value;
    }

    public Document.Bibliography getBibliography() {
        return bibliography;
    }

    public void setBibliography(Document.Bibliography value) {
        this.bibliography = value;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = { "bibItem" })
    public static class Bibliography {

        protected List<BibItem> bibItem;

        /**
         * Gets the value of the bibItem property.
         * 
         * <p>
         * This accessor method returns a reference to the live list, not a
         * snapshot. Therefore any modification you make to the returned list
         * will be present inside the JAXB object. This is why there is not a
         * <CODE>set</CODE> method for the bibItem property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * 
         * <pre>
         * getBibItem().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link BibItem }
         * 
         * 
         */
        public List<BibItem> getBibItem() {
            if (bibItem == null) {
                bibItem = new ArrayList<>();
            }
            return this.bibItem;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = { "introduction", "lifecycle" })
    public static class Description {

        @XmlElement(required = true)
        protected String introduction;
        @XmlElement(required = true)
        protected String lifecycle;

        public String getIntroduction() {
            return introduction;
        }

        public void setIntroduction(String value) {
            this.introduction = value;
        }

        public String getLifecycle() {
            return lifecycle;
        }

        public void setLifecycle(String value) {
            this.lifecycle = value;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {

    })
    public static class Safeguards {

        @XmlElement(required = true)
        protected Document.Safeguards.BasicSafeguards basicSafeguards;
        @XmlElement(required = true)
        protected Document.Safeguards.StandardSafeguards standardSafeguards;
        @XmlElement(required = true)
        protected Document.Safeguards.HighLevelSafeguards highLevelSafeguards;

        public Document.Safeguards.BasicSafeguards getBasicSafeguards() {
            return basicSafeguards;
        }

        public void setBasicSafeguards(Document.Safeguards.BasicSafeguards value) {
            this.basicSafeguards = value;
        }

        public Document.Safeguards.StandardSafeguards getStandardSafeguards() {
            return standardSafeguards;
        }

        public void setStandardSafeguards(Document.Safeguards.StandardSafeguards value) {
            this.standardSafeguards = value;
        }

        public Document.Safeguards.HighLevelSafeguards getHighLevelSafeguards() {
            return highLevelSafeguards;
        }

        public void setHighLevelSafeguards(Document.Safeguards.HighLevelSafeguards value) {
            this.highLevelSafeguards = value;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = { "safeguard" })
        public static class BasicSafeguards {

            protected List<Safeguard> safeguard;

            /**
             * Gets the value of the safeguard property.
             * 
             * <p>
             * This accessor method returns a reference to the live list, not a
             * snapshot. Therefore any modification you make to the returned
             * list will be present inside the JAXB object. This is why there is
             * not a <CODE>set</CODE> method for the safeguard property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * 
             * <pre>
             * getSafeguard().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link Safeguard }
             * 
             * 
             */
            public List<Safeguard> getSafeguard() {
                if (safeguard == null) {
                    safeguard = new ArrayList<>();
                }
                return this.safeguard;
            }

        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = { "safeguard" })
        public static class HighLevelSafeguards {

            protected List<Safeguard> safeguard;

            /**
             * Gets the value of the safeguard property.
             * 
             * <p>
             * This accessor method returns a reference to the live list, not a
             * snapshot. Therefore any modification you make to the returned
             * list will be present inside the JAXB object. This is why there is
             * not a <CODE>set</CODE> method for the safeguard property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * 
             * <pre>
             * getSafeguard().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link Safeguard }
             * 
             * 
             */
            public List<Safeguard> getSafeguard() {
                if (safeguard == null) {
                    safeguard = new ArrayList<>();
                }
                return this.safeguard;
            }

        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = { "safeguard" })
        public static class StandardSafeguards {

            protected List<Safeguard> safeguard;

            /**
             * Gets the value of the safeguard property.
             * 
             * <p>
             * This accessor method returns a reference to the live list, not a
             * snapshot. Therefore any modification you make to the returned
             * list will be present inside the JAXB object. This is why there is
             * not a <CODE>set</CODE> method for the safeguard property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * 
             * <pre>
             * getSafeguard().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link Safeguard }
             * 
             * 
             */
            public List<Safeguard> getSafeguard() {
                if (safeguard == null) {
                    safeguard = new ArrayList<>();
                }
                return this.safeguard;
            }

        }

    }

}
