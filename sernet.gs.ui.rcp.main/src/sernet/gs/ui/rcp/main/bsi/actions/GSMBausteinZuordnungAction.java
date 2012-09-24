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
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

import sernet.gs.model.Baustein;

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

import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.BausteinVorschlag;
import sernet.verinice.model.bsi.IBSIStrukturElement;
import sernet.verinice.model.bsi.Server;

import sernet.verinice.model.common.CnATreeElement;

/**
 * assing automaticly BSI modules of the basis of GSM-Scan
 * 
 * @author Julia Haas <jh[at]sernet[dot]de>
 * 
 */
public class GSMBausteinZuordnungAction extends RightsEnabledAction implements ISelectionListener {

    private static final Logger LOG = Logger.getLogger(GSMBausteinZuordnungAction.class);

    public static final String ID = "sernet.gs.ui.rcp.main.gsmbausteinzuordnungaction"; //$NON-NLS-1$

    private final IWorkbenchWindow window;

    private String rightID = null;

    private boolean serverIsRunning = true;
    

    private static final String GSMTYP_MAPPING_FILE = "gsm_baustein.properties"; //$NON-NLS-1$
    private static final String SUBTYP_MAPPING_FILE = "subtyp-baustein.properties"; //$NON-NLS-1$
    private Properties gsmtypproperties;
    private Properties subtypproperties;
    
    
    List<BausteinVorschlag> mapping = new ArrayList<BausteinVorschlag>(70);

    public GSMBausteinZuordnungAction(IWorkbenchWindow window) {
        this.window = window;
        setText(Messages.GSMBausteinZuordnungAction_1);
        setId(ID);
        setActionDefinitionId(ID);
        setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.AUTOBAUSTEIN));
        window.getSelectionService().addSelectionListener(this);
        setRightID(ActionRightIDs.BAUSTEINZUORDNUNG);
        if(Activator.getDefault().isStandalone()  && !Activator.getDefault().getInternalServer().isRunning()){
            serverIsRunning = false;
            IInternalServerStartListener listener = new IInternalServerStartListener(){
                @Override
                public void statusChanged(InternalServerEvent e) {
                    if(e.isStarted()){
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

    

    public void run() {
       
        
        loadtemplates();
        
        IStructuredSelection selection = (IStructuredSelection) window.getSelectionService().getSelection(BsiModelView.ID);
        if (selection == null) {
            return;
        }
        final List<IBSIStrukturElement> selectedElements = new ArrayList<IBSIStrukturElement>();
        for (Iterator iter = selection.iterator(); iter.hasNext();) {

            Object o = iter.next();
            if (o instanceof IBSIStrukturElement) {
                selectedElements.add((IBSIStrukturElement) o);
            }
        }

        try {
          
            String[] bausteine = getSplitBausteine();
            if(bausteine.length==0){
                Exception ee = new Exception();
                ExceptionUtil.log(ee, Messages.GSMBausteinZuordnungAction_2);
            }
            for (String bst : bausteine) {
               
                Baustein baustein = BSIKatalogInvisibleRoot.getInstance().getBausteinByKapitel(bst);
                
                if (baustein == null) {
                    LOG.debug("Kein Baustein gefunden fuer Nr.: " + bst); //$NON-NLS-1$
                } else {
                    // assign baustein to every selected target object:
                    for (IBSIStrukturElement target : selectedElements) {
                        if (target instanceof CnATreeElement) {
                            CnATreeElement targetElement = (CnATreeElement) target;
                           if (!targetElement.containsBausteinUmsetzung(baustein.getId()))
                            CnAElementFactory.getInstance().saveNew(targetElement, BausteinUmsetzung.TYPE_ID, new BuildInput<Baustein>(baustein));
                        }
                        }
                    }
                }

            

        } catch (Exception e) {
            ExceptionUtil.log(e, Messages.BausteinZuordnungAction_4);
        }
       

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
    
    private ArrayList<String> tagList() {
        ArrayList<String> gsmtaglist = new ArrayList<String>();

        Set<Entry<Object, Object>> entrySet = gsmtypproperties.entrySet();
        for (Entry<Object, Object> entry : entrySet) {
            String gsmkey = entry.getKey().toString();
            String tags = getTags();
            String[] splittags = tags.split(",\\s*");
            for (int split = 0; split < splittags.length; split++) {
                String name = splittags[split];

                String gsmproperty = entry.getValue().toString();
                if (name.equals(gsmkey)) {
                    String property = gsmproperty;

                    gsmtaglist.add(name);
                    gsmtaglist.add(property);
                }

            }
        }
        return gsmtaglist;
    }

    private ArrayList<String> readBausteine() {
        String value = "";
        String name = "";
        ArrayList<String> bausteinlist = new ArrayList<String>();
        Set<Entry<Object, Object>> subtypentrySet = subtypproperties.entrySet();
        for (Entry<Object, Object> subtypentry : subtypentrySet) {
            String subtyp = subtypentry.getKey().toString();
            String subtypproperty = subtypentry.getValue().toString();
            String[] subproperty = subtypproperty.split(",\\s*");
            for (int split = 0; split < subproperty.length; split++) {
                String property = subproperty[split];
                List gsmlist = tagList();

                Object[] objList = gsmlist.toArray();
                String[] strList = Arrays.copyOf(objList, objList.length, String[].class);
                for (int i = 0; i < strList.length; i++) {
                    name = strList[i];
                    if (name.equals(subtyp)) {
                        value = property;
                       // bausteinlist.add(name);
                        bausteinlist.add(value);
                    }

                }
            }
        }
        return bausteinlist;
    }

    private String[] bausteine() {
        ArrayList<String> bausteinlist = readBausteine();
        Object[] objList = bausteinlist.toArray();
        String[] strList = Arrays.copyOf(objList, objList.length, String[].class);
        return strList;
    }

    private String getTags() {
        String tag = null;
        IStructuredSelection selection = (IStructuredSelection) window.getSelectionService().getSelection(BsiModelView.ID);
        for (Iterator iter = selection.iterator(); iter.hasNext();) {
            Object o = iter.next();
            if (o instanceof Server) {
                Collection<? extends String> elementtags = ((IBSIStrukturElement) o).getTags();
                String tags = elementtags.toString();
                tag = tags.substring(1, tags.length() - 1);

            }
        }
        return tag;
    }

    public void selectionChanged(IWorkbenchPart part, ISelection input) {
        if (serverIsRunning) {
            setEnabled(checkRights());
        }
        if (input instanceof IStructuredSelection) {
            IStructuredSelection selection = (IStructuredSelection) input;

            if (selection.size() < 1) {
                setEnabled(false);
                return;
            }

            String kapitel = null;
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
        // no structured selection:
        setEnabled(false);

    }

    public String[] getSplitBausteine() {
        String[] baustein = bausteine();
        return baustein;
    }
   
   
}