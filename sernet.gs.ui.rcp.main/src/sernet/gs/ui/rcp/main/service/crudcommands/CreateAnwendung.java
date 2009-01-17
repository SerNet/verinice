package sernet.gs.ui.rcp.main.service.crudcommands;

import sernet.gs.ui.rcp.main.bsi.model.Anwendung;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;

public class CreateAnwendung extends CreateElement {

	public CreateAnwendung(CnATreeElement container, Class type) {
		super(container, type);
	}
	
	@Override
	public void execute() {
		super.execute();
		if (super.child instanceof Anwendung) {
			Anwendung anwendung = (Anwendung) child;
			anwendung.createCategories();
		}
	}
	
	@Override
	public Anwendung getNewElement() {
		return (Anwendung) super.getNewElement();
	}

}
