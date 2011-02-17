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
package sernet.gs.ui.rcp.main.bsi.views;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.DeleteNote;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadNotes;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.IBSIModelListener;
import sernet.verinice.model.bsi.Note;
import sernet.verinice.model.common.CnATreeElement;

public class NoteView extends ViewPart {

	private static final Logger LOG = Logger.getLogger(NoteView.class);
	
	public static final String ID = "sernet.gs.ui.rcp.main.bsi.views.NoteView"; //$NON-NLS-1$
	
	Composite parent;
	
	ExpandBar expandBar;
	
	private ISelectionListener selectionListener;

	private ICommandService	commandService;
	
	private CnATreeElement currentCnaElement;
	
	private Action addNoteAction;

	private IBSIModelListener modelListener;
	
	List<Note> noteList;

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
			LOG.error("Error while creating control", e); //$NON-NLS-1$
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
			if(element instanceof CnATreeElement && !element.equals(getCurrentCnaElement())) {
				addNoteAction.setEnabled(true);
				setCurrentCnaElement((CnATreeElement) element);
				clear();
				loadNotes();
				//parent.setSize(300,200);
			} else {
				addNoteAction.setEnabled(false);
			}
		} catch (Exception e) {
			LOG.error("Error while loading notes", e); //$NON-NLS-1$
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
				note.setCnAElementTitel(getCurrentCnaElement().getTitle());
				note.setTitel(Messages.NoteView_2);
				note.addListener(new Note.INoteChangedListener() {
					public void noteChanged() {
						clear();
						loadNotes();
					}
				});
				EditorFactory.getInstance().openEditor(note);			
			}
		};
		addNoteAction.setText(Messages.NoteView_3);
		addNoteAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.NOTE_NEW));
		addNoteAction.setEnabled(false);
	}

	public void loadNotes() {
		try {
			LoadNotes command = new LoadNotes(getCurrentCnaElement().getDbId());		
			command = getCommandService().executeCommand(command);		
			noteList = command.getNoteList();
			if(noteList!=null && noteList.size()>0) {		
				for (final Note note : noteList) {
					note.addListener(new Note.INoteChangedListener() {
						public void noteChanged() {
							clear();
							loadNotes();
						}
					});
				
					// set transient cna-element-titel
					note.setCnAElementTitel(getCurrentCnaElement().getTitle());
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
					gdText.heightHint=100;
					gdText.verticalSpan=2;
				    Text text = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL);
				    text.setLayoutData(gdText);
				    if(note.getText()!=null) {
				    	text.setText(note.getText());
				    }
					
					Button editButton = new Button(composite,SWT.NONE);
					editButton.setImage(ImageCache.getInstance().getImage(ImageCache.EDIT));
					editButton.setToolTipText(Messages.NoteView_4);
					editButton.addSelectionListener(new SelectionListener(){
						public void widgetDefaultSelected(SelectionEvent e) {				
						}
						public void widgetSelected(SelectionEvent e) {
							editNote(note);
						}		    	
				    });
					
					Button deleteButton = new Button(composite,SWT.NONE);
				    deleteButton.setImage(ImageCache.getInstance().getImage(ImageCache.DELETE));
				    deleteButton.setToolTipText(Messages.NoteView_5);
				    deleteButton.addSelectionListener(new SelectionListener(){
						public void widgetDefaultSelected(SelectionEvent e) {				
						}
						public void widgetSelected(SelectionEvent e) {
							boolean b = MessageDialog.openQuestion(
									NoteView.this.getSite().getShell(), 
									Messages.NoteView_6,
									NLS.bind(Messages.NoteView_7, note.getTitel()));
							if(b) {
								deleteNote(note);
							}
						}		    	
				    });
					
				    ExpandItem item0 = new ExpandItem(expandBar, SWT.NONE, 0);
				    if(note.getTitel()!=null) {
				    	item0.setText(note.getTitel());
				    }
				    item0.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
				    item0.setControl(composite);
				    item0.setExpanded(true);
				} // end for
			}
		} catch(Exception e) {
			LOG.error("Error while loading notes", e); //$NON-NLS-1$
			ExceptionUtil.log(e, "Error while loading notes"); //$NON-NLS-1$
		}
	}
	
	protected void editNote(Note note) {
		EditorFactory.getInstance().openEditor(note);
	}
	
	protected void deleteNote(Note note) {
		DeleteNote command = new DeleteNote(note);		
		try {
			command = getCommandService().executeCommand(command);
		} catch (CommandException e) {
			LOG.error("Error while saving note", e); //$NON-NLS-1$
			ExceptionUtil.log(e, Messages.NoteView_12);
		}
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
	
	@Override
	public void dispose() {
		getSite().getPage().removePostSelectionListener(selectionListener);
		BSIModel model = CnAElementFactory.getLoadedModel();
		model.removeBSIModelListener(modelListener);
		super.dispose();
	}

}
