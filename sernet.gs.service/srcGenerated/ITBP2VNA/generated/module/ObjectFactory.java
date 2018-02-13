//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2017.09.11 um 03:35:00 PM CEST 
//


package ITBP2VNA.generated.module;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the module package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Identifier_QNAME = new QName("", "identifier");
    private final static QName _Integrity_QNAME = new QName("", "integrity");
    private final static QName _ShortHand_QNAME = new QName("", "shortHand");
    private final static QName _Role_QNAME = new QName("", "role");
    private final static QName _Confidentiality_QNAME = new QName("", "confidentiality");
    private final static QName _Availability_QNAME = new QName("", "availability");
    private final static QName _Title_QNAME = new QName("", "title");
    private final static QName _Headline_QNAME = new QName("", "headline");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: module
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Document }
     * 
     */
    public Document createDocument() {
        return new Document();
    }

    /**
     * Create an instance of {@link Document.Requirements }
     * 
     */
    public Document.Requirements createDocumentRequirements() {
        return new Document.Requirements();
    }

    /**
     * Create an instance of {@link Document.ThreatScenario }
     * 
     */
    public Document.ThreatScenario createDocumentThreatScenario() {
        return new Document.ThreatScenario();
    }

    /**
     * Create an instance of {@link SpecificThreat }
     * 
     */
    public SpecificThreat createSpecificThreat() {
        return new SpecificThreat();
    }

    /**
     * Create an instance of {@link module.Description }
     * 
     */
    public ITBP2VNA.generated.module.Description createDescription() {
        return new ITBP2VNA.generated.module.Description();
    }

    /**
     * Create an instance of {@link Document.Description }
     * 
     */
    public Document.Description createDocumentDescription() {
        return new Document.Description();
    }

    /**
     * Create an instance of {@link Document.Bibliography }
     * 
     */
    public Document.Bibliography createDocumentBibliography() {
        return new Document.Bibliography();
    }

    /**
     * Create an instance of {@link Document.ElementalThreats }
     * 
     */
    public Document.ElementalThreats createDocumentElementalThreats() {
        return new Document.ElementalThreats();
    }

    /**
     * Create an instance of {@link Document.Crossreferences }
     * 
     */
    public Document.Crossreferences createDocumentCrossreferences() {
        return new Document.Crossreferences();
    }

    /**
     * Create an instance of {@link RequirementRef }
     * 
     */
    public RequirementRef createRequirementRef() {
        return new RequirementRef();
    }

    /**
     * Create an instance of {@link ElementalthreatRef }
     * 
     */
    public ElementalthreatRef createElementalthreatRef() {
        return new ElementalthreatRef();
    }

    /**
     * Create an instance of {@link Requirement }
     * 
     */
    public Requirement createRequirement() {
        return new Requirement();
    }

    /**
     * Create an instance of {@link ResponsibleRoles }
     * 
     */
    public ResponsibleRoles createResponsibleRoles() {
        return new ResponsibleRoles();
    }

    /**
     * Create an instance of {@link Cia }
     * 
     */
    public Cia createCia() {
        return new Cia();
    }

    /**
     * Create an instance of {@link BibItem }
     * 
     */
    public BibItem createBibItem() {
        return new BibItem();
    }

    /**
     * Create an instance of {@link Document.Requirements.FurtherResponsibleRoles }
     * 
     */
    public Document.Requirements.FurtherResponsibleRoles createDocumentRequirementsFurtherResponsibleRoles() {
        return new Document.Requirements.FurtherResponsibleRoles();
    }

    /**
     * Create an instance of {@link Document.Requirements.BasicRequirements }
     * 
     */
    public Document.Requirements.BasicRequirements createDocumentRequirementsBasicRequirements() {
        return new Document.Requirements.BasicRequirements();
    }

    /**
     * Create an instance of {@link Document.Requirements.StandardRequirements }
     * 
     */
    public Document.Requirements.StandardRequirements createDocumentRequirementsStandardRequirements() {
        return new Document.Requirements.StandardRequirements();
    }

    /**
     * Create an instance of {@link Document.Requirements.HighLevelRequirements }
     * 
     */
    public Document.Requirements.HighLevelRequirements createDocumentRequirementsHighLevelRequirements() {
        return new Document.Requirements.HighLevelRequirements();
    }

    /**
     * Create an instance of {@link Document.ThreatScenario.SpecificThreats }
     * 
     */
    public Document.ThreatScenario.SpecificThreats createDocumentThreatScenarioSpecificThreats() {
        return new Document.ThreatScenario.SpecificThreats();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "identifier")
    public JAXBElement<String> createIdentifier(String value) {
        return new JAXBElement<String>(_Identifier_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "integrity")
    public JAXBElement<String> createIntegrity(String value) {
        return new JAXBElement<String>(_Integrity_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "shortHand")
    public JAXBElement<String> createShortHand(String value) {
        return new JAXBElement<String>(_ShortHand_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "role")
    public JAXBElement<String> createRole(String value) {
        return new JAXBElement<String>(_Role_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "confidentiality")
    public JAXBElement<String> createConfidentiality(String value) {
        return new JAXBElement<String>(_Confidentiality_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "availability")
    public JAXBElement<String> createAvailability(String value) {
        return new JAXBElement<String>(_Availability_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "title")
    public JAXBElement<String> createTitle(String value) {
        return new JAXBElement<String>(_Title_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "headline")
    public JAXBElement<String> createHeadline(String value) {
        return new JAXBElement<String>(_Headline_QNAME, String.class, null, value);
    }

}
