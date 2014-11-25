package sernet.gs.ui.rcp.main.bsi.wizards;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import de.sernet.sync.data.SyncAttribute;
import de.sernet.sync.data.SyncData;
import de.sernet.sync.data.SyncObject;
import de.sernet.sync.mapping.SyncMapping;
import de.sernet.sync.mapping.SyncMapping.MapObjectType.MapAttributeType;
import de.sernet.sync.sync.SyncRequest;

public class ImportCSVWizard extends Wizard {
    private static final Logger LOG = Logger.getLogger(ImportCSVWizard.class);
    
	private EntitySelectionPage entityPage;
	private PropertiesSelectionPage propertyPage;

    private String sourceId;
    
    private SyncRequest sr = null;

    public ImportCSVWizard() {
		super();
		propertyPage = new PropertiesSelectionPage(Messages.EntitySelectionPage_0);
		entityPage = new EntitySelectionPage(Messages.PropertiesSelectionPage_0);
	}

    public boolean getInsertState() {
        return entityPage.getInsertState();
    }
    
    public boolean getUpdateState() {
        return entityPage.getUpdateState();
    }
    
    public boolean getDeleteState() {
        return entityPage.getDeleteState();
    }
	
	public SyncRequest getSyncRequest() {
        return sr;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
    public boolean performFinish() {
	    try {
	        sr = createSyncRequest();            		
	    } catch (Exception e) {
            LOG.error("Error while importing CSV data.", e); //$NON-NLS-1$
            ExceptionUtil.log(e, Messages.ImportCSVWizard_1);
        }
		return true;
	}
	
	@Override
    public void addPages() {
		addPage(entityPage);
		addPage(propertyPage);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#getNextPage(org.eclipse.jface.wizard.IWizardPage)
	 */
	@Override
    public IWizardPage getNextPage(IWizardPage page) {
		if (page == entityPage) {
		    try {
    		    propertyPage.setEntityName(entityPage.getEntityName());
    		    propertyPage.setEntityId(entityPage.getEntityNameId());             
    		    propertyPage.setCSVDatei(entityPage.getCSVDatei());
    			setSourceId(entityPage.getSourceIdText().getText());
    			propertyPage.setSeparator(entityPage.getSeparatorCombo().getText().charAt(0));
    			propertyPage.setCharset(Charset.forName(entityPage.getCharsetCombo().getText()));
    			propertyPage.setPageComplete(false);
    			String[] firstLine = propertyPage.getFirstLine();
    			if(firstLine==null || firstLine.length==0) {
    			    final String message = NLS.bind(Messages.ImportCSVWizard_2, entityPage.getCSVDatei().getPath());
    			    LOG.warn(message);
    			    MessageDialog.openWarning(this.getShell(), Messages.ImportCSVWizard_3, message);
    			} else {
    			    if(firstLine[0]==null || !firstLine[0].toLowerCase().contains("ext")) { //$NON-NLS-1$
    			        final String message = Messages.ImportCSVWizard_5;
                        LOG.warn(message + " File: " + entityPage.getCSVDatei().getPath()); //$NON-NLS-1$
                        entityPage.setWarning(message);
    			    } else {
    			        entityPage.setWarning(""); //$NON-NLS-1$
    			    }
    			}
                propertyPage.fillTable();
            } catch (Exception e) {
                LOG.error("Error while creating next wizard page.", e); //$NON-NLS-1$
            }
		}
		return super.getNextPage(page);
	}

	/**
	 * Creates {@link SyncRequest} instance out of the values and settings given in
	 * <code>propertyPage</code>.
	 * 
	 * @return
	 * @throws IOException 
	 */
	private SyncRequest createSyncRequest() throws IOException {
		String entityName = propertyPage.getEntityName();

		SyncRequest sr = new SyncRequest();
		sr.setSourceId(getSourceId());
		
		// Prepare mapping
		SyncMapping sm = new SyncMapping();
		sr.setSyncMapping(sm);

		List<SyncMapping.MapObjectType> mots = sm.getMapObjectType();
		SyncMapping.MapObjectType mot = new SyncMapping.MapObjectType();
		mot.setExtId(entityName);
		mot.setIntId(entityPage.getEntityNameId());
		
		// list of properties user has mapped
		List<String> extIdList = new LinkedList<String>();
		for (List<String> properties : propertyPage.getPropertyTable()) {
            String intId = properties.get(0);
		    String extId = properties.get(1);
		    if(isValid(intId,extId)) {
    			MapAttributeType mat = new MapAttributeType();
    			mat.setExtId(extId);
    			extIdList.add(extId);
    			mat.setIntId(intId);
    			mot.getMapAttributeType().add(mat);
		    }
		}
		mots.add(mot);

		// Prepare data
		SyncData sd = new SyncData();
		sr.setSyncData(sd);
		String[] columns = propertyPage.getFirstLine();
		int i = 0;
		List<SyncObject> syncObjects = sd.getSyncObject();
		for (List<String> tableLine : propertyPage.getContent()) {
			SyncObject so = new SyncObject();		
			so.setExtObjectType(entityName);
			List<SyncAttribute> syncAttributes = so.getSyncAttribute();
			int j = 0;
			for (String tableCell : tableLine) {
			    if(j==0) {
			        so.setExtId(tableCell);
			    } else if(extIdList.contains(columns[j])) { /* only import columns the user has mapped */
    				SyncAttribute sa = new SyncAttribute();
    				sa.setName(columns[j]);
    				
    				// TODO: Table element should actually be a list of values.
    				// With this approach the import will be fine but the CSV format
    				// will not have the ability to properly represent verinice's
    				// way of handling multiple property values (which are lists internally).
    				sa.getValue().add(tableCell);
    
    				syncAttributes.add(sa);
			    }
				j++;
			}
			syncObjects.add(so);
			i++;
		}
		return sr;
	}

    private boolean isValid(String intId, String extId) {       
        return intId!=null && extId!=null && !intId.trim().isEmpty() && !extId.trim().isEmpty();
    }

    public String getSourceId() {
        return sourceId;
    }
    
    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }
    
}
