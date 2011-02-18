package sernet.verinice.interfaces.ldap;

import java.io.Serializable;

public class PersonParameter implements Serializable {
	
	private String surname, title, department, company;

	public PersonParameter( String surname, String title, String department, String company) {
		super();
		this.surname = surname;
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
		&& (getDepartment()==null || getDepartment().isEmpty())
		&& (getCompany()==null || getCompany().isEmpty());
	}
	
	
}
