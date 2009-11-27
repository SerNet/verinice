/*******************************************************************************
 * Copyright (c) 2009 Daniel Murygin <dm@sernet.de>.
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
 *     Daniel Murygin <dm@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.views;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.bsi.model.Note;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.service.ICommandService;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.DeleteNote;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadNotes;

public class NoteView extends ViewPart {

	private static final Logger LOG = Logger.getLogger(NoteView.class);
	
	public static final String ID = "sernet.gs.ui.rcp.main.bsi.views.NoteView"; //$NON-NLS-1$
	
	Composite parent;
	
	ExpandBar expandBar;
	
	private ISelectionListener selectionListener;

	private ICommandService	commandService;
	
	private CnATreeElement currentCnaElement;
	
	private Action addNoteAction;

	public NoteView() {
	}

	@Override
	public void createPartControl(Composite parent) {
	
		this.parent = parent;
		parent.setLayout(new FillLayout());
        
		try {
			expandBar = new ExpandBar(parent,SWT.V_SCROLL);
		    expandBar.setSpacing(4);
			hookPageSelection();
		} catch (Exception e) {
			ExceptionUtil.log(e, Messages.BrowserView_3);
			LOG.error("Error while creating control", e);
		}
		
		makeActions();
		fillLocalToolBar();

	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}
	
	private void hookPageSelection() {
		selectionListener = new ISelectionListener() {
			public void selectionChanged(IWorkbenchPart part, ISelection selection) {
				pageSelectionChanged(part, selection);
			}
		};
		getSite().getPage().addPostSelectionListener(selectionListener);

	}

	protected void pageSelectionChanged(IWorkbenchPart part, ISelection selection) {
		if (part == this)
			return;

		if (!(selection instanceof IStructuredSelection))
			return;
		try {
			Object element = ((IStructuredSelection) selection).getFirstElement();
			if(element instanceof CnATreeElement) {
				addNoteAction.setEnabled(true);
				setCurrentCnaElement((CnATreeElement) element);
				clear();
				loadNotes();
				//parent.setSize(300,200);
			} else {
				addNoteAction.setEnabled(false);
			}
		} catch (Exception e) {
			LOG.error("Error while loading notes", e);
		}
	}
	
	private void fillLocalToolBar() {
		IActionBars bars = getViewSite().getActionBars();
		IToolBarManager manager = bars.getToolBarManager();
		manager.add(this.addNoteAction);
	}
	
	private void makeActions() {

		addNoteAction = new Action() {
			public void run() {
				Note note = new Note();
				note.setCnATreeElementId(getCurrentCnaElement().getDbId());
				note.setTitel("neue Notiz");
				EditorFactory.getInstance().openEditor(note);			
			}
		};
		addNoteAction.setText("Notiz hinzufügen...");
		addNoteAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.NOTE_NEW));
		addNoteAction.setEnabled(false);
	}

	public void loadNotes() {
		try {
			LoadNotes command = new LoadNotes(getCurrentCnaElement().getDbId());		
			command = getCommandService().executeCommand(command);		
			List<Note> noteList = command.getNoteList();
			if(noteList==null || noteList.size()==0) {
				Label test = new Label(parent, SWT.NONE);
				test.setText("keine Notiz vorhanden");		
				test.pack();
			} else {
				for (final Note note : noteList) {
					Composite composite = new Composite(expandBar, SWT.NONE);
				    GridLayout layout = new GridLayout(2, false);
				    layout.marginLeft = layout.marginTop = layout.marginRight = layout.marginBottom = 4;
				    layout.verticalSpacing = 10;
				    composite.setLayout(layout);
				    
				    GridData gdText = new GridData();
					gdText.grabExcessHorizontalSpace = true;
					gdText.grabExcessVerticalSpace = false;
					gdText.horizontalAlignment = GridData.FILL;
					gdText.verticalAlignment = GridData.CENTER;
					gdText.heightHint=50;
				    Text text = new Text(composite, SWT.BORDER | SWT.MULTI);
				    text.setLayoutData(gdText);
					text.setText(note.getText());
					
					Button deleteButton = new Button(composite,SWT.NONE);
				    deleteButton.setImage(ImageCache.getInstance().getImage(ImageCache.DELETE));
				    deleteButton.setToolTipText("Notiz löschen");
				    deleteButton.addSelectionListener(new SelectionListener(){

						public void widgetDefaultSelected(SelectionEvent e) {
							// TODO Auto-generated method stub					
						}

						public void widgetSelected(SelectionEvent e) {
							deleteNote(note);
						}
				    	
				    });
					
				    ExpandItem item0 = new ExpandItem(expandBar, SWT.NONE, 0);
				    item0.setText(note.getTitel());
				    item0.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
				    item0.setControl(composite);
				    item0.setExpanded(true);
				}
		}
		} catch(Exception e) {
			LOG.error("Error while loading notes", e);
			ExceptionUtil.log(e, "Error while loading notes");
		}
	}
	
	protected void deleteNote(Note note) {
		DeleteNote command = new DeleteNote(note);		
		try {
			command = getCommandService().executeCommand(command);
		} catch (CommandException e) {
			LOG.error("Error while saving note", e);
			ExceptionUtil.log(e, "Fehler beim Speichern der Notiz.");
		}
		note = command.getNote();
		clear();
		loadNotes();
	}

	public void clear() {
		for (Control ctl : expandBar.getChildren()) {
			ctl.dispose();
	    }
		for (ExpandItem item : expandBar.getItems()) {
			item.dispose();
	    }
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

	public CnATreeElement getCurrentCnaElement() {
		return currentCnaElement;
	}

	public void setCurrentCnaElement(CnATreeElement currentCnaElement) {
		this.currentCnaElement = currentCnaElement;
	}

}
