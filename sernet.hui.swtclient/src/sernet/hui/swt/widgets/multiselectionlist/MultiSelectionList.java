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
package sernet.hui.swt.widgets.multiselectionlist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;

import sernet.hui.common.connect.PropertyType;
import sernet.hui.common.multiselectionlist.IMLPropertyOption;
import sernet.hui.common.multiselectionlist.IMLPropertyType;
import sernet.hui.common.multiselectionlist.ISelectOptionHandler;

/**
 * @author prack
 */
public class MultiSelectionList {

	private Composite parent;

	private List<ISelectOptionHandler> eventHandler = new ArrayList<ISelectOptionHandler>();


	private Composite list;

	private Group group;

	// map with key-value "optionid : checkbox"
	private Map<String, Button> checkboxes = new HashMap<String, Button>();

	private GridData customLayout;

	private boolean referencesEntities;

	private List<IMLPropertyOption> options;

	private PropertyType type;

	private boolean contextMenuPresent = false;

	public boolean isContextMenuPresent() {
		return contextMenuPresent;
	}

	public MultiSelectionList(ISelectOptionHandler entity, PropertyType type,
			Composite parent, boolean referencesEntities) {
		this.parent = parent;
		this.eventHandler.add(entity);
		this.type = type;
		this.referencesEntities = referencesEntities;
	}

	public void addListener(ISelectOptionHandler listener) {
		this.eventHandler.add(listener);
	}

	public void refresh() {
		list.getParent().dispose();
		list = createScrolledList();
		int height = createButtons();
		GridData gd = (GridData) group.getLayoutData();
		gd.heightHint = height;
		list.pack();
		group.layout();
	}

	public void create() {
		if (referencesEntities) {
			options = type.getReferencedEntities();
			Collections.sort(options, new Comparator<IMLPropertyOption>() {
				public int compare(IMLPropertyOption o1, IMLPropertyOption o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
		} else {
			options = type.getOptions();
		}
		group = createGroup();
		list = createScrolledList();
		int height = createButtons();
		GridData gd = (GridData) group.getLayoutData();
		gd.heightHint = height;
		list.pack();
		parent.getParent().pack();
	}

	private Group createGroup() {
		Group group_ = new Group(parent, SWT.BORDER);

		GridData groupLData = new GridData();
		groupLData.verticalAlignment = GridData.CENTER;
		groupLData.horizontalAlignment = GridData.FILL;
		groupLData.widthHint = -1;
		groupLData.heightHint = 100;
		groupLData.horizontalIndent = 0;
		groupLData.horizontalSpan = 4;
		groupLData.verticalSpan = 1;
		groupLData.grabExcessHorizontalSpace = true;
		groupLData.grabExcessVerticalSpace = false;
		group_.setLayoutData(groupLData);
		group_.setText(type.getName());
		group_.setLayout(new FillLayout(SWT.V_SCROLL));
		return group_;
	}

	private int createButtons() {
		
		int btnsHeight = 0;
		int i = 0;
		for (Iterator<IMLPropertyOption> iter = options.iterator(); iter.hasNext();) {
			++i;
			final IMLPropertyOption option = iter.next();
			Button checkbox = new Button(list, SWT.CHECK | SWT.LEFT);
			checkbox.setText(option.getName());
			checkbox.setData(option);
			checkbox.pack();

			// add / delete property when user clicks on checkbox:
			checkbox.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent arg0) {
					Button box = (Button) arg0.widget;
					IMLPropertyOption option = (IMLPropertyOption) box.getData();
					if (box.getSelection()) {
						fireSelect(type, option);
						if (!type.isMultiselect()) {
							// remove all other selections:
							unselectOthers(option);
						}
					} else {
						fireUnselect(type, option);
					}
				}

				private void unselectOthers(IMLPropertyOption option) {
					ArrayList<IMLPropertyOption> opts = (ArrayList<IMLPropertyOption>)type.getOptions();
					Iterator<IMLPropertyOption> iterator = opts.iterator();
					while (iterator.hasNext())
					{
						IMLPropertyOption opt = iterator.next();
						
						if (!opt.getId().equals(option.getId())) {
							checkboxes.get(opt.getId()).setSelection(false);
							fireUnselect(type, opt);
						}
					}
				}

				public void widgetDefaultSelected(SelectionEvent arg0) {
				}
			});

			// add context menu if defined:
			if (option.getContextMenuListener() != null) {
				this.contextMenuPresent = true;
				checkbox.addListener(SWT.MenuDetect,  new Listener() {
					public void handleEvent(Event event) {
						option.getContextMenuListener().showContextMenu(event.x, event.y);
					}
				});
			}
			
			checkboxes.put(option.getId(), checkbox);
			if (i < 6) {
				btnsHeight = btnsHeight + checkbox.getBounds().height + 5;
			}
		}
		return btnsHeight;
	}

	/**
	 * 
	 */
	private Composite createScrolledList() {
		ScrolledComposite scrollPane = new ScrolledComposite(group,
				SWT.V_SCROLL);
		Composite list_ = new Composite(scrollPane, SWT.NULL);
		GridLayout listLayout = new GridLayout(1, false);
		list_.setLayout(listLayout);
		scrollPane.setContent(list_);
		return list_;
	}

	/**
	 * Selects an option.
	 * 
	 * @param optionId
	 *            the ID of the option to select
	 */
	public void setSelection(String optionId, boolean select) {
		Button checkbox = (Button) checkboxes.get(optionId);
		checkbox.setSelection(select);
	}

	/**
	 * (Un)Select list of options.
	 * 
	 * @param options
	 *            List of mlpropertyoptions to (un)select
	 * @param select
	 *            select or deselect listed options?
	 */
	public void setSelection(List options, boolean select) {
		for (Iterator iter = options.iterator(); iter.hasNext();) {
			IMLPropertyOption option = (IMLPropertyOption) iter.next();
			if (option != null){
				setSelection(option.getId(), select);
			}
		}
	}

	public void setEnabled(String optionId, boolean enabled) {
		Button checkbox = (Button) checkboxes.get(optionId);
		checkbox.setEnabled(enabled);
	}

	/**
	 * @param scrolledComposite1LData
	 */
	public void setLayoutData(GridData scrolledComposite1LData) {
		group.setLayoutData(scrolledComposite1LData);
		group.layout();
		this.customLayout = scrolledComposite1LData;
	}

	private void fireSelect(IMLPropertyType type, IMLPropertyOption option) {
		for (ISelectOptionHandler handler : eventHandler) {
			if (handler != null){
				handler.select(type, option);
			}
		}
	}

	private void fireUnselect(IMLPropertyType type, IMLPropertyOption option) {
		for (ISelectOptionHandler handler : eventHandler) {
			if (handler != null){
				handler.unselect(type, option);
			}
		}
	}
}
