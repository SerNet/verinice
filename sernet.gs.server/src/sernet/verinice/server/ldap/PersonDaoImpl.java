package sernet.verinice.server.ldap;

import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

import sernet.verinice.interfaces.ldap.IPersonDao;
import sernet.verinice.interfaces.ldap.PersonParameter;
import sernet.verinice.service.ldap.PersonInfo;

public class PersonDaoImpl implements IPersonDao {

    private String base;

    private String filter;

    private LdapTemplate ldapTemplate;

    private boolean usePasswordFromClient;

    @SuppressWarnings("unchecked")
    @Override
    public List<PersonInfo> getPersonList(PersonParameter parameter, String password) {
        if (usePasswordFromClient) {
            LdapContextSource contextSource = (LdapContextSource) ldapTemplate.getContextSource();
            contextSource.setPassword(password);
        }
        return ldapTemplate.search(getBase(), getUserFilter(parameter), new LdapPersonMapper());
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

    @Override

    public boolean isUsePasswordFromClient() {
        return usePasswordFromClient;
    }

    public void setUsePasswordFromClient(boolean usePasswordFromClient) {
        this.usePasswordFromClient = usePasswordFromClient;
    }

    private static final class LdapPersonMapper implements AttributesMapper {

        public Object mapFromAttributes(Attributes attrs) throws NamingException {

            String login = getFirstMatchOrNull(attrs, "sAMAccountName", "userPrincipalName", /*
                                                                                              * pre
                                                                                              * windows
                                                                                              * 2000
                                                                                              */
                    "uid" /* OpenLDAP */ );
            String givenName = determineGivenName(attrs);
            String surname = determineSurname(attrs);
            String email = getFirstMatchOrNull(attrs, "mail" /* AD */);
            String phone = getFirstMatchOrNull(attrs, "telephoneNumber" /* AD */);
            String title = getFirstMatchOrNull(attrs, "title" /* AD */ );
            String department = getFirstMatchOrNull(attrs, "department" /* AD */,
                    "subDepartment"/* LDAP */ );
            String company = getFirstMatchOrNull(attrs, "company" /* AD */,
                    "companyCode" /* LDAP */
            );

            return new PersonInfo(login, title, department, company, givenName, surname, email,
                    phone);
        }

        private String determineSurname(Attributes attrs) throws NamingException {
            String surname = null;
            if (attrs.get("sn") != null) {
                // AD
                return (String) attrs.get("sn").get();
            } else {
                // OpenLDAP
                return getSurname((String) attrs.get("cn").get());
            }
        }

        private String determineGivenName(Attributes attrs) throws NamingException {
            if (attrs.get("givenName") != null) {
                // AD
                return (String) attrs.get("givenName").get();
            } else {
                // OpenLDAP
                return getForename((String) attrs.get("cn").get());
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
    }
}