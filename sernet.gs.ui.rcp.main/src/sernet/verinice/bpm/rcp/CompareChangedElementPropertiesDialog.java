/******************************************************************************* 
 * Copyright (c) 2016 Viktor Schmidt. 
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
 *     Viktor Schmidt <vschmidt[at]ckc[dot]de> - initial API and implementation 
 ******************************************************************************/
package sernet.verinice.bpm.rcp;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import sernet.gs.service.RetrieveInfo;
import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyOption;
import sernet.hui.common.connect.PropertyType;
import sernet.hui.common.multiselectionlist.OptionSelectionHelper;
import sernet.snutils.AssertException;
import sernet.snutils.FormInputParser;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.bpm.ITask;
import sernet.verinice.interfaces.bpm.ITaskService;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.LoadAncestors;

/**
 * @author Viktor Schmidt <vschmidt[at]ckc[dot]de>
 */
public class CompareChangedElementPropertiesDialog extends TitleAreaDialog {

    private static final Logger LOG = Logger.getLogger(CompareChangedElementPropertiesDialog.class);

    private static final int DIALOG_WIDTH = 1000;
    private static final int DIALOG_HEIGHT = 450;

    private String title;

    private final ITask task;
    private CnATreeElement element;
    private Map<String, String> changedElementProperties;

    /**
     * @param parentShell
     * @throws CommandException
     */
    public CompareChangedElementPropertiesDialog(Shell parentShell, ITask task) throws CommandException {
        super(parentShell);
        this.task = task;

        RetrieveInfo ri = RetrieveInfo.getPropertyInstance();
        LoadAncestors loadControl = new LoadAncestors(task.getElementType(), task.getUuid(), ri);
        loadControl = getCommandService().executeCommand(loadControl);
        element = loadControl.getElement();

        loadChangedElementPropertiesFromTask();

        int style = SWT.MAX | SWT.CLOSE | SWT.TITLE;
        style = style | SWT.BORDER | SWT.APPLICATION_MODAL;
        setShellStyle(style | SWT.RESIZE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.
     * Shell)
     */
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
        newShell.setText(Messages.NewQmIssueDialog_9);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.
     * swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        final int defaultMarginWidth = 10;
        setTitle(Messages.CompareTaskChangesAction_1);
        if (changedElementProperties.isEmpty()) {
            setMessage(Messages.bind(Messages.CompareTaskChangesAction_6, task.getElementTitle()));
        } else {
            setMessage(Messages.bind(Messages.CompareTaskChangesAction_2, task.getElementTitle()));
        }
        final Composite composite = (Composite) super.createDialogArea(parent);
        GridLayout layout = (GridLayout) composite.getLayout();
        layout.marginWidth = defaultMarginWidth;
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        composite.setLayoutData(gd);

        ScrolledComposite scrolledComposite = new ScrolledComposite(composite, SWT.V_SCROLL);
        scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        scrolledComposite.setExpandHorizontal(true);

        Composite innerComposite = new Composite(scrolledComposite, SWT.NONE);
        scrolledComposite.setContent(innerComposite);
        innerComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        innerComposite.setLayout(new GridLayout(1, false));

        addFormElements(innerComposite);

        scrolledComposite.setVisible(true);
        Point size = innerComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        innerComposite.setSize(size);

        // Build the separator line
        Label separator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
        separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        composite.pack();

        setDialogLocation();

        return composite;
    }

    private void addFormElements(Composite composite) {
        final int dialogWidthSubtrahend = 30;
        if (title != null) {
            final Label titleLabel = new Label(composite, SWT.NONE);

            FontData[] fD = titleLabel.getFont().getFontData();
            for (int i = 0; i < fD.length; i++) {
                fD[i].setStyle(SWT.BOLD);
            }
            Font newFont = new Font(getShell().getDisplay(), fD);
            titleLabel.setFont(newFont);
            GC gc = new GC(titleLabel);
            Point size = gc.textExtent(title);
            if (size.x > DIALOG_WIDTH - dialogWidthSubtrahend) {
                title = trimTitleByWidthSize(gc, title, DIALOG_WIDTH - dialogWidthSubtrahend) + "..."; //$NON-NLS-1$
            }
            titleLabel.setText(title);
        }

        if (!changedElementProperties.isEmpty()) {
            createGridComposite(composite);
        }
    }

    private String trimTitleByWidthSize(GC gc, String elementTitle, int width) {
        String newTitle = elementTitle.substring(0, elementTitle.length() - 1);
        Point size = gc.textExtent(newTitle + "..."); //$NON-NLS-1$
        if (size.x > width) {
            newTitle = trimTitleByWidthSize(gc, newTitle, width);
        }
        return newTitle;
    }

    private void createGridComposite(final Composite parent) {
        GridLayout compositeLayout = new GridLayout(3, false);
        compositeLayout.marginWidth = 5;
        parent.setLayout(compositeLayout);

        createGridCompositeHeader(parent);
        createGridCompositeData(parent);

        parent.pack();
    }

    private void createGridCompositeHeader(final Composite parent) {
        final Label fealdNameHeaderLabel = new Label(parent, SWT.NONE);
        fealdNameHeaderLabel.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_BOTH));
        fealdNameHeaderLabel.setText(Messages.CompareTaskChangesAction_3);

        final Label fealdOldHeaderLabel = new Label(parent, SWT.NONE);
        fealdOldHeaderLabel.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_BOTH));
        fealdOldHeaderLabel.setText(Messages.CompareTaskChangesAction_4);

        final Label fealdNewHeaderLabel = new Label(parent, SWT.NONE);
        fealdNewHeaderLabel.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_BOTH));
        fealdNewHeaderLabel.setText(Messages.CompareTaskChangesAction_5);
    }

    private void createGridCompositeData(final Composite parent) {
        HUITypeFactory typeFactory = HUITypeFactory.getInstance();
        EntityType entityType = typeFactory.getEntityType(element.getEntity().getEntityType());
        List<PropertyType> propertyTypes = entityType.getAllPropertyTypesSorted();

        for (PropertyType propertyType : propertyTypes) {
            if (changedElementProperties.containsKey(propertyType.getId())) {
                String oldValue = element.getPropertyValue(propertyType.getId());
                String newValue = changedElementProperties.get(propertyType.getId());

                if (StringUtils.isNotBlank(oldValue) || StringUtils.isNotBlank(newValue)) {
                    createLabelForProperty(parent, typeFactory, propertyType);
                    createTextForOldValue(parent, propertyType, oldValue);
                    createTextForNewValue(parent, propertyType, newValue);
                }
            }
        }
    }

    private void createLabelForProperty(final Composite parent, HUITypeFactory typeFactory, PropertyType propertyType) {
        final Label titleLabel = new Label(parent, SWT.NONE);
        titleLabel.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_BOTH));
        titleLabel.setText(typeFactory.getMessage(propertyType.getId()));
    }

    private void createTextForOldValue(final Composite parent, PropertyType propertyType, String oldValue) {
        if (propertyType.isSingleSelect() && StringUtils.isEmpty(oldValue)) {
            oldValue = sernet.hui.swt.widgets.Messages.getString(PropertyOption.SINGLESELECTDUMMYVALUE);
        }
        final Text oldText = new Text(parent, SWT.BORDER | SWT.WRAP);
        oldText.setEditable(false);
        oldText.setText(oldValue);
        GridData gridData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_BOTH);
        gridData.widthHint = DIALOG_WIDTH / 3;
        oldText.setLayoutData(gridData);
    }

    private void createTextForNewValue(final Composite parent, PropertyType propertyType, String value) {
        final Text newText = new Text(parent, SWT.BORDER | SWT.WRAP);
        newText.setEditable(false);

        if (propertyType.isReference()) {
            value = loadTextForReferenceProperty(propertyType, value);
            newText.setText(value);
        } else if (propertyType.isSingleSelect() || propertyType.isMultiselect()) {
            value = loadTextForOptionProperty(propertyType, value);
            newText.setText(value);
        } else if (propertyType.isDate()) {
            newText.setText(getDate(value));
        } else {
            newText.setText(value);
        }

        GridData gridData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_BOTH);
        gridData.widthHint = DIALOG_WIDTH / 3;
        newText.setLayoutData(gridData);
    }

    private String getDate(String value) {
        try {
            return FormInputParser.dateToString(new java.sql.Date(Long.parseLong(value)));
        } catch (NumberFormatException | AssertException e) {
            LOG.error("Invalid date", e);
            return value;
        }
    }

    private String loadTextForOptionProperty(PropertyType propertyType, String newValue) {
        if (propertyType.isSingleSelect() && StringUtils.isEmpty(newValue)) {
            return sernet.hui.swt.widgets.Messages.getString(PropertyOption.SINGLESELECTDUMMYVALUE);
        } else {
            String[] propertyOptions = newValue.split(",");
            return OptionSelectionHelper.loadOptionLabels(propertyType, Arrays.asList(propertyOptions));
        }
    }

    private String loadTextForReferenceProperty(PropertyType propertyType, String newValue) {
        String[] propertyOptions = newValue.split(",");
        return OptionSelectionHelper.loadReferenceLabels(propertyType, Arrays.asList(propertyOptions));
    }

    private void setDialogLocation() {
        Rectangle monitorArea = getShell().getDisplay().getPrimaryMonitor().getBounds();
        Rectangle shellArea = getShell().getBounds();
        int x = monitorArea.x + (monitorArea.width - shellArea.width) / 2;
        int y = monitorArea.y + (monitorArea.height - shellArea.height) / 2;
        getShell().setLocation(x, y);
    }

    private void loadChangedElementPropertiesFromTask() {
        changedElementProperties = (Map<String, String>) getTaskService().loadChangedElementProperties(task.getId());
        LOG.info("Loaded changes for element properties from task."); //$NON-NLS-1$
    }

    private ITaskService getTaskService() {
        return (ITaskService) VeriniceContext.get(VeriniceContext.TASK_SERVICE);
    }

    public ICommandService getCommandService() {
        return (ICommandService) VeriniceContext.get(VeriniceContext.COMMAND_SERVICE);
    }

    @Override
    protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
        if (id == IDialogConstants.CANCEL_ID) {
            return null;
        }
        return super.createButton(parent, id, label, defaultButton);
    }
}
