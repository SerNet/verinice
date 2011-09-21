package sernet.verinice.interfaces.ldap;

import java.io.Serializable;

public class PersonParameter implements Serializable {
	
	private String surname, givenName, title, department, company;

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
	
	
}
