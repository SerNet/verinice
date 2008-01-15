package sernet.gs.ui.rcp.main.bsi.wizards;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.StatusLine;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.office.IOOTableRow;
import sernet.gs.ui.rcp.office.OOWrapper;
import sernet.snutils.ExceptionHandlerFactory;

/**
 * Wizard page to allow the user to choose the OpenOffice installation
 * that should be used for export, templates etc.
 * 
 * @author koderman@sernet.de
 *
 */
public class ChooseExportMethodPage extends WizardPage {


	private static final int MAX_SEARCH = 10000;

	private Text oodirText;
	
	private String ooDir = null;
	private int dirsSearched = 0;

	private Text calcTemplateText;

	private Text documentTemplateText;
	
	
	protected ChooseExportMethodPage() {
		super("chooseExport");
		setTitle("Office Paket auswählen");
		setDescription("Wählen Sie die zu verwendende OpenOffice Installation aus");
	}
	
	
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		container.setLayout(gridLayout);
		setControl(container);

		final Label label2 = new Label(container, SWT.NULL);
		GridData gridData7 = new GridData(GridData.HORIZONTAL_ALIGN_END);
		label2.setLayoutData(gridData7);
		label2.setText("OpenOffice Pfad:");

		oodirText = new Text(container, SWT.BORDER);
		GridData gridData2 = new GridData(GridData.FILL_HORIZONTAL);
		oodirText.setLayoutData(gridData2);
		oodirText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				((ExportWizard)getWizard()).setOoPath(getDirPath(oodirText));
				updatePageComplete();
			}
		});
		oodirText.setText(findOODir());

		final Button btn3 = new Button(container, SWT.NULL);
		btn3.setText("Browse...");
		btn3.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				browseForDir(oodirText);
			}
		});
		

		final Label label3 = new Label(container, SWT.NULL);
		GridData gridData9 = new GridData(GridData.HORIZONTAL_ALIGN_END);
		label3.setLayoutData(gridData9);
		label3.setText("OO Calc Vorlage: ");

		calcTemplateText = new Text(container, SWT.BORDER);
		calcTemplateText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		calcTemplateText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				((ExportWizard)getWizard()).setTemplatePath(getDirPath(calcTemplateText));
				updatePageComplete();
			}
		});
		Preferences prefs = Activator.getDefault().getPluginPreferences();
		calcTemplateText.setText(prefs.getString(PreferenceConstants.OOTEMPLATE));
		
		final Button btn5 = new Button(container, SWT.NULL);
		btn5.setText("Browse...");
		btn5.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				browseForFile(calcTemplateText);
			}
		});
		
		final Label label4 = new Label(container, SWT.NULL);
		GridData gridData10 = new GridData(GridData.HORIZONTAL_ALIGN_END);
		label4.setLayoutData(gridData10);
		label4.setText("OO Writer Vorlage: ");

		documentTemplateText = new Text(container, SWT.BORDER);
		documentTemplateText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		documentTemplateText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				((ExportWizard)getWizard()).setTextTemplatePath(getDirPath(documentTemplateText));
				updatePageComplete();
			}
		});
		documentTemplateText.setText(prefs.getString(PreferenceConstants.OOTEMPLATE_TEXT));

		final Button btn6 = new Button(container, SWT.NULL);
		btn6.setText("Browse...");
		btn6.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				browseForFile(documentTemplateText);
			}
		});

		updatePageComplete();
	}

	private String findOODir() {
		ooDir = null;
		String textOoDir = Activator.getDefault().getPluginPreferences()
			.getString(PreferenceConstants.OODIR);
		if (isOOPathValid(textOoDir))
			return textOoDir;
		
		Logger.getLogger(this.getClass())
			.error("OO Pfad in Preferences stimmt nicht. Suche...");

		String[] dirs = new String[] {textOoDir, "/usr/local", "/usr/lib" };
		for (int i = 0; i < dirs.length; i++) {
			dirsSearched = 0;
			if (dirsSearched > MAX_SEARCH)
				return "";
			Logger.getLogger(this.getClass()).debug("Suche OpenOffice Installation rekursiv in "+ dirs[i]);
			StatusLine.setErrorMessage("Eingestellter OpenOffice Pfad stimmt nicht. Suche rekursiv in "+ dirs[i]);
			findOOSubDir(dirs[i]);
			if (ooDir != null)
				return ooDir;
		}
		
		return "";
		
	}
	
	private void findOOSubDir(String dir) {
		File f = new File(dir);
		if (! f.isDirectory())
			return;
		++dirsSearched;
		
		File[] files = f.listFiles();
		if (files == null)
			return;
		Pattern pat = Pattern.compile("(office|ooo)", Pattern.CASE_INSENSITIVE);
		for (int i = 0; i < files.length; i++) {
			if (ooDir != null || dirsSearched > MAX_SEARCH)
				return;
			
			
			Matcher m = pat.matcher(files[i].getPath());
			if (m.find() && isOOPathValid(files[i].getAbsolutePath())) {
				ooDir = files[i].getAbsolutePath();
				return;
			}
			else {
				if (files[i].isDirectory()) {
					// recurse down:
					findOOSubDir(files[i].getAbsolutePath());
				}
			}
		}
		
		return;
	}
	
	
	
	
	
	protected void browseForDir(Text text) {
		DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.OPEN);
		dialog.setFilterPath(getDirPath(text));
		String path = dialog.open();
		text.setText(path != null ? path : "");
	}
	
	protected void browseForFile(Text text) {
		FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
		dialog.setFilterExtensions(new String[] {"*.odt", "*.ods", "*"});
		String path = dialog.open();
		text.setText(path != null ? path : "");
	}

	private String getDirPath(Text text) {
		String textString = text != null ? text.getText().trim() : "";
		Path path = new Path(textString);
		return path.toOSString();
	}


	ExportWizard getExportWizard() {
		return ((ExportWizard) getWizard());
	}
	
	private boolean isOOPathValid(String dir) {
		return new Path(
				dir + File.separator + "program"
			 	).toFile().exists() ;
	}
	

	private void updatePageComplete() {
		if ( ! isMasterValid()) {
			setMessage(null);
			setErrorMessage("Vorlage wurde nicht gefunden, Pfad überprüfen.");
			setPageComplete(false);
			return;
		}
		
		if ( ! isOOPathValid(getDirPath(oodirText)) ){
			setMessage(null);
			setErrorMessage("Kein gültiges OpenOffice Verzeichnis, Konfiguration " +
					"überprüfen!");
			setPageComplete(false);
			return;
		}

		setMessage("Bereit für Export.");
		setErrorMessage(null);
		setPageComplete(true);
	}

	private boolean isMasterValid() {
		File file = new Path(getDirPath(calcTemplateText)).toFile();
		File file2 = new Path(getDirPath(documentTemplateText)).toFile();
		return (file.exists() && file.isFile()
				&& file2.exists() && file2.isFile());
//		return (file.exists() && file.isFile());
	}
	
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			updatePageComplete();
		}
	}
	
}
