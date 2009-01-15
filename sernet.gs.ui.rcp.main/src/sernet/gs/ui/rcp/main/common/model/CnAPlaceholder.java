package sernet.gs.ui.rcp.main.common.model;

public class CnAPlaceholder extends CnATreeElement {

	public CnAPlaceholder(NullModel nullModel) {
		super(nullModel);
	}

	@Override
	public String getTitel() {
		return "DB-Verbindung: geschlossen";
	}

	@Override
	public String getTypeId() {
		return null;
	}

}
