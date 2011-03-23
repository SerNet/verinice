package sernet.verinice.server.ldap;

import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;

import sernet.verinice.interfaces.ldap.IPersonDao;
import sernet.verinice.interfaces.ldap.PersonParameter;
import sernet.verinice.model.iso27k.PersonIso;
import sernet.verinice.service.ldap.PersonInfo;

public class PersonDaoImpl implements IPersonDao {
	
	private String base;
	
	private String filter;
	
	private LdapTemplate ldapTemplate;

	@SuppressWarnings("unchecked")
	@Override
	public List<PersonInfo> getPersonList(PersonParameter parameter) {
		return ldapTemplate.search(getBase(), getUserFilter(parameter),
		         new AttributesMapper() {	
		            public Object mapFromAttributes(Attributes attrs)
		               throws NamingException {
			           PersonIso person = new PersonIso();
			           String login = null;
			           if(attrs.get("sAMAccountName")!=null) {
			        	   login = (String) attrs.get("sAMAccountName").get();
			           } else if(attrs.get("userPrincipalName")!=null) {
			        	   // pre windows 2000:
			        	   login = (String) attrs.get("userPrincipalName").get();
			           } else if(attrs.get("uid")!=null) {
                           // OpenLDAP
			               login = (String) attrs.get("uid").get();
                       }
		               if(attrs.get("givenName")!=null) {
		                   // AD
		            	   person.setName((String) attrs.get("givenName").get());
		               }else {
                           // OpenLDAP
                           String forname = getForename((String) attrs.get("cn").get());
                           if(forname!=null) {
                               person.setName(forname);
                           }
                       }
		               if(attrs.get("sn")!=null) {
		            	   // AD
		                   person.setSurname((String) attrs.get("sn").get());
		               } else {
		                   // OpenLDAP
		                   String surname = getSurname((String) attrs.get("cn").get());
		                   if(surname!=null) {
		                       person.setSurname(surname);
		                   }
		               }
		               if(attrs.get("telephoneNumber")!=null) {
		            	   person.setPhone((String) attrs.get("telephoneNumber").get());
		               }
		               
		               return new PersonInfo(person, login);
		            }

                    
		         });
	}
	
	private String getForename(String fullName) {
        String forename = null;
        if(fullName!=null) {
            int n = fullName.lastIndexOf(" ");
            if(n!=-1) {
                forename = fullName.substring(0,n);
            }
        }
        return forename;
    }
	
	private String getSurname(String fullName) {
        String surname = null;
        if(fullName!=null) {
            surname = fullName;
            int n = fullName.lastIndexOf(" ");
            if(n!=-1) {
                surname = fullName.substring(n+1);
            }
        }
        return surname;
    }
	
	private String getUserFilter(PersonParameter parameter) {
		StringBuilder sb = new StringBuilder();
		if(parameter!=null && !parameter.isEmpty() ) {
			sb.append("(&");
		}
		sb.append(getFilter());
		if(parameter!=null && !parameter.isEmpty() ) {
			if(parameter.getSurname()!=null && !parameter.getSurname().isEmpty()) {
				sb.append("(sn=").append(parameter.getSurname()).append("*)");
			}
			if(parameter.getTitle()!=null && !parameter.getTitle().isEmpty()) {
				sb.append("(title=").append(parameter.getTitle()).append("*)");
			}
			if(parameter.getDepartment()!=null && !parameter.getDepartment().isEmpty()) {
				sb.append("(department=").append(parameter.getDepartment()).append("*)");
			}
			if(parameter.getCompany()!=null && !parameter.getCompany().isEmpty()) {
				sb.append("(company=").append(parameter.getCompany()).append("*)");
			}
			sb.append(")");
		}
		return sb.toString();
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

}
