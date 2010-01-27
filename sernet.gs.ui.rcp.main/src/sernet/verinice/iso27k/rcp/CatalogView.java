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
 *     Daniel <dm@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.rcp;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.text.DateFormatter;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;
import org.jfree.util.Log;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.model.Attachment;
import sernet.gs.ui.rcp.main.bsi.model.AttachmentFile;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.views.Messages;
import sernet.gs.ui.rcp.main.bsi.views.actions.MassnahmenViewFilterAction;
import sernet.gs.ui.rcp.main.service.ICommandService;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadAttachmentFile;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadAttachments;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadBSIModel;
import sernet.gs.ui.rcp.main.service.crudcommands.SaveAttachment;
import sernet.gs.ui.rcp.main.service.crudcommands.SaveNote;
import sernet.verinice.iso27k.rcp.action.ControlDragListener;
import sernet.verinice.iso27k.service.IItem;
import sernet.verinice.iso27k.service.commands.CsvFile;
import sernet.verinice.iso27k.service.commands.ImportCatalog;

/**
 * @author Daniel <dm@sernet.de>
 * 
 */
public class CatalogView extends ViewPart {

	private static final Logger LOG = Logger.getLogger(CatalogView.class);

	public static final String ID = "sernet.verinice.iso27k.rcp.CatalogView"; //$NON-NLS-1$
	
	public static final DateFormat DATE_TIME_FORMAT_SHORT = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

	private Action addCatalogAction;
	
	private Action filterAction;
	
	private DragSourceListener dragListener;

	private ICommandService commandService;

	private TreeViewer viewer;
	
	private Label labelCatalog;
	
	private Combo comboCatalog;
	
	private Label labelFilter;
	
	private Text filter;
	
	private BSIModel bsiModel;
	
	List<Attachment> attachmentList;
	
	private CatalogTextFilter textFilter;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		
		GridLayout gl = new GridLayout(1, true);
		parent.setLayout(gl);
		
		Composite compForm = new Composite(parent,SWT.NONE);
		GridLayout glForm = new GridLayout(2, false);
		compForm.setLayout(glForm);
		labelCatalog = new Label(compForm,SWT.NONE);
		labelCatalog.setText("Katalog");
		comboCatalog = new Combo(compForm, SWT.DROP_DOWN);
		comboCatalog.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		comboCatalog.addSelectionListener(new SelectionAdapter() {
		      public void widgetSelected(SelectionEvent e) {
		    	  openCatalog();
		      }
		    });
		
		labelFilter = new Label(compForm,SWT.NONE);
		labelFilter.setText("Filter");
		filter = new Text(compForm, SWT.BORDER);
		filter.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		filter.addKeyListener(new KeyListener() {

			int minLength = 3;
			
			public void keyPressed(KeyEvent e) {
				
				
			}

			public void keyReleased(KeyEvent e) {
				textFilter.setPattern(filter.getText());
			}
		
		});
		
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		viewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		//viewer.setSorter(new KapitelSorter());
			
		getSite().setSelectionProvider(viewer);
		
		makeActions();
		hookActions();
		hookDNDListeners();
		fillLocalToolBar();
		
		loadCatalogAttachmets();
	}
	


	/**
	 * 
	 */
	private void loadCatalogAttachmets() {
		try {
			LoadAttachments command = new LoadAttachments(getBsiModel().getDbId());		
			command = getCommandService().executeCommand(command);		
			attachmentList = command.getAttachmentList();
			String[] fileNameArray = new String[attachmentList.size()];
			int i = 0;		
			for (Attachment attachment : attachmentList) {
				StringBuilder sb = new StringBuilder();
				sb.append(attachment.getFileName());
				sb.append(" (").append(DATE_TIME_FORMAT_SHORT.format(attachment.getDate())).append(")");
				fileNameArray[i] = sb.toString();			
				i++;
			}
			Arrays.sort(fileNameArray);
			comboCatalog.setItems(fileNameArray);
		} catch(Exception e) {
			
		}
	}
	
	/**
	 * @throws CommandException 
	 * 
	 */
	private void saveFile(ImportCatalog importCatalog) throws CommandException {
		CsvFile csvFile = importCatalog.getCsvFile();
		
		if(csvFile!=null) {			
			Attachment attachment = new Attachment();
			attachment.setCnATreeElementId(getBsiModel().getDbId());
			attachment.setCnAElementTitel(getBsiModel().getTitle());
			Date now = Calendar.getInstance().getTime();
			attachment.setDate(now);
			attachment.setFilePath(csvFile.getFilePath());
			attachment.setTitel(attachment.getFileName());
			attachment.setText("Control / threat catalog imported at: " + DateFormat.getDateTimeInstance().format(now));
			SaveNote command = new SaveNote(attachment);	
			command = getCommandService().executeCommand(command);
			attachment = (Attachment) command.getAddition();
			
			AttachmentFile attachmentFile = new AttachmentFile();
			attachmentFile.setDbId(attachment.getDbId());
			attachmentFile.setFileData(csvFile.getFileContent());
			SaveAttachment saveAttachmentFile = new SaveAttachment(attachmentFile);
			saveAttachmentFile = getCommandService().executeCommand(saveAttachmentFile);
			saveAttachmentFile.clear();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	private void makeActions() {
		addCatalogAction = new Action() {
			public void run() {
				importCatalog();
			}
		};
		addCatalogAction.setText("Katalog importieren...");
		addCatalogAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.NOTE_NEW));
		addCatalogAction.setEnabled(true);
		
		textFilter = new CatalogTextFilter(viewer);
		filterAction = new CatalogViewFilterAction(viewer, this.textFilter);
		
		dragListener = new ControlDragListener(viewer);
	}
	
	

	/**
	 * 
	 */
	private void hookActions() {
	}
	
	private void hookDNDListeners() {
		Transfer[] types = new Transfer[] { TextTransfer.getInstance(),FileTransfer.getInstance() };
		int operations = DND.DROP_COPY | DND.DROP_MOVE;
		viewer.addDragSupport(operations, types, dragListener);
	}
	/**
	 * 
	 */
	private void fillLocalToolBar() {
		IActionBars bars = getViewSite().getActionBars();
		IToolBarManager manager = bars.getToolBarManager();
		manager.add(this.addCatalogAction);
	}

	public ICommandService getCommandService() {
		if (commandService == null) {
			commandService = createCommandServive();
		}
		return commandService;
	}

	private ICommandService createCommandServive() {
		return ServiceFactory.lookupCommandService();
	}

	static class ViewContentProvider implements IStructuredContentProvider, ITreeContentProvider {

		public void dispose() {
		}

		public Object[] getChildren(Object parent) {
			return ((IItem) parent).getItems().toArray();		
		}

		public Object[] getElements(Object parent) {
			return getChildren(parent);
		}

		public Object getParent(Object child) {
			return null;
		}

		public boolean hasChildren(Object parent) {
			return ((IItem) parent).getItems().size()>0;
		}

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
	}
	
	static class ViewLabelProvider extends LabelProvider {

		public Image getImage(Object obj) {
			IItem item = (IItem) obj;
			String image = ImageCache.UNKNOWN;
			if(item.getDescription()!=null && item.getTypeId()==IItem.CONTROL) {
				image = ImageCache.WRENCH;
			}
			if(item.getDescription()!=null && item.getTypeId()==IItem.THREAD) {
				image = ImageCache.GEFAEHRDUNG;
			}
			return ImageCache.getInstance().getImage(image);	
		}

		public String getText(Object obj) {
			return ((IItem)obj).getName();
		}
	}

	public BSIModel getBsiModel() {
		if(bsiModel==null) {
			bsiModel = loadBsiModel();
		}
		return bsiModel;
	}

	public void setBsiModel(BSIModel bsiModel) {
		this.bsiModel = bsiModel;
	}
	
	public BSIModel loadBsiModel() {
		LoadBSIModel loadBSIModel = new LoadBSIModel();
		try {
			loadBSIModel = getCommandService().executeCommand(loadBSIModel);
		} catch (CommandException e) {
			LOG.error("Error while loading BSI-Model.", e);
		}
		bsiModel = loadBSIModel.getModel();
		return bsiModel;
	}

	private void openCatalog() {
		try {
			Attachment selected = attachmentList.get(comboCatalog.getSelectionIndex());
			LoadAttachmentFile loadAttachmentFile = new LoadAttachmentFile(selected.getDbId());
			loadAttachmentFile = getCommandService().executeCommand(loadAttachmentFile);
			if(loadAttachmentFile!=null && loadAttachmentFile.getAttachmentFile()!=null && loadAttachmentFile.getAttachmentFile().getFileData()!=null) {
				ImportCatalog importCatalog = new ImportCatalog(loadAttachmentFile.getAttachmentFile().getFileData());
				importCatalog = getCommandService().executeCommand(importCatalog);
				if(importCatalog.getCatalog()!=null) {
					viewer.setInput(importCatalog.getCatalog().getRoot());
				}
			}
			setContentDescription(selected.getFileName());
		} catch(Exception e) {
			LOG.error("Error while loading catalog", e);
			ExceptionUtil.log(e, "Error while loading catalog");
		}
	}

	private void importCatalog() {
		FileDialog fd = new FileDialog(CatalogView.this.getSite().getShell());
		fd.setText("Katalog auswählen...");
		fd.setFilterPath("~");
		fd.setFilterExtensions(new String[] { "*.csv" });
		String selected = fd.open();
		if (selected != null && selected.length() > 0) {
			try {
				ImportCatalog importCatalog = new ImportCatalog(selected);
				importCatalog = getCommandService().executeCommand(importCatalog);
				saveFile(importCatalog);
				if(importCatalog.getCatalog()!=null) {
					viewer.setInput(importCatalog.getCatalog().getRoot());
				}
				File file = new File(selected);
				setContentDescription(file.getName());	
				loadCatalogAttachmets();
			} catch (Exception e) {
				LOG.error("Error while reading file data", e);
				ExceptionUtil.log(e, "Fehler beim Lesen der Datei.");
			}
		}
	}
}
