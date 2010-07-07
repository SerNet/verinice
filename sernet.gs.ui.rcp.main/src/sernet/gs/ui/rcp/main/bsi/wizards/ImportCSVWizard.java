package sernet.gs.ui.rcp.main.bsi.wizards;

import java.io.File;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

import sernet.gs.ui.rcp.main.CreateXMLElement;
import sernet.gs.ui.rcp.main.Activator;
import sernet.hui.common.VeriniceContext;
import sernet.springclient.WebServiceClient;
import de.sernet.sync.data.SyncData;
import de.sernet.sync.mapping.SyncMapping;
import de.sernet.sync.sync.ISyncWS;
import de.sernet.sync.sync.StreamToFile;
import de.sernet.sync.sync.SyncRequest;


public class ImportCSVWizard extends Wizard{
	private EntitySelectionPage entityPage;
	private PropertiesSelectionPage propertyPage;
	private CreateXMLElement xmlDoc;
	private File syncRequestXML;
	
	public ImportCSVWizard(){
		super();
		propertyPage = new PropertiesSelectionPage("Select properties");
		entityPage = new EntitySelectionPage("Select entity");
		xmlDoc = new CreateXMLElement();
	}
	
	@Override
	public boolean performFinish() {
		xmlDoc.syncRequest(entityPage.getInsertState(), entityPage.getUpdateState(), entityPage.getDeleteState(), entityPage.getSourceId());
		xmlDoc.mapping(propertyPage.getEntityName(), entityPage.getEntityNameId(), propertyPage.getPropertyTable());
		xmlDoc.data(propertyPage.getEntityName(), propertyPage.getInhaltDerDatei(), propertyPage.getPropertyColumns());
		this.syncRequestXML = xmlDoc.getSyncRequestXML();
		xmlDoc.show();
		try {
            Activator.inheritVeriniceContextState();
            WebServiceClient syncService = (WebServiceClient) VeriniceContext
                            .get(VeriniceContext.WEB_SERVICE_CLIENT);
            System.out.println("send request!!!");
            syncService.simpleSendAndReceive(syncRequestXML);
	    } catch (Exception e) {
	    	System.out.println("Could not send request");
	        e.printStackTrace();
	    }
		return true;
	}
	
	public void addPages(){
		addPage(entityPage);
		addPage(propertyPage);
	}
	
	public IWizardPage getNextPage(IWizardPage page){
		if(page == entityPage) {
			propertyPage.setEntityName(entityPage.getEntityName());
			propertyPage.setCSVDatei(entityPage.getCSVDatei());
			propertyPage.fillTable();
		}
		return super.getNextPage(page);
	}
}
