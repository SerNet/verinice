package sernet.gs.ui.rcp.main.bsi.model;

import sernet.hui.common.connect.IEntityChangedListener;

public interface ISchutzbedarfProvider {
	public int getVertraulichkeit();
	public int getVerfuegbarkeit();
	public int getIntegritaet();
	
	public void setVertraulichkeit(int i);
	public void setIntegritaet(int i);
	public void setVerfuegbarkeit(int i);
	
	public String getVertraulichkeitDescription();
	public String getIntegritaetDescription();
	public String getVerfuegbarkeitDescription();

	public void setVertraulichkeitDescription(String text);
	public void setIntegritaetDescription(String text);
	public void setVerfuegbarkeitDescription(String text);
	
	public IEntityChangedListener getChangeListener();
	public void updateAll();
}
