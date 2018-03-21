package sernet.verinice.rcp.dataprotection;

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

public class ScrollableMultilineDialog extends TitleAreaDialog {
    private String message;

    /**
     * Create the dialog.
     * @param parentShell
     */
    public ScrollableMultilineDialog(Shell parentShell, String message) {
        super(parentShell);
        setShellStyle(SWT.RESIZE);
        this.message = message;
    }

    /**
     * Create contents of the dialog.
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        setMessage("The migration is finished.");
        setTitle("Migration finished.");
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

        Composite composite_1 = new Composite(composite, SWT.NONE);
        GridData gd_composite_1 = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
        gd_composite_1.heightHint = 32;
        composite_1.setLayoutData(gd_composite_1);

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

                        MessageDialog.openError(getShell(), "Error writing file.",
                                e1.getLocalizedMessage());
                    }
                }
            }
        });
        btnNewButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        btnNewButton.setAlignment(SWT.RIGHT);
        btnNewButton.setText("Save migration log.");
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
        return new Point(600, 400);
    }
}
