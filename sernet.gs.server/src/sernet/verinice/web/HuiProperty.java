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
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javax.faces.model.SelectItem;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import sernet.hui.common.connect.PropertyOption;
import sernet.hui.common.connect.PropertyType;
import sernet.hui.common.multiselectionlist.IMLPropertyOption;

/**
 * Represents a huiproperty of the hitro framework for JSF.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class HuiProperty implements Serializable {

    private static final long serialVersionUID = 2L;

    private static final Logger LOG = Logger.getLogger(HuiProperty.class);

    private String key;

    private String value;

    private PropertyType propertyType;

    private boolean showLabel = true;

    private boolean isEnabled = true;

    private final List<ValueChangeListener> valueChangeListeners = new LinkedList<>();

    private List<SelectItem> options;

    public HuiProperty(PropertyType type, String key, String value) {
        super();
        this.propertyType = type;
        this.key = key;
        this.value = value;
    }

    public String getName() {
        return propertyType.getName();
    }

    public String getInputName() {
        return propertyType.getInputName();
    }

    public boolean getIsLine() {
        return propertyType.isLine();
    }

    public boolean getIsText() {
        return propertyType.isText();
    }

    public boolean isEditable() {
        return propertyType.isEditable();
    }

    public boolean getIsEditable() {
        return isEditable();
    }

    public boolean isVisible() {
        return propertyType.isVisible();
    }

    public boolean getIsURL() {
        return propertyType.isURL();
    }

    public String getURLValue() {
        if (getIsURL() && getValue() != null && !getValue().isEmpty()) {
            try {
                int n = getIndexOf(getValue(), '"', 0);
                String[] a = (getValue()).substring(n).split(">");
                return a[0].replaceAll("\"", "");
            } catch (Exception e) {
                LOG.warn("Something went wrong on reading the URLValue", e);
            }
        }
        return "";
    }

    public String getURLText() {
        if (getIsURL() && getValue() != null && !getValue().isEmpty()) {
            try {
                int n = getIndexOf(getValue(), '"', 0);
                String[] a = (getValue()).substring(n).split(">");
                return a[1].replaceAll("</a", "");
            } catch (Exception e) {
                LOG.warn("Something went wrong on reading the URLText", e);
            }
        }
        return "";
    }

    public void setURLText(String urlText) {
        if (getIsURL()) {
            StringBuilder sb = new StringBuilder();
            sb.append("<a href=\"").append(getURLValue()).append("\">").append(urlText)
                    .append("</a>");
            setValue(sb.toString());
        }
    }

    public void setURLValue(String urlValue) {
        if (getIsURL()) {
            StringBuilder sb = new StringBuilder();
            sb.append("<a href=\"").append(urlValue).append("\">").append(getURLText())
                    .append("</a>");
            setValue(sb.toString());
        }

    }

    private static int getIndexOf(String str, char c, int n) {
        int pos = str.indexOf(c, 0);
        while (n-- > 0 && pos != -1) {
            pos = str.indexOf(c, pos + 1);
        }
        return pos;
    }

    public boolean getIsVisible() {
        return isVisible();
    }

    public boolean getIsSingleSelect() {
        return propertyType.isSingleSelect();
    }

    public boolean getIsNumericSelect() {
        return propertyType.isNumericSelect();
    }

    public boolean getIsDate() {
        return propertyType.isDate();
    }

    public boolean getIsBooleanSelect() {
        return propertyType.isBooleanSelect();
    }

    public boolean getIsReference() {
        return propertyType.isReference();
    }

    public boolean getIsMultiselect() {
        return propertyType.isMultiselect();
    }

    public boolean isShowInObjectBrowser() {
        return propertyType.isShowInObjectBrowser();
    }

    public Date getDate() {
        if (!getIsDate()) {
            return null;
        }
        Date date = null;
        if (!value.isEmpty()) {
            date = new Date(Long.valueOf(value));
        }
        return date;
    }

    public void setDate(Date date) {
        if (!getIsDate()) {
            return;
        }
        if (date != null) {
            value = Long.toString(date.getTime());
        } else {
            value = null;
        }
    }

    public boolean getSingleSelect() {
        boolean result = false;
        if (getValue() != null) {
            result = Integer.valueOf(getValue()) == 1;
        }
        return result;
    }

    public void setSingleSelect(boolean b) {
        setValue(b ? "1" : "0");
    }

    public List<String> getOptionList() {
        if (!getIsSingleSelect() && !getIsNumericSelect()) {
            return Collections.emptyList();
        }
        List<String> itemList = Collections.emptyList();
        if (propertyType.getOptions() != null) {
            itemList = new ArrayList<>(propertyType.getOptions().size());
            if (getIsSingleSelect()) {
                itemList.add(Messages.getString(PropertyOption.SINGLESELECTDUMMYVALUE));
            }
            for (IMLPropertyOption option : propertyType.getOptions()) {
                itemList.add(option.getName());
            }
        }
        return itemList;
    }

    public String getSelectedOption() {
        IMLPropertyOption option = null;
        if (!getIsSingleSelect() && !getIsNumericSelect()) {
            return null;
        }
        String item = null;

        if (getIsSingleSelect() && getValue() != null) {
            option = propertyType.getOption(getValue());
        }
        if (getIsNumericSelect() && getValue() != null && !getValue().equals("")) {
            option = propertyType.getOption(Integer.valueOf(getValue()));
        }

        if (option != null) {
            item = option.getName();
        }

        return item;
    }

    public void setSelectedOption(String item) {
        getSelectionValue(item);
    }

    public String getSelectionValue(String item) {
        if (!getIsSingleSelect() && !getIsNumericSelect()) {
            return null;
        }
        if (item == null) {
            return null;
        }
        if (getIsSingleSelect()
                && item.equals(Messages.getString(PropertyOption.SINGLESELECTDUMMYVALUE))) {
            setValue(null);
            return null;
        }
        for (IMLPropertyOption option : propertyType.getOptions()) {
            if (item.equals(option.getName())) {
                setValue(getSelectionValue(option));

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Set option value: " + value + " for label: " + item);
                }

                return getValue();
            }
        }

        return null;
    }

    private String getSelectionValue(IMLPropertyOption option) {
        if (getIsSingleSelect()) {
            return option.getId();
        }
        if (getIsNumericSelect()) {
            PropertyOption propertyOption = (PropertyOption) option;
            if (propertyOption.getValue() != null) {
                return propertyOption.getValue().toString();
            } else {
                return null;
            }
        }
        return null;
    }

    public int getMax() {
        if (!propertyType.isNumericSelect()) {
            return 0;
        }
        return propertyType.getMaxValue();
    }

    public int getMin() {
        if (!propertyType.isNumericSelect()) {
            return 0;
        }
        return propertyType.getMinValue();
    }

    public String getId() {
        return getKey();
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        if (!Objects.equals(this.value, value)) {
            this.value = value;
            fireChangeListeners();
        }
    }

    public PropertyType getType() {
        return propertyType;
    }

    public void setType(PropertyType type) {
        this.propertyType = type;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        HuiProperty other = (HuiProperty) obj;
        if (key == null) {
            if (other.key != null) {
                return false;
            }
        } else if (!key.equals(other.key)) {
            return false;
        }
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

    /**
     * According to the {@link sernet.hui.common.connect.DependsType} this
     * property is enabled. This additional getter is necessary for JSF and is
     * delegated to {@link #isEnabled()}.
     *
     * @return true if all dependencies are satisfied.
     */
    public boolean getIsEnabled() {
        return isEnabled();
    }

    /**
     * This uses {@link #setEnabled(boolean)} and is necessary for JSF
     * respectively for EL.
     *
     * @param isEnabled
     *            Indicates if the property satisfied all dependency, defined by
     *            {@link sernet.hui.common.connect.DependsType}.
     */
    public void setIsEnabled(boolean isEnabled) {
        this.setEnabled(isEnabled);
    }

    /**
     * According to the {@link sernet.hui.common.connect.DependsType} this
     * property is enabled.
     *
     * @return true if all dependencies are satisfied.
     */
    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    /**
     * Indicates if a {@link HuiProperty} is disabled through editable property
     * or a depend definition in the SNCA.xml.
     *
     * @return false If editable or depends is false.
     */
    public boolean isDisabled() {
        return !(getIsEditable() && getIsEnabled());
    }

    /**
     * Calls every registered {@link ValueChangeListener} manually.
     */
    public void fireChangeListeners() {
        for (ValueChangeListener valueChangeListener : valueChangeListeners) {
            valueChangeListener.processChangedValue(this);
        }
    }

    /**
     * Registers a {@link ValueChangeListener}.
     *
     * @param valueChangeListener
     *            The {@link ValueChangeListener} which is registered.
     */
    public void addValueChangeListener(ValueChangeListener valueChangeListener) {
        valueChangeListeners.add(valueChangeListener);
    }

    /**
     * Listener is called whenever the {@value is changed}.
     *
     * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
     *
     */
    public interface ValueChangeListener extends Serializable {

        /**
         * Called whenever the value of the {@link HuiProperty} is changed.
         *
         * @param huiProperty
         *            The {@link HuiProperty} which experienced a change of its
         *            {@link #value};
         */
        void processChangedValue(HuiProperty huiProperty);
    }

    @Override
    public String toString() {
        return "HuiProperty [key=" + key + ", value=" + value + ", propertyType=" + propertyType
                + ", showLabel=" + showLabel + ", isEnabled=" + isEnabled + "]";
    }

    public List<SelectItem> getOptions() {

        if (propertyType.isMultiselect() && options == null) {
            options = new ArrayList<>(propertyType.getOptions().size());
            for (IMLPropertyOption imlPropertyOption : propertyType.getOptions()) {
                SelectItem selectItem = new SelectItem(imlPropertyOption.getId(),
                        imlPropertyOption.getName());
                options.add(selectItem);
            }
        }

        return options;
    }

    public List<String> getSelectedOptions() {
        if (!getIsMultiselect()) {
            return Collections.emptyList();
        }

        String[] split = getValue() != null ? getValue().split(",s*") : new String[] {};
        List<String> selectedOptions = new ArrayList<>(split.length);
        for (int i = 0; i < split.length; i++) {
            selectedOptions.add(split[i].trim());

        }

        return selectedOptions;
    }

    public void setSelectedOptions(List<String> selectedOptions) {
        setValue(StringUtils.join(selectedOptions, ","));
    }
}
