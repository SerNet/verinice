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

import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;

import sernet.gs.ui.rcp.main.Activator;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyType;

/**
 * Persists columns settings for a specific {@link EntityType}.
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 */
public class PersistedSortedColumnStore extends ColumnStore {

    /**
     * All properties tag width this constant are displayed as default column.
     */
    private static final String DEFAULT_TAG_BASIC = "basic";

    IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();

    private String entityTypeId;

    private final static String COLUMN_PREFIX = "search_table_column_visibility_";

    private final static String COLUMNS_PERSISTED = "search_table_columns_preferences_";

    public PersistedSortedColumnStore(String entityTypeId) {
        super();

        this.entityTypeId = entityTypeId;

        if (!columnsArePersisted()) {
            restoreDefault();
        }

        initColumnStore();
    }

    @Override
    public void restoreDefault() {

        IColumn iconColumn = IColumnFactory.getIconColumn(this);
        IColumn titleColumn = IColumnFactory.getTitleColumn(this);
        IColumn scopeColumn = IColumnFactory.getScopeColumn(this);

        addColumn(iconColumn);
        addColumn(titleColumn);
        addColumn(scopeColumn);

        for (PropertyType propertyType : getAllPropertyTypes()) {
            IColumn col = IColumnFactory.getPropertyTypeColumn(propertyType, this);
            if (isDefaultColumn(col)) {
                addColumn(col);
            } else {
                setVisible(col, false);
            }
        }

        preferenceStore.setValue(COLUMNS_PERSISTED + entityTypeId, true);
    }

    private void initColumnStore() {

        IColumn iconColumn = IColumnFactory.getIconColumn(this);
        IColumn titleColumn = IColumnFactory.getTitleColumn(this);
        IColumn scopeColumn = IColumnFactory.getScopeColumn(this);

        addColumn(iconColumn);
        addColumn(titleColumn);
        addColumn(scopeColumn);

        for (PropertyType propertyType : getAllPropertyTypes()) {
            IColumn col = IColumnFactory.getPropertyTypeColumn(propertyType, this);
            if (isColumnVisible(col)) {
                addColumn(col);
            } else {
                setVisible(col, false);
            }
        }

    }

    private boolean columnsArePersisted() {
        return preferenceStore.getBoolean(COLUMNS_PERSISTED + entityTypeId);
    }

    public boolean isColumnVisible(IColumn column) {
        return preferenceStore.getBoolean((getPropertyVisibilitySettingIdentifier(column)));
    }

    private String getPropertyVisibilitySettingIdentifier(IColumn propertyType) {
        return COLUMN_PREFIX + propertyType.getTitle();
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
        List<PropertyType> propertyTypes = entityType.getAllPropertyTypes();
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
}
