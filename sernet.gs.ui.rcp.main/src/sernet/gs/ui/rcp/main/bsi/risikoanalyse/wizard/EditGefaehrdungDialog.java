package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import java.util.ArrayList;

import org.apache.log4j.Logger;
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
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.OwnGefaehrdung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.OwnGefaehrdungHome;

/**
 * Dialog to enter a new Gefaehrdung.
 * 
 * @author ahanekop@sernet.de
 *
 */
public class EditGefaehrdungDialog extends Dialog {

	private Text textNumber;
	private Text textName;
	private Text textDescription;
	private Combo textCategory;
	private OwnGefaehrdung ownGefaehrdung;
	
	public EditGefaehrdungDialog(Shell parentShell, OwnGefaehrdung gefaehrdung) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		ownGefaehrdung = gefaehrdung;
	}
	
	@Override
	protected Control createDialogArea(Composite parentShell) {
		Composite container = (Composite) super.createDialogArea(parentShell);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		container.setLayout(gridLayout);
		
		/* label number */
		final Label label1 = new Label(container, SWT.NONE);
		GridData data1 = new GridData();
		data1.horizontalAlignment = SWT.LEFT;
	    data1.verticalAlignment = SWT.CENTER;
	    label1.setText("Nummer:");
		label1.setLayoutData(data1);
		
		/* text number */
		textNumber = new Text(container, SWT.BORDER);
		GridData data2 = new GridData();
		data2.horizontalAlignment = SWT.FILL;
	    data2.verticalAlignment = SWT.CENTER;
	    data2.grabExcessHorizontalSpace = true;
		textNumber.setLayoutData(data2);
		textNumber.setText(ownGefaehrdung.getId());
		//textNumber.set
		
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
		textName.setText(ownGefaehrdung.getTitel());
		
		/* label description */
		final Label label3 = new Label(container, SWT.NONE);
		GridData data5 = new GridData();
		data5.horizontalAlignment = SWT.LEFT;
	    data5.verticalAlignment = SWT.TOP;
	    label3.setText("Beschreibung:");
		label3.setLayoutData(data5);
		
		/* text description */
		GridData data6 = new GridData();
		textDescription = new Text(container, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI | SWT.WRAP);
		data6.horizontalAlignment = SWT.FILL;
	    data6.verticalAlignment = SWT.FILL;
	    data6.grabExcessHorizontalSpace = true;
	    data6.grabExcessVerticalSpace = true;
	    data6.widthHint = 400;
	    data6.heightHint = 200;
		textDescription.setLayoutData(data6);
		textDescription.setText(ownGefaehrdung.getBeschreibung());
		
		/* label category */
		final Label label4 = new Label(container, SWT.NONE);
		GridData data7 = new GridData();
		data7.horizontalAlignment = SWT.LEFT;
	    data7.verticalAlignment = SWT.TOP;
	    label4.setText("Kategorie:");
		label4.setLayoutData(data7);
		
		/* text category */
		textCategory = new Combo(container, SWT.DROP_DOWN);
		GridData data8 = new GridData();
		data8.horizontalAlignment = SWT.FILL;
		data8.verticalAlignment = SWT.CENTER;
		data8.grabExcessHorizontalSpace = true;
		textCategory.setLayoutData(data8);
		textCategory.setItems(loadCategories());
		textCategory.setText(ownGefaehrdung.getOwnkategorie());
		
		 //add controls to composite as necessary
		 return container;
	}

	private String[] loadCategories() {
		ArrayList<String> newString =  new ArrayList<String> ();
		newString.add(Gefaehrdung.KAT_STRING_HOEHERE_GEWALT);
		newString.add(Gefaehrdung.KAT_STRING_ORG_MANGEL);
		newString.add(Gefaehrdung.KAT_STRING_MENSCH);
		newString.add(Gefaehrdung.KAT_STRING_TECHNIK);
		newString.add(Gefaehrdung.KAT_STRING_VORSATZ);
		newString.add(ownGefaehrdung.getOwnkategorie());

		// TODO alle eigenen Gefährdungs-Kategorien hinzufügen (aus DB)
		
		return newString.toArray(new String[newString.size()]);
	}
	
	@Override
	protected void okPressed() {
		ownGefaehrdung.setId(textNumber.getText());
		ownGefaehrdung.setTitel(textName.getText());
		ownGefaehrdung.setBeschreibung(textDescription.getText());
		ownGefaehrdung.setOwnkategorie(textCategory.getText());
		
		try {
			OwnGefaehrdungHome.getInstance().saveUpdate(ownGefaehrdung);
		} catch (Exception e) {
			ExceptionUtil.log(e, "Änderung konnte nicht gespeichert werden.");
		}
		
		super.okPressed();
	}
}
