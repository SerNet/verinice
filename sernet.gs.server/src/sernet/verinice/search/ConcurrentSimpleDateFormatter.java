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
package sernet.verinice.search;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Fast thread safe date formatter.
 *
 * <p>The {@link DateFormat} class is not thread safe, so whether we have to
 * synchronize the access to it or we wrap the access through a thread local.
 * Using thread local is usually the faster variant, but comes with higher costs
 * regarding memory consumption.</p>
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 */
public class ConcurrentSimpleDateFormatter {

    ThreadLocal<DateFormat> dateFormat = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return SimpleDateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
        };
    };

    public String getFormatedDate(long timestamp) {
        return this.dateFormat.get().format(timestamp);
    }
}
