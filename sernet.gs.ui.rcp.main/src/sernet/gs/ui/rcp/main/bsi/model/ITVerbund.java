/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.model;

import java.util.Collection;
import java.util.HashSet;

import sernet.gs.model.Baustein;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.FinishedRiskAnalysis;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.hui.common.connect.Entity;

public class ITVerbund extends CnATreeElement 
	implements IBSIStrukturElement {
	
	public static final String TYPE_ID = "itverbund"; //$NON-NLS-1$
	public static final String PROP_NAME = "itverbund_name"; //$NON-NLS-1$
	
	private String kuerzel = " "; //$NON-NLS-1$
	
	public String getKuerzel() {
		return kuerzel;
	}
	
	
	public ITVerbund(CnATreeElement parent) {
		super(parent);
		setEntity(new Entity(TYPE_ID));
		getEntity().createNewProperty(getEntityType().getPropertyType(PROP_NAME),
				"Neuer IT-Verbund");
	}
	
	public int getSchicht() {
		return 1;
	}
	
	public Collection<? extends String> getTags() {
		return new HashSet<String>(0);
	}
	
	protected ITVerbund() {
		// hibernate
	}
	
	public void createNewCategories() {
		addChild(new PersonenKategorie(this));
		addChild(new GebaeudeKategorie(this));
		addChild(new RaeumeKategorie(this));
		addChild(new AnwendungenKategorie(this));
		addChild(new ServerKategorie(this));
		addChild(new ClientsKategorie(this));
		addChild(new SonstigeITKategorie(this));
		addChild(new NKKategorie(this));
		addChild(new TKKategorie(this));
	}
	
	public CnATreeElement getCategory(String id) {
		for (CnATreeElement category : getChildren()) {
			if (category.getTypeId() != null
					&& category.getTypeId().equals(id))
				return (CnATreeElement) category;
		}
		return null;
	}
	
	@Override
	public String getTitle() {
		return getEntity().getSimpleValue(PROP_NAME);
		}

	@Override
	public String getTypeId() {
		return TYPE_ID;
	}
	
	@Override
	public boolean canContain(Object obj) {
		if (obj instanceof Baustein
				|| obj instanceof BausteinUmsetzung
				|| obj instanceof AnwendungenKategorie 
				|| obj instanceof ClientsKategorie
				|| obj instanceof SonstigeITKategorie
				|| obj instanceof GebaeudeKategorie
				|| obj instanceof NKKategorie
				|| obj instanceof PersonenKategorie
				|| obj instanceof RaeumeKategorie 
				|| obj instanceof ServerKategorie
				|| obj instanceof TKKategorie
				|| obj instanceof FinishedRiskAnalysis
				)
			return true;
		return false;
	}


	public void setTitel(String name) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_NAME), name);
	}


}
