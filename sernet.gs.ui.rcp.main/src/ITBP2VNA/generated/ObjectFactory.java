//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2017.07.28 um 04:46:44 PM CEST 
//


package ITBP2VNA.generated;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the generated package. 
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

    private final static QName _Document_QNAME = new QName("", "document");
    private final static QName _BibItemTypeDescriptionP_QNAME = new QName("", "p");
    private final static QName _BibItemTypeDescriptionDifferentiation_QNAME = new QName("", "differentiation");
    private final static QName _BibItemTypeDescriptionPurpose_QNAME = new QName("", "purpose");
    private final static QName _BibItemTypeDescriptionIntroduction_QNAME = new QName("", "introduction");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: generated
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link BibItemType }
     * 
     */
    public BibItemType createBibItemType() {
        return new BibItemType();
    }

    /**
     * Create an instance of {@link DocumentType }
     * 
     */
    public DocumentType createDocumentType() {
        return new DocumentType();
    }

    /**
     * Create an instance of {@link CiaType }
     * 
     */
    public CiaType createCiaType() {
        return new CiaType();
    }

    /**
     * Create an instance of {@link FurtherResponsibleRolesType }
     * 
     */
    public FurtherResponsibleRolesType createFurtherResponsibleRolesType() {
        return new FurtherResponsibleRolesType();
    }

    /**
     * Create an instance of {@link EthreatType }
     * 
     */
    public EthreatType createEthreatType() {
        return new EthreatType();
    }

    /**
     * Create an instance of {@link StandardRequirementsType }
     * 
     */
    public StandardRequirementsType createStandardRequirementsType() {
        return new StandardRequirementsType();
    }

    /**
     * Create an instance of {@link CrossreferencesType }
     * 
     */
    public CrossreferencesType createCrossreferencesType() {
        return new CrossreferencesType();
    }

    /**
     * Create an instance of {@link HighLevelRequirementsType }
     * 
     */
    public HighLevelRequirementsType createHighLevelRequirementsType() {
        return new HighLevelRequirementsType();
    }

    /**
     * Create an instance of {@link SpecificThreatType }
     * 
     */
    public SpecificThreatType createSpecificThreatType() {
        return new SpecificThreatType();
    }

    /**
     * Create an instance of {@link DifferentiationType }
     * 
     */
    public DifferentiationType createDifferentiationType() {
        return new DifferentiationType();
    }

    /**
     * Create an instance of {@link RequirementType }
     * 
     */
    public RequirementType createRequirementType() {
        return new RequirementType();
    }

    /**
     * Create an instance of {@link ElementalThreatsType }
     * 
     */
    public ElementalThreatsType createElementalThreatsType() {
        return new ElementalThreatsType();
    }

    /**
     * Create an instance of {@link SpecificThreatsType }
     * 
     */
    public SpecificThreatsType createSpecificThreatsType() {
        return new SpecificThreatsType();
    }

    /**
     * Create an instance of {@link DescriptionType }
     * 
     */
    public DescriptionType createDescriptionType() {
        return new DescriptionType();
    }

    /**
     * Create an instance of {@link BibliographyType }
     * 
     */
    public BibliographyType createBibliographyType() {
        return new BibliographyType();
    }

    /**
     * Create an instance of {@link IntroductionType }
     * 
     */
    public IntroductionType createIntroductionType() {
        return new IntroductionType();
    }

    /**
     * Create an instance of {@link ThreatScenarioType }
     * 
     */
    public ThreatScenarioType createThreatScenarioType() {
        return new ThreatScenarioType();
    }

    /**
     * Create an instance of {@link RequirementsType }
     * 
     */
    public RequirementsType createRequirementsType() {
        return new RequirementsType();
    }

    /**
     * Create an instance of {@link PurposeType }
     * 
     */
    public PurposeType createPurposeType() {
        return new PurposeType();
    }

    /**
     * Create an instance of {@link BasicRequirementsType }
     * 
     */
    public BasicRequirementsType createBasicRequirementsType() {
        return new BasicRequirementsType();
    }

    /**
     * Create an instance of {@link BibItemType.Description }
     * 
     */
    public BibItemType.Description createBibItemTypeDescription() {
        return new BibItemType.Description();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DocumentType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "document")
    public JAXBElement<DocumentType> createDocument(DocumentType value) {
        return new JAXBElement<DocumentType>(_Document_QNAME, DocumentType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "p", scope = BibItemType.Description.class)
    public JAXBElement<String> createBibItemTypeDescriptionP(String value) {
        return new JAXBElement<String>(_BibItemTypeDescriptionP_QNAME, String.class, BibItemType.Description.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DifferentiationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "differentiation", scope = BibItemType.Description.class)
    public JAXBElement<DifferentiationType> createBibItemTypeDescriptionDifferentiation(DifferentiationType value) {
        return new JAXBElement<DifferentiationType>(_BibItemTypeDescriptionDifferentiation_QNAME, DifferentiationType.class, BibItemType.Description.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PurposeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "purpose", scope = BibItemType.Description.class)
    public JAXBElement<PurposeType> createBibItemTypeDescriptionPurpose(PurposeType value) {
        return new JAXBElement<PurposeType>(_BibItemTypeDescriptionPurpose_QNAME, PurposeType.class, BibItemType.Description.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link IntroductionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "introduction", scope = BibItemType.Description.class)
    public JAXBElement<IntroductionType> createBibItemTypeDescriptionIntroduction(IntroductionType value) {
        return new JAXBElement<IntroductionType>(_BibItemTypeDescriptionIntroduction_QNAME, IntroductionType.class, BibItemType.Description.class, value);
    }

}
