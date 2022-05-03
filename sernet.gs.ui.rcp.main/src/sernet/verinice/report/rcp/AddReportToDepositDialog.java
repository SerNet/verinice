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
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import sernet.gs.service.RuntimeCommandException;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.IReportDepositService;
import sernet.verinice.interfaces.IReportTemplateService.OutputFormat;
import sernet.verinice.interfaces.ReportDepositException;
import sernet.verinice.interfaces.ReportTemplateServiceException;
import sernet.verinice.model.report.FileMetaData;
import sernet.verinice.model.report.ReportTemplateMetaData;
import sernet.verinice.model.report.ReportTemplateMetaData.ReportContext;

/**
 *
 */
public class AddReportToDepositDialog extends TitleAreaDialog {

    private static final String ERROR_MESSAGE_PREFIX = "\n • ";//$NON-NLS-1$

    private static final Logger LOG = Logger.getLogger(AddReportToDepositDialog.class);

    private Text reportName;
    private Combo reportContextCombo;

    private Button outputTypePDFCheckbox;
    private Button outputTypeHTMLCheckbox;
    private Button outputTypeWordCheckbox;
    private Button outputTypeODTCheckbox;
    private Button outputTypeODSCheckbox;
    private Button outputTypeExcelCheckbox;

    private Text reportTemplateText;

    private static final int SIZE_X = 150;
    private static final int SIZE_Y = 500;
    static final int MARGIN_WIDTH = 10;

    static final int DEFAULT_COL_NR = 3;

    private ReportTemplateMetaData editTemplate;

    private Button allowMultipleRootObjects;

    private boolean reportTemplateFilenameChanged = false;

    private Map<String, ReportTemplateMetaData> templateNames = Collections.emptyMap();

    private Button outputTypeDocxCheckbox;

    private Button outputTypeXLSXCheckbox;

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
        try {
            templateNames = getReportService().getReportTemplates(Locale.getDefault()).stream()
                    .collect(Collectors.toMap(ReportTemplateMetaData::getFilename,
                            Function.identity()));
        } catch (ReportTemplateServiceException e) {
            throw new RuntimeCommandException("report service not avalable");//$NON-NLS-1$
        }

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
        reportName.addListener(SWT.CHANGED, event -> updateDialog());

        SelectionListener updateDialogListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateDialog();
            }
        };

        Label reportContextLabel = new Label(dialogContent, SWT.NONE);
        reportContextLabel.setText(Messages.ReportMetaDataContext);

        GridData reportContextLabelGd = new GridData();
        reportContextLabelGd.horizontalAlignment = SWT.FILL;
        reportContextLabelGd.verticalAlignment = SWT.CENTER;
        reportContextLabelGd.grabExcessHorizontalSpace = false;
        reportContextLabelGd.horizontalSpan = 1;
        reportContextLabel.setLayoutData(reportNameLabelGd);

        reportContextCombo = new Combo(dialogContent, SWT.DROP_DOWN | SWT.READ_ONLY);
        reportContextCombo.setItems(Arrays.stream(ReportContext.values())
                .map(ReportContext::prettyString).collect(Collectors.toList())
                .toArray(new String[ReportContext.values().length]));
        GridData reportContextGd = new GridData();
        reportContextGd.horizontalAlignment = SWT.FILL;
        reportContextGd.verticalAlignment = SWT.TOP;
        reportContextGd.grabExcessHorizontalSpace = true;
        reportContextGd.horizontalSpan = 2;
        reportContextCombo.setLayoutData(reportContextGd);
        reportContextCombo.addSelectionListener(updateDialogListener);

        allowMultipleRootObjects = new Button(dialogContent, SWT.CHECK);
        allowMultipleRootObjects.setText(Messages.ReportDepositView_25);
        GridData allowMultipleRootObjectsGd = new GridData();
        allowMultipleRootObjectsGd.horizontalAlignment = SWT.FILL;
        allowMultipleRootObjectsGd.grabExcessHorizontalSpace = false;
        allowMultipleRootObjectsGd.horizontalSpan = 3;
        allowMultipleRootObjects.setLayoutData(allowMultipleRootObjectsGd);
        allowMultipleRootObjects.addSelectionListener(updateDialogListener);

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

        outputTypePDFCheckbox = new Button(outputFormatGroup, SWT.CHECK);
        outputTypePDFCheckbox.setText(OutputFormat.PDF.toString());
        outputTypePDFCheckbox.addSelectionListener(updateDialogListener);

        outputTypeHTMLCheckbox = new Button(outputFormatGroup, SWT.CHECK);
        outputTypeHTMLCheckbox.setText(OutputFormat.HTML.toString());
        outputTypeHTMLCheckbox.addSelectionListener(updateDialogListener);

        outputTypeODTCheckbox = new Button(outputFormatGroup, SWT.CHECK);
        outputTypeODTCheckbox.setText(OutputFormat.ODT.toString());
        outputTypeODTCheckbox.addSelectionListener(updateDialogListener);

        outputTypeODSCheckbox = new Button(outputFormatGroup, SWT.CHECK);
        outputTypeODSCheckbox.setText(OutputFormat.ODS.toString());
        outputTypeODSCheckbox.addSelectionListener(updateDialogListener);

        outputTypeDocxCheckbox = new Button(outputFormatGroup, SWT.CHECK);
        outputTypeDocxCheckbox.setText(OutputFormat.DOCX.toString());
        outputTypeDocxCheckbox.addSelectionListener(updateDialogListener);

        outputTypeXLSXCheckbox = new Button(outputFormatGroup, SWT.CHECK);
        outputTypeXLSXCheckbox.setText(OutputFormat.XLSX.toString());
        outputTypeXLSXCheckbox.addSelectionListener(updateDialogListener);

        outputTypeWordCheckbox = new Button(outputFormatGroup, SWT.CHECK);
        outputTypeWordCheckbox.setText(OutputFormat.DOC.toString());
        outputTypeWordCheckbox.addSelectionListener(updateDialogListener);

        outputTypeExcelCheckbox = new Button(outputFormatGroup, SWT.CHECK);
        outputTypeExcelCheckbox.setText(OutputFormat.XLS.toString());
        outputTypeExcelCheckbox.addSelectionListener(updateDialogListener);

        Label templateLabel = new Label(dialogContent, SWT.NONE);
        templateLabel.setText(Messages.ReportDepositView_3);
        templateLabel.setLayoutData(reportNameLabelGd);

        reportTemplateText = new Text(dialogContent, SWT.BORDER);
        reportTemplateText.setEditable(false);// otherwise we would need to
                                              // check if the file exist
        GridData reportTemplateTextGd = new GridData();
        reportTemplateTextGd.horizontalAlignment = SWT.FILL;
        reportTemplateTextGd.verticalAlignment = SWT.CENTER;
        reportTemplateTextGd.grabExcessHorizontalSpace = true;
        reportTemplateTextGd.horizontalSpan = 1;
        reportTemplateText.setLayoutData(reportTemplateTextGd);

        Button reportTemplateSelectButton = new Button(dialogContent, SWT.PUSH);
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
                reportTemplateFilenameChanged = editTemplate == null
                        || !editTemplate.getFilename().equals(getSelectedDesignFileName());
                updateDialog();
            }
        });

        if (isEditMode()) {
            prepareEditMode();
        }

        return composite;
    }

    private void prepareEditMode() {
        reportName.setText(editTemplate.getOutputname());
        outputTypePDFCheckbox = checkboxEditMode(outputTypePDFCheckbox, OutputFormat.PDF);
        outputTypeHTMLCheckbox = checkboxEditMode(outputTypeHTMLCheckbox, OutputFormat.HTML);
        outputTypeWordCheckbox = checkboxEditMode(outputTypeWordCheckbox, OutputFormat.DOC);
        outputTypeODTCheckbox = checkboxEditMode(outputTypeODTCheckbox, OutputFormat.ODT);
        outputTypeODSCheckbox = checkboxEditMode(outputTypeODSCheckbox, OutputFormat.ODS);
        outputTypeExcelCheckbox = checkboxEditMode(outputTypeExcelCheckbox, OutputFormat.XLS);
        outputTypeDocxCheckbox = checkboxEditMode(outputTypeDocxCheckbox, OutputFormat.DOCX);
        outputTypeXLSXCheckbox = checkboxEditMode(outputTypeXLSXCheckbox, OutputFormat.XLSX);
        reportTemplateText.setText(editTemplate.getFilename());
        allowMultipleRootObjects.setSelection(editTemplate.isMultipleRootObjects());
        reportContextCombo.setText(editTemplate.getContext().prettyString());
    }

    private Button checkboxEditMode(Button checkbox, OutputFormat format) {
        checkbox.setSelection(isOutputFormatPreSelected(format));
        return checkbox;
    }

    @Override
    protected void okPressed() {
        if (isEditMode()) {
            updateTemplate();
        } else {
            addTemplate();
        }
        super.okPressed();
    }

    private void updateTemplate() {
        try {
            FileMetaData fileMetaData = new FileMetaData(getSelectedDesignFileName(), null);
            ReportTemplateMetaData newMetaData = new ReportTemplateMetaData(fileMetaData,
                    getReportOutputName(), getReportOutputFormats(), true,
                    allowMultipleRootObjects.getSelection(), getContext());
            if (reportTemplateFilenameChanged) {
                byte[] rptDesignFile = FileUtils
                        .readFileToByteArray(new File(getSelectedDesginFile()));
                getReportService().update(editTemplate, rptDesignFile, newMetaData,
                        Locale.getDefault());
            } else {
                getReportService().update(newMetaData, Locale.getDefault());
            }
        } catch (ReportDepositException e) {
            LOG.error("Error while updating report template file", e); //$NON-NLS-1$
            ExceptionUtil.log(e, Messages.ReportDepositView_23);
        } catch (IOException e) {
            LOG.error("Error while accessing the template file", e); //$NON-NLS-1$
            ExceptionUtil.log(e, Messages.AddReportToDepositDialog_0);
        }
    }

    private void addTemplate() {
        try {
            byte[] rptDesignFile = FileUtils.readFileToByteArray(new File(getSelectedDesginFile()));
            FileMetaData fileMetaData = new FileMetaData(getSelectedDesignFileName(), null);
            ReportTemplateMetaData metaData = new ReportTemplateMetaData(fileMetaData,
                    getReportOutputName(), getReportOutputFormats(), true,
                    allowMultipleRootObjects.getSelection(), getContext());
            getReportService().add(metaData, rptDesignFile, Locale.getDefault());
        } catch (ReportDepositException e) {
            LOG.error("Error while adding new report template file", e); //$NON-NLS-1$
            ExceptionUtil.log(e, Messages.AddReportToDepositDialog_3);
        } catch (IOException e) {
            LOG.error("Error while accessing the template file", e); //$NON-NLS-1$
            ExceptionUtil.log(e, Messages.AddReportToDepositDialog_0);
        }
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
        }
    }

    private String getSelectedDesginFile() {
        return reportTemplateText.getText();
    }

    private String getSelectedDesignFileName() {
        return FilenameUtils.getName(getSelectedDesginFile());
    }

    private String getReportOutputName() {
        return reportName.getText();
    }

    private ReportContext getContext() {
        return ReportContext.fromString(reportContextCombo.getText());
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
        if (outputTypeDocxCheckbox.getSelection()) {
            list.add(OutputFormat.DOCX);
        }
        if (outputTypeXLSXCheckbox.getSelection()) {
            list.add(OutputFormat.XLSX);
        }
        return list.toArray(new OutputFormat[list.size()]);
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

    private IReportDepositService getReportService() {
        return ServiceFactory.lookupReportDepositService();
    }

    /**
     * Update the dialog state after data change.
     */
    private void updateDialog() {
        String errorMessage = buildErrorMessage();
        setErrorMessage(errorMessage);
        getButton(IDialogConstants.OK_ID).setEnabled(errorMessage == null);
    }

    /**
     * Build the error message, return null when the dialog contains no errors.
     */
    private String buildErrorMessage() {
        if (!templateExistRemote()) {
            ReportTemplateMetaData reportTemplateMetaData = templateNames
                    .get(getSelectedDesignFileName());
            return String.format(Messages.AddReportToDepositDialog_1,
                    reportTemplateMetaData.getFilename());
        }
        StringBuilder message = new StringBuilder();
        if (StringUtils.isEmpty(getSelectedDesginFile())) {
            message.append(ERROR_MESSAGE_PREFIX);
            message.append(Messages.AddReportToDepositDialog_5);
        }
        if (StringUtils.isEmpty(getReportOutputName())) {
            message.append(ERROR_MESSAGE_PREFIX);
            message.append(Messages.AddReportToDepositDialog_6);
        }
        if (reportContextCombo.getSelectionIndex() == -1) {
            message.append(ERROR_MESSAGE_PREFIX);
            message.append(Messages.AddReportToDepositDialog_7);
        }
        if (!isAnyFormatSelected()) {
            message.append(ERROR_MESSAGE_PREFIX);
            message.append(Messages.AddReportToDepositDialog_4);
        }
        if (message.length() == 0) {
            return null;
        }
        return Messages.ReportDepositView_12 + message.toString();
    }

    private boolean templateExistRemote() {
        String selectedDesginFile = getSelectedDesignFileName();
        if (isEditMode()) {
            return !templateNames.keySet().contains(selectedDesginFile)
                    || !reportTemplateFilenameChanged;
        } else {
            return !templateNames.keySet().contains(selectedDesginFile);
        }
    }
}