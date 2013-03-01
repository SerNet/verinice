/*******************************************************************************
 * Copyright (c) 2013 Daniel Murygin.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.bpm.gsm;

import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.ui.velocity.VelocityEngineUtils;

import sernet.gs.service.VeriniceCharset;
import sernet.verinice.interfaces.bpm.IGsmIsmExecuteProzess;
import sernet.verinice.interfaces.bpm.ITaskDescriptionHandler;

/**
 * GsmServiceTaskDescriptionHandler is part of the GSM vulnerability tracking process.
 * Process definition is: gsm-ism-execute.jpdl.xml
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("restriction")
public class GsmServiceTaskDescriptionHandler implements ITaskDescriptionHandler {

    private static final Logger LOG = Logger.getLogger(GsmServiceTaskDescriptionHandler.class);
    
    public static final String TEMPLATE_EXTENSION = ".vm"; //$NON-NLS-1$
    
    // template path without lang code "_en" and file extension ".vm"
    private String templateBasePath = "sernet/verinice/bpm/gsm/IsmExecuteDescription"; //$NON-NLS-1$
    
    private VelocityEngine velocityEngine;
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.ITaskDescriptionHandler#loadTitle(java.lang.String, java.util.Map)
     */
    @Override
    public String loadTitle(String taskId, Map<String, Object> processVars) {
        return sernet.verinice.model.bpm.Messages.getString(taskId, processVars.get(IGsmIsmExecuteProzess.VAR_CONTROL_GROUP_TITLE));
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.ITaskDescriptionHandler#loadDescription(java.lang.String, java.util.Map)
     */
    @Override
    public String loadDescription(String taskId, Map<String, Object> processVars) {      
        return loadDescriptionByVelocity(convertProcessVarsToTemplateVars(processVars));
    }

    private String loadDescriptionByVelocity(Map<String, Object> templateVars) {
        return VelocityEngineUtils.mergeTemplateIntoString(
                getVelocityEngine(), 
                getTemplatePath(), 
                VeriniceCharset.CHARSET_UTF_8.name(), 
                templateVars);
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> convertProcessVarsToTemplateVars(Map<String, Object> processVars) { 
        Map<String, Object> templateVars = new Hashtable<String, Object>();
         
        templateVars.put(IGsmIsmExecuteProzess.VAR_ASSET_DESCRIPTION_LIST, processVars.get(IGsmIsmExecuteProzess.VAR_ASSET_DESCRIPTION_LIST));
        templateVars.put(IGsmIsmExecuteProzess.VAR_CONTROL_DESCRIPTION, processVars.get(IGsmIsmExecuteProzess.VAR_CONTROL_DESCRIPTION));           
        templateVars.put(IGsmIsmExecuteProzess.VAR_RISK_VALUE, processVars.get(IGsmIsmExecuteProzess.VAR_RISK_VALUE));                             
        
        templateVars.put(IGsmIsmExecuteProzess.VAR_ASSIGNEE_DISPLAY_NAME, processVars.get(IGsmIsmExecuteProzess.VAR_ASSIGNEE_DISPLAY_NAME));   
        templateVars.put(IGsmIsmExecuteProzess.VAR_CONTROL_GROUP_TITLE, processVars.get(IGsmIsmExecuteProzess.VAR_CONTROL_GROUP_TITLE));
        return templateVars;
    }

    
    
    /**
     * Returns the bundle/jar relative path to the velocity email template.
     * First a localized template is search by the default locale of the java vm.
     * If localized template is not found default/english template is returned.
     * 
     * Localized template path: <TEMPLATE_BASE_PATH>_<LANG_CODE>.vm
     * Default template path: <TEMPLATE_BASE_PATH>.vm
     * 
     * @return bundle/jar relative path to the velocity email template
     */
    protected String getTemplatePath() {      
        String langCode = Locale.getDefault().getLanguage();
        String path = getTemplateBasePath() + "_" + langCode + TEMPLATE_EXTENSION; //$NON-NLS-1$
        if(this.getClass().getClassLoader().getResource(path)==null) {
            path = getTemplateBasePath() + TEMPLATE_EXTENSION;
        }
        return path;
    }

    public String getTemplateBasePath() {
        return templateBasePath;
    }

    public void setTemplateBasePath(String templateBasePath) {
        this.templateBasePath = templateBasePath;
    }

    public VelocityEngine getVelocityEngine() {
        return velocityEngine;
    }

    public void setVelocityEngine(VelocityEngine velocityEngine) {
        this.velocityEngine = velocityEngine;
    }

}
