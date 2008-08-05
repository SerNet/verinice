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

/**
 * Dialog to enter a new MassnahmenUmsetzung.
 * 
 * @author ahanekop@sernet.de
 *
 */
public class NewMassnahmenUmsetzungDialog extends Dialog {

	private Text textName;
	private ArrayList<MassnahmenUmsetzung> listMassnahmenUmsetzung;
	private CnATreeElement cnaElement;
	private MassnahmenUmsetzung newMassnahmenUmsetzung = new MassnahmenUmsetzung(cnaElement);
	
	public NewMassnahmenUmsetzungDialog(Shell parentShell, ArrayList<MassnahmenUmsetzung> newListGef, CnATreeElement newCnaElement) {
		// TODO übergabe des Feldes gibt Probleme, wenn der dialog nicht mehr modal ist!!
		// komme ich von hier anders an den RisikoAnlayseWizard ??
		//   2008-07-29 ah - ja: wizard übergeben. aber will man das?
		// 	   vergl. PropertiescomboBoxCellModifier 
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		listMassnahmenUmsetzung = newListGef;
		cnaElement = newCnaElement;
	}
	
	@Override
	protected Control createDialogArea(Composite parentShell) {
		Composite container = (Composite) super.createDialogArea(parentShell);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		container.setLayout(gridLayout);
		
		/* label name */
		final Label label2 = new Label(container, SWT.NONE);
		GridData data3 = new GridData();
		data3.horizontalAlignment = SWT.LEFT;
	    data3.verticalAlignment = SWT.CENTER;
	    label2.setText("Name:");
		label2.setLayoutData(data3);
		
		/* text name */
		textName = new Text(container, SWT.BORDER);
		GridData data4 = new GridData();
		data4.horizontalAlignment = SWT.FILL;
	    data4.verticalAlignment = SWT.CENTER;
	    data4.grabExcessHorizontalSpace = true;
		textName.setLayoutData(data4);
		
		 //add controls to composite as necessary
		 return container;
	}

	@Override
	protected void okPressed() {
		
		newMassnahmenUmsetzung.setName(textName.getText());
		listMassnahmenUmsetzung.add(newMassnahmenUmsetzung);

		/*
		// TODO neue Massnahme in DB speichern 
		
		try {
			OwnGefaehrdungHome.getInstance().saveNew(newOwnGef);
		} catch (Exception e) {
			ExceptionUtil.log(e, "Eigene Gefährdung konnte nicht gespeichert werden.");
		}
		*/
		
		super.okPressed();
	}
}
