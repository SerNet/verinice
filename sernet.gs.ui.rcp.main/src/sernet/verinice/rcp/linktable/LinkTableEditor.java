/*******************************************************************************
 * Copyright (c) 2016 Daniel Murygin <dm{a}sernet{dot}de>.
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
 *     Daniel Murygin <dm{a}sernet{dot}de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp.linktable;

import java.io.File;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.editors.EditorRegistry;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.rcp.linktable.handlers.ExportLinkTableHandler;
import sernet.verinice.rcp.linktable.ui.LinkTableComposite;
import sernet.verinice.rcp.linktable.ui.LinkTableFieldListener;
import sernet.verinice.service.linktable.vlt.VeriniceLinkTableIO;

/**
 *
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class LinkTableEditor extends EditorPart {

    public static final String EDITOR_ID = LinkTableEditor.class.getName();
    public static final String TITLE_DEFAULT = Messages.LinkTableEditor_0;
    private static final Logger LOG = Logger.getLogger(LinkTableEditor.class);
    
    private LinkTableEditorInput linkTableEditorInput;
    private boolean isDirty = false;

    private LinkTableFieldListener contentObserver;
    private boolean validQuery = false;

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.EditorPart#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
     */
    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        if (! (input instanceof LinkTableEditorInput)) {
            throw new PartInitException("Input is not an instance of " + LinkTableEditorInput.class.getSimpleName()); //$NON-NLS-1$
        }

        linkTableEditorInput = (LinkTableEditorInput) input;

        setSite(site);
        setInput(linkTableEditorInput);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        try {
            Activator.inheritVeriniceContextState();
            createEditor(parent);
        } catch (RuntimeException e) {
            // Log the exception via Log4j:
            LOG.error("Could not create the editor part", e); //$NON-NLS-1$
            throw e;
        }
    }

    public void createEditor(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);

        Composite buttonContainer = new Composite(container, SWT.NONE);
        Button exportButton = new Button(buttonContainer, SWT.PUSH);
        exportButton.setText(Messages.VeriniceLinkTableEditor_1);
        exportButton.setToolTipText(Messages.VeriniceLinkTableEditor_2);
        GridDataFactory.swtDefaults().applyTo(exportButton);

        exportButton.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent event) {


                ExportLinkTableHandler exportHandler = new ExportLinkTableHandler(true,
                        linkTableEditorInput.getInput());
                try {
                    exportHandler.execute(new ExecutionEvent());
                } catch (ExecutionException e) {
                    LOG.error("Error while exporting data", e); //$NON-NLS-1$
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });

        LinkTableComposite ltr = new LinkTableComposite(linkTableEditorInput.getInput(),
                ServiceFactory.lookupObjectModelService(),
                container);

        contentObserver = new LinkTableFieldListener() {

            @Override
            public void fieldValueChanged() {
                isDirty = true;
                firePropertyChange(IEditorPart.PROP_DIRTY);

            }

            @Override
            public void validate() {

                LinkTableValidationResult validationResult = LinkTableUtil
                        .isValidVeriniceLinkTable(linkTableEditorInput.getInput());

                Image defaultImage = ImageCache.getInstance().getImage(ImageCache.VLT);

                if (validationResult.isValid()) {
                    setTitleImage(defaultImage);
                    setPartName(linkTableEditorInput.getName());
                    validQuery = true;
                } else {
                    ImageDescriptor[] descriptors = new ImageDescriptor[5];
                    Image warningImage = ImageCache.getInstance().getImage(ImageCache.ERROR_DECORATOR);

                    descriptors[IDecoration.BOTTOM_LEFT] = ImageDescriptor
                            .createFromImage(warningImage);
                    Image decorated = new DecorationOverlayIcon(defaultImage, descriptors)
                            .createImage();

                    setTitleImage(decorated);
                    setPartName(linkTableEditorInput.getName() + Messages.VeriniceLinkTableEditor_7);
                    firePropertyChange(IEditorPart.PROP_DIRTY);
                    validQuery = false;
                }
                firePropertyChange(IEditorPart.PROP_TITLE);


            }
        };
        ltr.addListener(contentObserver);

        GridLayoutFactory.fillDefaults().generateLayout(ltr);
        GridLayoutFactory.fillDefaults().extendedMargins(0, 0, 20, 0)
                .generateLayout(buttonContainer);
        GridLayoutFactory.fillDefaults().margins(10, 10).generateLayout(container);
        contentObserver.validate();
    }
    
    /* 
     * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void doSave(IProgressMonitor monitor) {
        if (wantSaveEvenIfInvalid()) {
            String filePath = getFilePath(linkTableEditorInput, false);
            if (filePath != null) {
                executeSave(filePath);
            } else {
                LOG.error("filePath is null!!!");
            }
        }
    }
    
    /*
     * @see org.eclipse.ui.part.EditorPart#doSaveAs()
     */
    @Override
    public void doSaveAs() {
        if (wantSaveEvenIfInvalid()) {
            String filePath = getFilePath(linkTableEditorInput, true);
            if (filePath != null) {
                executeSave(filePath);
            } else {
                LOG.error("filePath is null!!!");
            }
        }
    }
    
    private boolean wantSaveEvenIfInvalid() {

        if (!validQuery) {
            MessageDialog confirmInvalidInput = new MessageDialog(
                    Display.getCurrent().getActiveShell(),
                    Messages.LinkTableHandler_1,
                    null,
                    Messages.LinkTableHandler_2
                            + Messages.LinkTableHandler_3,
                    MessageDialog.WARNING,
                    new String[] { Messages.LinkTableEditor_1, Messages.LinkTableEditor_2 }, 0);

            return confirmInvalidInput.open() == 0;
        }
        return true;
    }

    public static String getEditorTitle(String filePath) {
        String title = TITLE_DEFAULT;
        if(filePath!=null) {
            title = filePath.substring(filePath.lastIndexOf(File.separator) + 1,
                    filePath.length());
        }
        return title;
    }
    
    @Override
    public void dispose() {
        EditorRegistry.getInstance().closeEditor(((LinkTableEditorInput) getEditorInput()).getId());
        super.dispose();
    }
    
    private void executeSave(String filePath) {
        VeriniceLinkTableIO.write(linkTableEditorInput.getInput(), filePath);
        setPartName(getEditorTitle(filePath));
        isDirty = false;
        firePropertyChange(IEditorPart.PROP_DIRTY);
        contentObserver.validate();
    }

    private String getFilePath(LinkTableEditorInput linkTableEditorInput, boolean isSaveAs) {
        String filePath = linkTableEditorInput.getFilePath();
        if (isSaveAs || filePath == null) {
            filePath = LinkTableUtil.createVltFilePath(
                    Display.getCurrent().getActiveShell(), Messages.VeriniceLinkTableEditor_4,
                    SWT.SAVE, linkTableEditorInput.getName());
            if (filePath != null) {
                EditorRegistry.getInstance().closeEditor(linkTableEditorInput.getId());
                linkTableEditorInput.setFilePath(filePath);
                EditorRegistry.getInstance().registerOpenEditor(linkTableEditorInput.getId(), this);
            }
        }
        return filePath;
    }  

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.EditorPart#isDirty()
     */
    @Override
    public boolean isDirty() {
        return isDirty;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
     */
    @Override
    public boolean isSaveAsAllowed() {
        return true;
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        // nothing to do

    }

}
