package sernet.gs.ui.rcp.main.bsi.dialogs;

import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.openoffice.java.accessibility.ComboBox;

import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;

/**
 * 
 * @author koderman@sernet.de
 * 
 */
public class KonsolidatorDialog extends Dialog {

	private List<BausteinUmsetzung> selection;

	private BausteinUmsetzung source = null;

	public KonsolidatorDialog(Shell shell,
			List<BausteinUmsetzung> selectedElements) {
		super(shell);
		this.selection = selectedElements;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		container.setLayout(layout);

		Label intro = new Label(container, SWT.NONE);
		intro.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER,
				false, false, 1, 1));
		intro
				.setText("Wählen Sie den Baustein, der als Vorlage für die anderen verwendet"
						+ " werden soll:");

		final ListViewer viewer = new ListViewer(container, SWT.CHECK | SWT.BORDER);
		viewer.getList().setLayoutData(new GridData(GridData.FILL, GridData.FILL,
				true, true, 1, 1));

		viewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				BausteinUmsetzung bst = (BausteinUmsetzung) element;
				return bst.getKapitel() + ": " + bst.getParent().getTitle();
			}
		});

		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setInput(selection);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = ((IStructuredSelection) viewer.getSelection());
				if (sel.size() ==1)
					source = (BausteinUmsetzung) sel.getFirstElement();
			}
		});

		return container;
	}
	
	public static boolean askConsolidate(Shell shell) {
		if (!MessageDialog.openQuestion(
				shell,
				"Bausteine konsolidieren",
				"Wenn Sie fortfahren, werden alle Werte (inkl. Maßnahmen) der " +
				"Zielbausteine mit denen der gewählten Vorlage überschrieben.\n\n" +
				"Felder, die in der Vorlage leer sind, werden ignoriert.\n\n" +
				"Fortfahren?")) {
					return false;
				}
		return true;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Konsolidator");
	}

	public BausteinUmsetzung getSource() {
		return source;
	}

}
