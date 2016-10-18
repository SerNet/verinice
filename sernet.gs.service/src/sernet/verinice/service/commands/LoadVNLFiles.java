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
package sernet.verinice.service.commands;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import sernet.verinice.interfaces.GenericCommand;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class LoadVNLFiles extends GenericCommand {

    
    private List<String> vnlFiles = new ArrayList<String>();
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        String property = PropertyLoader.getVnlRepositoryLocation();
        if(StringUtils.isNotEmpty(property)){
            File f = new File(property);
            if(f.isDirectory()){
                List<String> filenames = Arrays.asList(f.list(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".vnl");
                    }
                }));
                for(String filename : filenames){
                    vnlFiles.add(FilenameUtils.concat(f.getAbsolutePath(), filename));
                }
            }
        }

    }
    
    public List<String> getVNLFiles(){
        return vnlFiles;
    }

}
