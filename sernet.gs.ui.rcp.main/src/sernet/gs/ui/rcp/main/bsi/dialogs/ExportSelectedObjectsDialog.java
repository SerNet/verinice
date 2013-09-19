/*******************************************************************************
 * Copyright (c) 2010 Andreas Becker <andreas[at]becker[dot]name>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Andreas Becker <andreas[at]becker[dot]name> - initial API and implementation
 ******************************************************************************/

package sernet.gs.ui.rcp.main.bsi.dialogs;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;

/**
 * Dialog class for the export dialog.
 * 
 * @author Andreas Becker
 */
public class ExportSelectedObjectsDialog extends TitleAreaDialog
{	
	/**
	 * Indicates if the output should be encrypted.
	 */
	private boolean encryptOutput = false;
	private String sourceId;
	private String storageLocation;

	public ExportSelectedObjectsDialog(Shell parentShell)
	{
		super(parentShell);
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
	    final int gridLayoutDefaultMarginHeight = 10;
	    final int gridLayoutDefaultMarginWidth = gridLayoutDefaultMarginHeight;
	    final int rowLayoutDefaultMarginTop = 15;
		/*++++
		 * Dialog title, message and layout:
		 *++++++++++++++++++++++++++++++++++*/
		
		setTitle(Messages.ExportDialog_0);
		setMessage(Messages.ExportSelectedObjectsDialog_0, IMessageProvider.INFORMATION);
		
		final Composite composite = (Composite) super.createDialogArea(parent);
		((GridLayout)composite.getLayout()).marginWidth = gridLayoutDefaultMarginWidth;
		((GridLayout)composite.getLayout()).marginHeight = gridLayoutDefaultMarginHeight;
		
		/*++++
		 * Widgets for source ID:
		 *+++++++++++++++++++++++*/
		
		final Composite compositeSourceId = new Composite(composite, SWT.NONE);
		compositeSourceId.setLayout(new RowLayout());
		final Label lblSourceId = new Label(compositeSourceId, SWT.NONE);
		lblSourceId.setText(Messages.ExportDialog_7);
		final Text txtSourceId = new Text(compositeSourceId, SWT.SINGLE | SWT.BORDER);
		txtSourceId.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				sourceId = ( (Text) e.getSource() ).getText();
			}
		});
		
		/*++++
		 * Widgets to enable/disable encryption:
		 *++++++++++++++++++++++++++++++++++++++*/
		
		final Composite encryptionOptionComposite = new Composite(composite, SWT.NONE);
		encryptionOptionComposite.setLayout(new RowLayout(SWT.HORIZONTAL));
		((RowLayout) encryptionOptionComposite.getLayout()).marginTop = rowLayoutDefaultMarginTop;
		
		final Button encryptionCheckbox = new Button(encryptionOptionComposite, SWT.CHECK);
		encryptionCheckbox.setText("Encrypt output");
		encryptionCheckbox.setSelection(encryptOutput);
		encryptionCheckbox.setEnabled(true);
		encryptionCheckbox.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button checkBox = (Button) e.getSource();
				encryptOutput = checkBox.getSelection();
			}
		});
		encryptionOptionComposite.pack();

		/*+++++
		 * Widgets to browse for storage location:
		 *++++++++++++++++++++++++++++++++++++++++*/
		
		final Composite compositeSaveLocation = new Composite(composite,SWT.NONE);
		compositeSaveLocation.setLayout(new RowLayout(SWT.HORIZONTAL));
		((RowLayout) compositeSaveLocation.getLayout()).marginTop = rowLayoutDefaultMarginTop;
		final Label labelLocation = new Label(compositeSaveLocation, SWT.NONE);
		labelLocation.setText(Messages.ExportDialog_5);
		final Text txtLocation = new Text(compositeSaveLocation, SWT.SINGLE | SWT.BORDER);
		final short textLocationWidth = 300;
		final short textLocationHeight = 30;
		txtLocation.setSize(textLocationWidth, textLocationHeight);
		final RowData textLocationData = new RowData();
		textLocationData.width = textLocationWidth;
		txtLocation.setLayoutData(textLocationData);
		composite.pack();
		final Button buttonBrowseLocations = new Button(compositeSaveLocation, SWT.NONE);
		buttonBrowseLocations.setText(Messages.ExportDialog_6);
		
		buttonBrowseLocations.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e)
			{
			    IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
                String defaultFolder = prefs.getString(PreferenceConstants.DEFAULT_FOLDER_EXPORT);
				FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell());
				dialog.setFilterExtensions(new String[]{ "*.xml" }); //$NON-NLS-1$
				dialog.setFilterPath(defaultFolder);
				String exportPath = dialog.open();
				if( exportPath != null )
				{
					txtLocation.setText(exportPath);
					storageLocation = exportPath;
				}
				else
				{
					txtLocation.setText(""); //$NON-NLS-1$
					storageLocation = ""; //$NON-NLS-1$
				}
			}
		});
		return composite;
	}
	
	/* Getters and Setters: */

	public String getSourceId()
	{
		return sourceId;
	}

	public String getStorageLocation()
	{
		return storageLocation;
	}
	
	public boolean getEncryptOutput()
	{
		return encryptOutput;
	}
}
