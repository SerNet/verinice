/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin <dm[at]sernet[dot]de>.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.taskcommands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.service.RuntimeCommandException;
import sernet.gs.ui.rcp.main.bsi.model.TodoViewItem;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;

/**
 * Loads a single control and wraps it in a {@link TodoViewItem}.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("serial")
public class FindMassnahmeById extends FindMassnahmenAbstract {

	private static final Logger log = Logger.getLogger(FindMassnahmeById.class);

	private Integer dbId = null;
	
	
	public FindMassnahmeById(Integer dbId) {
		super();
		this.dbId = dbId;
	}
	
	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.ICommand#execute()
	 */
	public void execute() {
		try {
			List<MassnahmenUmsetzung> list = new ArrayList<MassnahmenUmsetzung>();
			IBaseDao<MassnahmenUmsetzung, Serializable> dao = getDaoFactory().getDAO(MassnahmenUmsetzung.class);
			list.add(dao.findById(this.dbId));
			
			// create display items:
			fillList(list);
		} catch (CommandException e) {
			throw new RuntimeCommandException(e);
		}
	}
}
