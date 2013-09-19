package sernet.gs.ui.rcp.main.reports;

import sernet.verinice.model.iso27k.Organization;

/**
 * Marker interface for reports of ISM perspective items. 
 * 
 * @author akoderman
 *
 */
public interface ISMReport {
	
	Organization getOrganization();
	
	void setOrganization(Organization org);
}
