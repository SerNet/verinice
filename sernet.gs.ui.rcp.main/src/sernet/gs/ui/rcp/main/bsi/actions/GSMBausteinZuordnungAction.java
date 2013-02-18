/*******************************************************************************
 * Copyright (c) 2012 Julia Haas.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Julia Haas <jh[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.actions;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import sernet.gs.model.Baustein;
import sernet.gs.service.RetrieveInfo;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.actions.RightsEnabledAction;
import sernet.gs.ui.rcp.main.bsi.views.BSIKatalogInvisibleRoot;
import sernet.gs.ui.rcp.main.bsi.views.BsiModelView;
import sernet.gs.ui.rcp.main.common.model.BuildInput;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.IInternalServerStartListener;
import sernet.verinice.interfaces.InternalServerEvent;
import sernet.verinice.iso27k.service.Retriever;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.Server;

/**
 * assing automaticly BSI modules on the basis of GSM-Scan
 * 
 * @author Julia Haas <jh[at]sernet[dot]de>
 * 
 */
public class GSMBausteinZuordnungAction extends RightsEnabledAction implements ISelectionListener {

    private static final Logger LOG = Logger.getLogger(GSMBausteinZuordnungAction.class);

    public static final String ID = "sernet.gs.ui.rcp.main.gsmbausteinzuordnungaction"; //$NON-NLS-1$

    private final IWorkbenchWindow window;

    private boolean serverIsRunning = true;

    private static final String GSMTYP_MAPPING_FILE = "gsm_baustein.properties"; //$NON-NLS-1$
    private static final String SUBTYP_MAPPING_FILE = "subtyp-baustein.properties"; //$NON-NLS-1$
    private Properties gsmtypproperties;
    private Properties subtypproperties;

    public GSMBausteinZuordnungAction(IWorkbenchWindow window) {
        this.window = window;
        setText(Messages.GSMBausteinZuordnungAction_1);
        setId(ID);
        setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.AUTOBAUSTEIN));
        window.getSelectionService().addSelectionListener(this);
        setRightID(ActionRightIDs.BAUSTEINZUORDNUNG);
        if (Activator.getDefault().isStandalone() && !Activator.getDefault().getInternalServer().isRunning()) {
            serverIsRunning = false;
            IInternalServerStartListener listener = new IInternalServerStartListener() {
                @Override
                public void statusChanged(InternalServerEvent e) {
                    if (e.isStarted()) {
                        serverIsRunning = true;
                        setEnabled(checkRights());
                    }
                }
            };
            Activator.getDefault().getInternalServer().addInternalServerStatusListener(listener);
        } else {
            setEnabled(checkRights());
        }
    }

    @Override
    public void run() {

        loadtemplates();

        final IStructuredSelection selection = (IStructuredSelection) window.getSelectionService().getSelection(BsiModelView.ID);
        if (selection == null) {
            return;
        }

        try {
            PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    Activator.inheritVeriniceContextState();
                    for (Iterator iter = selection.iterator(); iter.hasNext();) {

                        Object o = iter.next();
                        if (o instanceof Server) {
                            Server server = (Server)o;
                            monitor.beginTask(Messages.GSMBausteinZuordnungAction_4, IProgressMonitor.UNKNOWN);
                            loadModulForServer(server);
                            monitor.done();
                        }
                    }
                }
            });
        }  catch (Exception e) {
                ExceptionUtil.log(e, Messages.GSMBausteinZuordnungAction_3);  
        } 
    }

    /**
     * @param serverelement
     */
    private void loadModulForServer(Server serverelement) {
        try{
            String[] bausteine = getSplitBausteine(serverelement);
            if (bausteine == null || bausteine.length == 0 ) {
                showInfoMessage();
                return;
            }
            RetrieveInfo ri = RetrieveInfo.getChildrenInstance().setChildrenProperties(true).setParent(true);
            serverelement = (Server) Retriever.retrieveElement(serverelement,ri);
            for (String bst : bausteine) {

                Baustein baustein = BSIKatalogInvisibleRoot.getInstance().getBausteinByKapitel(bst);

                if (baustein == null) {
                    LOG.error("Kein Baustein gefunden fuer Nr.: " + bst); //$NON-NLS-1$
                } else {

                    // assign baustein to every selected target object:
                    if (!serverelement.containsBausteinUmsetzung(baustein.getId())) {
                        try {
                            CnAElementFactory.getInstance().saveNew(serverelement, BausteinUmsetzung.TYPE_ID, new BuildInput<Baustein>(baustein));
                        } catch (Exception e) {
                            LOG.error("Error by saving.", e);
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }catch (Exception e) {
            LOG.error("Error while assigning modules",e);
            ExceptionUtil.log(e, Messages.GSMBausteinZuordnungAction_6);
        }
    }
    
    private void showInfoMessage(){
    Display.getDefault().asyncExec(new Runnable() {
        
        @Override
        public void run() {
            // code der in der GUI laufen soll 
            MessageDialog.openInformation(window.getShell(), "Info", Messages.GSMBausteinZuordnungAction_5);
        }
    });
    }
   
    private void loadtemplates() {
        gsmtypproperties = new Properties();
        subtypproperties = new Properties();
        InputStream stream = getClass().getClassLoader().getResourceAsStream(GSMTYP_MAPPING_FILE);
        InputStream stream2 = getClass().getClassLoader().getResourceAsStream(SUBTYP_MAPPING_FILE);
        try {
            gsmtypproperties.load(stream);
            subtypproperties.load(stream2);
        } catch (IOException e) {
            LOG.error(e);
        }
    }
    
    private List<String> tagList(Server server) {
        ArrayList<String> gsmtaglist = new ArrayList<String>();
        String property = "";
        Collection<? extends String> tags = server.getTags();
        String tagstoString = tags.toString();
        String tag = tagstoString.substring(1, tagstoString.length() - 1);
        String[] splittags = tag.split(",\\s*");
        try {
            for (int split = 0; split < splittags.length; split++) {
                String name = splittags[split];
                Set<Entry<Object, Object>> entrySet = gsmtypproperties.entrySet();
                for (Entry<Object, Object> entry : entrySet) {
                    String gsmkey = entry.getKey().toString();
                    String gsmproperty = entry.getValue().toString();
                    if (name.equals(gsmkey)) {
                        property = gsmproperty;
                    }
                }
                gsmtaglist.add(property);
            }
        } catch (Exception e) {
            LOG.error("Error while assigning modules", e);
            ExceptionUtil.log(e, Messages.GSMBausteinZuordnungAction_3);
        }
        return gsmtaglist;
    }

    private List<String> readBausteine(Server server) {
        List<String> gsmlist = tagList(server);
        ArrayList<String> bausteinlist = new ArrayList<String>();
        Set<Entry<Object, Object>> subtypentrySet = subtypproperties.entrySet();

        for (Entry<Object, Object> subtypentry : subtypentrySet) {
            String subtyp = subtypentry.getKey().toString();
            String subtypproperty = subtypentry.getValue().toString();
            String[] subproperty = subtypproperty.split(",\\s*");
            for (int split = 0; split < subproperty.length; split++) {
                String property = subproperty[split];
               
               for (String namesEntry : gsmlist) {
                    if (namesEntry.equals(subtyp)) {
                        String value = property;
                        bausteinlist.add(value);
                    }
                }
            }

        }
        return bausteinlist;
    }

    private String[] getSplitBausteine(Server server) {
        List<String> bausteinlist = readBausteine(server);
        Object[] objList = bausteinlist.toArray();
        return Arrays.copyOf(objList, objList.length, String[].class);
    }

    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection input) {
        if (serverIsRunning) {
            setEnabled(checkRights());      
            if (input instanceof IStructuredSelection) {
                IStructuredSelection selection = (IStructuredSelection) input;
    
                for (Iterator iter = selection.iterator(); iter.hasNext();) {
                    Object o = iter.next();
                    if (!(o instanceof Server)) {
                        setEnabled(false);
                        return;
                    }
                }
                if (checkRights()) {
                    setEnabled(true);
                }
                return;
            }
        }
        setEnabled(false);

    }
}