package sernet.verinice.iso27k.rcp.action;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import sernet.gs.model.Baustein;
import sernet.gs.model.Gefaehrdung;
import sernet.gs.model.Massnahme;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.IGSModelElementTransfer;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.hui.common.VeriniceContext;
import sernet.springclient.RightsServiceClient;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.RightEnabledUserInteraction;
import sernet.verinice.interfaces.iso27k.IItem;
import sernet.verinice.iso27k.rcp.GS2BSITransformOperation;
import sernet.verinice.iso27k.service.ItemTransformException;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.Group;
import sernet.verinice.model.iso27k.IncidentScenario;

public class BSIModelDropPerformer extends ViewerDropAdapter implements DropPerformer, RightEnabledUserInteraction {
	
	private boolean isActive = false;
	
	private static final Logger LOG = Logger.getLogger(BSIModelDropPerformer.class);
	
	private Object target = null;
	
	public BSIModelDropPerformer(TreeViewer viewer){
		super(viewer);
	}

    @Override
    public boolean performDrop(Object data) {
        if(!(validateDropObjects(target, data))){
            return false;
        }
        
        boolean success = isActive();
        if (isActive()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("performDrop..."); //$NON-NLS-1$
            }
            try {
                // because of validateDrop only Groups can be a target
                Group group = (Group) target;
                if(CnAElementHome.getInstance().isNewChildAllowed(group)) {
                    GS2BSITransformOperation operation = new GS2BSITransformOperation(group, data);
                    IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
                    progressService.run(true, true, operation);
                    IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
                    boolean dontShow = preferenceStore.getBoolean(PreferenceConstants.INFO_CONTROLS_ADDED);
                    String objectType = "";
                    if(operation.isScenario()){
                        objectType = Messages.getString("GS2BSITransformer.1");
                    } else {
                        objectType = Messages.getString("ControlDropPerformer.2");
                    }
                    if (!dontShow) {
                        MessageDialogWithToggle dialog = MessageDialogWithToggle.openInformation(PlatformUI.getWorkbench().getDisplay().getActiveShell(), Messages.getString("ControlDropPerformer.1"), //$NON-NLS-1$
                                NLS.bind(objectType, operation.getNumberProcessed(), ((Group) target).getTitle()), //$NON-NLS-1$
                                Messages.getString("ControlDropPerformer.3"), //$NON-NLS-1$
                                dontShow, preferenceStore, PreferenceConstants.INFO_CONTROLS_ADDED);
                        preferenceStore.setValue(PreferenceConstants.INFO_CONTROLS_ADDED, dialog.getToggleState());
                    }
                } else if (LOG.isDebugEnabled()) {
                    LOG.debug("User is not allowed to add elements to this group"); //$NON-NLS-1$
                }
             } catch (ItemTransformException e) {
                LOG.error("Error while transforming items to controls", e); //$NON-NLS-1$
                showException(e);
             } catch (InvocationTargetException e) {             
                LOG.error("Error while transforming items to controls", e); //$NON-NLS-1$
                Throwable t = e.getTargetException();
                if(t instanceof ItemTransformException) {
                    showException((ItemTransformException) t);
                } else {
                    ExceptionUtil.log(e, sernet.verinice.iso27k.rcp.action.Messages.getString("ControlDropPerformer.5")); //$NON-NLS-1$
                }
             } catch (Exception e) {             
                LOG.error("Error while transforming items to controls", e); //$NON-NLS-1$
                ExceptionUtil.log(e, sernet.verinice.iso27k.rcp.action.Messages.getString("ControlDropPerformer.5")); //$NON-NLS-1$
             }
        }
        return success;
    }
	
    @Override
    public void drop(DropTargetEvent event){
        if(target != null){
            target = (CnATreeElement) determineTarget(event);
        }
        super.drop(event);
    }
    
	@Override
	public boolean validateDrop(Object target, int operation,
			TransferData transferType) {
	    boolean valid = false;
		if(target != null){
		    this.target = target;
		}
		if (target instanceof Group && isSupportedData(transferType)) {
			List<String> childTypeList = Arrays.asList(((Group) target).getChildTypes());
			valid = childTypeList.contains(Control.TYPE_ID) 
			|| childTypeList.contains(IncidentScenario.TYPE_ID); 
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("validateDrop, target: " + target + " result: " + valid); //$NON-NLS-1$ //$NON-NLS-2$
		}
		isActive = valid;
		return isActive;
	}
	
	/**
	 * @param target
	 * @return
	 */
	public boolean validateDropObjects(Object target, Object data) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("validateDrop, target: " + target); //$NON-NLS-1$
		}
		boolean valid = false;
		
		if(!checkRights()){
		    return false;
		}

		List items = new ArrayList<Object>(0);
		
		if(data instanceof Object[]){
		    Object[] o = (Object[])data;
		    for(Object object : o){
		        items.add(object);
		    }
		} else if (data instanceof Object){
		    items.add(data);
		}
		
		if (items == null || items.isEmpty()) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("No items in drag list"); //$NON-NLS-1$
			}
			isActive = false;
			return isActive;
		}

		if (target instanceof Group) {
			List<String> childTypeList = Arrays.asList(((Group) target).getChildTypes());
			if(childTypeList.contains(Control.TYPE_ID)) {
				valid = isCorrectItemsForGroup(items, Control.TYPE_ID);			
			}
			if(!valid && childTypeList.contains(IncidentScenario.TYPE_ID)) {
				valid = isCorrectItemsForGroup(items, IncidentScenario.TYPE_ID);
			}
		}
		isActive = valid;
		return isActive;
	}
	
	
	/**
	 * @param items
	 * @param control
	 * @return
	 */
	private boolean isCorrectItemsForGroup(Collection<IItem> items, String type) {
		boolean valid = false;
		try{
			for (Object item : items) {
				if((item instanceof Massnahme || item instanceof Baustein) && type.equals(Control.TYPE_ID)){
					valid = true;
				}
				if((item instanceof Gefaehrdung || item instanceof Baustein) && type.equals(IncidentScenario.TYPE_ID)){
					valid = true;
				}
			}
		}
		catch (ClassCastException e){
			LOG.error("Wrong type of item dropped", e);
			valid = false;
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("isCorrectItemsForGroup result: " + valid); //$NON-NLS-1$
		}
		return valid;
	}

	public boolean isActive() {
		return isActive;
	}
	
    private void showException(ItemTransformException e) {
        final String message = Messages.getString("ControlDropPerformer.0") + e.getMessage(); //$NON-NLS-1$
        MessageDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), Messages.getString("ControlDropPerformer.4"), message); //$NON-NLS-1$
    }
    
    private boolean isSupportedData(TransferData transferType){
        return IGSModelElementTransfer.getInstance().isSupportedType(transferType);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.iso27k.rcp.action.DropPerformer#performDrop(java.lang.Object, java.lang.Object, org.eclipse.jface.viewers.Viewer)
     */
    public boolean performDrop(Object data, Object target, Viewer viewer) {
        return performDrop(data);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#checkRights()
     */
    @Override
    public boolean checkRights() {
        RightsServiceClient service = (RightsServiceClient)VeriniceContext.get(VeriniceContext.RIGHTS_SERVICE);
        return service.isEnabled(getRightID());
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#getRightID()
     */
    @Override
    public String getRightID() {
        return ActionRightIDs.TREEDND;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#setRightID(java.lang.String)
     */
    @Override
    public void setRightID(String rightID) {
    }
 

}
