//Neu hinzugefÃ¼gt vom Projektteam: XML import

package sernet.gs.ui.rcp.main.bsi.dialogs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.sync.commands.SyncCommand;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.common.CnATreeElement;

/**
 * 
 * @author: Projektteam
 * 
 */

public class XMLImportDialog extends Dialog {

	private boolean insert;
	private boolean update;
	private boolean delete;

	private Text dataPathText;
	private boolean dataPathFlag;
	private final static String[] FILTEREXTEND = { "*.xml" };

	private File dataFile;

	public XMLImportDialog(Shell shell) {
		super(shell);
	}

	@Override
	public void okPressed() {
		if (!dataPathFlag && (!insert && !update && !delete)) {
			createErrorMessage(3);
		} else if (!dataPathFlag) {
			createErrorMessage(1);
		} else if ((!insert && !update && !delete)) {
			createErrorMessage(2);
		} else {

			try {
				PlatformUI.getWorkbench().getProgressService().busyCursorWhile(
						new IRunnableWithProgress() {
							public void run(IProgressMonitor monitor)
									throws InvocationTargetException,
									InterruptedException {
								Activator.inheritVeriniceContextState();

								SyncCommand command;
								try {
									command = new SyncCommand(
											insert,
											update,
											delete,
											IOUtils.toByteArray(new FileInputStream(
															dataFile)));
								} catch (FileNotFoundException e1) {
									throw new IllegalStateException(e1);
								} catch (IOException e1) {
									throw new IllegalStateException(e1);
								}

								try {
									command = ServiceFactory
											.lookupCommandService()
											.executeCommand(command);

								} catch (CommandException e) {
									throw new IllegalStateException(e);
								}
								
								CnATreeElement importRootObject = command.getImportRootObject();
								if (importRootObject != null)
								{
								    CnAElementFactory.getModel(importRootObject).childAdded(importRootObject.getParent(), importRootObject);
                                    CnAElementFactory.getModel(importRootObject).databaseChildAdded(importRootObject);                                  
								}
								Set<CnATreeElement> changedElement = command.getElementSet();
								if(changedElement!=null) {
                                    for (CnATreeElement cnATreeElement : changedElement) {
                                        CnAElementFactory.getModel(cnATreeElement).childChanged(cnATreeElement.getParent(), cnATreeElement);
                                    }
								}
							}
						});
			} catch (InvocationTargetException e) {
				ExceptionUtil.log(e, "Fehler während des Importvorganges");
			} catch (InterruptedException e) {
				ExceptionUtil.log(e, "Fehler während des Importvorganges");
			}

			close();
		}
	}

	private void createErrorMessage(int caseNumber) {
		String titel = "Vorgang konnte nicht ausgeführt werden";
		String messageBody = "Something went terribly wrong...";

		switch (caseNumber) {
		case 1:
			messageBody = "Der Pfad ist fehlerhaft oder fehlt!\n-> Geben Sie einen gültigen Pfad ein.";
			break;
		case 2:
			messageBody = "Es wurde keine Operation ausgewählt!\n-> Wählen Sie eine oder mehrere Operationen aus.";
			break;
		case 3:
			messageBody = "Der Pfad ist fehlerhaft oder fehlt!\n-> Geben Sie einen gültigen Pfad ein."
					+ "\n\nEs wurde keine Operation ausgewählt!\n-> Wählen Sie eine oder mehrere Operationen aus.";
			break;
		case 4:
			messageBody = "Die Namenskonvention passt nicht zu den Dateien im Zip- Archiv!"
					+ "\n-> Geben Sie das richtige Zip- Archiv an oder halten Sie sich an die Namenskonvention.";
			break;
		}

		MessageDialog messageDialog = new MessageDialog(this.getShell(), titel,
				null, messageBody, MessageDialog.ERROR, new String[] { "OK" },
				1);
		messageDialog.open();
	}

	private void createHelpMessage() {
		String titel = "Hilfe zur Namenskonvention";

		String messageBody = "Für den Import müssen sich die im Zip- Archiv befindenden Inventar- und Mappingdaten in jeweils einer XML- Datei gesichert sein."
				+ " Diese XML- Dateien müssen data.xml und mapping.xml heißen!\n\n"
				+ "Inventardaten müssen in data.xml liegen und die"
				+ " Mappingdaten in mapping.xml\n\n"
				+ "Die Namen sind verbindlich denn sonst kann der Vorgang nicht ausgeführt werden!";

		MessageDialog messageDialog = new MessageDialog(this.getShell(), titel,
				null, messageBody, MessageDialog.INFORMATION,
				new String[] { "OK" }, 1);
		messageDialog.open();
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		final Composite container = (Composite) super.createDialogArea(parent);

		GridLayout layout = new GridLayout();
		layout.numColumns = 5;
		layout.verticalSpacing = 15;
		container.setLayout(layout);

		Label welcome = new Label(container, SWT.BOLD);
		welcome.setText(Messages.XMLImportDialog_2);
		welcome.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,
				true, false, 5, 1));

		Label seperator = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		seperator.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,
				true, false, 5, 1));

		// Operations of database (update,insert,delete)

		Group operationGroup = new Group(container, SWT.NULL);
		operationGroup.setText(Messages.XMLImportDialog_6);
		operationGroup.setLayoutData(new GridData(GridData.FILL,
				GridData.CENTER, true, false, 5, 3));

		layout = new GridLayout();
		layout.numColumns = 2;
		operationGroup.setLayout(layout);

		Label operationIntro = new Label(operationGroup, SWT.LEFT);
		operationIntro.setText(Messages.XMLImportDialog_7);
		operationIntro.setLayoutData(new GridData(GridData.FILL,
				GridData.CENTER, true, false, 2, 1));

		final Button insertCheck = new Button(operationGroup, SWT.CHECK);
		insertCheck.setText("insert");
		insertCheck.setLayoutData(new GridData(GridData.BEGINNING,
				GridData.CENTER, false, false, 1, 1));
		insertCheck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				insert = insertCheck.getSelection();
			}
		});

		Label insertText = new Label(operationGroup, SWT.LEFT);
		insertText.setText(Messages.XMLImportDialog_8);
		insertText.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,
				true, false, 1, 1));

		final Button updateCheck = new Button(operationGroup, SWT.CHECK);
		updateCheck.setText("update");
		updateCheck.setLayoutData(new GridData(GridData.BEGINNING,
				GridData.CENTER, false, false, 1, 1));
		updateCheck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				update = updateCheck.getSelection();
			}
		});

		Label updateText = new Label(operationGroup, SWT.LEFT);
		updateText.setText(Messages.XMLImportDialog_9);
		updateText.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,
				true, false, 1, 1));

		final Button deleteCheck = new Button(operationGroup, SWT.CHECK);
		deleteCheck.setText("delete");
		deleteCheck.setLayoutData(new GridData(GridData.BEGINNING,
				GridData.CENTER, false, false, 1, 1));
		deleteCheck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				delete = deleteCheck.getSelection();
			}
		});

		Label deleteText = new Label(operationGroup, SWT.LEFT);
		deleteText.setText(Messages.XMLImportDialog_10);
		deleteText.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,
				true, false, 1, 1));

		// set and save path to zip- archiv

		Group dataGroup = new Group(container, SWT.NULL);
		dataGroup.setText(Messages.XMLImportDialog_11);
		dataGroup.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,
				true, false, 5, 1));

		layout = new GridLayout();
		layout.numColumns = 4;
		layout.makeColumnsEqualWidth = true;
		dataGroup.setLayout(layout);

		Label dataIntro1 = new Label(dataGroup, SWT.LEFT);
		dataIntro1.setText(Messages.XMLImportDialog_12);
		dataIntro1.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,
				true, false, 4, 1));

		Label dataIntro2 = new Label(dataGroup, SWT.LEFT);
		dataIntro2.setText(Messages.XMLImportDialog_13);
		dataIntro2.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,
				true, false, 4, 1));

		Link helpLink = new Link(dataGroup, SWT.NONE);
		helpLink.setText("<a>Hilfe...</a>");
		helpLink.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,
				true, false, 4, 1));
		helpLink.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				createHelpMessage();
			}
		});

		dataPathText = new Text(dataGroup, SWT.BORDER);
		dataPathText.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,
				false, false, 3, 1));
		dataPathText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dataFile = new File(dataPathText.getText());
				if (dataFile.exists()) {
					dataPathFlag = true;
				} else {
					dataPathFlag = false;
				}
			}
		});

		final Button dataBrowse = new Button(dataGroup, SWT.PUSH);
		dataBrowse.setText(Messages.XMLImportDialog_14);
		dataBrowse.setLayoutData(new GridData(GridData.BEGINNING,
				GridData.CENTER, false, false, 1, 1));
		dataBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				displayFiles(container.getShell(), dataPathText, dataFile);
			}
		});

		return container;
	}

	private void displayFiles(Shell shell, Text pathText, File file) {
		FileDialog dialog = new FileDialog(shell, SWT.NULL);
		dialog.setFilterExtensions(FILTEREXTEND);
		String path = dialog.open();

		if (path != null) {
			file = new File(path);

			if (file.isFile()) {
				pathText.setText(file.getPath());
				pathText.setEditable(true);
			}
		}
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.XMLImportDialog_1);
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	public boolean getInsertState() {
		return insert;
	}

	public boolean getUpdateState() {
		return update;
	}

	public boolean getDeleteState() {
		return delete;
	}
}