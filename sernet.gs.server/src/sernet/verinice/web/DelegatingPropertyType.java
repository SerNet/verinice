/*******************************************************************************
 * Copyright (c) 2018 Jochen Kemnade.
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
package sernet.verinice.web;

import java.util.List;
import java.util.Map;
import java.util.Set;

import sernet.hui.common.connect.DependsType;
import sernet.hui.common.connect.HuiUrl;
import sernet.hui.common.connect.IReferenceResolver;
import sernet.hui.common.connect.IUrlResolver;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyOption;
import sernet.hui.common.connect.PropertyType;
import sernet.hui.common.multiselectionlist.IMLPropertyOption;
import sernet.hui.common.rules.IFillRule;
import sernet.hui.common.rules.IValidationRule;

/**
 * PropertyType implementation that delegates all method invocations to its
 * delegate
 */
public class DelegatingPropertyType extends PropertyType {

    private static final long serialVersionUID = -1487981701981356429L;
    protected final PropertyType delegate;

    public DelegatingPropertyType(PropertyType delegate) {
        this.delegate = delegate;
    }

    @Override
    public int getMinValue() {
        return delegate.getMinValue();
    }

    @Override
    public int getMaxValue() {
        return delegate.getMaxValue();
    }

    @Override
    public boolean isCrudButtons() {
        return delegate.isCrudButtons();
    }

    @Override
    public void setNumericDefault(String numericDefault) {
        delegate.setNumericDefault(numericDefault);
    }

    @Override
    public boolean isReportable() {
        return delegate.isReportable();
    }

    @Override
    public void addValidator(IValidationRule rule) {
        delegate.addValidator(rule);
    }

    @Override
    public boolean isEditable() {
        return delegate.isEditable();
    }

    @Override
    public void setEditable(boolean editable) {
        delegate.setEditable(editable);
    }

    @Override
    public IFillRule getDefaultRule() {
        return delegate.getDefaultRule();
    }

    @Override
    public void setDefaultRule(IFillRule defaultRule) {
        delegate.setDefaultRule(defaultRule);
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public void setId(String id) {
        delegate.setId(id);
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public void setName(String name) {
        delegate.setName(name);
    }

    @Override
    public List<IMLPropertyOption> getOptions() {
        return delegate.getOptions();
    }

    @Override
    public PropertyOption getOption(String id) {
        return delegate.getOption(id);
    }

    @Override
    public PropertyOption getOption(Integer id) {
        return delegate.getOption(id);
    }

    @Override
    public void setPredefinedValues(@SuppressWarnings("rawtypes") List predefinedValues) {
        delegate.setPredefinedValues(predefinedValues);
    }

    @Override
    public boolean isRequired() {
        return delegate.isRequired();
    }

    @Override
    public void setRequired(boolean required) {
        delegate.setRequired(required);
    }

    @Override
    public String getTooltiptext() {
        return delegate.getTooltiptext();
    }

    @Override
    public void setTooltiptext(String tooltiptext) {
        delegate.setTooltiptext(tooltiptext);
    }

    @Override
    public void setDependencies(Set<DependsType> set) {
        delegate.setDependencies(set);
    }

    @Override
    public Set<DependsType> getDependencies() {
        return delegate.getDependencies();
    }

    @Override
    public boolean isMultiselect() {
        return delegate.isMultiselect();
    }

    @Override
    public boolean isSingleSelect() {
        return delegate.isSingleSelect();
    }

    @Override
    public boolean isNumericSelect() {
        return delegate.isNumericSelect();
    }

    @Override
    public boolean isBooleanSelect() {
        return delegate.isBooleanSelect();
    }

    @Override
    public boolean isEnum() {
        return delegate.isEnum();
    }

    @Override
    public boolean isLine() {
        return delegate.isLine();
    }

    @Override
    public boolean isReference() {
        return delegate.isReference();
    }

    @Override
    public boolean isCnaLinkReference() {
        return delegate.isCnaLinkReference();
    }

    @Override
    public boolean isText() {
        return delegate.isText();
    }

    @Override
    public boolean isDate() {
        return delegate.isDate();
    }

    @Override
    public void setInputType(String attribute) {
        delegate.setInputType(attribute);
    }

    @Override
    public String getInputName() {
        return delegate.getInputName();
    }

    @Override
    public void setInitialFocus(boolean b) {
        delegate.setInitialFocus(b);
    }

    @Override
    public boolean isFocus() {
        return delegate.isFocus();
    }

    @Override
    public List<IValidationRule> getValidators() {
        return delegate.getValidators();
    }

    @Override
    public void setValidators(List<IValidationRule> validators) {
        delegate.setValidators(validators);
    }

    @Override
    public Map<String, Boolean> validate(String text, String[] params) {
        return delegate.validate(text, params);
    }

    @Override
    public void setVisible(boolean b) {
        delegate.setVisible(b);
    }

    @Override
    public boolean isVisible() {
        return delegate.isVisible();
    }

    @Override
    public void setURL(boolean url) {
        delegate.setURL(url);
    }

    @Override
    public boolean isURL() {
        return delegate.isURL();
    }

    @Override
    public void setReferencedEntityType(String attribute) {
        delegate.setReferencedEntityType(attribute);
    }

    @Override
    public void setReferencedCnaLinkType(String attribute) {
        delegate.setReferencedCnaLinkType(attribute);
    }

    @Override
    public String getReferencedEntityTypeId() {
        return delegate.getReferencedEntityTypeId();
    }

    @Override
    public String getReferencedCnaLinkType() {
        return delegate.getReferencedCnaLinkType();
    }

    @Override
    public List<IMLPropertyOption> getReferencedEntities() {
        return delegate.getReferencedEntities();
    }

    @Override
    public IReferenceResolver getReferenceResolver() {
        return delegate.getReferenceResolver();
    }

    @Override
    public void setReferenceResolver(IReferenceResolver referenceResolver) {
        delegate.setReferenceResolver(referenceResolver);
    }

    @Override
    public List<HuiUrl> getResolvedUrls() {
        return delegate.getResolvedUrls();
    }

    @Override
    public IUrlResolver getUrlResolver() {
        return delegate.getUrlResolver();
    }

    @Override
    public void setUrlResolver(IUrlResolver urlResolver) {
        delegate.setUrlResolver(urlResolver);
    }

    @Override
    public void setCrudButtons(boolean b) {
        delegate.setCrudButtons(b);
    }

    @Override
    public List<IMLPropertyOption> getReferencedEntities(List<Property> references) {
        return delegate.getReferencedEntities(references);
    }

    @Override
    public void setNumericMin(String minString) {
        delegate.setNumericMin(minString);
    }

    @Override
    public void setNumericMax(String maxString) {
        delegate.setNumericMax(maxString);
    }

    @Override
    public String getNameForValue(int i) {
        return delegate.getNameForValue(i);
    }

    @Override
    public void setReportable(boolean value) {
        delegate.setReportable(value);
    }

    @Override
    public int getNumericDefault() {
        return delegate.getNumericDefault();
    }

    @Override
    public void setTags(String tags) {
        delegate.setTags(tags);
    }

    @Override
    public String getTags() {
        return delegate.getTags();
    }

    @Override
    public void setTextRows(String rows) {
        delegate.setTextRows(rows);
    }

    @Override
    public int getTextrows() {
        return delegate.getTextrows();
    }

    @Override
    public boolean isShowInObjectBrowser() {
        return delegate.isShowInObjectBrowser();
    }

    @Override
    public void setShowInObjectBrowser(boolean showInObjectBrowser) {
        delegate.setShowInObjectBrowser(showInObjectBrowser);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    @Override
    public int compareTo(PropertyType o) {
        return delegate.compareTo(o);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

}
