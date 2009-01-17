package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;
import java.util.List;

import sernet.gs.model.Baustein;
import sernet.gs.model.Massnahme;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.common.model.BuildInput;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
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
	private Class type;
	private BausteinUmsetzung child;
	private Baustein baustein;

	public CreateBaustein(CnATreeElement container, Class<BausteinUmsetzung> type, Baustein baustein) {
		this.container = container;
		this.type = type;
		this.baustein = baustein;
	}
	
	public void execute() {
		IBaseDao<BausteinUmsetzung, Serializable> dao 
			= getDaoFactory().getDAO(type);
		try {
			BausteinUmsetzung bu = new BausteinUmsetzung(container);
			container.addChild(bu);
			
			bu.setKapitel(baustein.getId());
			bu.setName(baustein.getTitel());
			bu.setUrl(baustein.getUrl());
			bu.setStand(baustein.getStand());

			List<Massnahme> massnahmen = baustein
					.getMassnahmen();
			for (Massnahme mn : massnahmen) {
				createMassnahme(bu, mn);
			}
			// save massnahmen and changes:
			this.child = dao.merge(bu);
			
			
		} catch (Exception e) {
			throw new RuntimeCommandException(e);
		}
	}

	private void createMassnahme(BausteinUmsetzung bu, Massnahme mn) {
		MassnahmenUmsetzung mu = new MassnahmenUmsetzung(bu);
		mu.setKapitel(mn.getId());
		mu.setUrl(mn.getUrl());
		mu.setName(mn.getTitel());
		mu.setLebenszyklus(mn.getLZAsString());
		mu.setStufe(mn.getSiegelstufe());
		mu.setStand(mn.getStand());
		mu.setVerantwortlicheRollenInitiierung(mn.getVerantwortlichInitiierung());
		mu.setVerantwortlicheRollenUmsetzung(mn.getVerantwortlichUmsetzung());
		bu.addChild(mu);
	}

	public BausteinUmsetzung getNewElement() {
		return child;
	}

}
