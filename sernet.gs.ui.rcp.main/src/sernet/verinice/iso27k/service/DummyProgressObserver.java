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
package sernet.verinice.iso27k.service;

/**
 * @author Daniel Murygin <dm@sernet.de>
 */
public class DummyProgressObserver implements IProgressObserver {

    public DummyProgressObserver() {
        super();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.iso27k.service.IProgressObserver#beginTask(java.lang.String, int)
     */
    public void beginTask(String string, int numberOfItems) {
    }

    /* (non-Javadoc)
     * @see sernet.verinice.iso27k.service.IProgressObserver#done()
     */
    public void done() {

    }

    /* (non-Javadoc)
     * @see sernet.verinice.iso27k.service.IProgressObserver#isCanceled()
     */
    public boolean isCanceled() {
        return false;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.iso27k.service.IProgressObserver#processed(int)
     */
    public void processed(int n) {
    }

    /* (non-Javadoc)
     * @see sernet.verinice.iso27k.service.IProgressObserver#setTaskName(java.lang.String)
     */
    public void setTaskName(String text) {
    }

}
