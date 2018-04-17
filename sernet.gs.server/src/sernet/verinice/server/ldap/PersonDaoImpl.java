package sernet.verinice.server.ldap;

import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;

import sernet.verinice.interfaces.ldap.IPersonDao;
import sernet.verinice.interfaces.ldap.PersonParameter;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.PersonIso;
import sernet.verinice.service.ldap.PersonInfo;

public class PersonDaoImpl implements IPersonDao {

    private String base;
	
	private String filter;
	
	private LdapTemplate ldapTemplate;
	
	@SuppressWarnings("unchecked")
	@Override
	public List<PersonInfo> getPersonList(PersonParameter parameter) {
	    return ldapTemplate.search(getBase(), getUserFilter(parameter),new LdapPersonMapper());
	}
	
	/*
	 * @see sernet.verinice.interfaces.ldap.IPersonDao#getPersonList(sernet.verinice.interfaces.ldap.PersonParameter, boolean)
	 */
	@Override
	public List<PersonInfo> getPersonList(PersonParameter parameter, boolean importToITGS) {
	    return ldapTemplate.search(getBase(), getUserFilter(parameter),new LdapPersonMapper(importToITGS));
	}
	
	
	private String getForename(String fullName) {
        String forename = null;
        if(fullName!=null) {
            int n = fullName.lastIndexOf(' ');
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
            int n = fullName.lastIndexOf(' ');
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
		sb = appendUserParameter(parameter, sb);
		return sb.toString();
	}

    private StringBuilder appendUserParameter(PersonParameter parameter, StringBuilder sb) {
        if(parameter!=null && !parameter.isEmpty() ) {
			if(parameter.getSurname()!=null && !parameter.getSurname().isEmpty()) {
				sb.append("(sn=").append(parameter.getSurname()).append("*)");
			}
			if(parameter.getGivenName()!=null && !parameter.getGivenName().isEmpty()) {
                sb.append("(givenName=").append(parameter.getGivenName()).append("*)");
            }
			if(parameter.getTitle()!=null && !parameter.getTitle().isEmpty()) {
				sb.append("(title=").append(parameter.getTitle()).append("*)");
			}
			if(parameter.getDepartment()!=null && !parameter.getDepartment().isEmpty()) {
				sb.append("(|(department=").append(parameter.getDepartment()).append("*)");
                sb.append("(subDepartment=").append(parameter.getDepartment()).append("*))");
			}
			if(parameter.getCompany()!=null && !parameter.getCompany().isEmpty()) {
				sb.append("(|(company=").append(parameter.getCompany()).append("*)");
                sb.append("(companyCode=").append(parameter.getCompany()).append("*))");
			}
			sb.append(")");
		}
        return sb;
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
	
    private final class LdapPersonMapper implements AttributesMapper {
        private boolean useITGS = false;
        
        public LdapPersonMapper(){super();};
        public LdapPersonMapper(boolean itgs){
            super();
            this.useITGS = itgs;
        }
        public Object mapFromAttributes(Attributes attrs)
           throws NamingException {
           CnATreeElement person = null;
           if(!useITGS){
               person = new PersonIso();
           } else  {
               person = new Person(null);
           }
           String login = determineLogin(attrs);
           person = determineGivenName(attrs, person);
           person = determineSurname(attrs, person);
           person = determineEmailPhone(attrs, person);
           String title = determineTitle(attrs);
           String department = determineDepartment(attrs);
           String company = determineCompany(attrs);
           
           return new PersonInfo(person, login, title, department, company);
        }

        private String determineCompany(Attributes attrs) throws NamingException {
            String company = null;
               if(attrs.get("company")!=null) {
                   // AD
                   company = (String) attrs.get("company").get();
               } else if(attrs.get("companyCode")!=null)  {
                   // LDAP
                   company = (String) attrs.get("companyCode").get();
               }
            return company;
        }

        private String determineDepartment(Attributes attrs) throws NamingException {
            String department = null;
               if(attrs.get("department")!=null) {
                   // AD
                   department = (String) attrs.get("department").get();
               } else if(attrs.get("subDepartment")!=null) {
                   // LDAP
                   department = (String) attrs.get("subDepartment").get();
               }
            return department;
        }

        private String determineTitle(Attributes attrs) throws NamingException {
            String title = null;
               if(attrs.get("title")!=null) {
                   // AD
                   title = (String) attrs.get("title").get();
               }
            return title;
        }

        private CnATreeElement determineEmailPhone(Attributes attrs, CnATreeElement person) throws NamingException {
            if(attrs.get("telephoneNumber")!=null) {
                   // AD
                   person = setPersonPhone(person, ((String) attrs.get("telephoneNumber").get()));
               }
               if(attrs.get("mail")!=null) {
                   // AD
                   person = setPersonEmail(person, ((String) attrs.get("mail").get()));
               }
               return person;
        }

        private CnATreeElement determineSurname(Attributes attrs, CnATreeElement person) throws NamingException {
            String surname = null;
            if(attrs.get("sn")!=null) {
                   // AD
                   person = setPersonSurname(person, (String) attrs.get("sn").get());
               } else {
                   // OpenLDAP
                   surname = getSurname((String) attrs.get("cn").get());
                   if(surname!=null) {
                       person = setPersonSurname(person, surname);
                   }
               }
            return person;
        }

        private CnATreeElement determineGivenName(Attributes attrs, CnATreeElement person) throws NamingException {
            if(attrs.get("givenName")!=null) {
                // AD
                person = setPersonName(person, (String)attrs.get("givenName").get());
            }else {
                // OpenLDAP
                String forname = getForename((String) attrs.get("cn").get());
                if(forname!=null) {
                    person = setPersonName(person, forname);
                }
            }
            return person;
        }

        private String determineLogin(Attributes attrs) throws NamingException {
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
            return login;
        }
    }
    
    private CnATreeElement setPersonEmail(CnATreeElement person, String data) {
        if(person instanceof Person){
            ((Person)person).getEntity().setSimpleValue(
                    ((Person)person).getEntityType().getPropertyType(Person.P_EMAIL),
                    data);
            
        } else if (person instanceof PersonIso){
            ((PersonIso)person).setEmail(data);
        }
        
        return person;        
    } 
    
    private CnATreeElement setPersonSurname(CnATreeElement person, String data){
        if(person instanceof Person){
            ((Person)person).getEntity().setSimpleValue(
                    ((Person)person).getEntityType().getPropertyType(Person.P_NAME),
                    data);
        } else if(person instanceof PersonIso){
            ((PersonIso)person).setSurname(data);
        }
        return person;
    }
    
    private CnATreeElement setPersonName(CnATreeElement person, String data){
        if(person instanceof Person){
            ((Person)person).getEntity().setSimpleValue(
                    ((Person)person).getEntityType().getPropertyType(Person.P_VORNAME),
                    data);
         } else if (person instanceof PersonIso){
             ((PersonIso)person).setName(data);
         }
        return person;
    }
    
    private CnATreeElement setPersonPhone(CnATreeElement person, String data){
        if(person instanceof Person){
            ((Person)person).getEntity().setSimpleValue(
                    ((Person)person).getEntityType().getPropertyType(Person.P_PHONE),
                    data);
        } else if (person instanceof PersonIso){
            ((PersonIso)person).setPhone(data);
        }
        
        return person;
    }
    

}
