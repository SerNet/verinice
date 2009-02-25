package sernet.gs.ui.rcp.main.bsi.model;

import java.util.Date;

public interface IMassnahmeUmsetzung {
	public String getTitel();

	public String getUmsetzung() ;

	public Date getUmsetzungBis() ;

	public String getUmsetzungDurch() ;

	public char getStufe() ;

	public String getParentTitle() ;

	public String getUrl() ;

	public String getStand() ;
	
	public String getRevisionDurch();
	
	public Date getNaechsteRevision();
}
