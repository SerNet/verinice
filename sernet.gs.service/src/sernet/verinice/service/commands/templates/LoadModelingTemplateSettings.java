/*******************************************************************************  
 * Copyright (c) 2017 Viktor Schmidt.  
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
 *     Viktor Schmidt <vschmidt[at]ckc[dot]de> - initial API and implementation  
 ******************************************************************************/
package sernet.verinice.service.commands.templates;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.service.commands.PropertyLoader;

/**
 * Load the modeling template settings from file veriniceserver-plain.properties
 * 
 * @see PropertyLoader
 *
 * @author Viktor Schmidt <vschmidt[at]ckc[dot]de>
 */
public class LoadModelingTemplateSettings extends GenericCommand {

    private static final long serialVersionUID = 3380045738570923175L;

    private transient Logger log = Logger.getLogger(LoadModelingTemplateSettings.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(LoadModelingTemplateSettings.class);
        }
        return log;
    }
    
    private boolean modelingTemplateActive = false;
    private String modelingTemplateMaster = "admin";
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        modelingTemplateActive = PropertyLoader.isModelingTemplateActive();
        modelingTemplateMaster = PropertyLoader.getModelingTemplateMaster();
    }

    public boolean isModelingTemplateActive() {
        return modelingTemplateActive;
    }

    public String getModelingTemplateMaster() {
        return modelingTemplateMaster;
    }

}
