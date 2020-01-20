package ITBP2VNA.generated.module;

import java.util.ArrayList;
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
    protected String lastChange;
    @XmlElement(required = true)
    protected String lastCheck;
    @XmlElement(required = true)
    protected String draftVersion;
    @XmlElement(required = true)
    protected String headerIcon;
    @XmlElement(required = true)
    protected Document.Description description;
    @XmlElement(required = true)
    protected Document.ThreatScenario threatScenario;
    @XmlElement(required = true)
    protected Document.Requirements requirements;
    @XmlElement(required = true)
    protected String advancedInformationText;
    protected Document.Bibliography bibliography;
    @XmlElement(required = true)
    protected Document.ElementalThreats elementalThreats;
    @XmlElement(required = true)
    protected Document.Crossreferences crossreferences;

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

    public String getLastChange() {
        return lastChange;
    }

    public void setLastChange(String value) {
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

    public Document.ThreatScenario getThreatScenario() {
        return threatScenario;
    }

    public void setThreatScenario(Document.ThreatScenario value) {
        this.threatScenario = value;
    }

    public Document.Requirements getRequirements() {
        return requirements;
    }

    public void setRequirements(Document.Requirements value) {
        this.requirements = value;
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

    public Document.ElementalThreats getElementalThreats() {
        return elementalThreats;
    }

    public void setElementalThreats(Document.ElementalThreats value) {
        this.elementalThreats = value;
    }

    public Document.Crossreferences getCrossreferences() {
        return crossreferences;
    }

    public void setCrossreferences(Document.Crossreferences value) {
        this.crossreferences = value;
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
    @XmlType(name = "", propOrder = { "requirementRef" })
    public static class Crossreferences {

        @XmlElement(name = "requirement-ref")
        protected List<RequirementRef> requirementRef;

        /**
         * Gets the value of the requirementRef property.
         * 
         * <p>
         * This accessor method returns a reference to the live list, not a
         * snapshot. Therefore any modification you make to the returned list
         * will be present inside the JAXB object. This is why there is not a
         * <CODE>set</CODE> method for the requirementRef property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * 
         * <pre>
         * getRequirementRef().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link RequirementRef }
         * 
         * 
         */
        public List<RequirementRef> getRequirementRef() {
            if (requirementRef == null) {
                requirementRef = new ArrayList<>();
            }
            return this.requirementRef;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {

    })
    public static class Description {

        @XmlAnyElement(lax = true)
        protected List<Object> introduction;
        @XmlAnyElement(lax = true)
        protected List<Object> purpose;
        @XmlAnyElement(lax = true)
        protected List<Object> differentiation;

        public List<Object> getIntroduction() {
            return introduction;
        }

        public void setIntroduction(List<Object> value) {
            this.introduction = value;
        }

        public List<Object> getPurpose() {
            return purpose;
        }

        public void setPurpose(List<Object> value) {
            this.purpose = value;
        }

        public List<Object> getDifferentiation() {
            return differentiation;
        }

        public void setDifferentiation(List<Object> value) {
            this.differentiation = value;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = { "elementalThreat" })
    public static class ElementalThreats {

        protected List<String> elementalThreat;

        /**
         * Gets the value of the elementalThreat property.
         * 
         * <p>
         * This accessor method returns a reference to the live list, not a
         * snapshot. Therefore any modification you make to the returned list
         * will be present inside the JAXB object. This is why there is not a
         * <CODE>set</CODE> method for the elementalThreat property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * 
         * <pre>
         * getElementalThreat().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * 
         * 
         */
        public List<String> getElementalThreat() {
            if (elementalThreat == null) {
                elementalThreat = new ArrayList<>();
            }
            return this.elementalThreat;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {

    })
    public static class Requirements {

        protected String description;
        @XmlElement(required = true)
        protected String mainResponsibleRole;
        protected Document.Requirements.FurtherResponsibleRoles furtherResponsibleRoles;
        @XmlElement(required = true)
        protected Document.Requirements.BasicRequirements basicRequirements;
        @XmlElement(required = true)
        protected Document.Requirements.StandardRequirements standardRequirements;
        @XmlElement(required = true)
        protected Document.Requirements.HighLevelRequirements highLevelRequirements;

        public String getDescription() {
            return description;
        }

        public void setDescription(String value) {
            this.description = value;
        }

        public String getMainResponsibleRole() {
            return mainResponsibleRole;
        }

        public void setMainResponsibleRole(String value) {
            this.mainResponsibleRole = value;
        }

        public Document.Requirements.FurtherResponsibleRoles getFurtherResponsibleRoles() {
            return furtherResponsibleRoles;
        }

        public void setFurtherResponsibleRoles(
                Document.Requirements.FurtherResponsibleRoles value) {
            this.furtherResponsibleRoles = value;
        }

        public Document.Requirements.BasicRequirements getBasicRequirements() {
            return basicRequirements;
        }

        public void setBasicRequirements(Document.Requirements.BasicRequirements value) {
            this.basicRequirements = value;
        }

        public Document.Requirements.StandardRequirements getStandardRequirements() {
            return standardRequirements;
        }

        public void setStandardRequirements(Document.Requirements.StandardRequirements value) {
            this.standardRequirements = value;
        }

        public Document.Requirements.HighLevelRequirements getHighLevelRequirements() {
            return highLevelRequirements;
        }

        public void setHighLevelRequirements(Document.Requirements.HighLevelRequirements value) {
            this.highLevelRequirements = value;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = { "requirement" })
        public static class BasicRequirements {

            protected List<Requirement> requirement;

            /**
             * Gets the value of the requirement property.
             * 
             * <p>
             * This accessor method returns a reference to the live list, not a
             * snapshot. Therefore any modification you make to the returned
             * list will be present inside the JAXB object. This is why there is
             * not a <CODE>set</CODE> method for the requirement property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * 
             * <pre>
             * getRequirement().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link Requirement }
             * 
             * 
             */
            public List<Requirement> getRequirement() {
                if (requirement == null) {
                    requirement = new ArrayList<>();
                }
                return this.requirement;
            }

        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = { "role" })
        public static class FurtherResponsibleRoles {

            protected List<String> role;

            /**
             * Gets the value of the role property.
             * 
             * <p>
             * This accessor method returns a reference to the live list, not a
             * snapshot. Therefore any modification you make to the returned
             * list will be present inside the JAXB object. This is why there is
             * not a <CODE>set</CODE> method for the role property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * 
             * <pre>
             * getRole().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link String }
             * 
             * 
             */
            public List<String> getRole() {
                if (role == null) {
                    role = new ArrayList<>();
                }
                return this.role;
            }

        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = { "requirement" })
        public static class HighLevelRequirements {

            protected List<Requirement> requirement;

            /**
             * Gets the value of the requirement property.
             * 
             * <p>
             * This accessor method returns a reference to the live list, not a
             * snapshot. Therefore any modification you make to the returned
             * list will be present inside the JAXB object. This is why there is
             * not a <CODE>set</CODE> method for the requirement property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * 
             * <pre>
             * getRequirement().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link Requirement }
             * 
             * 
             */
            public List<Requirement> getRequirement() {
                if (requirement == null) {
                    requirement = new ArrayList<>();
                }
                return this.requirement;
            }

        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = { "requirement" })
        public static class StandardRequirements {

            protected List<Requirement> requirement;

            /**
             * Gets the value of the requirement property.
             * 
             * <p>
             * This accessor method returns a reference to the live list, not a
             * snapshot. Therefore any modification you make to the returned
             * list will be present inside the JAXB object. This is why there is
             * not a <CODE>set</CODE> method for the requirement property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * 
             * <pre>
             * getRequirement().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link Requirement }
             * 
             * 
             */
            public List<Requirement> getRequirement() {
                if (requirement == null) {
                    requirement = new ArrayList<>();
                }
                return this.requirement;
            }

        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {

    })
    public static class ThreatScenario {

        protected ITBP2VNA.generated.module.Description description;

        @XmlElement(required = true)
        protected Document.ThreatScenario.SpecificThreats specificThreats;

        public ITBP2VNA.generated.module.Description getDescription() {
            return description;
        }

        public void setDescription(ITBP2VNA.generated.module.Description value) {
            this.description = value;
        }

        public Document.ThreatScenario.SpecificThreats getSpecificThreats() {
            return specificThreats;
        }

        public void setSpecificThreats(Document.ThreatScenario.SpecificThreats value) {
            this.specificThreats = value;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = { "specificThreat" })
        public static class SpecificThreats {

            @XmlElement(required = true)
            protected List<SpecificThreat> specificThreat;

            /**
             * Gets the value of the specificThreat property.
             * 
             * <p>
             * This accessor method returns a reference to the live list, not a
             * snapshot. Therefore any modification you make to the returned
             * list will be present inside the JAXB object. This is why there is
             * not a <CODE>set</CODE> method for the specificThreat property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * 
             * <pre>
             * getSpecificThreat().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link SpecificThreat }
             * 
             * 
             */
            public List<SpecificThreat> getSpecificThreat() {
                if (specificThreat == null) {
                    specificThreat = new ArrayList<>();
                }
                return this.specificThreat;
            }

        }

    }

}
