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
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.IReportDepositService;
import sernet.verinice.interfaces.IReportTemplateService.OutputFormat;
import sernet.verinice.interfaces.ReportDepositException;
import sernet.verinice.model.report.ReportTemplateMetaData;

/**
 *
 */
public class AddReportToDepositDialog extends TitleAreaDialog {

    private static final Logger LOG = Logger.getLogger(AddReportToDepositDialog.class);

    private Text reportName;

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
    static final int MARGIN_WIDTH = 10;

    static final int DEFAULT_COL_NR = 3;

    private ReportTemplateMetaData editTemplate;

    private SelectionListener checkBoxSelectionListener;

    private Button allowMultipleRootObjects;

    /**
     * @param parentShell
     */
    public AddReportToDepositDialog(Shell parentShell) {
        super(parentShell);
        setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
    }

    public AddReportToDepositDialog(Shell parentShell, ReportTemplateMetaData metadata) {
        this(parentShell);
        this.editTemplate = metadata;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(
                isEditMode() ? Messages.ReportDepositView_17 : Messages.ReportDepositView_5);
        // newShell.setSize(SIZE_X, SIZE_Y);

        // open the window right under the mouse pointer:
        Point cursorLocation = Display.getCurrent().getCursorLocation();
        newShell.setLocation(
                new Point(cursorLocation.x - SIZE_X / 2, cursorLocation.y - SIZE_Y / 2));
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
        setTitle(isEditMode() ? Messages.ReportDepositView_17 : Messages.ReportDepositView_5);
        setMessage(isEditMode() ? Messages.ReportDepositView_18 : Messages.ReportDepositView_7);

        final Composite composite = (Composite) super.createDialogArea(parent);

        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Composite dialogContent = new Composite(composite, SWT.NONE);
        GridLayout threeColumnLayout = new GridLayout(3, false);
        dialogContent.setLayout(threeColumnLayout);
        dialogContent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        Label reportNameLabel = new Label(dialogContent, SWT.NONE);
        reportNameLabel.setText(Messages.ReportDepositView_1);
        GridData reportNameLabelGd = new GridData();
        reportNameLabelGd.horizontalAlignment = SWT.FILL;
        reportNameLabelGd.verticalAlignment = SWT.CENTER;
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

        allowMultipleRootObjects = new Button(dialogContent, SWT.CHECK);
        allowMultipleRootObjects.setText(Messages.ReportDepositView_25);
        GridData allowMultipleRootObjectsGd = new GridData();
        allowMultipleRootObjectsGd.horizontalAlignment = SWT.FILL;
        allowMultipleRootObjectsGd.grabExcessHorizontalSpace = false;
        allowMultipleRootObjectsGd.horizontalSpan = 3;
        allowMultipleRootObjects.setLayoutData(allowMultipleRootObjectsGd);

        Composite checkboxComposite = new Composite(dialogContent, SWT.NONE);
        GridLayout formatLayout = new GridLayout(3, false);
        checkboxComposite.setLayout(formatLayout);
        checkboxComposite.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false, 3, 1));
        GridData ofgd = new GridData();
        ofgd.horizontalAlignment = SWT.LEFT;
        ofgd.verticalAlignment = SWT.TOP;
        ofgd.grabExcessHorizontalSpace = true;
        ofgd.horizontalSpan = DEFAULT_COL_NR;

        GridData gdRadio = new GridData();
        gdRadio.grabExcessHorizontalSpace = true;
        gdRadio.horizontalSpan = 1;

        Group outputFormatGroup = new Group(dialogContent, SWT.NULL);
        outputFormatGroup.setText(Messages.ReportDepositView_2);

        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        outputFormatGroup.setLayout(gridLayout);

        GridData oflData = new GridData();
        oflData.horizontalSpan = 3;
        oflData.grabExcessHorizontalSpace = true;
        outputFormatGroup.setLayoutData(oflData);

        // Label outputFormatLabel = new Label(checkboxComposite, SWT.NONE);
        // outputFormatLabel.setText(Messages.ReportDepositView_2);

        outputTypePDFCheckbox = new Button(outputFormatGroup, SWT.CHECK);
        outputTypePDFCheckbox.setText(OutputFormat.PDF.toString());

        outputTypeHTMLCheckbox = new Button(outputFormatGroup, SWT.CHECK);
        outputTypeHTMLCheckbox.setText(OutputFormat.HTML.toString());

        outputTypeWordCheckbox = new Button(outputFormatGroup, SWT.CHECK);
        outputTypeWordCheckbox.setText(OutputFormat.DOC.toString());

        outputTypeODTCheckbox = new Button(outputFormatGroup, SWT.CHECK);
        outputTypeODTCheckbox.setText(OutputFormat.ODT.toString());

        outputTypeODSCheckbox = new Button(outputFormatGroup, SWT.CHECK);
        outputTypeODSCheckbox.setText(OutputFormat.ODS.toString());

        outputTypeExcelCheckbox = new Button(outputFormatGroup, SWT.CHECK);
        outputTypeExcelCheckbox.setText(OutputFormat.XLS.toString());

        Label templateLabel = new Label(dialogContent, SWT.NONE);
        templateLabel.setText(Messages.ReportDepositView_3);
        templateLabel.setLayoutData(reportNameLabelGd);

        reportTemplateText = new Text(dialogContent, SWT.BORDER);
        GridData reportTemplateTextGd = new GridData();
        reportTemplateTextGd.horizontalAlignment = SWT.FILL;
        reportTemplateTextGd.verticalAlignment = SWT.CENTER;
        reportTemplateTextGd.grabExcessHorizontalSpace = true;
        reportTemplateTextGd.horizontalSpan = 1;
        reportTemplateText.setLayoutData(reportTemplateTextGd);

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

        if (isEditMode()) {
            prepareEditMode();
        }

        return composite;
    }

    private void prepareEditMode() {
        reportName.setText(editTemplate.getOutputname());
        reportName.addListener(SWT.CHANGED, new Listener() {
            public void handleEvent(Event event) {
                if (!reportName.getText().isEmpty()
                        && !reportName.getText().equals(editTemplate.getOutputname())) {
                    event.doit = true;
                    getButton(IDialogConstants.OK_ID).setEnabled(true);

                } else if (reportName.getText().equals(editTemplate.getOutputname())) {
                    event.doit = false;
                    getButton(IDialogConstants.OK_ID).setEnabled(false);
                }
            }
        });

        outputTypePDFCheckbox = checkboxEditMode(outputTypePDFCheckbox, OutputFormat.PDF);
        outputTypeHTMLCheckbox = checkboxEditMode(outputTypeHTMLCheckbox, OutputFormat.HTML);
        outputTypeWordCheckbox = checkboxEditMode(outputTypeWordCheckbox, OutputFormat.DOC);
        outputTypeODTCheckbox = checkboxEditMode(outputTypeODTCheckbox, OutputFormat.ODT);
        outputTypeODSCheckbox = checkboxEditMode(outputTypeODSCheckbox, OutputFormat.ODS);
        outputTypeExcelCheckbox = checkboxEditMode(outputTypeExcelCheckbox, OutputFormat.XLS);
        reportTemplateText.setText(editTemplate.getFilename());
        reportTemplateText.setEnabled(false);
        reportTemplateSelectButton.setEnabled(false);
        reportTemplateSelectButton.setEnabled(false);
        allowMultipleRootObjects.setSelection(editTemplate.isMultipleRootObjects());
        allowMultipleRootObjects.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (editTemplate.isMultipleRootObjects() != allowMultipleRootObjects
                        .getSelection()) {
                    e.doit = true;
                    getButton(IDialogConstants.OK_ID).setEnabled(true);

                } else {
                    e.doit = false;
                    getButton(IDialogConstants.OK_ID).setEnabled(false);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });
    }

    private Button checkboxEditMode(Button checkbox, OutputFormat format) {
        checkbox.setSelection(isOutputFormatPreSelected(format));
        checkbox.addSelectionListener(getCheckBoxSelectionListener());
        return checkbox;
    }

    @Override
    protected void okPressed() {
        if (!isAnyFormatSelected() || getReportOutputName() == null
                || getSelectedDesginFile() == null) {
            ExceptionUtil.log(new RuntimeException(), Messages.ReportDepositView_12);
            return;
        }
        if (isEditMode()) {
            updateTemplate();
        } else {
            addTemplate();
        }
        super.okPressed();

    }

    private void updateTemplate() {
        try {
            ReportTemplateMetaData metaData = new ReportTemplateMetaData(
                    FilenameUtils.getName(getSelectedDesginFile()), getReportOutputName(),
                    getReportOutputFormats(), true, null, allowMultipleRootObjects.getSelection());
            getReportService().update(metaData, getLanguage());
        } catch (ReportDepositException e) {
            LOG.error("Error while updating report template file", e); //$NON-NLS-1$
            ExceptionUtil.log(e, Messages.ReportDepositView_23);
        }
    }

    private void addTemplate() {
        try {
            byte[] rptDesignFile = FileUtils.readFileToByteArray(new File(getSelectedDesginFile()));
            ReportTemplateMetaData metaData = new ReportTemplateMetaData(
                    FilenameUtils.getName(getSelectedDesginFile()), getReportOutputName(),
                    getReportOutputFormats(), true, null, allowMultipleRootObjects.getSelection());
            getReportService().add(metaData, rptDesignFile, getLanguage());
        } catch (IOException | ReportDepositException e) {
            LOG.error("Error while adding new report template file", e); //$NON-NLS-1$
            ExceptionUtil.log(e, Messages.AddReportToDepositDialog_3);
        }
    }

    private String getLanguage() {
        return Locale.getDefault().getLanguage();
    }

    public void selectTemplateFile() {
        FileDialog dlg = new FileDialog(getParentShell(), SWT.SELECTED);
        String defaultTemplatePath = Activator.getDefault().getPreferenceStore()
                .getString(PreferenceConstants.DEFAULT_TEMPLATE_FOLDER_REPORT);
        if (defaultTemplatePath != null && !defaultTemplatePath.isEmpty()) {
            dlg.setFilterPath(defaultTemplatePath);
        }
        ArrayList<String> extensionList = new ArrayList<>();
        extensionList.add("*.rptdesign"); //$NON-NLS-1$
        extensionList.add("*.*"); //$NON-NLS-1$
        dlg.setFilterExtensions(extensionList.toArray(new String[extensionList.size()]));
        String fn = dlg.open();
        if (fn != null) {
            reportTemplateText.setText(fn);
            if (reportName.getText() == null || reportName.getText().isEmpty()) {
                reportName.setText(StringUtils.capitalize(FilenameUtils.getBaseName(fn)));
            }
            getButton(IDialogConstants.OK_ID).setEnabled(true);
        }
    }

    private String getSelectedDesginFile() {
        return reportTemplateText.getText();
    }

    private String getReportOutputName() {
        return reportName.getText();
    }

    private OutputFormat[] getReportOutputFormats() {
        ArrayList<OutputFormat> list = new ArrayList<>(0);
        if (outputTypeExcelCheckbox.getSelection()) {
            list.add(OutputFormat.XLS);
        }
        if (outputTypeHTMLCheckbox.getSelection()) {
            list.add(OutputFormat.HTML);
        }
        if (outputTypeWordCheckbox.getSelection()) {
            list.add(OutputFormat.DOC);
        }
        if (outputTypePDFCheckbox.getSelection()) {
            list.add(OutputFormat.PDF);
        }
        if (outputTypeODTCheckbox.getSelection()) {
            list.add(OutputFormat.ODT);
        }
        if (outputTypeODSCheckbox.getSelection()) {
            list.add(OutputFormat.ODS);
        }
        return list.toArray(new OutputFormat[list.size()]);
    }

    private boolean outputFormatEquals() {
        if (editTemplate != null) {
            return Arrays.equals(getReportOutputFormats(), editTemplate.getOutputFormats());
        }

        return false;
    }

    private boolean isAnyFormatSelected() {
        return outputTypeExcelCheckbox.getSelection() || outputTypeHTMLCheckbox.getSelection()
                || outputTypeODSCheckbox.getSelection() || outputTypeODTCheckbox.getSelection()
                || outputTypePDFCheckbox.getSelection() || outputTypeWordCheckbox.getSelection();
    }

    private boolean isEditMode() {
        return editTemplate != null;
    }

    private boolean isOutputFormatPreSelected(OutputFormat format) {
        return Arrays.asList(editTemplate.getOutputFormats()).contains(format);
    }

    private SelectionListener getCheckBoxSelectionListener() {
        if (checkBoxSelectionListener == null) {
            checkBoxSelectionListener = new SelectionListener() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (!outputFormatEquals()) {
                        e.doit = true;
                        getButton(IDialogConstants.OK_ID).setEnabled(true);

                    } else {
                        e.doit = false;
                        getButton(IDialogConstants.OK_ID).setEnabled(false);
                    }
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                    widgetSelected(e);
                }
            };
        }
        return checkBoxSelectionListener;
    }

    private IReportDepositService getReportService() {
        return ServiceFactory.lookupReportDepositService();
    }
}
