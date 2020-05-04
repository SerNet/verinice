package ITBP2VNA.generated.implementationhint;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the implementationhint package.
 * <p>
 * An ObjectFactory allows you to programatically construct new instances of the
 * Java representation for XML content. The Java representation of XML content
 * can consist of schema derived interfaces and classes representing the binding
 * of schema type definitions, element declarations and model groups. Factory
 * methods for each of these are provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private static final QName _Identifier_QNAME = new QName("", "identifier");
    private static final QName _ShortHand_QNAME = new QName("", "shortHand");
    private static final QName _Integrity_QNAME = new QName("", "integrity");
    private static final QName _Role_QNAME = new QName("", "role");
    private static final QName _Confidentiality_QNAME = new QName("", "confidentiality");
    private static final QName _Availability_QNAME = new QName("", "availability");
    private static final QName _Title_QNAME = new QName("", "title");

    /**
     * Create a new ObjectFactory that can be used to create new instances of
     * schema derived classes for package: implementationhint
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
     * Create an instance of {@link Document.Safeguards }
     * 
     */
    public Document.Safeguards createDocumentSafeguards() {
        return new Document.Safeguards();
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
     * Create an instance of {@link implementationhint.Description }
     * 
     */
    public ITBP2VNA.generated.implementationhint.Description createDescription() {
        return new ITBP2VNA.generated.implementationhint.Description();
    }

    /**
     * Create an instance of {@link BibItem }
     * 
     */
    public BibItem createBibItem() {
        return new BibItem();
    }

    /**
     * Create an instance of {@link Cia }
     * 
     */
    public Cia createCia() {
        return new Cia();
    }

    /**
     * Create an instance of {@link ResponsibleRoles }
     * 
     */
    public ResponsibleRoles createResponsibleRoles() {
        return new ResponsibleRoles();
    }

    /**
     * Create an instance of {@link Safeguard }
     * 
     */
    public Safeguard createSafeguard() {
        return new Safeguard();
    }

    /**
     * Create an instance of {@link Document.Safeguards.BasicSafeguards }
     * 
     */
    public Document.Safeguards.BasicSafeguards createDocumentSafeguardsBasicSafeguards() {
        return new Document.Safeguards.BasicSafeguards();
    }

    /**
     * Create an instance of {@link Document.Safeguards.StandardSafeguards }
     * 
     */
    public Document.Safeguards.StandardSafeguards createDocumentSafeguardsStandardSafeguards() {
        return new Document.Safeguards.StandardSafeguards();
    }

    /**
     * Create an instance of {@link Document.Safeguards.HighLevelSafeguards }
     * 
     */
    public Document.Safeguards.HighLevelSafeguards createDocumentSafeguardsHighLevelSafeguards() {
        return new Document.Safeguards.HighLevelSafeguards();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String
     * }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "identifier")
    public JAXBElement<String> createIdentifier(String value) {
        return new JAXBElement<>(_Identifier_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String
     * }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "shortHand")
    public JAXBElement<String> createShortHand(String value) {
        return new JAXBElement<>(_ShortHand_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String
     * }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "integrity")
    public JAXBElement<String> createIntegrity(String value) {
        return new JAXBElement<>(_Integrity_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String
     * }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "role")
    public JAXBElement<String> createRole(String value) {
        return new JAXBElement<>(_Role_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String
     * }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "confidentiality")
    public JAXBElement<String> createConfidentiality(String value) {
        return new JAXBElement<>(_Confidentiality_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String
     * }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "availability")
    public JAXBElement<String> createAvailability(String value) {
        return new JAXBElement<>(_Availability_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String
     * }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "title")
    public JAXBElement<String> createTitle(String value) {
        return new JAXBElement<>(_Title_QNAME, String.class, null, value);
    }

}
