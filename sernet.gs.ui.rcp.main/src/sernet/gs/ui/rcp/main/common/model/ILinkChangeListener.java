package sernet.gs.ui.rcp.main.common.model;

import sernet.gs.ui.rcp.main.bsi.model.ISchutzbedarfProvider;

public interface ILinkChangeListener {
	public void vertraulichkeitChanged(CascadingTransaction ta);
	public void integritaetChanged(CascadingTransaction ta);
	public void verfuegbarkeitChanged(CascadingTransaction ta);
	
	
}
