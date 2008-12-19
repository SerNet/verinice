package sernet.gs.ui.rcp.main.bsi.model;

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;

public class DocumentReference {

	private CnATreeElement cnatreeelement;
	private DocumentLink parent;

	public DocumentReference(CnATreeElement element) {
		cnatreeelement = element;
	}

	public CnATreeElement getCnaTreeElement() {
		return this.cnatreeelement;
	}
	
	public void setParent(DocumentLink parent) {
		this.parent = parent;
	}

	public DocumentLink getParent() {
		return parent;
	}

}
