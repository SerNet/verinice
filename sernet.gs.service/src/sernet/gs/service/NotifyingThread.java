/*******************************************************************************
 * Copyright (c) 2012 Daniel Murygin.
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
package sernet.gs.service;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * A Thread which notifies listeners when it's finished.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public abstract class NotifyingThread extends Thread {
    
    private final Set<IThreadCompleteListener> listeners = new CopyOnWriteArraySet<IThreadCompleteListener>();
    
    public final void addListener(final IThreadCompleteListener listener) {
      listeners.add(listener);
    }
    
    public final void removeListener(final IThreadCompleteListener listener) {
      listeners.remove(listener);
    }
    
    private final void notifyListeners() {
      for (IThreadCompleteListener listener : listeners) {
        listener.notifyOfThreadComplete(this);
      }
    }
    
    @Override
    public final void run() {
      try {
        doRun();
      } finally {
        notifyListeners();
      }
    
    }
    public abstract void doRun();
 }