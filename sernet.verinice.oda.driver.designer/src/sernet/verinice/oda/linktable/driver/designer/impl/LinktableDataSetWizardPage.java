/***************************************************************************
 * Copyright (c) 2016 Daniel Murygin.
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ***************************************************************************/
package sernet.verinice.oda.linktable.driver.designer.impl;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.datatools.connectivity.oda.IConnection;
import org.eclipse.datatools.connectivity.oda.IDriver;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.eclipse.datatools.connectivity.oda.design.DataSetDesign;
import org.eclipse.datatools.connectivity.oda.design.DesignFactory;
import org.eclipse.datatools.connectivity.oda.design.ResultSetColumns;
import org.eclipse.datatools.connectivity.oda.design.ResultSetDefinition;
import org.eclipse.datatools.connectivity.oda.design.ui.designsession.DesignSessionUtil;
import org.eclipse.datatools.connectivity.oda.design.ui.wizards.DataSetWizardPage;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.springframework.remoting.RemoteConnectFailureException;

import sernet.verinice.oda.driver.designer.Activator;
import sernet.verinice.oda.driver.impl.Driver;
import sernet.verinice.oda.linktable.driver.impl.Query;
import sernet.verinice.oda.linktable.driver.impl.ResultSetMetaData;
import sernet.verinice.rcp.linktable.LinkTableUtil;
import sernet.verinice.rcp.linktable.LinkTableValidationResult;
import sernet.verinice.rcp.linktable.ui.LinkTableComposite;
import sernet.verinice.service.linktable.vlt.VeriniceLinkTable;
import sernet.verinice.service.linktable.vlt.VeriniceLinkTableIO;
import sernet.verinice.service.model.IObjectModelService;

/**
 * Wizard page for vDesigner to edit a link table data set.
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class LinktableDataSetWizardPage extends DataSetWizardPage {

    private static final Logger log = Logger.getLogger(LinktableDataSetWizardPage.class);

    private static final String DEFAULT_MESSAGE = Messages.LinktableDataSetWizardPage_0;

    Composite composite;

    LinkTableComposite linkTableComposite;

    public LinktableDataSetWizardPage(String pageName) {
        super(pageName);
        setTitle(pageName);
        setMessage(DEFAULT_MESSAGE);
    }

    public LinktableDataSetWizardPage(String pageName, String title, ImageDescriptor titleImage) {
        super(pageName, title, titleImage);
        setMessage(DEFAULT_MESSAGE);
    }

    @Override
    public void createPageCustomControl(Composite parent) {
        setControl(createPageControl(parent));
        initializeControl();
    }

    /**
     * Creates custom control for user-defined query text.
     */
    private Control createPageControl(Composite parent) {
        composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));
        GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | 
                GridData.VERTICAL_ALIGN_FILL);
        composite.setLayoutData(gridData);

        addButtonComposite(composite);      
        Composite ltComposite = addLinkTableComposite(composite);
        
        GridData data = new GridData(GridData.FILL_BOTH); 
        data.heightHint = 100; 
        ltComposite.setLayoutData(data);
        
        return composite;
    }

    private void initializeControl() {
        validateData();
        setMessage(DEFAULT_MESSAGE);
    }
    
    private Composite addLinkTableComposite(Composite composite) { 
        VeriniceLinkTable linkTable;
        String query = getQueryFromDataSet();
        if(query!=null) {
            linkTable = VeriniceLinkTableIO.readContent(query);
        } else {
            linkTable = new VeriniceLinkTable.Builder().build();
        }
        IDriver customDriver = new Driver();
        IConnection customConn;
        IObjectModelService objectModelService = null;
        try {
            customConn = customDriver.getConnection(null);
            java.util.Properties connProps = DesignSessionUtil
                    .getEffectiveDataSourceProperties(getInitializationDesign()
                            .getDataSourceDesign());
            customConn.open(connProps);

            objectModelService = Activator.getDefault().getObjectModelService();
        } catch (OdaException e) {
            log.error("Error while opening the server connection.",e);
        } 

        try {
            linkTableComposite = new LinkTableComposite(linkTable, objectModelService, composite, false);
        } catch (RemoteConnectFailureException exception) {
            setErrorMessage(Messages.linktableDataSetWizardPage_snca_error);
            log.error("no connection to verinice server available", exception);
            setPageComplete(false);
            return composite;
        }
        GridLayoutFactory.fillDefaults().generateLayout(linkTableComposite); 
        return linkTableComposite;
    }
    
    private String getQueryFromDataSet() {
        DataSetDesign dataSetDesign = getInitializationDesign();
        if (dataSetDesign == null) {
            return null;
        }
        String query = dataSetDesign.getQueryText();
        if(query!=null && query.isEmpty()) {
            query = null;
        }
        return query;
    }

    @Override
    protected DataSetDesign collectDataSetDesign(DataSetDesign design) {
        if (getControl() == null) {
            return design;
        }
        if (!hasValidData()) {
            return null;
        }
        try {
            updateDesign(design);
        } catch (Exception e) {
            log.error("Error while creating data set design.", e); //$NON-NLS-1$
        }
        return design;
    }

    /**
     * Updates the specified dataSetDesign with the latest design definition.
     * 
     * @throws OdaException
     */
    private void updateDesign(DataSetDesign dataSetDesign) throws OdaException {
        String vlt = VeriniceLinkTableIO.getContent(linkTableComposite.getVeriniceLinkTable());
        dataSetDesign.setQueryText(vlt);
        List<String> columnList = Query.getColumnList(dataSetDesign.getQueryText());
        IResultSetMetaData md = new ResultSetMetaData(
                columnList.toArray(new String[columnList.size()]));

        ResultSetColumns columns = DesignSessionUtil.toResultSetColumnsDesign(md);

        ResultSetDefinition resultSetDefn = DesignFactory.eINSTANCE.createResultSetDefinition();
        resultSetDefn.setResultSetColumns(columns);

        // no exception in conversion; go ahead and assign to specified
        // dataSetDesign
        dataSetDesign.setPrimaryResultSet(resultSetDefn);
        dataSetDesign.getResultSets().setDerivedMetaData(true);
    }
    
    private void addButtonComposite(Composite composite) {
        
        if(isSNCALoaded()) {
            return;
        }

        Composite buttonComposite = new Composite(composite, SWT.NONE);
        buttonComposite.setLayout(new GridLayout(2, false));
        GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | 
                GridData.VERTICAL_ALIGN_FILL);
        buttonComposite.setLayoutData(gridData); 
        
        Button loadButton = createButton(buttonComposite, Messages.LinktableDataSetWizardPage_2, 
                Messages.LinktableDataSetWizardPage_3, true);  
        
        loadButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                loadVltFile();
            }
            
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });
        
        Button saveButton = createButton(buttonComposite, Messages.LinktableDataSetWizardPage_4, 
                Messages.LinktableDataSetWizardPage_5, false);
        saveButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                saveVltFile();
            }    

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }    
        });
    }

    private boolean isSNCALoaded() {
        return linkTableComposite == null;
    }
    
    public void loadVltFile() {
        final String filePath = LinkTableUtil.createVltFilePath(
                Display.getCurrent().getActiveShell(), 
                Messages.LinktableDataSetWizardPage_6, 
                SWT.OPEN, 
                null);
        if (filePath != null) { 
            linkTableComposite.setVeriniceLinkTable(VeriniceLinkTableIO.read(filePath));
            linkTableComposite.refresh();
        }
    }
    
    public void saveVltFile() {
        final String filePath = LinkTableUtil.createVltFilePath(
                Display.getCurrent().getActiveShell(), 
                Messages.LinktableDataSetWizardPage_7, 
                SWT.SAVE, 
                null);
        if (filePath != null) {
            VeriniceLinkTableIO.write(linkTableComposite.getVeriniceLinkTable(),filePath);         
        }
    }
   
    /**
     * Indicates whether the custom page has valid data to proceed with defining
     * a data set.
     */
    private boolean hasValidData() {
        validateData();

        return canLeave();
    }

    /**
     * Validates the user-defined value in the page control exists and not a
     * blank text. Set page message accordingly.
     */
    private void validateData() {

        if(isSNCALoaded()){
            setPageComplete(false);
            return;
        }

        LinkTableValidationResult validationResult = LinkTableUtil.isValidVeriniceLinkTable(linkTableComposite.getVeriniceLinkTable());
        boolean isValid = validationResult.isValid();
        if (isValid) {
            setMessage(DEFAULT_MESSAGE);
        } else {
            setMessage(Messages.LinktableDataSetWizardPage_8, ERROR);
            if (log.isInfoEnabled()) {
                log.info("Query is invalid: " + validationResult.getMessage()); //$NON-NLS-1$
            }
        }
        setPageComplete(true);
    }
    
    public Button createButton(Composite buttonComposite, String title, String toolTip, boolean grabExcessHorizontalSpace) {
        Button loadButton = new Button(buttonComposite, SWT.PUSH);
        loadButton.setText(title);
        loadButton.setToolTipText(toolTip);
        loadButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, grabExcessHorizontalSpace, true));
        return loadButton;
    }
    

}
