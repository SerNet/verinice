package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control; 
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import sernet.gs.model.Gefaehrdung;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.OwnGefaehrdung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.OwnGefaehrdungHome;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahmenUmsetzung;

/**
 * Dialog to enter a new MassnahmenUmsetzung.
 * 
 * @author ahanekop@sernet.de
 *
 */
public class NewRisikoMassnahmenUmsetzungDialog extends Dialog {

	private Text textName;
	private Text textDescription;
	private ArrayList<RisikoMassnahmenUmsetzung> listRisikoMassnahmenUmsetzung;
	private CnATreeElement cnaElement;
	private RisikoMassnahmenUmsetzung newRisikoMassnahmenUmsetzung = new RisikoMassnahmenUmsetzung(cnaElement, null);
	
	/**
	 * Constructor of NewMassnahmenUmsetzungDialog.
	 * The dialog creates a new RiskoMassnahmenUmsetzung and adds it to
	 * the given list.
	 * 
	 * @param parentShell (Shell) - shell of the viewer in which the Dialog
	 * 		  is called
	 * @param newListGef (ArrayList<RisikoMassnahmenUmsetzung>) - list of
	 * 		  RiskoMassnahmenUmsetzung to add the new RisikoMassnahmenUmsetzung
	 * 		  to
	 * @param  newCnaElement (CnATreeElement) - the parent Element, which the
	 * 		   RisikoAnalayse is made for 
	 */
	public NewRisikoMassnahmenUmsetzungDialog(Shell parentShell,
			ArrayList<RisikoMassnahmenUmsetzung> newListGef,
			CnATreeElement newCnaElement) {
		// TODO übergabe des Feldes gibt Probleme, wenn der dialog nicht mehr
		// modal ist!!
		// komme ich von hier anders an den RisikoAnlayseWizard ??
		// 2008-07-29 ah - ja: wizard übergeben. aber will man das?
		// vergl. PropertiescomboBoxCellModifier
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		listRisikoMassnahmenUmsetzung = newListGef;
		cnaElement = newCnaElement;
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
		textDescription = new Text(container, SWT.BORDER);
		GridData gridTextDescription = new GridData();
		gridTextDescription.horizontalAlignment = SWT.FILL;
	    gridTextDescription.verticalAlignment = SWT.CENTER;
	    gridTextDescription.grabExcessHorizontalSpace = true;
		textDescription.setLayoutData(gridTextDescription);
		
		return container;
	}

	/**
	 * Notifies that the ok button of this dialog has been pressed.
	 */
	@Override
	protected void okPressed() {
		newRisikoMassnahmenUmsetzung.setTitle(textName.getText());
		newRisikoMassnahmenUmsetzung.setDescription(textDescription.getText());
		listRisikoMassnahmenUmsetzung.add(newRisikoMassnahmenUmsetzung);

		/*
		// TODO neue RisikoMassnahmenUmsetzung in DB speichern 
		
		try {
			OwnGefaehrdungHome.getInstance().saveNew(newOwnGef);
		} catch (Exception e) {
			ExceptionUtil.log(e, "Eigene Gefährdung konnte nicht gespeichert werden.");
		}
		*/
		
		super.okPressed();
	}
}
