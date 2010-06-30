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
package sernet.gs.ui.rcp.main.service.migrationcommands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import sernet.gs.model.Baustein;
import sernet.gs.model.Massnahme;
import sernet.gs.service.GSServiceException;
import sernet.gs.service.RuntimeCommandException;
import sernet.gs.ui.rcp.main.bsi.views.BSIKatalogInvisibleRoot;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadBSIModelComplete;
import sernet.gs.ui.rcp.main.service.crudcommands.SaveElement;
import sernet.gs.ui.rcp.main.service.crudcommands.UpdateMultipleElements;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Converts old massnahmenumsetzungen to new ones: add roles definitions.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class MigrateDbTo0_93 extends DbMigration {


	
	private Map<String, Massnahme> massnahmen = new HashMap<String, Massnahme>();
	
	private List<CnATreeElement> changedElements = new ArrayList<CnATreeElement>();
	

	public void run() throws Exception {
		Logger.getLogger(this.getClass()).debug("Updating DB model to V 0.93.");
		
		LoadBSIModelComplete command2 = new LoadBSIModelComplete(true); /* include massnahmen */
		command2 = ServiceFactory.lookupCommandService().executeCommand(command2);
		List<CnATreeElement> allElements = command2.getModel().getAllElementsFlatList(true);

//		progress.beginTask("Migriere Datenbank auf Version 0.93", allElements.size());
		for (CnATreeElement cnATreeElement : allElements) {
//			progress.worked(1);
			if (cnATreeElement instanceof MassnahmenUmsetzung) {
				MassnahmenUmsetzung mnums = (MassnahmenUmsetzung)cnATreeElement;
				Massnahme vorlagenMassnahme = findMassnahme(mnums);
				if (vorlagenMassnahme == null) {
					Logger.getLogger(this.getClass())
						.debug("Keine Vorlage gefunden für Massnahme " + cnATreeElement.getTitle());
					continue;
				}
				
				mnums.setVerantwortlicheRollenUmsetzung(vorlagenMassnahme.getVerantwortlichUmsetzung());
				mnums.setVerantwortlicheRollenInitiierung(vorlagenMassnahme.getVerantwortlichInitiierung());
//				progress.setTaskName("Ergänze Rollen für " + mnums.getTitel());
				changedElements.add(mnums);
			}
		}
		
//		progress.beginTask("Speichere alle veränderten Objekte. Bitte warten...", IProgress.UNKNOWN_WORK);
		Logger.getLogger(this.getClass()).debug("Speichere alle veränderten Objekte. Bitte warten...");

		UpdateMultipleElements command3 = new UpdateMultipleElements(changedElements, ChangeLogEntry.STATION_ID);
		command3 = getCommandService().executeCommand(command3);
		
		BSIModel model = command2.getModel();
		model.setDbVersion(getVersion());
		SaveElement<BSIModel> command4 = new SaveElement<BSIModel>(model);
		command4 = getCommandService().executeCommand(command4);
		
//		progress.done();
		
		
	}

	private Massnahme findMassnahme(MassnahmenUmsetzung mnUms) throws GSServiceException {
		synchronized(massnahmen) {
			if (massnahmen.size() == 0) {
				List<Baustein> bausteine = BSIKatalogInvisibleRoot.getInstance().getBausteine();
				for (Baustein baustein : bausteine) {
					List<Massnahme> bstMassnahmen = baustein.getMassnahmen();
					for (Massnahme massnahme : bstMassnahmen) {
						massnahmen.put(massnahme.getUrl(), massnahme);
					}
				}
			}
		}
		
		return massnahmen.get(mnUms.getUrl());
	}

	@Override
	public double getVersion() {
		return 0.93D;
	}

	public void execute() {
		try {
			run();
		} catch (Exception e) {
			throw new RuntimeCommandException(e);
		}
	}


	
	

}
