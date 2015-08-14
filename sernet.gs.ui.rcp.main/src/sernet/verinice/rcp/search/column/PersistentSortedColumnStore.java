/*******************************************************************************
 * Copyright (c) 2015 Benjamin Weißenfels.
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
 *     Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp.search.column;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;

import sernet.gs.ui.rcp.main.Activator;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.IEntityElement;
import sernet.hui.common.connect.PropertyGroup;
import sernet.hui.common.connect.PropertyType;

/**
 * Persists columns settings for a specific {@link EntityType}.
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 */
public class PersistentSortedColumnStore extends ColumnStore {

    /**
     * All properties tag with this constant are displayed as default column.
     */
    private static final String DEFAULT_TAG_BASIC = "basic";

    private IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();

    private String entityTypeId;

    private final static String COLUMN_PREFIX = "search_table_column_visibility_";

    private final static String COLUMNS_PERSISTED = "search_table_columns_preferences_";

    private final static String COLUMN_WIDTH = "search_table_column_width_";

    public PersistentSortedColumnStore(String entityTypeId) {
        super();

        this.entityTypeId = entityTypeId;

        if (!columnsArePersisted()) {
            restoreDefault();
        } else {
            initColumnStore();
        }
    }

    @Override
    public void restoreDefault() {

        IColumn iconColumn = IColumnFactory.getIconColumn(this);
        IColumn titleColumn = IColumnFactory.getTitleColumn(this);
        IColumn scopeColumn = IColumnFactory.getScopeColumn(this);
        IColumn occurenceColumn = IColumnFactory.getOccurenceColumn(this);

        addColumn(iconColumn);
        addColumn(titleColumn);
        addColumn(scopeColumn);
        addColumn(occurenceColumn);

        iconColumn.setWidth(IconColumn.DEFAULT_WIDTH);
        titleColumn.setWidth(IColumn.DEFAULT_WIDTH);
        scopeColumn.setWidth(IColumn.DEFAULT_WIDTH);
        occurenceColumn.setWidth(IColumn.DEFAULT_WIDTH);

        int order = 0;
        for (PropertyType propertyType : getAllPropertyTypes()) {
            // reference types are ignored (VN-1204)
            if(!propertyType.isReference()) {
                IColumn col = IColumnFactory.getPropertyTypeColumn(propertyType, this, order++);
                preferenceStore.setValue(getPrefixForWidthProperty(col), IColumn.DEFAULT_WIDTH);
                if (isDefaultColumn(col)) {
                    addColumn(col);
                } else {
                    setVisible(col, false);
                }
            }
        }

        preferenceStore.setValue(COLUMNS_PERSISTED + entityTypeId, true);
    }

    private void initColumnStore() {

        IColumn iconColumn = IColumnFactory.getIconColumn(this);
        IColumn titleColumn = IColumnFactory.getTitleColumn(this);
        IColumn scopeColumn = IColumnFactory.getScopeColumn(this);
        IColumn occurenceColumn = IColumnFactory.getOccurenceColumn(this);

        setVisible(iconColumn, isColumnVisible(iconColumn));
        setVisible(titleColumn, isColumnVisible(titleColumn));
        setVisible(scopeColumn, isColumnVisible(scopeColumn));
        addColumn(occurenceColumn);

        int order = 0;
        for (PropertyType propertyType : getAllPropertyTypes()) {
            // reference types are ignored (VN-1204)
            if(!propertyType.isReference()) {
                IColumn col = IColumnFactory.getPropertyTypeColumn(propertyType, this, order++);
                if (isColumnVisible(col)) {
                    addColumn(col);
                } else {
                    setVisible(col, false);
                }
            }
        }

    }

    private boolean columnsArePersisted() {
        return preferenceStore.getBoolean(COLUMNS_PERSISTED + entityTypeId);
    }

    public boolean isColumnVisible(IColumn column) {
        return preferenceStore.getBoolean((getPropertyVisibilitySettingIdentifier(column)));
    }

    private String getPropertyVisibilitySettingIdentifier(IColumn column) {
        return new StringBuilder()
            .append(COLUMN_PREFIX)
            .append(entityTypeId)
            .append(column.getId()).toString();
    }

    public boolean isDefaultColumn(IColumn propertyType) {

        if (propertyType instanceof PropertyTypeColumn) {
            if (((PropertyTypeColumn) propertyType).getPropertyTags() != null) {
                return ((PropertyTypeColumn) propertyType).getPropertyTags().toLowerCase().contains(DEFAULT_TAG_BASIC);
            }
        }

        return false;
    }

    public List<PropertyType> getAllPropertyTypes() {

        HUITypeFactory huiTypeFactory = HUITypeFactory.getInstance();
        EntityType entityType = huiTypeFactory.getEntityType(entityTypeId);

        List<IEntityElement> allElements = entityType.getElements();
        List<PropertyType> propertyTypes = new ArrayList<PropertyType>(0);
        for (Iterator<IEntityElement> iter = allElements.iterator(); iter.hasNext();) {
            Object obj = iter.next();
            if (obj instanceof PropertyType) {
                propertyTypes.add((PropertyType) obj);
            }

            if (obj instanceof PropertyGroup) {
                for (PropertyType propertyType : ((PropertyGroup) obj).getPropertyTypes()) {
                    propertyTypes.add(propertyType);
                }
            }
        }
        return propertyTypes;
    }

    @Override
    public void addColumn(IColumn column) {
        super.addColumn(column);
        preferenceStore.setValue(getPropertyVisibilitySettingIdentifier(column), true);
    }

    @Override
    public void setVisible(IColumn column, boolean visible) {
        super.setVisible(column, visible);
        preferenceStore.setValue(getPropertyVisibilitySettingIdentifier(column), visible);
    }

    @Override
    public void setWidth(IColumn column, int width) {
        preferenceStore.setValue(getPrefixForWidthProperty(column), width);
    }

    private String getPrefixForWidthProperty(IColumn column) {
        return new StringBuilder()
            .append(COLUMN_WIDTH)
            .append(entityTypeId)
            .append(column.getId())
            .toString();
    }

    @Override
    public int getWidth(IColumn column) {
        return preferenceStore.getInt(getPrefixForWidthProperty(column));
    }
}
