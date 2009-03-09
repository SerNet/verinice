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
package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;
import java.util.List;

import sernet.gs.model.Baustein;
import sernet.gs.model.Massnahme;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenFactory;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.common.model.BuildInput;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.WhereAmIUtil;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.main.service.commands.RuntimeCommandException;

/**
 * Create and save new element of type type to the database using its class to lookup
 * the DAO from the factory.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 * @param <T>
 */
public class CreateBaustein extends GenericCommand {

	private CnATreeElement container;
	private BausteinUmsetzung child;
	private Baustein baustein;
	private boolean reloadObject;

	public CreateBaustein(CnATreeElement container, Baustein baustein) {
		this.container = container;
		this.baustein = baustein;
		
		// cause reload on execution if command was created on client:
		// on server: not necessary because element is already attached to session
		if (WhereAmIUtil.runningOnClient())
			reloadObject = true;
	}
	
	public void execute() {
		IBaseDao<BausteinUmsetzung, Serializable> dao 
			= getDaoFactory().getDAO(BausteinUmsetzung.class);
		IBaseDao<Object, Serializable> containerDAO = getDaoFactory().getDAOForObject(container);
		
		try {
			if (reloadObject)
				containerDAO.reload(container, container.getDbId());
			
			if (container.containsBausteinUmsetzung(baustein.getId()))
				return;
			
			MassnahmenFactory massnahmenFactory = new MassnahmenFactory();

			child = new BausteinUmsetzung(container);
			container.addChild(child);
			
			child.setKapitel(baustein.getId());
			child.setName(baustein.getTitel());
			child.setUrl(baustein.getUrl());
			child.setStand(baustein.getStand());

			List<Massnahme> massnahmen = baustein
					.getMassnahmen();
			for (Massnahme mn : massnahmen) {
				massnahmenFactory.createMassnahmenUmsetzung(child, mn);
			}
			
		} catch (Exception e) {
			throw new RuntimeCommandException(e);
		}
	}

	public BausteinUmsetzung getNewElement() {
		return child;
	}

}
