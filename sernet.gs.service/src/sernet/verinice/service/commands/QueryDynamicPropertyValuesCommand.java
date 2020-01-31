/*******************************************************************************
 * Copyright (c) 2020 Jonas Jordan
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
 ******************************************************************************/
package sernet.verinice.service.commands;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.elasticsearch.common.cli.commons.MissingArgumentException;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;

import sernet.hui.common.connect.HitroUtil;
import sernet.hui.common.connect.Property;
import sernet.verinice.interfaces.GenericCommand;

/**
 * Queries all dinstinct values for given dynamic (SNCA) property type.
 * 
 * Input: setPropertyType() [required]
 * 
 * Output: getValues()
 */
public class QueryDynamicPropertyValuesCommand extends GenericCommand {

    private static final Logger log = Logger.getLogger(QueryDynamicPropertyValuesCommand.class);
    private List<String> values;
    private String dynamicPropertyType;

    /**
     * Sets dynamic property type name without the entity type prefix (e.g. pass
     * "release" for "bp_safeguard_release", "bp_threat_release" etc.).
     */
    public void setPropertyType(@NonNull String dynamicPropertyType) {
        this.dynamicPropertyType = dynamicPropertyType;
    }

    @Override
    public void execute() {
        try {
            if (dynamicPropertyType == null) {
                throw new MissingArgumentException("Property type not set.");
            }
            values = getDaoFactory().getDAO(Property.class).findByCriteria(getCriteria());
        } catch (Exception ex) {
            log.error("Failed querying values for dynamic property type " + dynamicPropertyType,
                    ex);
            throw new RuntimeException(ex);
        }
    }

    public List<String> getValues() {
        return values;
    }

    private DetachedCriteria getCriteria() {
        return DetachedCriteria.forClass(Property.class)
                .setProjection(Projections.distinct(Projections.sqlProjection(
                        "cast(propertyValue as varchar(1000)) as propertyValueVarchar",
                        new String[] { "propertyValueVarchar" }, new Type[] { new StringType() })))
                .add(Restrictions.in("propertyType", getPrefixedPropertyTypeIds()))
                .add(Restrictions.isNotNull("propertyValue"))
                .add(Restrictions.sqlRestriction("length(propertyValue) != 0"));
    }

    private String[] getPrefixedPropertyTypeIds() {
        return HitroUtil.getInstance().getTypeFactory().getAllEntityTypes().stream()
                .flatMap(entityType -> entityType.getAllPropertyTypes().stream().filter(
                        p -> p.getId().equals(entityType.getId() + "_" + dynamicPropertyType))
                        .map(p -> p.getId()))
                .toArray(String[]::new);
    }
}
