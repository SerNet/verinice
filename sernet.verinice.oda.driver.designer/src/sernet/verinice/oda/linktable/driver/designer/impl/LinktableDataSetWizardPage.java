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
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.eclipse.datatools.connectivity.oda.design.DataSetDesign;
import org.eclipse.datatools.connectivity.oda.design.DesignFactory;
import org.eclipse.datatools.connectivity.oda.design.ResultSetColumns;
import org.eclipse.datatools.connectivity.oda.design.ResultSetDefinition;
import org.eclipse.datatools.connectivity.oda.design.ui.designsession.DesignSessionUtil;
import org.eclipse.datatools.connectivity.oda.design.ui.wizards.DataSetWizardPage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import sernet.verinice.oda.linktable.driver.impl.Query;
import sernet.verinice.oda.linktable.driver.impl.ResultSetMetaData;

/**
 *
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class LinktableDataSetWizardPage extends DataSetWizardPage {

    private static final Logger LOG = Logger.getLogger(LinktableDataSetWizardPage.class);

    private static final String DEFAULT_MESSAGE = "Create a report query";

    private Text queryText;

    private String vlt;

    /**
     * Constructor
     * 
     * @param pageName
     */
    public LinktableDataSetWizardPage(String pageName) {
        super(pageName);
        setTitle(pageName);
        setMessage(DEFAULT_MESSAGE);
    }

    /**
     * Constructor
     * 
     * @param pageName
     * @param title
     * @param titleImage
     */
    public LinktableDataSetWizardPage(String pageName, String title, ImageDescriptor titleImage) {
        super(pageName, title, titleImage);
        setMessage(DEFAULT_MESSAGE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.datatools.connectivity.oda.design.ui.wizards.
     * DataSetWizardPage
     * #createPageCustomControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPageCustomControl(Composite parent) {
        setControl(createPageControl(parent));
        initializeControl();
    }

    /**
     * Creates custom control for user-defined query text.
     */
    private Control createPageControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));
        GridData gridData = new GridData(
                GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL);
        composite.setLayoutData(gridData);

        Label fieldLabel = new Label(composite, SWT.NONE);
        fieldLabel.setText("VLT file content:");

        queryText = new Text(composite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);

        GridData data = new GridData(GridData.FILL_BOTH);
        data = new GridData(GridData.FILL_BOTH);
        data.heightHint = 100;
        queryText.setLayoutData(data);
        queryText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                vlt = queryText.getText();
            }
        });

        return composite;
    }
    
    private void initializeControl() {
        /*
         * To optionally restore the designer state of the previous design
         * session, use getInitializationDesignerState();
         */

        // Restores the last saved data set design
        DataSetDesign dataSetDesign = getInitializationDesign();
        if (dataSetDesign == null)
            return; // nothing to initialize

        String query = dataSetDesign.getQueryText();
        

        if (query != null) {    
            queryText.setText((query == null ? "" : query));
        }
        validateData();
        setMessage(DEFAULT_MESSAGE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.datatools.connectivity.oda.design.ui.wizards.
     * DataSetWizardPage
     * #collectDataSetDesign(org.eclipse.datatools.connectivity.oda.design.
     * DataSetDesign)
     */
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
        } catch (OdaException e) {
            LOG.error("Error while creating data set design.", e);
        }
        return design;
    }

    /**
     * Updates the specified dataSetDesign with the latest design definition.
     * 
     * @throws OdaException
     */
    private void updateDesign(DataSetDesign dataSetDesign) throws OdaException {
        dataSetDesign.setQueryText(vlt);
        List<String> columnList = Query.getColumnList(dataSetDesign.getQueryText());
        IResultSetMetaData md = new ResultSetMetaData(
                columnList.toArray(new String[columnList.size()]));

        ResultSetColumns columns = DesignSessionUtil.toResultSetColumnsDesign(md);

        ResultSetDefinition resultSetDefn = DesignFactory.eINSTANCE.createResultSetDefinition();
        // resultSetDefn.setName( value ); // result set name
        resultSetDefn.setResultSetColumns(columns);

        // no exception in conversion; go ahead and assign to specified
        // dataSetDesign
        dataSetDesign.setPrimaryResultSet(resultSetDefn);
        dataSetDesign.getResultSets().setDerivedMetaData(true);
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
        boolean isValid = true;

        if (isValid) {
            setMessage(DEFAULT_MESSAGE);
        } else {
            setMessage("Requires input value.", ERROR);
        }

        setPageComplete(isValid);
    }

}
