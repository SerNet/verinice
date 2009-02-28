package sernet.gs.ui.rcp.main.bsi.model;

import sernet.gs.ui.rcp.main.common.model.CascadingTransaction;


public interface ISchutzbedarfProvider {
	public int getVertraulichkeit();
	public int getVerfuegbarkeit();
	public int getIntegritaet();
	
	public void setVertraulichkeit(int i, CascadingTransaction ta);
	public void setIntegritaet(int i, CascadingTransaction ta);
	public void setVerfuegbarkeit(int i, CascadingTransaction ta);
	
	public String getVertraulichkeitDescription();
	public String getIntegritaetDescription();
	public String getVerfuegbarkeitDescription();

	public void setVertraulichkeitDescription(String text, CascadingTransaction ta);
	public void setIntegritaetDescription(String text, CascadingTransaction ta);
	public void setVerfuegbarkeitDescription(String text, CascadingTransaction ta);
	
	public void updateVertraulichkeit(CascadingTransaction ta);
	public void updateIntegritaet(CascadingTransaction ta);
	public void updateVerfuegbarkeit(CascadingTransaction ta);
}
