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
import java.util.Map;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;
import sernet.verinice.model.common.CnATreeElement;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class JsonBuilder {
    
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
        if(element.getEntity()!=null && element.getEntity().getTypedPropertyLists()!=null) {
            Map<String, PropertyList> properties = element.getEntity().getTypedPropertyLists();
            addProperties(builder, properties);
        }
        return builder.endObject().string();
    }

    private static void addProperties(XContentBuilder builder, Map<String, PropertyList> properties) throws IOException {
        for (String key : properties.keySet()) {
            PropertyList list = (PropertyList)properties.get(key);
            for(Property p : list.getProperties()){
                builder.field(p.getPropertyTypeID(), p.getPropertyValue());
            }
        }    
    }
}
