package sernet.verinice.service.ldap;

import java.io.Serializable;

import sernet.verinice.model.iso27k.PersonIso;

public class PersonInfo implements Serializable, Comparable<PersonInfo>{
	
	private PersonIso person;
	private String loginName;

    private String title, department, company;
	
	public PersonInfo(PersonIso person, String loginName, String title, String department, String company) {
		super();
		this.person = person;
		this.loginName = loginName;
        this.title = title;
        this.department = department;
        this.company = company;
	}

	public PersonIso getPerson() {
		return person;
	}

	public void setPerson(PersonIso person) {
		this.person = person;
	}

	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}
	
	/**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the department
     */
    public String getDepartment() {
        return department;
    }

    /**
     * @param department the department to set
     */
    public void setDepartment(String department) {
        this.department = department;
    }

    /**
     * @return the company
     */
    public String getCompany() {
        return company;
    }

    /**
     * @param company the company to set
     */
    public void setCompany(String company) {
        this.company = company;
    }

    @Override
	public int compareTo(PersonInfo o) {
		int result;
		if(this.getPerson()!=null && this.getPerson().getTitle()!=null && !this.getPerson().getTitle().trim().isEmpty()
		   && o.getPerson()!=null && o.getPerson().getTitle()!=null && !o.getPerson().getTitle().trim().isEmpty() ) {
			result = this.getPerson().getTitle().compareTo(o.getPerson().getTitle());
		} else if(this.getPerson()!=null && this.getPerson().getTitle()!=null && !this.getPerson().getTitle().trim().isEmpty() ) {
			result = -1;
		} else {
			result = 1;
		}
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((person == null) ? 0 : person.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj){
			return true;
		}
		if (obj == null){
			return false;
		}
		if (getClass() != obj.getClass()){
			return false;
		}
		PersonInfo other = (PersonInfo) obj;
		if (person == null) {
			if (other.person != null){
				return false;
			}
		} else if (!person.equals(other.person)){
			return false;
		}
		return true;
	}
	
}
