/*******************************************************************************
 * Copyright (c) 2009 Daniel Murygin <dm[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.model;

import java.util.Collection;

import sernet.gs.model.Baustein;
import sernet.gs.ui.rcp.main.bsi.model.AnwendungenKategorie;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.ClientsKategorie;
import sernet.gs.ui.rcp.main.bsi.model.GebaeudeKategorie;
import sernet.gs.ui.rcp.main.bsi.model.NKKategorie;
import sernet.gs.ui.rcp.main.bsi.model.PersonenKategorie;
import sernet.gs.ui.rcp.main.bsi.model.RaeumeKategorie;
import sernet.gs.ui.rcp.main.bsi.model.ServerKategorie;
import sernet.gs.ui.rcp.main.bsi.model.SonstigeITKategorie;
import sernet.gs.ui.rcp.main.bsi.model.TKKategorie;
import sernet.gs.ui.rcp.main.bsi.model.TagHelper;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.FinishedRiskAnalysis;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.hui.common.connect.Entity;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("serial")
public class Organization extends CnATreeElement implements IISO27kGroup {

	public static final String TYPE_ID = "org"; //$NON-NLS-1$
	public static final String PROP_ABBR = "org_abbr"; //$NON-NLS-1$
	public static final String PROP_NAME = "org_name"; //$NON-NLS-1$
	public static final String PROP_TAG = "org_tag"; //$NON-NLS-1$
	
	/**
	 * Creates an empty Organization
	 */
	public Organization() {
		super();
	}
	
	public Organization(CnATreeElement parent) {
		super(parent);
		setEntity(new Entity(TYPE_ID));
		getEntity().createNewProperty(getEntityType().getPropertyType(PROP_NAME), "New Organization");
		addChild(new AssetGroup(this));
		addChild(new ControlGroup(this));
		addChild(new AuditGroup(this));
		addChild(new ExceptionGroup(this));
		addChild(new PersonGroup(this));
		addChild(new RequirementGroup(this));
		addChild(new IncidentGroup(this));
		addChild(new IncidentScenarioGroup(this));
		addChild(new ResponseGroup(this));
		addChild(new ThreatGroup(this));
		addChild(new VulnerabilityGroup(this));
		addChild(new DocumentGroup(this));
		addChild(new EvidenceGroup(this));
		addChild(new FindingGroup(this));
		addChild(new InterviewGroup(this));
		addChild(new RecordGroup(this));
		addChild(new ProcessGroup(this));
	}
	
	@Override
	public boolean canContain(Object child) {
		return (child instanceof Group);
	}
	
	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.common.model.CnATreeElement#getTypeId()
	 */
	@Override
	public String getTypeId() {
		return TYPE_ID;
	}
	
	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.common.model.CnATreeElement#getTitel()
	 */
	@Override
	public String getTitle() {
		return getEntity().getSimpleValue(PROP_NAME);
	}
	
	public void setTitle(String name) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_NAME), name);
	}
	
	public String getAbbreviation() {
		return getEntity().getSimpleValue(PROP_ABBR);
	}
	
	public void setAbbreviation(String abbreviation) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_ABBR), abbreviation);
	}
	
	public Collection<? extends String> getTags() {
		return TagHelper.getTags(getEntity().getSimpleValue(PROP_TAG));
	}
	
}
