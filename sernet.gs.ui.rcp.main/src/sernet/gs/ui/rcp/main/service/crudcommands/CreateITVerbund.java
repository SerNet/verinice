package sernet.gs.ui.rcp.main.service.crudcommands;

import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;

public class CreateITVerbund extends CreateElement {

	public CreateITVerbund(CnATreeElement container, Class type) {
		super(container, type);
	}
	
	@Override
	public void execute() {
		super.execute();
		if (super.child instanceof ITVerbund) {
			ITVerbund verbund = (ITVerbund) child;
			verbund.createNewCategories();
		}
	}
	
	@Override
	public ITVerbund getNewElement() {
		return (ITVerbund) super.getNewElement();
	}

}
