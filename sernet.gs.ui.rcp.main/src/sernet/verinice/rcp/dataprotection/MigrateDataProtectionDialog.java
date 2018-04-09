package sernet.verinice.rcp.dataprotection;

import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.iso27k.rcp.OrganizationMultiselectWidget;
import sernet.verinice.model.common.CnATreeElement;

/**
 * A dialog to select the organizations for the migration of the dataprotection.
 */
public class MigrateDataProtectionDialog extends TitleAreaDialog {
    private static final Logger LOG = Logger.getLogger(MigrateDataProtectionDialog.class);

    private ITreeSelection selection;
    private CnATreeElement selectedElement;
    private OrganizationMultiselectWidget organizationWidget;
    private boolean showMigrationDialog = true;

    /**
     * Create the dialog.
     * @param parentShell
     */
    public MigrateDataProtectionDialog(Shell parentShell) {
        super(parentShell);
        setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE);
    }

    /**
     * Create contents of the dialog.
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        setTitle(Messages.MigrateDataProtectionDialog_title);
        setMessage(Messages.MigrateDataProtectionDialog_message);
        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        container.setLayout(new GridLayout(1, false));
        container.setLayoutData(new GridData(GridData.FILL_BOTH));

        try {
            organizationWidget = new OrganizationMultiselectWidget(container, selection,
                    selectedElement);
            organizationWidget.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (organizationWidget.getSelectedElementSet().isEmpty()) {
                        setErrorMessage(Messages.MigrateDataProtectionDialog_select_org);
                    } else {
                        setErrorMessage(null);
                    }
                }
            });
            Composite composite = new Composite(container, SWT.NONE);
            composite.setLayout(new GridLayout(1, false));
            composite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));

        } catch (CommandException ex) {
            LOG.error("Error while loading organizations", ex); //$NON-NLS-1$
            setMessage(sernet.verinice.rcp.risk.Messages.OrganizationPage_ErrorMessage,
                    IMessageProvider.ERROR);
        }

        return area;
    }

    /**
     * Create contents of the button bar.
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    /**
     * Return the initial size of the dialog.
     */
    @Override
    protected Point getInitialSize() {
        return new Point(766, 568);
    }

    public Set<CnATreeElement> getSelectedElementSet() {
        return organizationWidget.getSelectedElementSet();
    }

    public void setSelectedElement(CnATreeElement selectedElement) {
        this.selectedElement = selectedElement;
    }

    public void setSelection(ITreeSelection selection) {
        this.selection = selection;
    }
}
