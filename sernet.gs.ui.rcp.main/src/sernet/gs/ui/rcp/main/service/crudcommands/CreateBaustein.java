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
package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.model.Baustein;
import sernet.gs.model.Massnahme;
import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.RuntimeCommandException;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenFactory;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.ChangeLoggingCommand;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IAuthAwareCommand;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IChangeLoggingCommand;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Permission;
import sernet.verinice.service.commands.LoadAncestors;

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
			
			if (container.containsBausteinUmsetzung(baustein.getId())){
			    // up to now no import of userdefined bausteine
			    // TODO: implement import of userdefined bausteine (and massnahmen)
			    getLogger().error("ElementContainer:\t" + getElementPath(container.getUuid(), typeId) + "(" + container.getDbId() + ")" + "\twith TypeId:\t" + typeId + " contains already a baustein with id:\t" + baustein.getId() + "\t" + baustein.getTitel() + " is skipped because of this");
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
	
	private String getElementPath(String uuid, String typeId){
        RetrieveInfo ri = RetrieveInfo.getPropertyInstance();

        LoadAncestors command = new LoadAncestors(typeId, uuid, ri);
        try {
            command = ServiceFactory.lookupCommandService().executeCommand(command);
        } catch (CommandException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        CnATreeElement current = command.getElement();

        // build object path
        StringBuilder sb = new StringBuilder();
        sb.insert(0, current.getTitle());

        while (current.getParent() != null) {
            current = current.getParent();
            sb.insert(0, "/");
            sb.insert(0, current.getTitle());
        }



        // crop the root element, which is always ISO .. or BSI ...
        String[] p = sb.toString().split("/");
        sb = new StringBuilder();
        for (int i = 1; i < p.length; i++) {
            sb.append("/").append(p[i]);
        }
        return sb.toString();
	}

}
