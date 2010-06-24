/*******************************************************************************
 * Copyright (c) 2009 Daniel Murygin <dm[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.editors;

import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.SaveNote;
import sernet.hui.common.connect.IEntityChangedListener;
import sernet.hui.common.connect.PropertyChangedEvent;
import sernet.hui.common.multiselectionlist.IMLPropertyOption;
import sernet.hui.common.multiselectionlist.IMLPropertyType;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.bsi.Note;
import sernet.verinice.model.bsi.Addition.INoteChangedListener;

public class NoteEditor extends EditorPart {

	private static final Logger LOG = Logger.getLogger(NoteEditor.class);
	
	public static final String EDITOR_ID = "sernet.gs.ui.rcp.main.bsi.editors.noteeditor"; //$NON-NLS-1$
	
	Note note;
	
	Composite contentComp;
	
	Text textNote;
	
	private ICommandService	commandService;

	private boolean isModelModified = false;
	
	private IEntityChangedListener modelListener = new IEntityChangedListener() {
		public void dependencyChanged(IMLPropertyType arg0, IMLPropertyOption arg1) {
			// not relevant
		}	
		public void selectionChanged(IMLPropertyType arg0, IMLPropertyOption arg1) {
			modelChanged();
		}
		public void propertyChanged(PropertyChangedEvent evt) {
			modelChanged();
		}
	};
	
	void modelChanged() {
		boolean wasDirty = isDirty();
		isModelModified = true;
		
		if (!wasDirty)
			firePropertyChange(IEditorPart.PROP_DIRTY);
	}
	
	@Override
	public boolean isDirty() {
		return isModelModified;
	}
	
	@Override
	public void doSave(IProgressMonitor monitor) {
		monitor.beginTask(Messages.NoteEditor_0, IProgressMonitor.UNKNOWN);
		Set<INoteChangedListener> listener = note.getListener(); 
		SaveNote command = new SaveNote(note);		
		try {
			command = getCommandService().executeCommand(command);
		} catch (CommandException e) {
			LOG.error("Error while saving note", e); //$NON-NLS-1$
			ExceptionUtil.log(e, Messages.NoteEditor_1);
		}
		monitor.done();
		note = (Note) command.getAddition();
		note.getListener().addAll(listener);
		isModelModified = false;
		firePropertyChange(IEditorPart.PROP_DIRTY);
		note.getEntity().addChangeListener(this.modelListener);
		setPartName(note.getTitel());
		note.fireChange();
	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		if (! (input instanceof NoteEditorInput)) {
			throw new PartInitException(Messages.NoteEditor_2);
		}
		NoteEditorInput noteEditorInput = (NoteEditorInput) input;
		note=noteEditorInput.getInput();
		setSite(site);
		setInput(noteEditorInput);
		setPartName(noteEditorInput.getName());
		// add listener to mark editor as dirty on changes:
		noteEditorInput.getInput().getEntity().addChangeListener(this.modelListener);
	}

	@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		contentComp = new Composite(parent, SWT.NULL);
		
		GridData contentCompLD = new GridData();
		contentCompLD.grabExcessHorizontalSpace = true;
		contentCompLD.horizontalAlignment = GridData.FILL;
		contentCompLD.grabExcessHorizontalSpace = true;
		contentComp.setLayoutData(contentCompLD);
		
		GridLayout contentCompLayout = new GridLayout(2, false);
		contentCompLayout.marginWidth = 5;
		contentCompLayout.marginHeight = 5;
		contentCompLayout.numColumns = 2;
		contentCompLayout.horizontalSpacing = 5;
		contentCompLayout.verticalSpacing = 5;
		
		contentComp.setLayout(contentCompLayout);
		
		GridData gdElement= new GridData();
		gdElement.grabExcessHorizontalSpace = true;
		gdElement.horizontalAlignment = GridData.FILL;
		gdElement.horizontalSpan=2;
		Label labelElement = new Label(contentComp,SWT.TOP );
		labelElement.setText(Messages.NoteEditor_3 + note.getCnAElementTitel());
		labelElement.setLayoutData(gdElement);
		
		Label labelTitle = new Label(contentComp,SWT.TOP );
		labelTitle.setText(Messages.NoteEditor_4);
		
		GridData gdTitel= new GridData();
		gdTitel.grabExcessHorizontalSpace = true;
		gdTitel.grabExcessVerticalSpace = false;
		gdTitel.horizontalAlignment = GridData.FILL;
		gdTitel.verticalAlignment = GridData.BEGINNING;
		Text title = new Text(contentComp,SWT.BORDER | SWT.NULL);	
		
		title.setLayoutData(gdTitel);
		title.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event e) {
				Text field = (Text) e.widget;
				note.setTitel(field.getText());
			}
		});
		
		Label labelNote = new Label(contentComp,SWT.TOP);
		labelNote.setText(Messages.NoteEditor_5);	
		
		GridData gdText = new GridData();
		gdText.grabExcessHorizontalSpace = true;
		gdText.grabExcessVerticalSpace = false;
		gdText.horizontalAlignment = GridData.FILL;
		gdText.verticalAlignment = GridData.CENTER;
		gdText.heightHint=200;
		textNote = new Text(contentComp,SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		textNote.setLayoutData(gdText);
		textNote.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event e) {
				Text field = (Text) e.widget;
				note.setText(field.getText());
			}
		});
		
		if(note!=null) {
			if(note.getTitel()!=null) {
				title.setText(note.getTitel());
			}
			if(note.getText()!=null) {
				textNote.setText(note.getText());
			}
		}
		
		contentComp.pack();
		contentComp.layout();
	}

	@Override
	public void setFocus() {
		textNote.setFocus();
	}
	
	@Override
	public void dispose() {
		EditorRegistry.getInstance().closeEditor(String.valueOf(((NoteEditorInput)getEditorInput()).getId()));
		super.dispose();
	}
	
	public ICommandService getCommandService() {
		if(commandService==null) {
			commandService = createCommandServive();
		}
		return commandService;
	}

	private ICommandService createCommandServive() {
		return ServiceFactory.lookupCommandService();
	}

}
