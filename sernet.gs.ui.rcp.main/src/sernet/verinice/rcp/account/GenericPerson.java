package sernet.verinice.rcp.account;

import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.PersonIso;
import sernet.verinice.rcp.ElementTitleCache;

class GenericPerson {
    
    CnATreeElement person;
    
    public GenericPerson(CnATreeElement person) {
        super();
        this.person = person;
    }

    String getName() {
        String name = null;
        if(person instanceof PersonIso) {
            name = ((PersonIso)person).getFullName();
        }
        if(person instanceof Person) {
            name = ((Person)person).getFullName();
        }
        return name;
    }
    
    String getParentName() {
        String name = null;
        if(person instanceof PersonIso) {
            name = ElementTitleCache.get(person.getParentId());
        }
        if(person instanceof Person) {
            name = sernet.verinice.model.bsi.Messages.PersonenKategorie_0;
        }
        return name;
    }
    
}