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
package sernet.verinice.rcp;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import sernet.gs.ui.rcp.main.Activator;
import sernet.verinice.iso27k.rcp.ComboModel;
import sernet.verinice.iso27k.rcp.ComboModelLabelProvider;

/**
 * 
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class IconSelectDialog extends Dialog {

    private static final Logger LOG = Logger.getLogger(IconSelectDialog.class);
    
    public static final String ICON_DIRECTORY = "tree-icons";
    
    private static final FileFilter ICON_FILE_FILTER = new IconFileFilter();

    private static final int SIZE_Y = 370;
    private static final int SIZE_X = 485;
    private static final int NUMBER_OF_COLUMNS = 10;
    private static final int ICON_SPACING = 10;
    private static final int THUMBNAIL_SIZE = 20;

    private Combo dirCombo;
    private ComboModel<IconPathDescriptor> dirComboModel;

    private TableViewer viewer;

    private IconPathDescriptor directory;

    private String selectedPath;

    private boolean defaultIcon = false;

    /**
     * @param parentShell
     */
    protected IconSelectDialog(Shell parentShell) {
        super(parentShell);
        initComboValues();
    }

    private void initComboValues() {
        dirComboModel = new ComboModel<IconPathDescriptor>(new ComboModelLabelProvider<IconPathDescriptor>() {
            @Override
            public String getLabel(IconPathDescriptor descriptor) {
                return descriptor.getName();
            }
        });
        URL[] inconUrlArray = FileLocator.findEntries(Platform.getBundle(Activator.PLUGIN_ID), new Path(ICON_DIRECTORY), null);

        try {
            for (URL inconUrl : inconUrlArray) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Icon dir: " + inconUrl);
                }
                URL realFileUrl = FileLocator.toFileURL(inconUrl);
                File baseDir = new File(FileLocator.resolve(realFileUrl).toURI());
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Icon dir (file system): " + baseDir.getPath());
                }
                String[] directories = baseDir.list(DirectoryFileFilter.INSTANCE);
                for (String dir : directories) {
                    dirComboModel.add(new IconPathDescriptor(dir, baseDir.getPath() + File.separator + dir));
                }
            }
        } catch (Exception e) {
            LOG.error("Error while reading icon directory: " + ICON_DIRECTORY, e);
            return;
        }

        if (!dirComboModel.isEmpty()) {
            dirComboModel.sort();
        }
    }

    public void showComboValues() {
        dirCombo.setItems(dirComboModel.getLabelArray());
        if (dirComboModel.isEmpty()) {
            dirCombo.setEnabled(false);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets
     * .Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        
        final int gridDataSizeSubtrahend = 20;
        
        Composite comp = (Composite) super.createDialogArea(parent);

        Label dirLabel = new Label(comp, SWT.NONE);
        dirLabel.setText("Directory");
        dirCombo = new Combo(comp, SWT.VERTICAL | SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
        GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false);
        dirCombo.setLayoutData(gd);
        dirCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                dirComboModel.setSelectedIndex(dirCombo.getSelectionIndex());
                directory = dirComboModel.getSelectedObject();
                loadIcons(directory.getPath());
            }
        });
        showComboValues();

        Group group = new Group(comp, SWT.NONE);
        group.setText("Select icon");
        GridLayout groupOrganizationLayout = new GridLayout(1, true);
        group.setLayout(groupOrganizationLayout);
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.minimumWidth = SIZE_Y - gridDataSizeSubtrahend;
        gd.heightHint = SIZE_X - gridDataSizeSubtrahend;
        group.setLayoutData(gd);

        createTable(group);

        group.layout();

        final Button defaultCheckbox = new Button(comp, SWT.CHECK);
        defaultCheckbox.setText("Show default icon instead");
        defaultCheckbox.setSelection(false);
        defaultCheckbox.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                defaultIcon = defaultCheckbox.isEnabled();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        comp.pack();
        return comp;
    }

    private void loadIcons(String path) {
        final int maxRowCount = 10;
        File internalDirectory = new File(path);
        File[] files = internalDirectory.listFiles(ICON_FILE_FILTER);
        Arrays.sort(files);

        List<IconDescriptor[]> iconDescriptorList = new ArrayList<IconDescriptor[]>();
        IconDescriptor[] iconRow = new IconDescriptor[NUMBER_OF_COLUMNS];
        int i = 0;
        for (File file : files) {
            iconRow[i] = new IconDescriptor(file);
            i++;
            if (i == maxRowCount) {
                iconDescriptorList.add(iconRow);
                iconRow = new IconDescriptor[NUMBER_OF_COLUMNS];
                i = 0;
            }
        }
        if (i != 0) {
            iconDescriptorList.add(iconRow);
        }
        IconDescriptor[][] iconDescriptorArray = null;
        iconDescriptorArray = iconDescriptorList.toArray(new IconDescriptor[iconDescriptorList.size()][NUMBER_OF_COLUMNS]);
        viewer.setInput(iconDescriptorArray);
    }

    private void createTable(Composite parent) {
        
        final int gdHeightSubtrahend = 100;
        final int iconRowSize = 10;
        
        int style = SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER;
        style = style | SWT.SINGLE | SWT.FULL_SELECTION;
        viewer = new TableViewer(parent, style);
        viewer.setContentProvider(new ArrayContentProvider());

        TableViewerFocusCellManager focusCellManager = new TableViewerFocusCellManager(viewer, new FocusCellOwnerDrawHighlighter(viewer));
        ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(viewer) {
            @Override
            protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
                boolean retVal = event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL || event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION;
                retVal = retVal || (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && event.keyCode == SWT.CR);
                return  retVal || event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
            }
        };

        TableViewerEditor.create(viewer, focusCellManager, actSupport, ColumnViewerEditor.TABBING_HORIZONTAL | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR | ColumnViewerEditor.TABBING_VERTICAL | ColumnViewerEditor.KEYBOARD_ACTIVATION);

        Table table = viewer.getTable();
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.heightHint = SIZE_X - gdHeightSubtrahend;
        table.setLayoutData(gd);

        table.addListener(SWT.MeasureItem, new Listener() {
            @Override
            public void handleEvent(Event event) {
                // height cannot be per row so simply set
                event.height = getThumbnailSize() + ICON_SPACING;
            }
        });

        table.addMouseListener(new MouseListener() {

            @Override
            public void mouseUp(MouseEvent e) {
            }

            @Override
            public void mouseDown(MouseEvent e) {
                ViewerCell cell = viewer.getCell(new Point(e.x, e.y));
                if (cell != null) {
                    selectedPath = getRelativePath(((IconDescriptor[]) cell.getElement())[cell.getColumnIndex()].getPath());
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Icon: " + selectedPath);
                    }
                }

            }

            @Override
            public void mouseDoubleClick(MouseEvent e) {
            }
        });

        for (int i = 0; i < iconRowSize; i++) {
            TableViewerColumn imageColumn = new TableViewerColumn(viewer, SWT.LEFT);
            imageColumn.setLabelProvider(new IconCellProvider(i));
            imageColumn.getColumn().setWidth(getThumbnailSize() + ICON_SPACING);
        }

        if (!dirComboModel.isEmpty()) {
            dirComboModel.setSelectedIndex(2);
            dirCombo.select(2);
            directory = dirComboModel.getSelectedObject();
            loadIcons(directory.getPath());
        } else {
            loadIcons(ICON_DIRECTORY + "silk");
        }
    }

    /**
     * @param path
     * @return
     */
    protected String getRelativePath(String path) {
        String relative = path;
        if (path.contains(ICON_DIRECTORY)) {
            relative = path.substring(path.indexOf(ICON_DIRECTORY));
        }
        if(relative.contains("\\")) {
            relative = relative.replace('\\', '/');
        }
        return relative;
    }

    /**
     * @return
     */
    protected int getThumbnailSize() {
        return THUMBNAIL_SIZE;
    }

    public String getSelectedPath() {
        return selectedPath;
    }

    public boolean isDefaultIcon() {
        return defaultIcon;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Icon");
        newShell.setSize(SIZE_Y, SIZE_X);

        // open the window right under the mouse pointer:
        Point cursorLocation = Display.getCurrent().getCursorLocation();
        newShell.setLocation(new Point(cursorLocation.x - SIZE_X / 2, cursorLocation.y - SIZE_Y / 2));

    }
}

class IconFileFilter implements FileFilter {

    @Override
    public boolean accept(File file) {
        boolean accept = false;
        if(file!=null && file.getName()!=null) {
            String filename = file.getName().toLowerCase();
            accept = filename.endsWith("gif") || filename.endsWith("png");
        }
        return accept;
    }
}
