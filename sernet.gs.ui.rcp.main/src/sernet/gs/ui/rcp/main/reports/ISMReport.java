package sernet.gs.ui.rcp.main.reports;

import sernet.verinice.iso27k.model.Organization;

/**
 * Marker interface for reports of ISM perspective items. 
 * 
 * @author akoderman
 *
 */
public interface ISMReport {
	
	public Organization getOrganization();
	
	public void setOrganization(Organization org);
}
