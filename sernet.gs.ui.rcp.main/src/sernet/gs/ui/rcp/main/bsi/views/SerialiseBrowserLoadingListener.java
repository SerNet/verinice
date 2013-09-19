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
package sernet.gs.ui.rcp.main.bsi.views;

import org.apache.log4j.Logger;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;

/**
 * Serialises the loading of pages in a SWT browser.
 * If browser is busy rendering while HTML content changes
 * new HTML is saved and loaded after rendering is completed. 
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class SerialiseBrowserLoadingListener implements ProgressListener {

    private static final Logger LOG = Logger.getLogger(SerialiseBrowserLoadingListener.class);
    
    private Browser browser;
    
    private boolean isLoading = false;
    
    private String html;
    
    public SerialiseBrowserLoadingListener(Browser browser) {
        this.browser = browser;
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.browser.ProgressListener#changed(org.eclipse.swt.browser.ProgressEvent)
     */
    @Override
    public void changed(ProgressEvent event) {
    }

    /**
     * If HTML code is waiting for rendering, rendering is started.
     * 
     * @see org.eclipse.swt.browser.ProgressListener#completed(org.eclipse.swt.browser.ProgressEvent)
     */
    @Override
    public void completed(ProgressEvent event) {
        isLoading = false;
        if (LOG.isDebugEnabled()) {
            LOG.debug("Loading completed.");
        }
        if(html!=null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Browser will now render saved HTML...");
            }
            setText(html);
            html=null;
        }
    }

    
    /**
     * Sets the contents to be displayed in the browser.
     * 
     * @param text The HTML to be displayed
     */
    public void setText(String text) {
        if(!isLoading) {
            isLoading = true;
            try {
                browser.setText(text);
            } catch(RuntimeException e) {
                isLoading = false;
                throw e;
            } catch(Exception e) {
                isLoading = false;
                throw new RuntimeException(e);
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Browser is busy, HTML is saved and rendered later.");
            }
            html = text;
        }
    }
}
