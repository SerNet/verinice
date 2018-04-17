/*******************************************************************************
 * Copyright (c) 2010 Alexander Koderman <ak@sernet.de>.
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
package sernet.gs.ui.rcp.gsimport;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Preferences;

import sernet.gs.reveng.MbBaust;
import sernet.gs.reveng.importData.NotizenMassnahmeResult;
import sernet.gs.reveng.importData.ZielobjektTypeResult;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.snutils.DBException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.service.commands.task.ImportNotesForZielobjekt;
import sernet.verinice.service.gstoolimport.TransferData;

/**
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class ImportNotesTask extends AbstractGstoolImportTask  {
	
	private IProgress monitor;

	public void executeTask(int importType, IProgress monitor) throws DBException, CommandException {
		Preferences prefs = Activator.getDefault().getPluginPreferences();
		String sourceDbUrl = prefs.getString(PreferenceConstants.GS_DB_URL);
		if (sourceDbUrl.indexOf("odbc") > -1) {
			throw new DBException("Kann nicht direkt aus MDB-Datei importieren. Datenbank vorher anhängen in Menü \"Bearbeiten, Einstellungen\".");
		}
		this.monitor = monitor;	
		importNotes();
	}
	

	/**
	 * Import notes for existing elements, based on their exact name.
	 * @throws CommandException 
	 */
	private void importNotes() throws CommandException {
		List<ZielobjektTypeResult> allZielobjekte = getGstoolDao().findZielobjektTypAll();
		int n = allZielobjekte.size();
		int i = 0;
		monitor.beginTask("Importiere Notizen", n);
		for (ZielobjektTypeResult zielobjekt : allZielobjekte) {
			String name = zielobjekt.zielobjekt.getName();
			monitor.worked(1);
			i++;
			monitor.subTask(i + "/" + n + " - Zielobjekt: " + name);
			List<NotizenMassnahmeResult> notesResults = getGstoolDao().findNotizenForZielobjekt(name);
			Map<MbBaust, List<NotizenMassnahmeResult>> notizenMap = TransferData.convertZielobjektNotizenMap(notesResults);
			ImportNotesForZielobjekt command = new ImportNotesForZielobjekt(name, notizenMap);
			command = ServiceFactory.lookupCommandService().executeCommand(command);
		}
	}

}
