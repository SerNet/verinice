/*******************************************************************************
 * Copyright (c) 2020 Alexander Ben Nasrallah.
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
package sernet.gs.service;

import java.io.File;
import java.util.Locale;
import java.util.function.Function;

import org.apache.commons.io.FilenameUtils;

/**
 * Provides utility methods for properties files.
 */
public class PropertiesFileUtil {

    /**
     * <p>
     * Returns the properties file that corresponds to a given file depending on
     * the locale.
     *
     * <p>
     * Basically the file extension is replaced by _<language>.properties. "en"
     * is considered the default language and not added to the file path.
     * 
     * <p>
     * If the given files's basename already ends with the _<language> suffix,
     * it is not appended again.
     */
    public static File getPropertiesFile(final File file, final Locale locale) {
        final String localeSuffix = "en".equals(locale.getLanguage()) ? ""
                : "_" + locale.getLanguage();
        final Function<File, String> getPath = File::getPath;
        return getPath.andThen(FilenameUtils::removeExtension)
                .andThen(s -> s.endsWith(localeSuffix) ? s : s.concat(localeSuffix))
                .andThen(append(FilenameUtils.EXTENSION_SEPARATOR_STR))
                .andThen(append("properties")).andThen(File::new).apply(file);
    }

    /**
     * Returns a function that appends the given string to a string.
     */
    private static Function<String, String> append(final String str) {
        return s -> s.concat(str);
    }

    private PropertiesFileUtil() {
    }
}
