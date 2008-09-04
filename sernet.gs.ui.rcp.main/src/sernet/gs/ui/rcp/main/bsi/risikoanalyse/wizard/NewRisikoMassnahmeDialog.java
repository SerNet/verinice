package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import java.util.ArrayList;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control; 
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahme;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahmeHome;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahmenUmsetzung;

/**
 * Dialog to enter a new MassnahmenUmsetzung.
 * 
 * @author ahanekop@sernet.de
 *
 */
public class NewRisikoMassnahmeDialog extends Dialog {

	private Text textNumber;
	private Text textName;
	private Text textDescription;
	private RisikoMassnahme newRisikoMassnahme;
	
	
	/**
	 * Constructor of NewMassnahmenDialog.
	 * The dialog creates a new RiskoMassnahmen and adds it to
	 * the given list.
	 * 
	 * @param parentShell (Shell) - shell of the viewer in which the Dialog
	 * 		  is called
	 * 
	 * @param newListGef (ArrayList<RisikoMassnahmenUmsetzung>) - list of
	 * 		  RiskoMassnahmen to add the new RisikoMassnahme
	 * 		  to
	 * 
	 * 
	 */
	public NewRisikoMassnahmeDialog(Shell parentShell) {
		super(parentShell);
		newRisikoMassnahme = new RisikoMassnahme();
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}
	
	
	
	public RisikoMassnahme getNewRisikoMassnahme() {
		return newRisikoMassnahme;
	}



	/**
	 * Creates and returns the contents of the upper part of this dialog (above
	 * the button bar). Overrides dialog.createDialogArea(Composite parent).
	 * 
	 * @return the dialog area control
	 */
	@Override
	protected Control createDialogArea(Composite parentShell) {
		Composite container = (Composite) super.createDialogArea(parentShell);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		container.setLayout(gridLayout);
		
		/* label number */
		final Label labelNumber = new Label(container, SWT.NONE);
		GridData gridLabelNumber = new GridData();
		gridLabelNumber.horizontalAlignment = SWT.LEFT;
	    gridLabelNumber.verticalAlignment = SWT.CENTER;
	    labelNumber.setText("Nummer:");
		labelNumber.setLayoutData(gridLabelNumber);
		
		/* text number */
		textNumber = new Text(container, SWT.BORDER);
		GridData gridTextNumber = new GridData();
		gridTextNumber.horizontalAlignment = SWT.FILL;
	    gridTextNumber.verticalAlignment = SWT.CENTER;
	    gridTextNumber.grabExcessHorizontalSpace = true;
		textNumber.setLayoutData(gridTextNumber);
		
		/* label name */
		final Label labelName = new Label(container, SWT.NONE);
		GridData gridLabelName = new GridData();
		gridLabelName.horizontalAlignment = SWT.LEFT;
	    gridLabelName.verticalAlignment = SWT.CENTER;
	    labelName.setText("Name:");
		labelName.setLayoutData(gridLabelName);
		
		/* text name */
		textName = new Text(container, SWT.BORDER);
		GridData gridTextName = new GridData();
		gridTextName.horizontalAlignment = SWT.FILL;
	    gridTextName.verticalAlignment = SWT.CENTER;
	    gridTextName.grabExcessHorizontalSpace = true;
		textName.setLayoutData(gridTextName);
		
		/* label description */
		final Label labelDescription = new Label(container, SWT.NONE);
		GridData gridLabelDescription = new GridData();
		gridLabelDescription.horizontalAlignment = SWT.LEFT;
	    gridLabelDescription.verticalAlignment = SWT.CENTER;
	    labelDescription.setText("Beschreibung:");
		labelDescription.setLayoutData(gridLabelDescription);
		
		/* text description */
		textDescription = new Text(container, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI | SWT.WRAP);
		GridData gridTextDescription = new GridData();
		gridTextDescription.horizontalAlignment = SWT.FILL;
		gridTextDescription.verticalAlignment = SWT.FILL;
	    gridTextDescription.grabExcessHorizontalSpace = true;
	    gridTextDescription.grabExcessVerticalSpace = true;
	    gridTextDescription.widthHint = 400;
	    gridTextDescription.heightHint = 200;
		textDescription.setLayoutData(gridTextDescription);
		
		return container;
	}

	/**
	 * Notifies that the ok button of this dialog has been pressed.
	 */
	@Override
	protected void okPressed() {
		newRisikoMassnahme.setNumber(textNumber.getText());
		newRisikoMassnahme.setName(textName.getText());
		newRisikoMassnahme.setDescription(textDescription.getText());

		// DONE neue RisikoMassnahmenUmsetzung in DB speichern
		
		try {
			RisikoMassnahmeHome.getInstance().saveNew(newRisikoMassnahme);
		} catch (Exception e) {
			ExceptionUtil.log(e, "Neue Massnahme konnte nicht gespeichert werden!");
		}
		
		super.okPressed();
	}
}
