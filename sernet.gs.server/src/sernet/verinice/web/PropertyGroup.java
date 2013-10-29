/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin.
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
package sernet.verinice.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class PropertyGroup implements Serializable {
    
    private String id;
    private String name;
    private List<HuiProperty<String, String>> propertyList = new ArrayList<HuiProperty<String, String>>();
    
    public PropertyGroup(String id, String name) {
        super();
        this.id = id;
        this.name = name;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void addPropertyType(HuiProperty<String, String> prop) {
        propertyList.add(prop);
    }
    public List<HuiProperty<String, String>> getLabelPropertyList() {
        List<HuiProperty<String, String>> labelList = Collections.emptyList();
        List<HuiProperty<String, String>> list = getPropertyList();
        if(list!=null) {
            labelList = new LinkedList<HuiProperty<String,String>>();
            for (HuiProperty<String, String> property : getPropertyList()) {
                if(property.isShowLabel()) {
                    labelList.add(property);
                }
            }  
        }
        return labelList;
    }
    public List<HuiProperty<String, String>> getNoLabelPropertyList() {
        List<HuiProperty<String, String>> noLabelList = Collections.emptyList();
        List<HuiProperty<String, String>> list = getPropertyList();
        if(list!=null) {
            noLabelList = new LinkedList<HuiProperty<String,String>>();
            for (HuiProperty<String, String> property : getPropertyList()) {
                if(!property.isShowLabel()) {
                    noLabelList.add(property);
                }
            }  
        }
        return noLabelList;
    }
    public List<HuiProperty<String, String>> getPropertyList() {
        return propertyList;
    }
    public void setPropertyList(List<HuiProperty<String, String>> propertyTypes) {
        this.propertyList = propertyTypes;
    }

    
}
