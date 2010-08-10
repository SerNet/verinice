package sernet.gs.ui.rcp.main.bsi.wizards;

import java.util.List;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.sync.commands.SyncCommand;
import sernet.verinice.interfaces.CommandException;
import de.sernet.sync.data.SyncData;
import de.sernet.sync.data.SyncData.SyncObject;
import de.sernet.sync.data.SyncData.SyncObject.SyncAttribute;
import de.sernet.sync.mapping.SyncMapping;
import de.sernet.sync.mapping.SyncMapping.MapObjectType.MapAttributeType;
import de.sernet.sync.sync.SyncRequest;

public class ImportCSVWizard extends Wizard {
	private EntitySelectionPage entityPage;
	private PropertiesSelectionPage propertyPage;

	public ImportCSVWizard() {
		super();
		propertyPage = new PropertiesSelectionPage("Select properties");
		entityPage = new EntitySelectionPage("Select entity");
	}

	@Override
	public boolean performFinish() {
		Activator.inheritVeriniceContextState();

		SyncRequest sr = createSyncRequest();

		SyncCommand command = new SyncCommand(sr);
		try {
			command = ServiceFactory.lookupCommandService().executeCommand(
					command);

		} catch (CommandException e) {
			throw new IllegalStateException(e);
		}

		return true;
	}

	public void addPages() {
		addPage(entityPage);
		addPage(propertyPage);
	}

	public IWizardPage getNextPage(IWizardPage page) {
		if (page == entityPage) {
			propertyPage.setEntityName(entityPage.getEntityName());
			propertyPage.setCSVDatei(entityPage.getCSVDatei());
			propertyPage.fillTable();
		}
		return super.getNextPage(page);
	}

	/**
	 * Creates {@link SyncRequest} instance out of the values and settings given in
	 * <code>propertyPage</code>.
	 * 
	 * @return
	 */
	private SyncRequest createSyncRequest() {
		String entityName = propertyPage.getEntityName();

		SyncRequest sr = new SyncRequest();
		
		// Set basic parameters
		sr.setInsert(entityPage.getInsertState());
		sr.setUpdate(entityPage.getUpdateState());
		sr.setDelete(entityPage.getDeleteState());
		sr.setSourceId(entityPage.getSourceId());

		// Prepare mapping
		SyncMapping sm = new SyncMapping();
		sr.setSyncMapping(sm);

		List<SyncMapping.MapObjectType> mots = sm.getMapObjectType();
		SyncMapping.MapObjectType mot = new SyncMapping.MapObjectType();
		mot.setExtId(entityName);
		mot.setIntId(entityPage.getEntityNameId());
		
		for (List<String> properties : propertyPage.getPropertyTable()) {
			MapAttributeType mat = new MapAttributeType();
			mat.setExtId(properties.get(1));
			mat.setIntId(properties.get(0));
			mot.getMapAttributeType().add(mat);
		}
		mots.add(mot);

		// Prepare data
		SyncData sd = new SyncData();
		sr.setSyncData(sd);
		String[] columns = propertyPage.getPropertyColumns();
		int i = 0;
		List<SyncObject> syncObjects = sd.getSyncObject();
		for (List<String> tableLine : propertyPage.getInhaltDerDatei()) {
			SyncObject so = new SyncObject();
			so.setExtId(entityName + i);
			so.setExtObjectType(entityName);

			List<SyncAttribute> syncAttributes = so.getSyncAttribute();

			int j = 0;
			for (String tableElement : tableLine) {
				SyncAttribute sa = new SyncAttribute();
				sa.setName(columns[j]);
				sa.setValue(tableElement);

				syncAttributes.add(sa);
				j++;
			}

			syncObjects.add(so);
			i++;
		}

		return sr;
	}
}
