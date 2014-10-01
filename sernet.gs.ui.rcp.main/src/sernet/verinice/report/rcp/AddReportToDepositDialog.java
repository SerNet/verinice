/*******************************************************************************
 * Copyright (c) 2014 Sebastian Hagedorn <sh@sernet.de>.
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
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.report.rcp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IReportDepositService.OutputFormat;
import sernet.verinice.model.report.ReportTemplateMetaData;
import sernet.verinice.service.commands.AddReportTemplateToDepositCommand;

/**
 *
 */
public class AddReportToDepositDialog extends TitleAreaDialog {
    
    private static final Logger LOG = Logger.getLogger(AddReportToDepositDialog.class);
    
    private static final String OUTPUT_FORMAT_PDF_LABEL = "PDF";
    private static final String OUTPUT_FORMAT_HTML_LABEL = "HTML";
    private static final String OUTPUT_FORMAT_ODT_LABEL = "ODT";
    private static final String OUTPUT_FORMAT_ODS_LABEL = "ODS";
    private static final String OUTPUT_FORMAT_DOC_LABEL = "DOC";
    private static final String OUTPUT_FORMAT_XLS_LABEL = "XLS";
    
    private Text reportName;
    
    private Composite outputTypeComposite;
    
    private Button outputTypePDFCheckbox;
    private Button outputTypeHTMLCheckbox;
    private Button outputTypeWordCheckbox;
    private Button outputTypeODTCheckbox;
    private Button outputTypeODSCheckbox;
    private Button outputTypeExcelCheckbox;
    
    private Text reportTemplateText;
    private Button reportTemplateSelectButton;
    
    private static final int SIZE_X = 150;
    private static final int SIZE_Y = 500;
    final int marginWidth = 10;

    final int defaultColNr = 3;
    
    private String selectedDesginFile;
    
    // perhaps via buttonbar
    private Button cancelButton;
    private Button saveButton;
    
    private ReportTemplateMetaData editTemplate;

    /**
     * @param parentShell
     */
    public AddReportToDepositDialog(Shell parentShell) {
        super(parentShell);
        setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
    }
    
    public AddReportToDepositDialog(Shell parentShell,
            ReportTemplateMetaData metadata){
        this(parentShell);
        this.editTemplate = metadata;
    }
    
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.ReportDepositView_5);
        // newShell.setSize(SIZE_X, SIZE_Y);

        // open the window right under the mouse pointer:
        Point cursorLocation = Display.getCurrent().getCursorLocation();
        newShell.setLocation(new Point(cursorLocation.x - SIZE_X / 2, cursorLocation.y - SIZE_Y / 2));
    }
    
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        getButton(IDialogConstants.OK_ID).setEnabled(false);
        getButton(IDialogConstants.OK_ID).setText(Messages.ReportDepositView_9);
        getButton(IDialogConstants.CANCEL_ID).setText(Messages.ReportDepositView_10);
    }
    
    @Override
    protected Control createDialogArea(Composite parent) {
        setTitle(Messages.ReportDepositView_6);
        setMessage(Messages.ReportDepositView_7);

        final Composite composite = (Composite) super.createDialogArea(parent);
        GridLayout layout = (GridLayout) composite.getLayout();
        layout.marginWidth = marginWidth;
        layout.marginHeight = marginWidth;
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        
        Composite dialogContent = new Composite(composite, SWT.NONE);
        GridLayout threeColumnLayout = new GridLayout(3, false);
        dialogContent.setLayout(threeColumnLayout);
        dialogContent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        Label reportNameLabel = new Label(dialogContent, SWT.NONE);
        reportNameLabel.setText(Messages.ReportDepositView_1);
        GridData reportNameLabelGd = new GridData();
        reportNameLabelGd.horizontalAlignment = SWT.FILL;
        reportNameLabelGd.verticalAlignment = SWT.TOP;
        reportNameLabelGd.grabExcessHorizontalSpace = false;
        reportNameLabelGd.horizontalSpan = 1;
        reportNameLabel.setLayoutData(reportNameLabelGd);
        
        reportName = new Text(dialogContent, SWT.NONE | SWT.BORDER);
        GridData reportNameTextGd = new GridData();
        reportNameTextGd.horizontalAlignment = SWT.FILL;
        reportNameTextGd.verticalAlignment = SWT.TOP;
        reportNameTextGd.grabExcessHorizontalSpace = true;
        reportNameTextGd.horizontalSpan = 2;
        reportName.setLayoutData(reportNameTextGd);
        if(isEditMode()){
            reportName.setText(editTemplate.getOutputname());
        }
        


        Composite checkboxComposite = new Composite(dialogContent, SWT.NONE | SWT.BORDER);
        GridLayout formatLayout = new GridLayout(3, true);
        checkboxComposite.setLayout(formatLayout);
        checkboxComposite.setLayoutData(new GridData(GridData.FILL, SWT.LEFT, true, false, 3, 1));
        GridData ofgd = new GridData();
        ofgd.horizontalAlignment = SWT.LEFT;
        ofgd.verticalAlignment = SWT.TOP;
        ofgd.grabExcessHorizontalSpace = true;
        ofgd.horizontalSpan = defaultColNr;
        
        Label outputFormatLabel = new Label(checkboxComposite, SWT.NONE);
        outputFormatLabel.setText(Messages.ReportDepositView_2);
        outputFormatLabel.setLayoutData(ofgd);
        
        outputTypePDFCheckbox = new Button(checkboxComposite, SWT.CHECK);
        outputTypePDFCheckbox.setText(OUTPUT_FORMAT_PDF_LABEL);
        GridData gdRadio = new GridData();
        gdRadio.horizontalAlignment = SWT.LEFT;
        gdRadio.verticalAlignment = SWT.TOP;
        gdRadio.grabExcessHorizontalSpace = false;
        gdRadio.horizontalSpan = 1;
        outputTypePDFCheckbox.setLayoutData(gdRadio);
        if(isEditMode()){
            outputTypePDFCheckbox.setSelection(isOutputFormatPreSelected(OUTPUT_FORMAT_PDF_LABEL.toLowerCase()));
        }

        outputTypeHTMLCheckbox = new Button(checkboxComposite, SWT.CHECK);
        outputTypeHTMLCheckbox.setText(OUTPUT_FORMAT_HTML_LABEL);
        outputTypeHTMLCheckbox.setLayoutData(gdRadio);
        if(isEditMode()){
            outputTypeHTMLCheckbox.setSelection(isOutputFormatPreSelected(OUTPUT_FORMAT_HTML_LABEL.toLowerCase()));
        }

        outputTypeWordCheckbox = new Button(checkboxComposite, SWT.CHECK);
        outputTypeWordCheckbox.setText(OUTPUT_FORMAT_DOC_LABEL);
        outputTypeWordCheckbox.setLayoutData(gdRadio);
        if(isEditMode()){
            outputTypeWordCheckbox.setSelection(isOutputFormatPreSelected(OUTPUT_FORMAT_DOC_LABEL.toLowerCase()));
        }
        
        outputTypeODTCheckbox = new Button(checkboxComposite, SWT.CHECK);
        outputTypeODTCheckbox.setText(OUTPUT_FORMAT_ODT_LABEL);
        outputTypeODTCheckbox.setLayoutData(gdRadio);
        if(isEditMode()){
            outputTypeODTCheckbox.setSelection(isOutputFormatPreSelected(OUTPUT_FORMAT_ODT_LABEL.toLowerCase()));
        }
        
        outputTypeODSCheckbox = new Button(checkboxComposite, SWT.CHECK);
        outputTypeODSCheckbox.setText(OUTPUT_FORMAT_ODS_LABEL);
        outputTypeODSCheckbox.setLayoutData(gdRadio);
        if(isEditMode()){
            outputTypeODSCheckbox.setSelection(isOutputFormatPreSelected(OUTPUT_FORMAT_ODS_LABEL.toLowerCase()));
        }
        
        outputTypeExcelCheckbox = new Button(checkboxComposite, SWT.CHECK);
        outputTypeExcelCheckbox.setText(OUTPUT_FORMAT_XLS_LABEL);
        outputTypeExcelCheckbox.setLayoutData(gdRadio);
        if(isEditMode()){
            outputTypeExcelCheckbox.setSelection(isOutputFormatPreSelected(OUTPUT_FORMAT_XLS_LABEL.toLowerCase()));
        }
        
        Label templateLabel = new Label(dialogContent, SWT.NONE);
        templateLabel.setText(Messages.ReportDepositView_3);
        templateLabel.setLayoutData(reportNameLabelGd);
        
        reportTemplateText = new Text(dialogContent, SWT.NONE | SWT.BORDER);
        GridData reportTemplateTextGd = new GridData();
        reportTemplateTextGd.horizontalAlignment = SWT.FILL;
        reportTemplateTextGd.verticalAlignment = SWT.TOP;
        reportTemplateTextGd.grabExcessHorizontalSpace = true;
        reportTemplateTextGd.horizontalSpan = 1;
        reportTemplateText.setLayoutData(reportTemplateTextGd);
        if(isEditMode()){
            reportTemplateText.setText(editTemplate.getFilename());
        }
        
        reportTemplateSelectButton = new Button(dialogContent, SWT.PUSH);
        GridData selectButtonGd = new GridData();
        gdRadio.horizontalAlignment = SWT.RIGHT;
        gdRadio.verticalAlignment = SWT.TOP;
        gdRadio.grabExcessHorizontalSpace = false;
        gdRadio.horizontalSpan = 1;
        reportTemplateSelectButton.setLayoutData(selectButtonGd);
        reportTemplateSelectButton.setText(Messages.ReportDepositView_8);
        reportTemplateSelectButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                selectTemplateFile();
            }
        });
        if(isEditMode()){
            reportTemplateSelectButton.setEnabled(false);
        }
        
        
        return composite;
    }
    
    @Override
    protected void okPressed() {
        if(!isAnyFormatSelected() ||
                getReportOutputName() == null ||
                getSelectedDesginFile() == null){
            MessageDialog.openError(this.getParentShell(), Messages.ReportDepositView_11, Messages.ReportDepositView_12);
            return;
        }
        addTemplate();
        super.okPressed();
        
    }

    @Override
    protected void cancelPressed() {
//        resetFormValues();
        super.cancelPressed();
    }
    
    private void addTemplate(){
        byte[] rptDesign = new byte[0];
        try {
            rptDesign = FileUtils.readFileToByteArray(new File(getSelectedDesginFile()));
        if(rptDesign.length > 0){
            AddReportTemplateToDepositCommand command = 
                    new AddReportTemplateToDepositCommand(getReportOutputName(), getReportOutputFormats(), 
                            rptDesign, getSelectedDesginFile(), Activator.getDefault().isStandalone());
            ServiceFactory.lookupCommandService().executeCommand(command);
        }
        } catch (IOException e) {
            LOG.error("Error reading Template file", e);
        } catch (CommandException e){
            LOG.error("Error writing template to deposit", e);
        }
    }
    
    public void selectTemplateFile() {
        FileDialog dlg = new FileDialog(getParentShell(), SWT.SELECTED);
        ArrayList<String> extensionList = new ArrayList<String>();
        extensionList.add("*.rptdesign"); //$NON-NLS-1$
        extensionList.add("*.*"); //$NON-NLS-1$
        dlg.setFilterExtensions(extensionList.toArray(new String[extensionList.size()]));
        String fn = dlg.open();
        if (fn != null) {
            reportTemplateText.setText(fn);
            getButton(IDialogConstants.OK_ID).setEnabled(true);
        }
    }
    
    private String[] getSelectedOutputFormats(){
        return new String[]{};
    }

    private String getSelectedDesginFile() {
        return reportTemplateText.getText();
    }
    
    private String getReportOutputName(){
        return reportName.getText();
    }
    
    private OutputFormat[] getReportOutputFormats(){
            ArrayList<OutputFormat> list = new ArrayList<OutputFormat>(0);
            if(outputTypeExcelCheckbox.getSelection()){
                list.add(OutputFormat.XLS);
            }
            if(outputTypeHTMLCheckbox.getSelection()){
                list.add(OutputFormat.HTML);
            } 
            if(outputTypeWordCheckbox.getSelection()){
                list.add(OutputFormat.DOC);
            } 
            if(outputTypePDFCheckbox.getSelection()){
                list.add(OutputFormat.PDF);
            }
            if(outputTypeODTCheckbox.getSelection()){
                list.add(OutputFormat.ODT);
            }
            if(outputTypeODSCheckbox.getSelection()){
                list.add(OutputFormat.ODS);
            }
            return list.toArray(new OutputFormat[list.size()]);
    }
    
    private boolean isAnyFormatSelected(){
        return outputTypeExcelCheckbox.getSelection() ||
                outputTypeHTMLCheckbox.getSelection() ||
                outputTypeODSCheckbox.getSelection() ||
                outputTypeODTCheckbox.getSelection() ||
                outputTypePDFCheckbox.getSelection() ||
                outputTypeWordCheckbox.getSelection();
    }
    
    private boolean isEditMode(){
        return editTemplate != null;
    }
    
    private boolean isOutputFormatPreSelected(String format){
        return Arrays.asList(editTemplate.getOutputFormats()).contains(format);
    }
    
}
