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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.faces.model.SelectItem;

import org.apache.log4j.Logger;
import org.hibernate.hql.ast.tree.IsNullLogicOperatorNode;

import sernet.hui.common.connect.PropertyOption;
import sernet.hui.common.connect.PropertyType;
import sernet.hui.common.multiselectionlist.IMLPropertyOption;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class HuiProperty<K,V> implements Serializable{
    
    private static final Logger LOG = Logger.getLogger(HuiProperty.class);
    
    public static SimpleDateFormat dateFormat = new SimpleDateFormat();
    
    private K key;
    
    private V value;
    
    private PropertyType type;
    
    private boolean showLabel = true;
    
    public HuiProperty(PropertyType type, K key, V value) {
        super();
        this.type = type;
        this.key = key;
        this.value = value;
    }
    
    public String getName() {
        return type.getName();
    }
    
    public String getInputName() {
        return type.getInputName();
    }
    
    public boolean getIsLine() {
        return type.isLine();
    }
    
    public boolean getIsText() {
        return type.isText();
    }
    
    public boolean isEditable() {
        return type.isEditable();
    }
    
    public boolean getIsEditable() {
        return isEditable();
    }
    
    public boolean getIsSingleSelect() {
        return type.isSingleSelect();
    }
    
    public boolean getIsNumericSelect() {
        return type.isNumericSelect();
    }
    
    public boolean getIsDate() {
        return type.isDate();
    }
    
    public Date getDate() {
        if(!getIsDate()) {
            throw new RuntimeException("Not a date property, can not return a date.");
        }
        return (value!=null) ? new Date(Long.valueOf((String)value)) : null;
    }
    
    public void setDate(Date date) {
        if(!getIsDate()) {
            throw new RuntimeException("Not a date property, can not set date.");
        }
        if(date!=null) {
            value = (V) Long.valueOf(date.getTime()).toString();
        } else {
            value = null;
        }
    }
    
    public List<String> getOptionList() {
        if(!getIsSingleSelect() && !getIsNumericSelect()) {
            throw new RuntimeException("Not a single select property, can not return option list.");
        }
        List<String> itemList = Collections.emptyList();
        if(type.getOptions()!=null) {
            itemList = new ArrayList<String>(type.getOptions().size());
            for (IMLPropertyOption option : type.getOptions()) {
                itemList.add(option.getName());
            }   
        }
        return itemList;
    }
    
    public String getSelectedOption() {    
        IMLPropertyOption option = null;
        if(!getIsSingleSelect() && !getIsNumericSelect()) {
            throw new RuntimeException("Not a single select property, can not return selected option.");
        }
        String item = null;
        if(getIsSingleSelect()) {
            option = type.getOption((String)getValue());
        }
        if(getIsNumericSelect()) {
            option = type.getOption(Integer.valueOf((String)getValue()));           
        }
        if(option!=null) {
            item = option.getName();
        }
        return item;
    }
  
    public void setSelectedOption(String item) {
        if(!getIsSingleSelect() && !getIsNumericSelect()) {
            throw new RuntimeException("Not a single select property, can not set option.");
        }
        if(item!=null) {
            for (IMLPropertyOption option : type.getOptions()) {
                if(item.equals(option.getName())) {
                    if(getIsSingleSelect()) {
                        value = (V) option.getId();                    
                    }
                    if(getIsNumericSelect()) {
                        PropertyOption propertyOption = (PropertyOption)option;
                        if(propertyOption.getValue()!=null) {
                            value = (V) propertyOption.getValue().toString(); 
                        } else {
                            value = null;
                        }
                    } 
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Set option value: "+ value + " for label: " + item);
                    }
                    break;
                }          
            }
        } else {
            value = null;
        }
    }
    
    public int getMax() {
        if(!type.isNumericSelect()) {
            throw new RuntimeException("Not a numeric select property, can not return max value.");
        }
        return type.getMaxValue();
    }
    
    public int getMin() {
        if(!type.isNumericSelect()) {
            throw new RuntimeException("Not a numeric select property, can not return min value.");
        }
        return type.getMinValue();
    }
    
    public K getId() {
        return getKey();
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public PropertyType getType() {
        return type;
    }

    public void setType(PropertyType type) {
        this.type = type;
    }
    
    public boolean isShowLabel() {
        return showLabel;
    }
    
    public boolean getShowLabel() {
        return isShowLabel();
    }

    public void setShowLabel(boolean showLabel) {
        this.showLabel = showLabel;
    }

    public String getDatePattern() {
        return dateFormat.toPattern();
    }
    
    public DateFormat getDateFormat() {
        return dateFormat;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        HuiProperty other = (HuiProperty) obj;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key))
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }
    
}
