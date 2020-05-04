package sernet.verinice.service.ldap;

import java.io.Serializable;

public class PersonInfo implements Serializable {

    private static final long serialVersionUID = 4591313031491466231L;

    private final String loginName;
    private final String title;
    private final String department;
    private final String company;
    private final String givenName;
    private final String surname;
    private final String eMail;
    private final String phone;

    public PersonInfo(String loginName, String title, String department, String company,
            String givenName, String surname, String eMail, String phone) {
        this.loginName = loginName;
        this.title = title;
        this.department = department;
        this.company = company;
        this.givenName = givenName;
        this.surname = surname;
        this.eMail = eMail;
        this.phone = phone;
    }

    public String getLoginName() {
        return loginName;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the department
     */
    public String getDepartment() {
        return department;
    }

    /**
     * @return the company
     */
    public String getCompany() {
        return company;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((loginName == null) ? 0 : loginName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PersonInfo other = (PersonInfo) obj;
        if (loginName == null) {
            if (other.loginName != null)
                return false;
        } else if (!loginName.equals(other.loginName))
            return false;
        return true;
    }

    public String getGivenName() {
        return givenName;
    }

    public String getSurname() {
        return surname;
    }

    public String getEMail() {
        return eMail;
    }

    public String getPhone() {
        return phone;
    }

}
