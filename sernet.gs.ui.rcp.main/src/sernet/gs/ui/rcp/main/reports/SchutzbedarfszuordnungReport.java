package sernet.gs.ui.rcp.main.reports;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import sernet.gs.ui.rcp.main.bsi.model.Anwendung;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.IBSIStrukturElement;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.bsi.views.CnAImageProvider;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.office.IOOTableRow;

/**
 * This report prints out the association of
 * protection levels with
 * IT assets such as clients, applications, rooms etc.
 * 
 * Programmatically this is just an asset report
 * with different output columns.
 * 
 * @author koderman@sernet.de
 *
 */
public class SchutzbedarfszuordnungReport
	extends StrukturanalyseReport
	implements IBSIReport {

	
	public SchutzbedarfszuordnungReport(Properties reportProperties) {
		super(reportProperties);
		// TODO Auto-generated constructor stub
	}

	public String getTitle() {
		return "[BSI] Schutzbedarfszuordnung";
	}

}
