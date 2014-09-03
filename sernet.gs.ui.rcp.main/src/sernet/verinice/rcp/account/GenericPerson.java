package sernet.verinice.rcp.account;

import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.PersonIso;

class GenericPerson {
    
    CnATreeElement person;
    
    public GenericPerson(CnATreeElement person) {
        super();
        this.person = person;
    }

    String getName() {
        String name = null;
        if(person instanceof PersonIso) {
            name = ((PersonIso)person).getName();
        }
        if(person instanceof Person) {
            name = ((Person)person).getFullName();
        }
        return name;
    }
    
}