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
package sernet.gs.ui.rcp.main.service.grundschutzparser;

import java.util.List;

import sernet.gs.model.Baustein;
import sernet.gs.service.RuntimeCommandException;
import sernet.gs.ui.rcp.main.bsi.model.GSScraperUtil;
import sernet.gs.ui.rcp.main.common.model.IProgress;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.INoAccessControl;

@SuppressWarnings("serial")
public class LoadBausteine extends GenericCommand implements INoAccessControl {

	private List<Baustein> bausteine;

	public void execute() {
		try {
			bausteine = GSScraperUtil
			.getInstance()
			.getModel()
			.loadBausteine(new IProgress() {

				public void beginTask(String name, int totalWork) {
					// TODO Auto-generated method stub
					
				}

				public void done() {
					// TODO Auto-generated method stub
					
				}

				public void setTaskName(String string) {
					// TODO Auto-generated method stub
					
				}

				public void subTask(String string) {
					// TODO Auto-generated method stub
					
				}

				public void worked(int work) {
					// TODO Auto-generated method stub
					
				}
				
			});
		} catch (Exception e) {
			throw new RuntimeCommandException(e);
		}
	}

	public List<Baustein> getBausteine() {
		return bausteine;
	}

}
