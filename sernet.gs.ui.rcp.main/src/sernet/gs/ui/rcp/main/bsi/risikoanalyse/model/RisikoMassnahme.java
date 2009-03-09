/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
/**
 * 
 */
package sernet.gs.ui.rcp.main.bsi.risikoanalyse.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.swt.graphics.Image;

import sernet.gs.model.Baustein;
import sernet.gs.model.Gefaehrdung;
import sernet.gs.model.Massnahme;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.IBSIStrukturElement;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.OwnGefaehrdung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.OwnGefaehrdungHome;
import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard.IGefaehrdungsBaumElement;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard.RiskAnalysisWizard;
import sernet.gs.ui.rcp.main.bsi.views.BSIKatalogInvisibleRoot;
import sernet.gs.ui.rcp.main.bsi.wizards.ChooseExportMethodPage;
import sernet.gs.ui.rcp.main.bsi.wizards.ChoosePropertiesPage;
import sernet.gs.ui.rcp.main.bsi.wizards.ChooseReportPage;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.reports.IBSIReport;
import sernet.gs.ui.rcp.main.reports.PropertiesRow;
import sernet.gs.ui.rcp.main.reports.PropertySelection;
import sernet.gs.ui.rcp.main.reports.TextReport;
import sernet.gs.ui.rcp.office.IOOTableRow;
import sernet.gs.ui.rcp.office.OOWrapper;
import sernet.hui.common.connect.EntityType;
import sernet.snutils.ExceptionHandlerFactory;


/**
 * @author ahanekop@sernet.de
 *
 */
public class RisikoMassnahme extends Massnahme {
	
	private int dbId;
	private String number;
	private String name;
	private String description;
	
	private String uuid;
	
	public static final String SIEGEL = "Z";
	
	
	public RisikoMassnahme() {
		uuid = UUID.randomUUID().toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		return (this == obj
				|| (obj instanceof RisikoMassnahme
					&& this.uuid.equals(((RisikoMassnahme)obj).getUuid())
					)
				);
	}
	
	@Override
	public int hashCode() {
		return uuid.hashCode();
	}
	

	/**
	 * Must be implemented due to Interface IGefaehrdungsBaumElement.
	 * 
	 * @return - description (String) of the RisikoMassnahmenUmsetzung.
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Sets the description of the RisikoMassnahmenUmsetzung.
	 * 
	 * @param newDescription - new description (String) of the
	 * 		  RisikoMassnahmenUmsetzung
	 */
	public void setDescription(String newDescription) {
		description = newDescription;
	}

	/**
	 * Returns the nuber of the Massnahme.
	 * 
	 * @return the number
	 */
	public String getNumber() {
		return number;
	}

	/**
	 * Sets the number of the Massnahme.
	 * 
	 * @param newNumber the number to set
	 */
	public void setNumber(String newNumber) {
		number = newNumber;
	}
	
	/**
	 * Returns the Siegelstufe of the Massnahme, which is always "Z" (for
	 * additional Massnahmen).
	 * 
	 * @return the Siegelstufe of the Massnahme
	 */
	public String getSiegel() {
		return SIEGEL;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getDbId() {
		return dbId;
	}

	public void setDbId(int dbId) {
		this.dbId = dbId;
	}


	public String getUuid() {
		return uuid;
	}


	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
}
