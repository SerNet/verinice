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

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.model.Attachment;
import sernet.gs.ui.rcp.main.bsi.model.AttachmentFile;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.service.ICommandService;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadBSIModel;
import sernet.gs.ui.rcp.main.service.crudcommands.SaveAttachment;
import sernet.gs.ui.rcp.main.service.crudcommands.SaveNote;
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

	private Action addCatalogAction;

	private ICommandService commandService;

	private TreeViewer viewer;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {		
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		//viewer.setSorter(new KapitelSorter());
			
		getSite().setSelectionProvider(viewer);
		
		makeActions();
		hookActions();
		fillLocalToolBar();
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
						
					} catch (Exception e) {
						LOG.error("Error while reading file data", e);
						ExceptionUtil.log(e, "Fehler beim Lesen der Datei.");
					}
				}
			}
		};
		addCatalogAction.setText("Katalog importieren...");
		addCatalogAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.NOTE_NEW));
		addCatalogAction.setEnabled(true);
	}
	
	/**
	 * @throws CommandException 
	 * 
	 */
	private void saveFile(ImportCatalog importCatalog) throws CommandException {
		CsvFile csvFile = importCatalog.getCsvFile();
		
		if(csvFile!=null) {
			LoadBSIModel loadBSIModel = new LoadBSIModel();
			loadBSIModel = getCommandService().executeCommand(loadBSIModel);
			BSIModel model = loadBSIModel.getModel();
			
			Attachment attachment = new Attachment();
			attachment.setCnATreeElementId(model.getDbId());
			attachment.setCnAElementTitel(model.getTitel());
			Date now = Calendar.getInstance().getTime();
			attachment.setDate(now);
			String fileName = csvFile.getFilePath();
			char separator = '\\';
			if(fileName.contains("/")) {
				separator = '/';
			}
			int lastSeparator = fileName.lastIndexOf(separator);
			if(lastSeparator!=-1) {
				fileName = fileName.substring(lastSeparator+1);
			}
			attachment.setFileName(fileName);
			attachment.setTitel(fileName);
			attachment.setText("Measure catalog imported at: " + DateFormat.getDateTimeInstance().format(now));
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

	/**
	 * 
	 */
	private void hookActions() {
		// TODO Auto-generated method stub

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
			// TODO: return a nice image
			return ImageCache.getInstance().getImage(ImageCache.UNKNOWN);	
		}

		public String getText(Object obj) {
			return ((IItem)obj).getName();
		}
	}
}
