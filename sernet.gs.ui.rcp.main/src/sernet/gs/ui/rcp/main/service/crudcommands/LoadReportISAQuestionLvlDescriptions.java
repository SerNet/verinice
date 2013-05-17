/*******************************************************************************
 * Copyright (c) 2012 Sebastian Hagedorn <sh@sernet.de>.
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
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.crudcommands;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.samt.SamtTopic;

/**
 *
 */
public class LoadReportISAQuestionLvlDescriptions extends GenericCommand implements ICachedCommand{
    
    private static final Logger LOG = Logger.getLogger(LoadReportISAQuestionLvlDescriptions.class);

    private Integer requestedLvl = 0;
    private Integer rootElmt;
    
    private List<List<String>> results;
    
    private boolean resultInjectedFromCache = false;
    
    public static final String[] COLUMNS = new String[] { 
                                                "lvl_top"
    };

    public LoadReportISAQuestionLvlDescriptions(Integer root, Integer lvl){
        this.requestedLvl = lvl;
        this.rootElmt = root;
    }
    

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        if(!resultInjectedFromCache){
            int count = 0;
            results = new ArrayList<List<String>>(0);
            StringBuilder measureText = new StringBuilder();
            Level tLevel = Logger.getLogger("org.hibernate.engine.StatefulPersistenceContext").getLevel();
            Logger.getLogger("org.hibernate.engine.StatefulPersistenceContext").setLevel(Level.OFF);
            try{
                //load samtTopic from rootElmt (needs to be a samtTopic)
                SamtTopic st = (SamtTopic)getDaoFactory().getDAO(SamtTopic.TYPE_ID).findById(rootElmt);
                // compute text of deduced measures

                // load generic measures
                LoadReportLinkedElements measureLoader = new LoadReportLinkedElements(Control.TYPE_ID, st.getDbId(), false, true);
                measureLoader = getCommandService().executeCommand(measureLoader);
                // list of measures to inspect
                ArrayList<Control> measuresOfInterest = new ArrayList<Control>(0);
                // iterate generic measures
                for(CnATreeElement cmg : measureLoader.getElements()){
                    LoadCnAElementById controlReloader = new LoadCnAElementById(Control.TYPE_ID, cmg.getDbId());
                    controlReloader = getCommandService().executeCommand(controlReloader);
                    cmg = controlReloader.getFound();
                    if(cmg instanceof Control){
                        Control mg = (Control)cmg;
                        // check if specific measures exist
                        LoadReportLinkedElements sMeasureLoader = new LoadReportLinkedElements(Control.TYPE_ID, mg.getDbId(), false, true);
                        sMeasureLoader = getCommandService().executeCommand(sMeasureLoader);
                        if(sMeasureLoader.getElements().size() > 0){
                            //specific measures found, ignore generic measures
                            for(CnATreeElement cms : sMeasureLoader.getElements()){
                                if(cms instanceof Control){
                                    measuresOfInterest.add((Control)cms);
                                }
                            }
                        } else {
                            // no specific measure found, add generic measure
                            measuresOfInterest.add(mg);
                        }
                    }
                }
                for(Control measure : measuresOfInterest){

                    if(getLevel(loadParent(measure.getParent().getDbId()).getTitle()) == requestedLvl){
                        String description = measure.getTitle();
                        Pattern subChapt = Pattern.compile("^\\d+.\\d+.*");
                        int idIdx = 0;
                        if(description.contains(" ")){
                            String[] tokens = description.split(" ");
                            for(int j = 0; j < tokens.length; j++){
                                Matcher matcher = subChapt.matcher(tokens[j]);
                                if(matcher.matches()){
                                    idIdx = j;
                                    break;
                                }
                            }
                            if(idIdx > 0){
                                StringBuilder sb = new StringBuilder();
                                for(int j = idIdx + 1; j < tokens.length; j++){
                                    sb.append(tokens[j]);
                                    sb.append(" ");
                                }
                                description = sb.toString().trim();
                            }
                        }
                        // first results needs lvlPrefix and be bold
                        if(count == 0){
                            description = "<B> Level " + requestedLvl + ": " + description + "</B>";
                            //every result > 1 needs to be a list item
                        } else {
                            description = "<LI>" + description + "</LI>";
                        }
                        // if more than one result, add <UL>
                        if(count == 1){
                            description = "<UL>" + description;
                        }
                        measureText.append(description);
                        count++;
                    }
                }
            } catch (CommandException e){
                LOG.error("Error while executing command", e);
            }
            // if more than one result, <ul> needs to be closed
            if(count > 1){
                measureText.append("</UL>");
            }
            ArrayList<String> nList = new ArrayList<String>(0);
            nList.add(measureText.toString());
            results.add(nList);
            Logger.getLogger("org.hibernate.engine.StatefulPersistenceContext").setLevel(tLevel);
        }
    }
    
    private ControlGroup loadParent(Integer parentid) throws CommandException{
        LoadCnAElementById controlReloader = new LoadCnAElementById(ControlGroup.TYPE_ID, parentid);
        controlReloader = getCommandService().executeCommand(controlReloader);
        return (ControlGroup)controlReloader.getFound();
    }
    
    /**
     * Extracts 3 out of a String like "Chapter 5.1, Level 3"
     * invoke only on parents of generic/specific measures linked to isaTopic
     * @param controlGroupTitle
     * @return
     */
    private int getLevel(String controlGroupTitle){
        int lvl = 0;
        final int maxSupportedLvl = 3;
        if(controlGroupTitle.contains(",")){
            String lvlString = controlGroupTitle.substring(controlGroupTitle.lastIndexOf(',') + 1).trim();
            if(lvlString.contains(" ")){
                String lvlNrString = lvlString.substring(lvlString.lastIndexOf(' ')).trim();
                try{
                    lvl = Integer.parseInt(lvlNrString);
                    if(lvl > maxSupportedLvl){ // lvl > 3 not supported
                        lvl  = 0;
                    }
                } catch (NumberFormatException e){
                    LOG.error("Invalid Controlgrouptitle, contains no lvl information", e);
                }
            }
        }
        return lvl;
    }
    
    public List<List<String>> getResults(){
        return results;
    }


    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheID()
     */
    @Override
    public String getCacheID() {
        StringBuilder cacheID = new StringBuilder(); 
        cacheID.append(String.valueOf(this.getClass().getSimpleName().hashCode()));
        cacheID.append(String.valueOf(rootElmt.hashCode()));
        cacheID.append(String.valueOf(requestedLvl.hashCode()));
        return cacheID.toString();
    }


    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#injectCacheResult(java.lang.Object)
     */
    @Override
    public void injectCacheResult(Object result) {
        results = (ArrayList<List<String>>) result;
        resultInjectedFromCache = true;
        if(LOG.isDebugEnabled()){
            LOG.debug("Result in " + this.getClass().getCanonicalName() + " injected from cache");
        }
    }


    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheableResult()
     */
    @Override
    public Object getCacheableResult() {
        return results;
    }

}
