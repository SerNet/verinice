/*******************************************************************************
 * Copyright (c) 2016 Sebastian Hagedorn.
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
package sernet.verinice.interfaces;

/**
 * Collection of Strings that represents vm properties and gets
 * passed to "System.getPropery(String id)", to prevent creating several
 * new Strings all over the system all the time
 * 
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public interface IVeriniceConstants {
    
    public static final String FILE_SEPARATOR = "file.separator";
    public static final String HTTP_PROXY_HOST = "http.proxyHost";
    public static final String HTTP_PROXY_NAME = "http.proxyName";
    public static final String HTTP_PROXY_PASSWORD = "http.proxyPassword";
    public static final String HTTP_PROXY_PORT = "http.proxyPort";
    public static final String JAVA_IO_TMPDIR = "java.io.tmpdir";
    public static final String JAVA_VERSION = "java.version";
    public static final String LINE_SEPARATOR = "line.separator";
    public static final String OS_ARCH = "os.arch";
    public static final String OS_NAME = "os.name";
    public static final String OSGI_INSTANCE_AREA = "osgi.instance.area";
    public static final String USER_HOME = "user.home";
    
    

}
