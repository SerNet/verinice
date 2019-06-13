package sernet.verinice.server.ldap;

import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;

import sernet.verinice.interfaces.ldap.IPersonDao;
import sernet.verinice.interfaces.ldap.PersonParameter;
import sernet.verinice.model.bp.elements.BpPerson;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Domain;
import sernet.verinice.model.iso27k.PersonIso;
import sernet.verinice.service.ldap.PersonInfo;

public class PersonDaoImpl implements IPersonDao {

    private String base;

    private String filter;

    private LdapTemplate ldapTemplate;

    @SuppressWarnings("unchecked")
    @Override
    public List<PersonInfo> getPersonList(PersonParameter parameter) {
        return ldapTemplate.search(getBase(), getUserFilter(parameter), new LdapPersonMapper());
    }

    /*
     * @see
     * sernet.verinice.interfaces.ldap.IPersonDao#getPersonList(sernet.verinice.
     * interfaces.ldap.PersonParameter, sernet.verinice.model.common.Domain)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<PersonInfo> getPersonList(PersonParameter parameter, Domain importDomain) {
        return ldapTemplate.search(getBase(), getUserFilter(parameter),
                new LdapPersonMapper(importDomain));
    }

    private String getUserFilter(PersonParameter parameter) {
        StringBuilder sb = new StringBuilder();
        if (parameter != null && !parameter.isEmpty()) {
            sb.append("(&");
        }
        sb.append(getFilter());
        appendUserParameter(parameter, sb);
        return sb.toString();
    }

    private void appendUserParameter(PersonParameter parameter, StringBuilder sb) {
        if (parameter == null || parameter.isEmpty()) {
            return;
        }

        if (parameter.getSurname() != null && !parameter.getSurname().isEmpty()) {
            sb.append("(sn=").append(parameter.getSurname()).append("*)");
        }
        if (parameter.getGivenName() != null && !parameter.getGivenName().isEmpty()) {
            sb.append("(givenName=").append(parameter.getGivenName()).append("*)");
        }
        if (parameter.getTitle() != null && !parameter.getTitle().isEmpty()) {
            sb.append("(title=").append(parameter.getTitle()).append("*)");
        }
        if (parameter.getDepartment() != null && !parameter.getDepartment().isEmpty()) {
            sb.append("(|(department=").append(parameter.getDepartment()).append("*)");
            sb.append("(subDepartment=").append(parameter.getDepartment()).append("*))");
        }
        if (parameter.getCompany() != null && !parameter.getCompany().isEmpty()) {
            sb.append("(|(company=").append(parameter.getCompany()).append("*)");
            sb.append("(companyCode=").append(parameter.getCompany()).append("*))");
        }
        sb.append(")");

    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public LdapTemplate getLdapTemplate() {
        return ldapTemplate;
    }

    public void setLdapTemplate(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    private static final class LdapPersonMapper implements AttributesMapper {
        private Domain importDomain = Domain.ISM;

        public LdapPersonMapper() {
            super();
        }

        public LdapPersonMapper(Domain importDomain) {
            super();
            this.importDomain = importDomain;
        }

        public Object mapFromAttributes(Attributes attrs) throws NamingException {
            CnATreeElement person;
            switch (importDomain) {
            case ISM:
                person = new PersonIso();
                break;
            case BASE_PROTECTION_OLD:
                person = new Person(null);
                break;
            case BASE_PROTECTION:
                person = new BpPerson(null);
                break;
            default:
                throw new IllegalArgumentException("Unknown domain " + importDomain);
            }

            String login = getFirstMatchOrNull(attrs, "sAMAccountName", "userPrincipalName", /*
                                                                                              * pre
                                                                                              * windows
                                                                                              * 2000
                                                                                              */
                    "uid" /* OpenLDAP */ );
            setGivenName(attrs, person);
            setSurname(attrs, person);
            setEmailPhone(attrs, person);
            String title = getFirstMatchOrNull(attrs, "title" /* AD */ );
            String department = getFirstMatchOrNull(attrs, "department" /* AD */,
                    "subDepartment"/* LDAP */ );
            String company = getFirstMatchOrNull(attrs, "company" /* AD */,
                    "companyCode" /* LDAP */
            );

            return new PersonInfo(person, login, title, department, company);
        }

        private void setEmailPhone(Attributes attrs, CnATreeElement person) throws NamingException {
            if (attrs.get("telephoneNumber") != null) {
                // AD
                setPersonPhone(person, ((String) attrs.get("telephoneNumber").get()));
            }
            if (attrs.get("mail") != null) {
                // AD
                setPersonEmail(person, ((String) attrs.get("mail").get()));
            }
        }

        private void setSurname(Attributes attrs, CnATreeElement person) throws NamingException {
            String surname = null;
            if (attrs.get("sn") != null) {
                // AD
                setPersonSurname(person, (String) attrs.get("sn").get());
            } else {
                // OpenLDAP
                surname = getSurname((String) attrs.get("cn").get());
                if (surname != null) {
                    setPersonSurname(person, surname);
                }
            }
        }

        private void setGivenName(Attributes attrs, CnATreeElement person) throws NamingException {
            if (attrs.get("givenName") != null) {
                // AD
                setPersonName(person, (String) attrs.get("givenName").get());
            } else {
                // OpenLDAP
                String forname = getForename((String) attrs.get("cn").get());
                if (forname != null) {
                    setPersonName(person, forname);
                }
            }
        }

        private String getForename(String fullName) {
            String forename = null;
            if (fullName != null) {
                int n = fullName.lastIndexOf(' ');
                if (n != -1) {
                    forename = fullName.substring(0, n);
                }
            }
            return forename;
        }

        private String getSurname(String fullName) {
            String surname = null;
            if (fullName != null) {
                surname = fullName;
                int n = fullName.lastIndexOf(' ');
                if (n != -1) {
                    surname = fullName.substring(n + 1);
                }
            }
            return surname;
        }

        private static String getFirstMatchOrNull(Attributes attrs, String... attributeNames)
                throws NamingException {
            for (String attributeName : attributeNames) {
                Attribute attribute = attrs.get(attributeName);
                if (attribute != null) {
                    return (String) attribute.get();
                }
            }
            return null;
        }

        private void setPersonEmail(CnATreeElement person, String data) {
            if (person instanceof Person) {
                setPersonProperty(person, Person.P_EMAIL, data);
            } else if (person instanceof PersonIso) {
                setPersonProperty(person, PersonIso.PROP_EMAIL, data);
            } else if (person instanceof BpPerson) {
                setPersonProperty(person, BpPerson.PROP_EMAIL, data);
            } else {
                throwUnsupportedType(person);
            }

        }

        private void setPersonSurname(CnATreeElement person, String data) {
            if (person instanceof Person) {
                setPersonProperty(person, Person.P_NAME, data);
            } else if (person instanceof PersonIso) {
                setPersonProperty(person, PersonIso.PROP_SURNAME, data);
            } else if (person instanceof BpPerson) {
                setPersonProperty(person, BpPerson.PROP_LAST_NAME, data);
            } else {
                throwUnsupportedType(person);
            }
        }

        private void setPersonName(CnATreeElement person, String data) {
            if (person instanceof Person) {
                setPersonProperty(person, Person.P_VORNAME, data);
            } else if (person instanceof PersonIso) {
                setPersonProperty(person, PersonIso.PROP_NAME, data);
            } else if (person instanceof BpPerson) {
                setPersonProperty(person, BpPerson.PROP_FIRST_NAME, data);
            } else {
                throwUnsupportedType(person);
            }
        }

        private void setPersonPhone(CnATreeElement person, String data) {
            if (person instanceof Person) {
                setPersonProperty(person, Person.P_PHONE, data);
            } else if (person instanceof PersonIso) {
                setPersonProperty(person, PersonIso.PROP_TELEFON, data);
            } else if (person instanceof BpPerson) {
                setPersonProperty(person, BpPerson.PROP_PHONE, data);
            } else {
                throwUnsupportedType(person);
            }

        }

        private static void setPersonProperty(CnATreeElement person, String propertyName,
                String value) {
            person.getEntity().setSimpleValue(person.getEntityType().getPropertyType(propertyName),
                    value);
        }

        private static void throwUnsupportedType(CnATreeElement person) {
            throw new UnsupportedOperationException(
                    "Cannot handle person with unsupported type " + person.getClass());
        }
    }
}