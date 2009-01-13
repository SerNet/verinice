package sernet.gs.ui.rcp.main.service.migrationcommands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.internal.resources.mapping.ChangeDescription;

import sernet.gs.model.Baustein;
import sernet.gs.model.Massnahme;
import sernet.gs.service.GSServiceException;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.model.Anwendung;
import sernet.gs.ui.rcp.main.bsi.model.BSIMassnahmenModel;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.Client;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.bsi.model.Server;
import sernet.gs.ui.rcp.main.bsi.model.SonstIT;
import sernet.gs.ui.rcp.main.bsi.model.TelefonKomponente;
import sernet.gs.ui.rcp.main.bsi.views.BSIKatalogInvisibleRoot;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.DbVersion;
import sernet.gs.ui.rcp.main.common.model.IProgress;
import sernet.gs.ui.rcp.main.ds.model.IDatenschutzElement;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.commands.RuntimeCommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadBSIModelComplete;
import sernet.gs.ui.rcp.main.service.crudcommands.SaveElement;
import sernet.gs.ui.rcp.main.service.crudcommands.UpdateMultipleElements;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;
import sernet.hui.common.connect.PropertyType;

/**
 * Converts old massnahmenumsetzungen to new ones: add roles definitions.
 * 
 * @author koderman@sernet.de
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
						.debug("Keine Vorlage gefunden für Massnahme " + cnATreeElement.getTitel());
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

		UpdateMultipleElements command3 = new UpdateMultipleElements(changedElements);
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
