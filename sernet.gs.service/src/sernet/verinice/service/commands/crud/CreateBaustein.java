/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
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
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.commands.crud;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.model.Baustein;
import sernet.gs.model.Massnahme;
import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.ChangeLoggingCommand;
import sernet.verinice.interfaces.IAuthAwareCommand;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IChangeLoggingCommand;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Permission;
import sernet.verinice.service.gstoolimport.MassnahmenFactory;

/**
 * Create and save new element of type baustein to the database using its class to lookup
 * the DAO from the factory.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 * @param <T>
 */
@SuppressWarnings("serial")
public class CreateBaustein extends ChangeLoggingCommand implements IChangeLoggingCommand, 
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
	private transient IAuthService authService;
    private String typeId;
    private String language;
    
	public CreateBaustein(CnATreeElement container, Baustein baustein, String language) {
		
		dbId = container.getDbId();
		typeId = container.getTypeId();
		
		this.baustein = baustein;
		stationId = ChangeLogEntry.STATION_ID;
		
		this.language = language;
		
	}
	
	public void execute() {
		IBaseDao<BausteinUmsetzung, Serializable> dao 
			= getDaoFactory().getDAO(BausteinUmsetzung.class);
		
		try {
	          IBaseDao<CnATreeElement, Integer> containerDao = getDaoFactory().getDAO(typeId);
	          CnATreeElement container = containerDao.findById(dbId);
			if(dbId == null || typeId == null || baustein == null){
			    getLogger().warn("Some parameter equals null, not importing current ITGS module");
			    throw new RuntimeCommandException("Some parameter was null, not importing current ITGS module");
			}
			
			
			if (container.containsBausteinUmsetzung(baustein.getId())){
			    // up to now no import of userdefined bausteine
			    // TODO: implement import of userdefined bausteine (and massnahmen)
			    GetElementPathCommand pathLoader = new GetElementPathCommand(container.getUuid(), container.getTypeId());
			    String elementPath = getCommandService().executeCommand(pathLoader).getResult();
			    getLogger().error("ElementContainer:\t" + elementPath + "(" + container.getDbId() + ")" + "\twith TypeId:\t" + typeId + " contains already a baustein with id:\t" + baustein.getId() + "\t" + baustein.getTitel() + " is skipped because of this");
				return;
			}
			
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
				massnahmenFactory.createMassnahmenUmsetzung(child, mn, language);
			}
			
			if (authService.isPermissionHandlingNeeded())
			{
				child.setPermissions(
					Permission.clonePermissionSet(
							child,
							container.getPermissions()));
				
				for (CnATreeElement elmt: child.getChildren()) {
					elmt.setPermissions(
							Permission.clonePermissionSet(
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
