package sernet.gs.ui.rcp.main.common.model;

import sernet.gs.ui.rcp.main.bsi.model.ISchutzbedarfProvider;

public interface ILinkChangeListener {
	public void vertraulichkeitChanged();
	public void integritaetChanged();
	public void verfuegbarkeitChanged();
	
}
