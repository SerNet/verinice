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
package sernet.gs.ui.rcp.gsimport;

import java.io.File;
import java.sql.SQLException;

import sernet.gs.reveng.importData.GSVampire;
import sernet.gs.ui.rcp.main.CnAWorkspace;

public class AttachDbFileTask {
	private GSVampire vampire;

	public AttachDbFileTask() {
		File conf = new File(CnAWorkspace.getInstance().getConfDir()
				+ File.separator + "hibernate-vampire.cfg.xml");
		vampire = new GSVampire(conf.getAbsolutePath());
	}
	
	public void attachDBFile(String url, String user, String pass, String fileName, String newDbName) throws SQLException, ClassNotFoundException {
		if (fileName == null || fileName.length() <1){
			return;
		}
		vampire.attachFile(newDbName, fileName, url, user, pass);
	}
	
	
}
