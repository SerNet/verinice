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
package sernet.verinice.service.commands;

/**
 * This class provides static direct access to properties.
 * See configuration in veriniceserver-plain.xml.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class PropertyLoader {

    public static final String FILESIZE_MAX = "veriniceserver.filesize.max";
    public static final String MODELING_TEMPLATE_ACTIVE = "modeling.template.active";
    public static final String MODELING_TEMPLATE_MASTER = "modeling.template.master";
    
    private static String fileSizeMax;
    private static boolean modelingTemplateActive;
    private static String modelingTemplateMaster;

    /**
     * Returns the value of veriniceserver.filesize.max property in file veriniceserver-plain.properties
     * 
     * @return value of property veriniceserver.filesize.max
     */
    public static String getFileSizeMax() {
        return PropertyLoader.fileSizeMax;
    }

    public void setFileSizeMax(String fileSizeMax) {
        PropertyLoader.fileSizeMax = fileSizeMax;
    }

    /**
     * Returns the value of modeling.template.active property in file
     * veriniceserver-plain.properties
     * 
     * @return value of property modeling.template.active
     */
    public static boolean isModelingTemplateActive() {
        return modelingTemplateActive;
    }

    public void setModelingTemplateActive(boolean modelingTemplateActive) {
        PropertyLoader.modelingTemplateActive = modelingTemplateActive;
    }

    /**
     * Returns the value of modeling.template.master property in file
     * veriniceserver-plain.properties
     * 
     * @return value of property modeling.template.master
     */
    public static String getModelingTemplateMaster() {
        return modelingTemplateMaster;
    }

    public void setModelingTemplateMaster(String modelingTemplateMaster) {
        PropertyLoader.modelingTemplateMaster = modelingTemplateMaster;
    }
}
