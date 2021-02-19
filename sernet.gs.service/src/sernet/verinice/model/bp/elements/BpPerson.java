/*******************************************************************************
 * Copyright (c) 2017 Sebastian Hagedorn.
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
 *     Sebastian Hagedorn sh[at]sernet.de - initial API and implementation
 ******************************************************************************/
package sernet.verinice.model.bp.elements;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import sernet.gs.service.StringUtil;
import sernet.hui.common.connect.IAbbreviatedElement;
import sernet.hui.common.connect.IPerson;
import sernet.hui.common.connect.ITaggableElement;
import sernet.verinice.model.bp.IBpElement;
import sernet.snutils.TagHelper;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.configuration.Configuration;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class BpPerson extends CnATreeElement
        implements IBpElement, IAbbreviatedElement, ITaggableElement, IPerson {

    private static final long serialVersionUID = 976554080854876269L;

    public static final String TYPE_ID = "bp_person"; //$NON-NLS-1$
    public static final String PROP_LAST_NAME = "bp_person_last_name"; //$NON-NLS-1$
    public static final String PROP_FIRST_NAME = "bp_person_first_name"; //$NON-NLS-1$
    public static final String PROP_TAG = "bp_person_tag"; //$NON-NLS-1$
    public static final String PROP_EMAIL = "bp_person_email"; //$NON-NLS-1$
    public static final String PROP_PHONE = "bp_person_phone"; //$NON-NLS-1$
    public static final String PROP_ABBR = "bp_person_abbr"; //$NON-NLS-1$
    public static final String PROP_TITLE = "bp_person_title"; //$NON-NLS-1$

    private Set<Configuration> configurations = new HashSet<>(1);

    protected BpPerson() {
    }

    public BpPerson(CnATreeElement parent) {
        super(parent);
        init();
    }

    public Set<Configuration> getConfigurations() {
        return configurations;
    }

    public void setConfigurations(Set<Configuration> configurations) {
        this.configurations = configurations;
    }

    @Override
    public String getTitle() {
        StringBuilder sb = new StringBuilder();
        final String surname = getEntity().getPropertyValue(PROP_LAST_NAME);
        if (surname != null && !surname.isEmpty()) {
            sb.append(surname);
        }
        final String name = getEntity().getPropertyValue(PROP_FIRST_NAME);
        if (name != null && !name.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(name);
        }
        return sb.toString();
    }

    @Override
    public void setTitel(String name) {
        // empty, otherwise title get scrambled while copying
    }

    @Override
    public String getTypeId() {
        return TYPE_ID;
    }

    @Override
    public Collection<String> getTags() {
        return TagHelper.getTags(getEntity().getPropertyValue(PROP_TAG));
    }

    @Override
    public String getAbbreviation() {
        return getEntity().getPropertyValue(PROP_ABBR);
    }

    @Override
    public String getFirstName() {
        return getEntity().getRawPropertyValue(PROP_FIRST_NAME);
    }

    @Override
    public String getLastName() {
        return getEntity().getRawPropertyValue(PROP_LAST_NAME);
    }

    @Override
    public String getSalutation() {
        return StringUtil.replaceEmptyStringByNull(getEntity().getPropertyValue(PROP_TITLE));
    }

    @Override
    public String getEMailAddress() {
        return getEntity().getRawPropertyValue(PROP_EMAIL);
    }
}
