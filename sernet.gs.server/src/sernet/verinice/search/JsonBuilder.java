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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyOption;
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
        builder.field("icon-path", element.getIconPath());
        builder.field("permission-roles", getPermissionString(element));
        if(element.getEntity()!=null && element.getEntity().getTypedPropertyLists()!=null) {
            builder = addProperties(builder, element.getEntityType().getAllPropertyTypeIds(), element.getEntity());
        }
        return builder.endObject().string();
    }

    /**
     * @param element
     * @return
     */
    private static String getPermissionString(CnATreeElement element) {
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
        return sb.toString();
    }

    private static XContentBuilder addProperties(XContentBuilder builder, String[] propertyTypeIds, Entity e) throws IOException {
        HUITypeFactory factory = HUITypeFactory.getInstance();
        for(String propertyTypeId : propertyTypeIds){
            builder.field(propertyTypeId, mapPropertyString(e, factory.getPropertyType(e.getEntityType(), propertyTypeId)));

        }
        return builder;
    }
    
    
    private static String mapPropertyString(Entity e, PropertyType pType){
        String value = e.getSimpleValue(pType.getId());
        String mappedValue = "";
        if(StringUtils.isEmpty(value)){
            mappedValue = getNullValue();
        } else if(pType.isDate()){
            mappedValue = mapDateProperty(value);
        } else if(pType.isSingleSelect() || pType.isMultiselect()){
            mappedValue = mapMultiSelectProperty(value, pType);
        } else if(pType.isNumericSelect()){
            mappedValue = mapNumericSelectProperty(value, pType);
        } else {
            mappedValue = value;
        }
        return mappedValue;
    }
    
    private static String getNullValue(){
        if(Locale.GERMAN.equals(Locale.getDefault()) || Locale.GERMANY.equals(Locale.getDefault())){
            return "unbearbeitet";
        } else {
            return "unedited";
        }
    }
    
    
    
    private static String mapNumericSelectProperty(String value, PropertyType type){
        return type.getNameForValue(Integer.parseInt(value));
    }
    
    private static String mapMultiSelectProperty(String value, PropertyType type){
        PropertyOption o = type.getOption(value);
        if(value == null || type == null || o == null){
            if(LOG.isDebugEnabled()){
                LOG.debug("No mapping for:\t" + value + "\t on <" + type.getId() + "> found, returning value");
            }
            return value;
        }
        if(LOG.isDebugEnabled()){
            LOG.debug("Mapping for:\t" + value + "\t on <" + type.getId() + ">:\t" + o.getName());
        }
        return type.getOption(value).getName();
    }
    
    private static String mapDateProperty(String value){
        if(StringUtils.isNotEmpty(value)){
            try{
                return tryParseDate(value);
            } catch (Exception e){
                LOG.error("Error on mapping date property", e);
            }
        }
        return value;
    }
    
    private static String tryParseDate(String value){

        String[] patterns = new String[]{"EEE MMM dd HH:mm:ss zzzz yyyy",
                "dd.MM.yy",
                "EE, dd.MM.yyyy",
                "dd.MM.yyyy",
                "dd.MM.yyyy HH:mm",
                "MMM dd, yyyy hh:mm aa",
                "MMM dd, yyyy hh:mm aa",
                "yyyy-MM-dd",
        "EEE MMM dd HH:mm:ss z yyyy"};

        
        Locale[] locales = new Locale[]{null, Locale.GERMAN, Locale.GERMANY, Locale.ENGLISH, Locale.US, Locale.UK, Locale.FRANCE, Locale.FRENCH, Locale.ITALIAN, Locale.ITALY};
        for(int i = 0; i < patterns.length; i++){
            for(int j = 0; j < locales.length; j++){
                try{
                    SimpleDateFormat formatter = null;
                    if(locales[j] == null){
                        formatter = new SimpleDateFormat(patterns[i]);
                    } else {
                        formatter = new SimpleDateFormat(patterns[i], locales[j]);
                    }
                    formatter.setLenient(true);
                    Date parsedDate = formatter.parse(value);
                    return getLocalizedDatePattern().format(parsedDate);
                } catch (Exception e) {
                    // do nothing and go on with next locale
                }
            }
        }
        LOG.error("Date not parseable:\t" + value);
        return "unparseable Date:\t" + value;
    }
    
    private static DateFormat getLocalizedDatePattern(){
        return SimpleDateFormat.getDateInstance(SimpleDateFormat.LONG, Locale.getDefault());
    }
}
