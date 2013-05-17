/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
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
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.views;

import java.io.File;
import java.util.Calendar;

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;

import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.verinice.model.bsi.Attachment;

/**
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */
public class FileDropTarget extends DropTargetAdapter {

	private FileView view;

	/**
	 * @param viewer
	 */
	public FileDropTarget(FileView view) {
		this.view = view;
		DropTarget target = new DropTarget(view.getViewer().getControl(), DND.DROP_MOVE
				| DND.DROP_COPY);
		target.setTransfer(new Transfer[] { FileTransfer.getInstance() });
		target.addDropListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.swt.dnd.DropTargetAdapter#drop(org.eclipse.swt.dnd.
	 * DropTargetEvent)
	 */
	@Override
	public void drop(DropTargetEvent event) {
		if (FileTransfer.getInstance().isSupportedType(event.currentDataType)) {
			String[] files = (String[]) event.data;
			for (int i = 0; i < files.length; i++) {
				String selected = files[i];
				 if(selected!=null && selected.length()>0) {
					 	File file = new File(selected);
					 	// the user can drag-n-drop a directory with files, but only one level deep:
					 	if (file.isDirectory()) {
					 		File[] dirFiles = file.listFiles();
					 		for (File dirFile : dirFiles) {
					 			createFile(dirFile.getAbsolutePath());
							}
					 	}
						createFile(selected);
			        }
			}
		}
	}

	/**
	 * @param selected
	 */
	private void createFile(String selected) {
		File file = new File(selected);
		if (file.isDirectory()){
			return;
		}
		Attachment attachment = new Attachment();
		attachment.setCnATreeElementId(view.getCurrentCnaElement().getDbId());
		attachment.setCnAElementTitel(view.getCurrentCnaElement().getTitle());
		attachment.setTitel(file.getName());
		attachment.setDate(Calendar.getInstance().getTime());
		attachment.setFilePath(selected);
		attachment.addListener(new Attachment.INoteChangedListener() {
			public void noteChanged() {
				view.loadFiles();
			}
		});
		EditorFactory.getInstance().openEditor(attachment);			
	}
}
