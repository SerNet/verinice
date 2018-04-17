package sernet.verinice.rcp.dialogs;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * A generic scrollable dialog to show a long text witch can be copied to the
 * clippboard or exported as file.
 */
public class ScrollableMultilineDialog extends TitleAreaDialog {
    private String message;
    private String dialogTitle;
    private String dialogMessage;
    private boolean showSaveButton = true;

    /**
     * Create the dialog.
     *
     * @param parentShell
     */
    public ScrollableMultilineDialog(Shell parentShell, String message, String dialogTitle,
            String dialogMessage) {
        super(parentShell);
        setHelpAvailable(false);
        setShellStyle(SWT.RESIZE | SWT.APPLICATION_MODAL);
        this.message = message;
        this.dialogTitle = dialogTitle;
        this.dialogMessage = dialogMessage;
    }

    /**
     * Create contents of the dialog.
     *
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        setMessage(dialogMessage);
        setTitle(dialogTitle);
        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        container.setLayout(new GridLayout(1, false));
        container.setLayoutData(new GridData(GridData.FILL_BOTH));

        ScrolledComposite scrolledComposite = new ScrolledComposite(container,
                SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);

        StyledText styledText = new StyledText(scrolledComposite, SWT.BORDER);
        styledText.setText(message);
        scrolledComposite.setContent(styledText);
        scrolledComposite.setMinSize(styledText.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        Composite composite = new Composite(container, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));
        composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        composite.setVisible(showSaveButton);

        Composite compositeExport = new Composite(composite, SWT.NONE);
        GridData gdComposite = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
        gdComposite.heightHint = 32;
        compositeExport.setLayoutData(gdComposite);

        Button btnNewButton = new Button(composite, SWT.NONE);
        btnNewButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog fileDialog = new FileDialog(getShell(), SWT.SAVE);
                String filename = fileDialog.open();
                if (filename != null) {
                    try {
                        FileUtils.writeStringToFile(new File(filename), message);
                    } catch (IOException e1) {
                        MessageDialog.openError(getShell(), "Error writing file.", //$NON-NLS-1$
                                e1.getLocalizedMessage());
                    }
                }
            }
        });
        btnNewButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        btnNewButton.setAlignment(SWT.RIGHT);
        btnNewButton.setText(Messages.ScrollableMultilineDialog_save_button_text);
        return area;
    }

    /**
     * Create contents of the button bar.
     *
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
        return new Point(600, 400);
    }

    /**
     * To hide the save button set to false.
     */
    public void setShowSaveButton(boolean showSaveButton) {
        this.showSaveButton = showSaveButton;
    }
}
