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
package sernet.hui.swt.widgets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.PopupList;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import sernet.hui.common.connect.DependsType;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.IEntityChangedListener;
import sernet.hui.common.connect.PropertyChangedEvent;
import sernet.hui.common.connect.PropertyGroup;
import sernet.hui.common.connect.PropertyOption;
import sernet.hui.common.connect.PropertyType;
import sernet.hui.common.multiselectionlist.IMLPropertyOption;
import sernet.hui.common.multiselectionlist.IMLPropertyType;
import sernet.hui.swt.widgets.URL.URLControl;
import sernet.hui.swt.widgets.multiselectionlist.MultiSelectionControl;
import sernet.snutils.AssertException;
import sernet.snutils.DBException;
import sernet.snutils.ExceptionHandlerFactory;

/**
 * Creates the editable SWT view of a collection of properties as defined
 * by a <code>DynamicDocumentation</code> instance. 
 * The user can select or enter values, which are saved to and retrieved from
 * a relational database.
 * 
 * 
 * @author koderman[at]sernet[dot]de
 * 
 */
public class HitroUIView implements IEntityChangedListener   {

    private static final Logger LOG = Logger.getLogger(HitroUIView.class);
    
	private Composite huiComposite;

	private Entity entity;

	// map of typeid : widget
	private Map<String, IHuiControl> fields = new HashMap<String, IHuiControl>();
	
	/**
     * IEditorBehavior instances implementing special "behavior" of this
     *       editor See method addBehavior.
     */
    private List<IEditorBehavior> editorBehaviorList = new ArrayList<IEditorBehavior>(1);

	private Composite formComp;

	private ScrolledComposite scrolledComp;

	private boolean editable;

	private IHuiControl focusField;
	private IHuiControl firstField;

	private boolean useRules;

    private String[] filterTags;

    private boolean taggedOnly = false;

    private List<String> validationList;
    
    private boolean useValidationGuiHints = false;


	/**
	 * Create a new view for dynamic documentation.
	 * 
	 * All new fields are put into sysdoccomposite. Then, composite2 will be
	 * resized according to the calculated size of sysTabComposite.
	 * 
	 * @param scrolledComposite
	 *            the top scrolled composite, height will be adapted to created fields
	 * @param formComposite
	 *            parent container for dynamic - and possibly additional - widgets
	 * @param huiComposite
	 *            container in which dynamic fields will be generated
	 * @param tabFolderMain
	 */
	public HitroUIView(ScrolledComposite scrolledComposite,Composite formComposite, Composite huiComposite) {
		this.huiComposite = huiComposite;
		this.formComp = formComposite;
		this.scrolledComp = scrolledComposite;
		this.editable = false;
	}
	
	/**
	 * Enables simple content assist for text fields.
	 * 
	 * @param typeID
	 * @param helper
	 */
	public void setInputHelper(String typeID, final IInputHelper helper, int type, final boolean showHint) {
		IHuiControl field = this.fields.get(typeID);
		if (field == null){
			return;
		}
		final Control control = field.getControl();
		if (!(control instanceof Text)){
			return;
		}
		KeyStroke keyStroke = null;
		try {
			keyStroke = KeyStroke.getInstance("ARROW_DOWN");
		} catch (ParseException e) {
		}
		ContentProposalAdapter adapter = new ContentProposalAdapter(
				control, 
				new TextContentAdapter(), 
				new SimpleContentProposalProvider(helper.getSuggestions()),
				keyStroke, 
				null);
		if (type == IInputHelper.TYPE_REPLACE){
			adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
		} else { 
			adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_INSERT);
		}
		final FocusAdapter focusAdapter = new FocusAdapter() {
			private Shell tip = null;
			@Override
            public void focusGained(FocusEvent arg0) {
				if (!showHint){
					return; // do not show activation hint
				}
				if (helper.getSuggestions().length<1){
					return; // no suggestions
				}
				tip = new Shell (control.getShell(),
						SWT.ON_TOP | SWT.NO_FOCUS | SWT.TOOL);
				FillLayout layout = new FillLayout ();
				layout.marginWidth = 2;
				tip.setLayout (layout);
			
				Label label = new Label (tip, SWT.NONE);
				label.setText ("Hilfe: Pfeil-Runter-Taste");
			
				Point size = tip.computeSize (SWT.DEFAULT, SWT.DEFAULT);
				Rectangle rect = control.getBounds ();
				Point pt = control.getParent().toDisplay (rect.x, rect.y);
				tip.setBounds (pt.x, pt.y + rect.height, size.x, size.y);
				tip.setVisible (true);
			}
			
			@Override
			public void focusLost(FocusEvent e) {
				if (showHint && tip != null) {
					tip.dispose();
					tip=null;
				}
			}
		};
		
		control.addFocusListener(focusAdapter);
		control.addDisposeListener(new DisposeListener() {
			@Override
            public void widgetDisposed(DisposeEvent arg0) {
				control.removeFocusListener(focusAdapter);
			}
		});
		
	}

	protected void showPopupList(Control control, IInputHelper helper) {
			PopupList popup = new PopupList(
					new Shell(control.getShell(), SWT.ON_TOP | SWT.TOOL));
			popup.setItems(helper.getSuggestions());
			Rectangle rect = control.getBounds();
			Point pt = control.getParent().toDisplay (rect.x, rect.y);
			String choice = popup.open(new Rectangle(
					pt.x,
					pt.y,
					control.getBounds().width,
					control.getBounds().height));
			if (choice != null && choice.length() > 0){
				((Text)control).setText(choice);
			}
	}

	public void closeView() {
		entity.removeListener(this);
	}
	
	/**
	 * Create form fields for all defined property types and fill them with
	 * property values.
	 * 
	 * @param entity entitty to edit
	 * @param edit editable or read only?
	 * @param useRules use validation and default-value rules?
	 * @param tags use tags to filter view?
	 * @param taggedPropertiesOnly 
	 * true: show only properties with matching tag (filter out all without tag or with different tags)
	 * false: show properties without tags and those with matching tag (filter out all with different tag) 
	 * @param validationMap contains validation results for {@link PropertyType}s  
	 * @throws DBException
	 */
	public void createView(Entity entity, boolean edit, boolean useRules, String[] tags, boolean taggedPropertiesOnly,
	        List<String> validationList, boolean useValidationGuiHints) throws DBException {
		
		this.editable = edit;
		this.useRules = useRules;
		this.filterTags = (tags != null) ? tags.clone() : null;
		this.taggedOnly = taggedPropertiesOnly;
		this.validationList = validationList;
		this.useValidationGuiHints = useValidationGuiHints;
		
		huiComposite.setVisible(false);
		this.entity = entity;
		entity.addChangeListener(this);
		clearComposite();
		HUITypeFactory typeFactory = HUITypeFactory.getInstance();

		EntityType entityType = typeFactory.getEntityType(entity.getEntityType());
		// create all form fields:
		List allElements = entityType.getElements();
		for (Iterator iter = allElements.iterator(); iter.hasNext();) {
			Object obj = iter.next();
			if (obj instanceof PropertyType) {
				PropertyType type = (PropertyType) obj;
				createField(type, huiComposite, validationList.contains(type.getId()), useValidationGuiHints);
			} else if (obj instanceof PropertyGroup) {
				PropertyGroup group = (PropertyGroup) obj;
				createGroup(group, huiComposite);
			}
		}
        addBehavior(entityType);
		huiComposite.layout();
		huiComposite.setVisible(true);
		resizeContainer();
		setInitialFocus();
	}

	private void createGroup(PropertyGroup group, Composite parent) {	
		if (hideBecauseOfTags(group.getTags())){
		    return;
		}
		
		PropertyTwistie twistie = new PropertyTwistie(this, parent, group);
		twistie.create();
		for (PropertyType type: group.getPropertyTypes() ) {
			createField(type, twistie.getFieldsComposite(), validationList.contains(type.getId()), useValidationGuiHints);
		}
		
	}

	private void createField(PropertyType type, Composite parent, boolean showValidationHint, boolean useValidationGuiHints) {		
		// only allow edit if both view and field settings are true:
		boolean editableField = editable && type.isEditable();
		
		if (!type.isVisible() || hideBecauseOfTags(type.getTags())){
			return;
		}
		if (type.isURL()){
			createURLField(type, parent, showValidationHint, useValidationGuiHints);
		} else if (type.isLine()) {
			createTextField(type, editableField, parent, type.isFocus(), 1 /*one line high*/, showValidationHint, useValidationGuiHints);
		} else if (type.isSingleSelect()) {
			createSingleOptionField(type, editableField, parent, type.isFocus(), showValidationHint, useValidationGuiHints);
		} else if (type.isReference()) {
			createMultiOptionField(type, editableField, parent, type.isFocus(), true, type.isCrudButtons(), false, showValidationHint, useValidationGuiHints);
		} else if (type.isMultiselect()) {
			createMultiOptionField(type, editableField, parent, type.isFocus(), false, false, false, showValidationHint, useValidationGuiHints);
		} else if (type.isDate()) {
			createDateField(type, editableField, parent, type.isFocus(), showValidationHint, useValidationGuiHints);
		} else if (type.isText()) {
			createTextField(type, editableField, parent, type.isFocus(), type.getTextrows(), showValidationHint, useValidationGuiHints);
		} else if (type.isBooleanSelect()) {
		    createBooleanSelect(type, parent, type.isFocus());
		} else if (type.isNumericSelect()) {
		    createNumericSelect(type, editableField, parent, type.isFocus(), showValidationHint, useValidationGuiHints);
		} else if (type.isCnaLinkReference()) {
            createMultiOptionField(type, editableField, parent, type.isFocus(), false, type.isCrudButtons(), true, showValidationHint, useValidationGuiHints);
		}
	}
	
	private void addBehavior(EntityType entityType) {
        for (PropertyType type : entityType.getAllPropertyTypes()) {
            addBehavior(entityType,type);
        } 
    }
    
    private void addBehavior(EntityType entityType, PropertyType type) {
        IHuiControl control = fields.get(type.getId());
        DependsBehavior mainBehavior = null;
        DependsBehavior currentBehavior = null;
        for (DependsType depends : type.getDependencies()) {
            IHuiControl controlDependsOn = fields.get(depends.getPropertyId());
            PropertyType typeDependsOn = entityType.getPropertyType(depends.getPropertyId());
            if(controlDependsOn==null || controlDependsOn.getControl()==null) {
                LOG.error("Depends on control not find, id: " + depends.getPropertyId());
                continue;
            }      
            DependsBehavior behavior = BehaviorFactory.createBehaviorForControl(controlDependsOn.getControl());
            behavior.setControl(control.getControl());
            behavior.setInverse(depends.isInverse());
            behavior.setValueDependsOn(depends.getPropertyValue());
            if(typeDependsOn.isSingleSelect() || typeDependsOn.isNumericSelect()) {
                PropertyOption option = typeDependsOn.getOption(depends.getPropertyValue());
                if(option!=null) {
                    behavior.setValueDependsOn(option.getName());
                }
            }
            if(currentBehavior==null) {             
                currentBehavior = behavior;
                mainBehavior = behavior;
            } else {
                currentBehavior.setNext(behavior);
                currentBehavior = behavior;
            }            
        }
        if(mainBehavior!=null) {
            initBehaviour(mainBehavior);
        }       
    }
    
    public void initBehaviour(IEditorBehavior behavior) {
        editorBehaviorList.add(behavior);
        behavior.addBehavior();
        behavior.init();
    }

	/**
     * @param propertyTags
     * @return
     */
    private boolean hideBecauseOfTags(String propertyTags) {
        if (taggedOnly) {
            // for properties with tags set, display them if tag matches:
            return !(tagMatches(propertyTags));
        } else {
            // show all without tag and matching tags:
            if (propertyTags==null || propertyTags.length()==0){
                return false;
            }
            return !(tagMatches(propertyTags));
        }
    }

    /**
     * Find out if one of the wanted tags matches the property.
     * 
     * @param propertyTags
     * @return
     */
    private boolean tagMatches(String propertyTags) {
        if (filterTags == null || filterTags.length==0 || propertyTags == null || propertyTags.length() == 0){
            return false;
        }
        for(String searchTag: filterTags) {
           if (propertyTags.indexOf(searchTag) > -1){
               return true;
           }
        }
        return false;
    }

    /**
	 * @param type
	 * @param editableField
	 * @param parent
	 * @param focus
	 */
	private void createNumericSelect(PropertyType fieldType, boolean editableField,
			Composite parent, boolean focus, boolean showValidationHint, boolean useValidationGuiHints) {
		NumericSelectionControl sglControl = new NumericSelectionControl(entity, fieldType,
				parent, editableField, showValidationHint, useValidationGuiHints);
		sglControl.create();
		if (focus){
			focusField = sglControl;
		}
		fields.put(fieldType.getId(), sglControl);
		sglControl.validate();
		setFirstField(sglControl);
	}
	
	private void createBooleanSelect(PropertyType fieldType, Composite parent, boolean focus) {
        BooleanSelectionControl sglControl = new BooleanSelectionControl(entity, fieldType,
                parent, editable);
        sglControl.create();
        if (focus){
            focusField = sglControl;
        }
        fields.put(fieldType.getId(), sglControl);
        sglControl.validate();
        setFirstField(sglControl);
    }

	private void createURLField(PropertyType type,
			Composite parent, boolean showValidationHint, boolean useValidationGuiHints) {
		URLControl urlControl = new URLControl(entity, type, parent, editable, showValidationHint, useValidationGuiHints);
		urlControl.create();
		fields.put(type.getId(), urlControl);
		urlControl.validate();
		setFirstField(urlControl);
	}

	public void resizeContainer() {
		formComp.layout(true);
		scrolledComp.setMinHeight(calculateMinHeight(formComp));
		scrolledComp.layout(true);
	}

	private int calculateMinHeight(Composite comp) {
		int size = 0;
		Control[] children = comp.getChildren();
		for (int i = 0; i < children.length; i++) {
			size += children[i].getSize().y;
		}
		// add 2 for margin (1px at top, 1px at bottom, see contentCompLayout in HitroUIComposite.java)
		return size+2;
	}

	private void createTextField(PropertyType type, boolean editable, Composite parent,
			boolean focus, int lines, boolean showValidationHint, boolean useValidationGuiHints) {
		TextControl textControl = new TextControl(entity, type, parent, editable, lines, useRules, showValidationHint, useValidationGuiHints);
		textControl.create();
		if (focus){
			focusField = textControl;
		}
		fields.put(type.getId(), textControl);
		textControl.validate();
		setFirstField(textControl);
		setFirstField(textControl);
	}

	/**
     * @param textControl
     */
    private void setFirstField(IHuiControl control) {
        if (this.firstField == null){
            firstField = control;
        }
    }

    /**
	 * Create a selection list for the given property with all defined options.
	 * @param crudButtons 
	 * 
	 * @param props
	 * @throws AssertException
	 */
	private void createMultiOptionField(PropertyType type, boolean editable, Composite parent,
			boolean focus, boolean reference, boolean crudButtons, boolean cnalinkreference, boolean showValidationHint, boolean useValidationGuiHints) {
		MultiSelectionControl mlControl = new MultiSelectionControl(entity, type,
				parent, editable, reference, crudButtons, cnalinkreference, showValidationHint, useValidationGuiHints);
		mlControl.create();
		if (focus){
			focusField = mlControl;
		}
		fields.put(type.getId(), mlControl);
		mlControl.validate();
		setFirstField(mlControl);
	}
	
	private void createSingleOptionField(PropertyType fieldType, boolean editable, 
			Composite parent, boolean focus, boolean showValidationHint, boolean useValidationGuiHints) {
		SingleSelectionControl sglControl = new SingleSelectionControl(entity, fieldType,
				parent, editable, showValidationHint, useValidationGuiHints);
		sglControl.create();
		if (focus)
			focusField = sglControl;
		fields.put(fieldType.getId(), sglControl);
		sglControl.validate();
		setFirstField(sglControl);
	}
	
	public void setInitialFocus() {
		if (focusField != null) {
		    focusField.setFocus();
		}
		else if (firstField != null) {
		    firstField.setFocus();
		}
		    
	}
	
	private void createDateField(PropertyType fieldType, boolean editable, Composite parent,
			boolean focus, boolean showValidationHint, boolean useValidationGuiHints) {
		DateSelectionControl dateCtl = 
			new DateSelectionControl(entity, fieldType, parent, editable, this.useRules, showValidationHint, useValidationGuiHints);
		dateCtl.create();
		if (focus){
			focusField = dateCtl;
		}
		fields.put(fieldType.getId(), dateCtl);
		dateCtl.validate();
		setFirstField(dateCtl);
	}
	
	/**
	 * Clear all fields.
	 * 
	 */
	private void clearComposite() {
		Control[] children = huiComposite.getChildren();
		for (int i = 0; i < children.length; i++) {
			children[i].dispose();
		}
		fields = new HashMap<String, IHuiControl>();
		
	}

	/* (non-Javadoc)
	 * @see sernet.snkdb.connect.docproperties.IDynDocChangedListener#dependencyChanged(sernet.snkdb.guiswt.multiselectionlist.MLPropertyType, sernet.snkdb.guiswt.multiselectionlist.MLPropertyOption)
	 */
	public void dependencyChanged(IMLPropertyType type, IMLPropertyOption opt) {
		try {
			closeView();
			createView(entity, editable, useRules, filterTags, taggedOnly, validationList, useValidationGuiHints);
		} catch (DBException e) {
			ExceptionHandlerFactory.getDefaultHandler().handleException(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see sernet.snkdb.connect.docproperties.IDynDocChangedListener#selectionChanged(sernet.snkdb.guiswt.multiselectionlist.MLPropertyType, sernet.snkdb.guiswt.multiselectionlist.MLPropertyOption)
	 */
	@Override
    public void selectionChanged(IMLPropertyType type, IMLPropertyOption opt) {
		Object object = fields.get(type.getId());
		MultiSelectionControl control;
		if (object instanceof MultiSelectionControl) {
			control = (MultiSelectionControl) object;
			control.writeToTextField();
			control.validate();
		}
		// FIXME this really should be handeled by ML field itself
	}

	@Override
    public void propertyChanged(PropertyChangedEvent event) {
		IHuiControl control = fields.get(event.getProperty().getPropertyTypeID());
		if (control != null){
			control.update();
		}
	}
	
	public void addSelectionListener(String id, SelectionListener listener) {
        IHuiControl huiControl = fields.get(id);
        if(huiControl!=null && huiControl.getControl()!=null && huiControl.getControl() instanceof Button) {
            ((Button)huiControl.getControl()).addSelectionListener(listener);
        }
    }
	
	public void removeSelectionListener(String id, SelectionListener listener) {
	    IHuiControl huiControl = fields.get(id);
        if(huiControl!=null && huiControl.getControl()!=null && huiControl.getControl() instanceof Button) {
            ((Button)huiControl.getControl()).removeSelectionListener(listener);
        }
        
    }
	
	public void setFieldEnabled(String id, boolean enabled) {
        IHuiControl huiControl = fields.get(id);
        if(huiControl!=null && huiControl.getControl()!=null ) {
            huiControl.getControl().setEnabled(enabled);
        }
    }

    public Control getField(String id) {
        Control control = null;
        IHuiControl huiControl = fields.get(id);
        if(huiControl!=null) {
            control = huiControl.getControl();
        }
        return control;
    }

    

}
