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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.model.Baustein;
import sernet.gs.model.Massnahme;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenFactory;
import sernet.gs.ui.rcp.main.common.model.ChangeLogEntry;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.Permission;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.IAuthService;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.main.service.commands.IAuthAwareCommand;
import sernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand;
import sernet.gs.ui.rcp.main.service.commands.RuntimeCommandException;

/**
 * Create and save new element of type baustein to the database using its class to lookup
 * the DAO from the factory.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 * @param <T>
 */
public class CreateBaustein extends GenericCommand implements IChangeLoggingCommand, 
	IAuthAwareCommand {

	private transient Logger log = Logger.getLogger(CreateBaustein.class);
	
	private Logger getLogger() {
		if(log==null) {
			log = Logger.getLogger(CreateBaustein.class);
		}
		return log;
	}
	
	private BausteinUmsetzung child;
	private Baustein baustein;
	private String stationId;
	private Integer dbId;
	private Class<? extends CnATreeElement> clazz;
	private transient IAuthService authService;

	public CreateBaustein(CnATreeElement container, Baustein baustein) {
		
		dbId = container.getDbId();
		clazz = container.getClass();
		
		this.baustein = baustein;
		stationId = ChangeLogEntry.STATION_ID;
		
	}
	
	public void execute() {
		IBaseDao<BausteinUmsetzung, Serializable> dao 
			= getDaoFactory().getDAO(BausteinUmsetzung.class);
		
		try {
			IBaseDao<? extends CnATreeElement, Serializable> containerDao = getDaoFactory().getDAO(clazz);
			CnATreeElement container = containerDao.findById(dbId);
			
			if (container.containsBausteinUmsetzung(baustein.getId()))
				return;
			
			
			MassnahmenFactory massnahmenFactory = new MassnahmenFactory();

			child = new BausteinUmsetzung(container);
			child = dao.merge(child, false);
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
			
			if (authService.isPermissionHandlingNeeded())
			{
				child.setPermissions(
					Permission.clonePermissions(
							child,
							container.getPermissions()));
				
				for (CnATreeElement elmt: child.getChildren()) {
					elmt.setPermissions(
							Permission.clonePermissions(
									elmt,
									container.getPermissions()));
				}
			}
			
			
		} catch (Exception e) {
			getLogger().error("Error while creating executing", e);
			throw new RuntimeCommandException(e);
		}
	}

	public BausteinUmsetzung getNewElement() {
		return child;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand#getChangeType()
	 */
	public int getChangeType() {
		return ChangeLogEntry.TYPE_INSERT;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand#getChangedElements()
	 */
	public List<CnATreeElement> getChangedElements() {
		ArrayList<CnATreeElement> result = new ArrayList<CnATreeElement>(1);
		result.add(child);
		return result;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand#getStationId()
	 */
	public String getStationId() {
		return stationId;
	}
	
	public IAuthService getAuthService() {
		return authService;
	}

	public void setAuthService(IAuthService service) {
		this.authService = service;
	}

}
