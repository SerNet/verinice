package sernet.verinice.interfaces.ldap;

import java.io.Serializable;

public class PersonParameter implements Serializable {
	
	private String surname;
	private String givenName;
	private String title;
	private String department;
	private String company;

	public PersonParameter( String surname, String giveName, String title, String department, String company) {
		super();
		this.surname = surname;
		this.givenName = giveName;
        this.title = title;
		this.department = department;
		this.company = company;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	/**
     * @return the givenName
     */
    public String getGivenName() {
        return givenName;
    }

    /**
     * @param givenName the givenName to set
     */
    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}
	
	public boolean isEmpty() {
		return (getTitle()==null || getTitle().isEmpty())
		&& (getSurname()==null || getSurname().isEmpty())
        && (getGivenName()==null || getGivenName().isEmpty())
		&& (getDepartment()==null || getDepartment().isEmpty())
		&& (getCompany()==null || getCompany().isEmpty());
	}

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((company == null) ? 0 : company.hashCode());
        result = prime * result + ((department == null) ? 0 : department.hashCode());
        result = prime * result + ((givenName == null) ? 0 : givenName.hashCode());
        result = prime * result + ((surname == null) ? 0 : surname.hashCode());
        result = prime * result + ((title == null) ? 0 : title.hashCode());
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
        PersonParameter other = (PersonParameter) obj;
        if (company == null) {
            if (other.company != null)
                return false;
        } else if (!company.equals(other.company))
            return false;
        if (department == null) {
            if (other.department != null)
                return false;
        } else if (!department.equals(other.department))
            return false;
        if (givenName == null) {
            if (other.givenName != null)
                return false;
        } else if (!givenName.equals(other.givenName))
            return false;
        if (surname == null) {
            if (other.surname != null)
                return false;
        } else if (!surname.equals(other.surname))
            return false;
        if (title == null) {
            if (other.title != null)
                return false;
        } else if (!title.equals(other.title))
            return false;
        return true;
    }
	
	
}
