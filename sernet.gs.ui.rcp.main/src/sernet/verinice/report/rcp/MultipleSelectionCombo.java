package sernet.verinice.report.rcp;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import sernet.verinice.model.report.ReportTemplateMetaData;

/**
 * http://www.ibm.com/developerworks/cn/opensource/os-cn-swtmulti/index.html?ca=drs-cn-1022
 */
public class MultipleSelectionCombo<T> extends Composite {
    @FunctionalInterface
    public interface SelectionConsumer<T> {
        void consume(List<T> selectedElements);
    }

    Text displayText = null;
    Object[] selectedElements = {};
    Shell floatShell = null;
    CheckboxTableViewer list = null;
    Button dropButton = null;
    private ReportTemplateMetaData[] input;
    private ILabelProvider lableProvider;
    private ViewerFilter viewerFilter;
    private SelectionConsumer<T> listener;
    private int stringLength;

    public MultipleSelectionCombo(Composite parent, int style) {
        super(parent, style);
        init();
    }

    private void init() {
        GridLayout layout = new GridLayout();
        layout.marginBottom = 0;
        layout.marginTop = 0;
        layout.marginLeft = 0;
        layout.marginRight = 0;
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        setLayout(layout);
        displayText = new Text(this, SWT.BORDER);
        displayText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        displayText.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent event) {
                if (floatShell == null) {
                    initFloatShell();
                }
            }
        });
    }

    private void initFloatShell() {
        Point p = displayText.getParent().toDisplay(displayText.getLocation());
        Point size = displayText.getSize();
        stringLength = (int) (size.x / displayText.getFont().getFontData()[0].height);
        Rectangle shellRect = new Rectangle(p.x, p.y + size.y, size.x, 0);
        floatShell = new Shell(MultipleSelectionCombo.this.getShell(), SWT.NO_TRIM);

        GridLayout gl = new GridLayout();
        gl.marginBottom = 2;
        gl.marginTop = 2;
        gl.marginRight = 0;
        gl.marginLeft = 0;
        gl.marginWidth = 0;
        gl.marginHeight = 0;
        floatShell.setLayout(gl);

        list = CheckboxTableViewer.newCheckList(floatShell,
                SWT.BORDER | SWT.CHECK | SWT.H_SCROLL | SWT.V_SCROLL);

        list.setContentProvider(ArrayContentProvider.getInstance());
        list.setInput(input);
        list.setLabelProvider(lableProvider);
        if (viewerFilter != null) {
            list.setFilters(viewerFilter);
        }
        list.setCheckedElements(selectedElements);
        if (selectedElements.length != 0) {
            list.reveal(selectedElements[0]);
        }
        GridData gd = new GridData(GridData.FILL_BOTH);
        list.getTable().setLayoutData(gd);
        floatShell.setSize(shellRect.width, 160);
        floatShell.setLocation(shellRect.x, shellRect.y);

        floatShell.addShellListener(new ShellAdapter() {
            @Override
            public void shellDeactivated(ShellEvent arg0) {
                if (floatShell != null && !floatShell.isDisposed()) {
                    internalSelectItems(list.getCheckedElements());
                    floatShell.close();
                    floatShell = null;
                }
            }
        });
        floatShell.open();
    }

    public void setText(String text) {
        displayText.setText(text);
    }

    public String getText() {
        return displayText.getText();
    }

    public void setInput(ReportTemplateMetaData[] reportTemplates) {
        input = reportTemplates;
    }

    public void setLabelProvider(LabelProvider labelProvider) {
        this.lableProvider = labelProvider;
    }

    public void setFilters(ViewerFilter viewerFilter) {
        this.viewerFilter = viewerFilter;
        if (list != null && !list.getTable().isDisposed()) {
            list.setFilters(viewerFilter);
        }

    }

    public void resetFilters() {
        this.viewerFilter = null;
        if (list != null && !list.getTable().isDisposed()) {
            list.resetFilters();
        }
    }

    public void setSelectionChangedConsumer(SelectionConsumer<T> listener) {
        this.listener = listener;
    }

    public void setSelectedElements(Object[] selectedElements) {
        internalSelectItems(selectedElements);
    }

    /**
     * Used for the rcptt test to select items by name. The string get split by
     * ','.
     */
    public void setSelectedIndexByName(String selectedElements) {
        List<String> selectedElement = Arrays.asList(selectedElements.split(","));
        Object[] objects = Arrays.asList(this.input).stream()
                .filter(rmd -> selectedElement.contains(rmd.getDecoratedOutputname()))
                .collect(Collectors.toList()).toArray();
        internalSelectItems(objects);
    }

    private void displayText() {
        String dt = Arrays.asList(selectedElements).stream().map(s -> lableProvider.getText(s))
                .map(String.class::cast)
                .collect(Collectors.joining(Messages.MultipleSelectionCombo_separator));
        displayText.setToolTipText(dt);
        if (selectedElements.length > 2 && dt.length() > stringLength) {
            displayText.setText(Messages.bind(Messages.MultipleSelectionCombo_cutted_expression,
                    selectedElements.length, dt.substring(0, stringLength)));
        } else {
            displayText.setText(dt);
        }
    }

    private void internalSelectItems(Object[] objects) {
        selectedElements = objects;
        displayText();
        callSelectionConsumer();
    }

    private void callSelectionConsumer() {
        if (listener != null) {
            @SuppressWarnings("unchecked")
            List<T> asList = (List<T>) Arrays.asList(this.selectedElements);
            listener.consume(asList);
        }
    }
}
