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

import sernet.hui.common.connect.PropertyOption;
import sernet.hui.common.connect.PropertyType;
import sernet.hui.common.multiselectionlist.IMLPropertyOption;

/**
 * A delegating property type that overrides the available options
 */
public class OverrideOptionsPropertyType extends DelegatingPropertyType {

    private static final long serialVersionUID = -3836173784252216933L;
    private final List<IMLPropertyOption> options;

    public OverrideOptionsPropertyType(PropertyType delegate, List<IMLPropertyOption> options) {
        super(delegate);
        this.options = options;
    }

    @Override
    public List<IMLPropertyOption> getOptions() {
        return options;
    }

    @Override
    public PropertyOption getOption(String id) {
        return options.stream().filter(option -> option.getId().equals(id)).findFirst()
                .map(PropertyOption.class::cast).orElse(null);
    }

}
