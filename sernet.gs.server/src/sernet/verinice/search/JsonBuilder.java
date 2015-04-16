/*******************************************************************************
 * Copyright (c) 2015 Daniel Murygin.
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
package sernet.verinice.search;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Permission;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class JsonBuilder {
    
    private static final Logger LOG = Logger.getLogger(JsonBuilder.class);
    
    public static final String getJson(CnATreeElement element) {       
        try {
            return doGetJson(element);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }  
    }

    private static String doGetJson(CnATreeElement element) throws IOException {
        XContentBuilder builder;
        builder = XContentFactory.jsonBuilder().startObject();      
        builder.field("uuid", element.getUuid());
        builder.field("dbid", element.getDbId());
        builder.field("title", element.getTitle());
        builder.field("element-type", element.getTypeId());
        builder.field("ext-id", element.getExtId());
        builder.field("source-id", element.getSourceId());
        builder.field("scope-id", element.getScopeId());
        builder.field("parent-id", element.getParentId());
        Iterator<Permission> iter = element.getPermissions().iterator();
        StringBuilder sb = new StringBuilder();
        while(iter.hasNext()){
            Permission p = iter.next();
            sb.append(p.getRole()).append("(");
            if(p.isReadAllowed()){
                sb.append("r");
            }
            if(p.isWriteAllowed()){
                sb.append("w");
            }
            
            sb.append(")");
            
            if(iter.hasNext()){
                sb.append(", ");
            }
        }
        builder.field("permission-roles", sb.toString());
        if(element.getEntity()!=null && element.getEntity().getTypedPropertyLists()!=null) {
            Entity e = element.getEntity();
            Map<String, PropertyList> properties = element.getEntity().getTypedPropertyLists();
            addProperties(builder, properties, e);
        }
        return builder.endObject().string();
    }

    private static void addProperties(XContentBuilder builder, Map<String, PropertyList> properties, Entity e) throws IOException {
        for (String key : properties.keySet()) {
            PropertyList list = (PropertyList)properties.get(key);
            HUITypeFactory factory = HUITypeFactory.getInstance();
            for(Property p : list.getProperties()){
                String eType = e.getEntityType();
                String pTypeId = p.getPropertyTypeID();
                PropertyType pType = factory.getPropertyType(eType, pTypeId);
                String value = "";
                if(eType != null && pTypeId != null && pType != null){
                    value = mapPropertyToString(p, pType);
                } else {
                    value = p.getPropertyValue();
                }
                if(StringUtils.isNotEmpty(value)){
                    builder.field(p.getPropertyTypeID(), value);
                }
            }
        }    
    }
    
    private static String mapPropertyToString(Property p, PropertyType type){
        if(type.isDate()){
           return mapDateProperty(p); 
        } else if(type.isMultiselect()){
            return mapMultiSelectProperty(p, type);
        } else if(type.isSingleSelect()){
            return mapMultiSelectProperty(p, type);
        } else if(type.isNumericSelect()){
            return mapNumericSelectProperty(p, type);
        }
        
        return p.getPropertyValue();
    }
    
    private static String mapNumericSelectProperty(Property p, PropertyType type){
        String r = type.getNameForValue(Integer.parseInt(p.getPropertyValue()));
        if(LOG.isDebugEnabled()){
            LOG.debug("ID:\t" + p.getPropertyTypeID() + "\tValue:\t" + p.getPropertyValue() + "\tNameForValue:\t" + r);
        }
        return r;
    }
    
    private static String mapMultiSelectProperty(Property p, PropertyType type){
       if(!p.getPropertyValue().contains("none") && !("".equals(p.getPropertyValue()))){
           return type.getOption(p.getPropertyValue()).getName();
        }
       return p.getPropertyValue();
    }
    
    private static String mapDateProperty(Property p){
        if(StringUtils.isNotEmpty(p.getPropertyValue())){
            Date d = new Date(Long.parseLong(p.getPropertyValue()));
            SimpleDateFormat sdf = new SimpleDateFormat(getLocalizedDatePattern(), Locale.getDefault());
            sdf.setLenient(true);
            return sdf.format(d);
        }
        return p.getPropertyValue();
        
    }
    
    private static String getLocalizedDatePattern(){
        if(Locale.getDefault().equals(Locale.GERMAN)){
            return "dd.MM.yyyy";
        } else {
            return "MM.dd.yyyy";
        }
    }
}
